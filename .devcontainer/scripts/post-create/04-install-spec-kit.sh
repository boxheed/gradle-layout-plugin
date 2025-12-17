#!/bin/bash

echo "Installing spec-kit (specify-cli) using uv..."
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
echo "spec-kit (specify-cli) installed successfully."
