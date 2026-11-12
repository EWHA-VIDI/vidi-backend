#!/usr/bin/env bash
set -euo pipefail

command_id=${1:?Provide the SSM command ID}
instance_id=${2:?Provide the EC2 instance ID}
error_file=$(mktemp)
trap 'rm -f "$error_file"' EXIT

for attempt in $(seq 1 120); do
    if status=$(aws ssm get-command-invocation \
        --command-id "$command_id" --instance-id "$instance_id" \
        --query Status --output text 2>"$error_file"); then
        case "$status" in
            Success) exit 0 ;;
            Pending|InProgress|Delayed) ;;
            *)
                printf 'SSM command ended with status: %s\n' "$status" >&2
                exit 1
                ;;
        esac
    else
        exit_code=$?
        if [[ $(<"$error_file") != *'(InvocationDoesNotExist)'* ]]; then
            cat "$error_file" >&2
            exit "$exit_code"
        fi
    fi
    if (( attempt < 120 )); then
        sleep 5
    fi
done

printf 'Timed out waiting for the SSM command.\n' >&2
exit 1
