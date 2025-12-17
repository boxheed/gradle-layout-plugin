#!/bin/bash

# Update package lists and install python3-venv, python3-pip, and pipx
echo "Updating package lists and installing python3-venv, python3-pip, and pipx..."
sudo apt-get update
sudo apt-get install -y python3-venv python3-pip pipx

# Install uv using pipx
echo "Installing uv..."
# Ensure pipx's path is correctly configured for the current session.
python3 -m pipx ensurepath
pipx install uv
echo "uv installed successfully."
