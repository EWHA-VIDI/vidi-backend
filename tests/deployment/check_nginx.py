import ipaddress
import json
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import uuid


ROOT = Path(__file__).resolve().parents[2]
IMAGE = next(
    line.split()[1]
    for line in (ROOT / 'deploy/frontend.Dockerfile').read_text().splitlines()
    if line.startswith('FROM nginx:')
)
HTTP = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def docker(*arguments):
    return subprocess.check_output(['docker', *arguments], text=True).strip()


def response(url):
    try:
        with HTTP.open(url, timeout=2) as result:
            return result.status, result.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode()
    except (urllib.error.URLError, TimeoutError):
        return 0, ''


def eventually(check, description):
    deadline = time.monotonic() + 30
    while time.monotonic() < deadline:
        if check():
            return
        time.sleep(0.5)
    raise AssertionError(description)


def main():
    network = 'vidi-nginx-check-' + uuid.uuid4().hex[:10]
    frontend = network + '-frontend'
    backends = [network + '-backend-1', network + '-backend-2']
    containers = []
    docker('network', 'create', network)
    try:
        subnet = ipaddress.ip_network(docker(
            'network', 'inspect', network, '--format', '{{(index .IPAM.Config 0).Subnet}}',
        ))
        with tempfile.TemporaryDirectory() as directory:
            workspace = Path(directory)
            upstream = workspace / 'nginx.conf'
            upstream.write_text('''events {}
http {
    server {
        listen 8080;
        if ($http_host ~ "_") { return 400; }
        location = /actuator/health { return 200 '{"status":"UP"}'; }
        location / { return 200 "$request_uri"; }
    }
}
''')
            (workspace / 'index.html').write_text('<main>VIDI SPA</main>')
            (workspace / 'index.html').chmod(0o644)

            def start_backend(name, address):
                docker(
                    'run', '-d', '--name', name, '--network', network,
                    '--network-alias', 'backend', '--ip', str(address),
                    '-v', f'{upstream}:/etc/nginx/nginx.conf:ro', IMAGE,
                )
                containers.append(name)

            start_backend(backends[0], subnet.network_address + 10)
            container_id = docker(
                'run', '-d', '--name', frontend, '--network', network,
                '-p', '127.0.0.1::80',
                '-v', f'{ROOT / "deploy/nginx.conf"}:/etc/nginx/conf.d/default.conf:ro',
                '-v', f'{workspace / "index.html"}:/usr/share/nginx/html/index.html:ro', IMAGE,
            )
            containers.append(frontend)
            bindings = json.loads(docker('inspect', frontend))[0]['NetworkSettings']['Ports']
            base = 'http://127.0.0.1:' + bindings['80/tcp'][0]['HostPort']

            def healthy():
                status, body = response(base + '/healthz/backend')
                return status == 200 and json.loads(body).get('status') == 'UP'

            eventually(healthy, 'Initial proxied backend health did not become UP')
            assert response(base + '/api/probe?phase=first') == (200, '/api/probe?phase=first')
            assert response(base + '/healthz')[0] == 200
            assert 'VIDI SPA' in response(base + '/login')[1]
            assert response(base + '/assets/missing.js')[0] == 404

            docker('rm', '-f', backends[0])
            containers.remove(backends[0])
            assert response(base + '/healthz')[0] == 200
            assert not healthy(), 'Proxied health must fail while the backend is unavailable'

            start_backend(backends[1], subnet.network_address + 11)
            eventually(healthy, 'Nginx did not recover after the backend IP changed')
            assert response(base + '/api/probe?phase=replaced') == (
                200, '/api/probe?phase=replaced',
            )
            assert docker('inspect', frontend, '--format', '{{.Id}}') == container_id
            print('PASS: IP replacement recovers without frontend reload; health, API, SPA, assets')
    finally:
        for name in reversed(containers):
            subprocess.run(['docker', 'rm', '-f', name], stdout=subprocess.DEVNULL, check=False)
        subprocess.run(['docker', 'network', 'rm', network], stdout=subprocess.DEVNULL, check=True)


if __name__ == '__main__':
    main()
