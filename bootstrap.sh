#!/usr/bin/env sh
set -eu

LAYOUT_NAME=""
LAYOUT_URL=""
REMOTE_ROOT=""
TARGET_DIR="."
OVERWRITE="false"
DRY_RUN="false"
NON_INTERACTIVE="false"
PLUGIN_VERSION="${LAYOUT_PLUGIN_VERSION:-7.1.0}"
GRADLE_VERSION="${LAYOUT_GRADLE_VERSION:-8.14}"
PROJECT_NAME=""
PROJECT_GROUP=""
PROJECT_VERSION=""
PROJECT_DESCRIPTION=""
TMP_DIR=""

usage() {
  cat <<'EOF'
Usage: bootstrap.sh [OPTIONS]

Bootstrap a directory with a gradle-layout-plugin layout.

Options:
  -n, --name <name>               Named layout from the remote layout repository
  -u, --url <url>                 Direct URL to a layout.json descriptor
      --remote <base-url>         Override remote layout root URL
  -d, --dir <path>                Target directory (default: .)
      --overwrite                 Force overwrite existing files
      --dry-run                   Print resolved configuration without applying
  -y, --non-interactive           Do not prompt on /dev/tty
      --plugin-version <ver>      gradle-layout-plugin version (default: 7.1.0)
      --gradle-version <ver>      Gradle version to download if missing (default: 8.14)
      --project-name <name>       Project name for interpolation (default: dir name)
      --project-group <group>     Project group for interpolation
      --project-version <ver>     Project version for interpolation
      --project-description <d>   Project description for interpolation
  -h, --help                      Show this help message
EOF
}

log_info() {
  printf '%s\n' "[layout-bootstrap] $*"
}

log_warn() {
  printf '%s\n' "[layout-bootstrap] WARNING: $*" >&2
}

log_err() {
  printf '%s\n' "[layout-bootstrap] ERROR: $*" >&2
}

cleanup() {
  if [ -n "${TMP_DIR}" ] && [ -d "${TMP_DIR}" ]; then
    rm -rf "${TMP_DIR}"
  fi
}

require_arg() {
  if [ "$#" -lt 2 ] || [ -z "$2" ]; then
    log_err "Option $1 requires a value."
    usage >&2
    exit 1
  fi
}

parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      -n|--name)
        require_arg "$1" "${2:-}"
        LAYOUT_NAME="$2"
        shift 2
        ;;
      -u|--url)
        require_arg "$1" "${2:-}"
        LAYOUT_URL="$2"
        shift 2
        ;;
      --remote)
        require_arg "$1" "${2:-}"
        REMOTE_ROOT="$2"
        shift 2
        ;;
      -d|--dir)
        require_arg "$1" "${2:-}"
        TARGET_DIR="$2"
        shift 2
        ;;
      --overwrite)
        OVERWRITE="true"
        shift
        ;;
      --dry-run)
        DRY_RUN="true"
        shift
        ;;
      -y|--non-interactive)
        NON_INTERACTIVE="true"
        shift
        ;;
      --plugin-version)
        require_arg "$1" "${2:-}"
        PLUGIN_VERSION="$2"
        shift 2
        ;;
      --gradle-version)
        require_arg "$1" "${2:-}"
        GRADLE_VERSION="$2"
        shift 2
        ;;
      --project-name)
        require_arg "$1" "${2:-}"
        PROJECT_NAME="$2"
        shift 2
        ;;
      --project-group)
        require_arg "$1" "${2:-}"
        PROJECT_GROUP="$2"
        shift 2
        ;;
      --project-version)
        require_arg "$1" "${2:-}"
        PROJECT_VERSION="$2"
        shift 2
        ;;
      --project-description)
        require_arg "$1" "${2:-}"
        PROJECT_DESCRIPTION="$2"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        log_err "Unknown option: $1"
        usage >&2
        exit 1
        ;;
    esac
  done
}

can_prompt() {
  if [ "${NON_INTERACTIVE}" = "true" ] || [ -n "${CI:-}" ]; then
    return 1
  fi
  [ -r /dev/tty ] && [ -w /dev/tty ]
}

prompt_missing_inputs() {
  if [ -n "${LAYOUT_NAME}" ] || [ -n "${LAYOUT_URL}" ]; then
    return 0
  fi

  if can_prompt; then
    printf '%s' "Layout name (e.g. gradle-plugin): " >/dev/tty
    IFS= read -r LAYOUT_NAME </dev/tty || true
    if [ -z "${LAYOUT_NAME}" ]; then
      log_err "Layout name cannot be empty."
      exit 1
    fi
  else
    log_err "Either --name or --url is required in non-interactive mode."
    usage >&2
    exit 1
  fi
}

resolve_target_dir() {
  mkdir -p "${TARGET_DIR}"
  TARGET_DIR="$(cd "${TARGET_DIR}" && pwd)"
  if [ -z "${PROJECT_NAME}" ]; then
    PROJECT_NAME="$(basename "${TARGET_DIR}")"
  fi
}

