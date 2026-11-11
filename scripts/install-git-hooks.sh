#!/bin/sh

set -eu
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/commit-msg scripts/verify.sh
