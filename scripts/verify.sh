#!/bin/sh

set -eu

mode="${1:-quick}"

git diff --check
git diff --cached --check

case "$mode" in
  quick)
    ./gradlew ktlintCheck detekt test --console=plain
    ;;
  full)
    ./gradlew ktlintCheck detekt test build --console=plain
    ;;
  *)
    echo "Usage: ./scripts/verify.sh [quick|full]"
    exit 2
    ;;
esac
