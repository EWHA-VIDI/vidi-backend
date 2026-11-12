import base64
import json
import os
import shlex
from pathlib import Path

required = ('AWS_REGION', 'PARAMETER_PREFIX', 'BACKEND_IMAGE', 'FRONTEND_IMAGE')
values = {key: os.environ[key] for key in required}
compose = base64.b64encode(Path('deploy/compose.yml').read_bytes()).decode()
script = base64.b64encode(Path('deploy/remote-deploy.sh').read_bytes()).decode()
exports = ' '.join(f'{key}={shlex.quote(value)}' for key, value in values.items())
commands = [
    'set -eu',
    'install -d -m 0700 /opt/vidi',
    f"printf %s {shlex.quote(compose)} | base64 -d > /opt/vidi/compose.yml",
    f"printf %s {shlex.quote(script)} | base64 -d > /opt/vidi/deploy.sh",
    f'{exports} bash /opt/vidi/deploy.sh',
]
print(json.dumps({'commands': commands}))
