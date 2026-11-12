#!/usr/bin/env bash
set -euo pipefail
umask 077
cd /opt/vidi
: "${AWS_REGION:?}" "${PARAMETER_PREFIX:?}" "${BACKEND_IMAGE:?}" "${FRONTEND_IMAGE:?}"
registry=${BACKEND_IMAGE%%/*}
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$registry"
params_file=$(mktemp)
validation_dir=$(mktemp -d)
trap 'rm -f "$params_file" backend.env.next images.env.next; rm -rf "$validation_dir"' EXIT
aws ssm get-parameters-by-path --region "$AWS_REGION" --path "$PARAMETER_PREFIX" \
    --with-decryption --output json > "$params_file"
python3 - "$params_file" <<'PYENV'
import json, sys
from pathlib import Path
parameters = json.loads(Path(sys.argv[1]).read_text())['Parameters']
values = {p['Name'].rsplit('/', 1)[-1]: p['Value'] for p in parameters}
required = ('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')
if any(not values.get(key) for key in required):
    raise SystemExit('Required database parameters are missing')
if any('\n' in value or '\r' in value for value in values.values()):
    raise SystemExit('Multiline environment values are unsupported')
Path('backend.env.next').write_text(''.join(f'{key}={values[key]}\n' for key in required))
PYENV
printf 'BACKEND_IMAGE=%s\nFRONTEND_IMAGE=%s\n' "$BACKEND_IMAGE" "$FRONTEND_IMAGE" > images.env.next
# Validate and pull staged configuration before replacing the running configuration.
cp compose.yml "$validation_dir/compose.yml"
cp backend.env.next "$validation_dir/backend.env"
cp images.env.next "$validation_dir/images.env"
docker compose --project-directory "$validation_dir" --env-file "$validation_dir/images.env" -f "$validation_dir/compose.yml" config --quiet
docker compose --project-directory "$validation_dir" --env-file "$validation_dir/images.env" -f "$validation_dir/compose.yml" pull
mv backend.env.next backend.env
mv images.env.next images.env
docker compose --env-file images.env -f compose.yml up -d
health_is_up() {
    local body
    body=$(curl --fail --silent --show-error --connect-timeout 2 --max-time 5 "$1") || return 1
    python3 -c 'import json,sys; sys.exit(json.load(sys.stdin).get("status") != "UP")' <<<"$body"
}

deadline=$((SECONDS + 120))
while (( SECONDS < deadline )); do
    if health_is_up http://127.0.0.1:8080/actuator/health &&
        health_is_up http://127.0.0.1:8088/healthz/backend &&
        curl --fail --silent --show-error --connect-timeout 2 --max-time 5 \
            http://127.0.0.1:8088/healthz >/dev/null; then
        exit 0
    fi
    sleep 2
done

printf 'Deployment readiness checks did not pass.\n' >&2
exit 1
