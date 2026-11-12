import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]


class SsmWaitTests(unittest.TestCase):
    def run_wait(self, responses, fallback=None):
        with tempfile.TemporaryDirectory() as directory:
            workspace = Path(directory)
            (workspace / 'responses.json').write_text(json.dumps(responses))
            (workspace / 'aws').write_text(
                '#!/usr/bin/env python3\n'
                'import json, os, pathlib, sys\n'
                'p = pathlib.Path(os.environ["TEST_STATE"])\n'
                'count = int((p / "calls").read_text()) if (p / "calls").exists() else 0\n'
                '(p / "calls").write_text(str(count + 1))\n'
                'responses = json.loads((p / "responses.json").read_text())\n'
                'value = responses[count] if count < len(responses) else os.environ["TEST_FALLBACK"]\n'
                'if value.startswith("error:"):\n'
                '    print("An error occurred (" + value[6:] + ")", file=sys.stderr)\n'
                '    sys.exit(255)\n'
                'print(value)\n'
            )
            (workspace / 'sleep').write_text('#!/bin/sh\nexit 0\n')
            for name in ['aws', 'sleep']:
                (workspace / name).chmod(0o755)
            environment = os.environ | {
                'PATH': directory + os.pathsep + os.environ['PATH'],
                'TEST_STATE': directory,
                'TEST_FALLBACK': fallback or 'unexpected-extra-call',
            }
            result = subprocess.run(
                ['bash', str(ROOT / 'deploy/wait-for-command.sh'), 'command', 'instance'],
                env=environment, capture_output=True, text=True, timeout=30,
            )
            return result, int((workspace / 'calls').read_text())

    def test_eventual_consistency_then_success(self):
        result, calls = self.run_wait([
            'error:InvocationDoesNotExist', 'Pending', 'InProgress', 'Delayed', 'Success',
        ])
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(calls, 5)

    def test_terminal_failures_stop_immediately(self):
        for status in ['Failed', 'Cancelled', 'TimedOut', 'Cancelling']:
            with self.subTest(status=status):
                result, calls = self.run_wait([status, 'Success'])
                self.assertEqual(result.returncode, 1)
                self.assertEqual(calls, 1)

    def test_permission_errors_are_not_retried(self):
        result, calls = self.run_wait(['error:AccessDeniedException', 'Success'])
        self.assertEqual(result.returncode, 255)
        self.assertEqual(calls, 1)
        self.assertIn('AccessDeniedException', result.stderr)

    def test_unknown_status_fails(self):
        result, calls = self.run_wait(['unexpected-status'])
        self.assertEqual(result.returncode, 1)
        self.assertEqual(calls, 1)

    def test_pending_status_has_a_retry_limit(self):
        result, calls = self.run_wait([], fallback='Pending')
        self.assertEqual(result.returncode, 1)
        self.assertEqual(calls, 120)
        self.assertIn('Timed out', result.stderr)


if __name__ == '__main__':
    unittest.main()
