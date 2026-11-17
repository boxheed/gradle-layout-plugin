#!/usr/bin/env bats

setup() {
  TEST_TEMP_DIR="$(mktemp -d)"
  REPO_ROOT="$(cd "${BATS_TEST_DIRNAME}/.." && pwd)"
  BOOTSTRAP_SH="${REPO_ROOT}/bootstrap.sh"
}

teardown() {
  rm -rf "${TEST_TEMP_DIR}"
}

@test "prints usage on --help" {
  run sh "${BOOTSTRAP_SH}" --help
  [ "$status" -eq 0 ]
  [[ "$output" == *"Usage: bootstrap.sh [OPTIONS]"* ]]
  [[ "$output" == *"--name"* ]]
  [[ "$output" == *"--dir"* ]]
}

@test "fails on unknown option" {
  run sh "${BOOTSTRAP_SH}" --unknown-flag
  [ "$status" -ne 0 ]
  [[ "$output" == *"Unknown option: --unknown-flag"* ]]
}

@test "fails when option value is missing" {
  run sh "${BOOTSTRAP_SH}" --name
  [ "$status" -ne 0 ]
  [[ "$output" == *"Option --name requires a value."* ]]
}

@test "fails in non-interactive mode when neither --name nor --url is given" {
  run env CI=true sh "${BOOTSTRAP_SH}" --non-interactive --dir "${TEST_TEMP_DIR}/proj"
  [ "$status" -ne 0 ]
  [[ "$output" == *"Either --name or --url is required"* ]]
}

@test "dry-run creates target dir and defaults project-name to dir basename" {
  local target="${TEST_TEMP_DIR}/my-new-plugin"
  run sh "${BOOTSTRAP_SH}" --non-interactive --name gradle-plugin --dir "${target}" --dry-run
  [ "$status" -eq 0 ]
  [ -d "${target}" ]
  [[ "$output" == *"Target directory: ${target}"* ]]
  [[ "$output" == *"Project name: my-new-plugin"* ]]
  [[ "$output" == *"name 'gradle-plugin'"* ]]
  [[ "$output" == *"overwrite false"* ]]
  [[ "$output" == *"com.fizzpod:gradle-layout-plugin:7.1.0"* ]]
}

@test "dry-run supports url, remote, overwrite, plugin-version, and project metadata" {
  local target="${TEST_TEMP_DIR}/custom"
  run sh "${BOOTSTRAP_SH}" \
    --non-interactive \
    --url "https://example.com/custom/layout.json" \
    --remote "https://example.com/layouts/" \
    --overwrite \
    --plugin-version "7.2.0" \
    --project-name "custom'app" \
    --project-group "com.example" \
    --project-version "1.0.0" \
    --project-description "Example project" \
    --dir "${target}" \
    --dry-run
  [ "$status" -eq 0 ]
  [[ "$output" == *"com.fizzpod:gradle-layout-plugin:7.2.0"* ]]
  [[ "$output" == *"proj.extensions.getByName('layout').write(new File('${target}'))"* ]]
  [[ "$output" == *"proj.group = 'com.example'"* ]]
  [[ "$output" == *"proj.version = '1.0.0'"* ]]
  [[ "$output" == *"proj.description = 'Example project'"* ]]
  [[ "$output" == *"overwrite true"* ]]
  [[ "$output" == *"remote 'https://example.com/layouts/'"* ]]
  [[ "$output" == *"url 'https://example.com/custom/layout.json'"* ]]
}

@test "works when piped into sh" {
  local target="${TEST_TEMP_DIR}/piped-repo"
  run sh -c "cat '${BOOTSTRAP_SH}' | sh -s -- --non-interactive --name gradle-plugin --dir '${target}' --dry-run"
  [ "$status" -eq 0 ]
  [ -d "${target}" ]
  [[ "$output" == *"Project name: piped-repo"* ]]
  [[ "$output" == *"name 'gradle-plugin'"* ]]
}

@test "resolve_gradle_bin prefers target gradlew when present" {
  local target="${TEST_TEMP_DIR}/with-wrapper"
  mkdir -p "${target}"
  cat >"${target}/gradlew" <<'EOF'
#!/usr/bin/env sh
exit 0
EOF
  chmod +x "${target}/gradlew"

  run env BOOTSTRAP_SOURCE_ONLY=1 sh -c ". '${BOOTSTRAP_SH}'; TARGET_DIR='${target}'; resolve_gradle_bin"
  [ "$status" -eq 0 ]
  [ "$output" = "${target}/gradlew" ]
}

@test "resolve_gradle_bin uses cached gradle when gradle is not on PATH" {
  local target="${TEST_TEMP_DIR}/no-wrapper"
  local cache="${TEST_TEMP_DIR}/cache"
  mkdir -p "${target}" "${cache}/gradle-8.14/bin"
  cat >"${cache}/gradle-8.14/bin/gradle" <<'EOF'
#!/usr/bin/env sh
exit 0
EOF
  chmod +x "${cache}/gradle-8.14/bin/gradle"

  # Provide a minimal PATH that includes java but not gradle
  local fake_bin="${TEST_TEMP_DIR}/bin"
  mkdir -p "${fake_bin}"
  cat >"${fake_bin}/java" <<'EOF'
#!/usr/bin/env sh
exit 0
EOF
  chmod +x "${fake_bin}/java"

  run env BOOTSTRAP_SOURCE_ONLY=1 PATH="${fake_bin}:/bin:/usr/bin" sh -c "
    # Ensure no system gradle interferes if present
    gradle() { return 1; }
    . '${BOOTSTRAP_SH}'
    TARGET_DIR='${target}'
    CACHE_DIR='${cache}'
    GRADLE_VERSION='8.14'
    if command -v gradle >/dev/null 2>&1; then
      echo 'SKIP_SYSTEM_GRADLE'
    else
      resolve_gradle_bin
    fi
  "
  [ "$status" -eq 0 ]
  if [ "$output" != "SKIP_SYSTEM_GRADLE" ]; then
    [ "$output" = "${cache}/gradle-8.14/bin/gradle" ]
  fi
}

