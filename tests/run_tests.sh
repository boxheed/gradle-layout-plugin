#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=== Running ShellCheck ==="
shellcheck "${REPO_ROOT}/bootstrap.sh" "${SCRIPT_DIR}/run_tests.sh"

echo "=== Running Bats Unit Tests ==="
bats "${SCRIPT_DIR}/bootstrap.bats"

echo "=== All shell script checks passed! ==="