resolve_gradle_bin() {
  if [ -x "${TARGET_DIR}/gradlew" ]; then
    printf '%s\n' "${TARGET_DIR}/gradlew"
    return 0
  fi

  if command -v gradle >/dev/null 2>&1; then
    command -v gradle
    return 0
  fi

  if ! command -v java >/dev/null 2>&1; then
    log_err "Java is required to run Gradle but was not found on PATH."
    exit 1
  fi

  dl_dir="${TMP_DIR:-$(mktemp -d)}"
  temp_gradle="${dl_dir}/gradle-${GRADLE_VERSION}/bin/gradle"
  if [ -x "${temp_gradle}" ]; then
    printf '%s\n' "${temp_gradle}"
    return 0
  fi

  if ! command -v curl >/dev/null 2>&1; then
    log_err "'curl' is required to download Gradle ${GRADLE_VERSION} when gradle is not installed."
    exit 1
  fi

  zip_path="${dl_dir}/gradle-${GRADLE_VERSION}-bin.zip"
  dist_url="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  printf '%s\n' "[layout-bootstrap] Downloading Gradle ${GRADLE_VERSION}..." >&2
  curl -sSLf "${dist_url}" -o "${zip_path}"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "${zip_path}" -d "${dl_dir}"
  elif command -v python3 >/dev/null 2>&1; then
    python3 -m zipfile -e "${zip_path}" "${dl_dir}"
  elif command -v jar >/dev/null 2>&1; then
    (cd "${dl_dir}" && jar xf "${zip_path}")
  else
    rm -f "${zip_path}"
    log_err "'unzip', 'python3', or 'jar' is required to extract Gradle ${GRADLE_VERSION}."
    exit 1
  fi
  rm -f "${zip_path}"
  if [ -f "${temp_gradle}" ]; then
    chmod +x "${temp_gradle}"
  fi

  if [ ! -x "${temp_gradle}" ]; then
    log_err "Failed to install Gradle ${GRADLE_VERSION} into ${dl_dir}."
    exit 1
  fi

  printf '%s\n' "${temp_gradle}"
}

escape_groovy_string() {
  printf '%s' "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/\\\\'/g"
}

generate_init_script() {
  init_file="$1"
  esc_project_name="$(escape_groovy_string "${PROJECT_NAME}")"
  esc_project_group="$(escape_groovy_string "${PROJECT_GROUP}")"
  esc_project_version="$(escape_groovy_string "${PROJECT_VERSION}")"
  esc_project_desc="$(escape_groovy_string "${PROJECT_DESCRIPTION}")"
  esc_target_dir="$(escape_groovy_string "${TARGET_DIR}")"
  esc_remote="$(escape_groovy_string "${REMOTE_ROOT}")"
  esc_name="$(escape_groovy_string "${LAYOUT_NAME}")"
  esc_url="$(escape_groovy_string "${LAYOUT_URL}")"
  esc_plugin_ver="$(escape_groovy_string "${PLUGIN_VERSION}")"

  cat >"${TMP_DIR}/settings.gradle" <<EOF
rootProject.name = '${esc_project_name}'
EOF

  cat >"${init_file}" <<EOF
initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = 'https://artifacts.fizzpod.com/public/mvn/'
        }
    }
    dependencies {
        classpath 'com.fizzpod:gradle-layout-plugin:${esc_plugin_ver}'
    }
}

gradle.rootProject { proj ->
    if ('${esc_project_group}') {
        proj.group = '${esc_project_group}'
    }
    if ('${esc_project_version}') {
        proj.version = '${esc_project_version}'
    }
    if ('${esc_project_desc}') {
        proj.description = '${esc_project_desc}'
    }
    proj.apply(plugin: com.fizzpod.gradle.plugins.layout.LayoutPlugin)
    def layoutExt = proj.extensions.getByName('layout')
    layoutExt.with {
        overwrite ${OVERWRITE}
EOF

  if [ -n "${REMOTE_ROOT}" ]; then
    printf "        remote '%s'\n" "${esc_remote}" >>"${init_file}"
  fi
  if [ -n "${LAYOUT_NAME}" ]; then
    printf "        name '%s'\n" "${esc_name}" >>"${init_file}"
  fi
  if [ -n "${LAYOUT_URL}" ]; then
    printf "        url '%s'\n" "${esc_url}" >>"${init_file}"
  fi

  cat >>"${init_file}" <<EOF
    }
    proj.tasks.register('bootstrapLayout') {
        doLast {
            proj.extensions.getByName('layout').write(new File('${esc_target_dir}'))
        }
    }
}
EOF
}

main() {
  parse_args "$@"
  prompt_missing_inputs
  resolve_target_dir

  TMP_DIR="$(mktemp -d)"
  trap cleanup EXIT HUP INT TERM

  init_file="${TMP_DIR}/init.gradle"
  generate_init_script "${init_file}"

  if [ "${DRY_RUN}" = "true" ]; then
    log_info "Dry run enabled. No files will be modified."
    log_info "Target directory: ${TARGET_DIR}"
    log_info "Project name: ${PROJECT_NAME}"
    log_info "Plugin version: ${PLUGIN_VERSION}"
    [ -n "${LAYOUT_NAME}" ] && log_info "Layout name: ${LAYOUT_NAME}"
    [ -n "${LAYOUT_URL}" ] && log_info "Layout URL: ${LAYOUT_URL}"
    log_info "Generated init script:"
    cat "${init_file}"
    return 0
  fi

  gradle_bin="$(resolve_gradle_bin)"
  log_info "Applying layout in ${TARGET_DIR} using ${gradle_bin}..."
  "${gradle_bin}" --no-daemon --project-dir "${TMP_DIR}" --init-script "${init_file}" bootstrapLayout
  log_info "Layout applied successfully."
}

if [ "${BOOTSTRAP_SOURCE_ONLY:-0}" != "1" ]; then
  main "$@"
fi