@test "invokes gradle with project-dir and init-script and cleans up temp dir" {
  local target="${TEST_TEMP_DIR}/target-repo"
  local fake_bin="${TEST_TEMP_DIR}/fake-bin"
  local args_log="${TEST_TEMP_DIR}/gradle-args.log"
  local tmp_log="${TEST_TEMP_DIR}/tmp-dir.log"
  mkdir -p "${target}" "${fake_bin}"

  cat >"${fake_bin}/gradle" <<EOF
#!/usr/bin/env sh
printf '%s\n' "\$@" > "${args_log}"
init_script=""
prev=""
for arg in "\$@"; do
  if [ "\${prev}" = "--init-script" ]; then
    init_script="\${arg}"
  fi
  prev="\${arg}"
done
[ -f "\${init_script}" ] || exit 99
dirname "\${init_script}" > "${tmp_log}"
EOF
  chmod +x "${fake_bin}/gradle"

  run env PATH="${fake_bin}:${PATH}" sh "${BOOTSTRAP_SH}" --non-interactive --name gradle-plugin --dir "${target}"
  [ "$status" -eq 0 ]
  [[ "$output" == *"Layout applied successfully."* ]]
  [ -f "${args_log}" ]
  [ ! -d "$(cat "${tmp_log}")" ]

  run cat "${args_log}"
  [[ "$output" == *"--no-daemon"* ]]
  [[ "$output" == *"--project-dir"* ]]
  [[ "$output" == *"--init-script"* ]]
  [[ "$output" == *"bootstrapLayout"* ]]
}

@test "cleans up temp dir when gradle fails" {
  local target="${TEST_TEMP_DIR}/failing-repo"
  local fake_bin="${TEST_TEMP_DIR}/fake-bin"
  local tmp_log="${TEST_TEMP_DIR}/tmp-dir.log"
  mkdir -p "${target}" "${fake_bin}"

  cat >"${fake_bin}/gradle" <<EOF
#!/usr/bin/env sh
init_script=""
prev=""
for arg in "\$@"; do
  if [ "\${prev}" = "--init-script" ]; then
    init_script="\${arg}"
  fi
  prev="\${arg}"
done
dirname "\${init_script}" > "${tmp_log}"
exit 42
EOF
  chmod +x "${fake_bin}/gradle"

  run env PATH="${fake_bin}:${PATH}" sh "${BOOTSTRAP_SH}" --non-interactive --name gradle-plugin --dir "${target}"
  [ "$status" -eq 42 ]
  [ -f "${tmp_log}" ]
  [ ! -d "$(cat "${tmp_log}")" ]
}

@test "handles spaces in target directory path" {
  local target="${TEST_TEMP_DIR}/my project dir"
  run sh "${BOOTSTRAP_SH}" --non-interactive --name gradle-plugin --dir "${target}" --dry-run
  [ "$status" -eq 0 ]
  [ -d "${target}" ]
  [[ "$output" == *"Target directory: ${target}"* ]]
  [[ "$output" == *"Project name: my project dir"* ]]
}

@test "fails when target path is an existing regular file" {
  local target="${TEST_TEMP_DIR}/not-a-dir"
  touch "${target}"
  run sh "${BOOTSTRAP_SH}" --non-interactive --name gradle-plugin --dir "${target}" --dry-run
  [ "$status" -ne 0 ]
}

@test "fails when neither gradle nor java is available" {
  local target="${TEST_TEMP_DIR}/no-java"
  local empty_bin="${TEST_TEMP_DIR}/empty-bin"
  mkdir -p "${target}" "${empty_bin}"

  run env BOOTSTRAP_SOURCE_ONLY=1 sh -c "PATH='${empty_bin}'; export PATH; . '${BOOTSTRAP_SH}'; TARGET_DIR='${target}'; resolve_gradle_bin"
  [ "$status" -ne 0 ]
  [[ "$output" == *"Java is required to run Gradle but was not found on PATH."* ]]
}

@test "fails when java is present without gradle or curl" {
  local target="${TEST_TEMP_DIR}/no-curl"
  local fake_bin="${TEST_TEMP_DIR}/fake-bin"
  local cache="${TEST_TEMP_DIR}/empty-cache"
  mkdir -p "${target}" "${fake_bin}" "${cache}"
  cat >"${fake_bin}/java" <<'EOF'
#!/usr/bin/env sh
exit 0
EOF
  chmod +x "${fake_bin}/java"

  run env BOOTSTRAP_SOURCE_ONLY=1 sh -c "PATH='${fake_bin}'; export PATH; . '${BOOTSTRAP_SH}'; TARGET_DIR='${target}'; CACHE_DIR='${cache}'; resolve_gradle_bin"
  [ "$status" -ne 0 ]
  [[ "$output" == *"'curl' is required to download Gradle"* ]]
}
