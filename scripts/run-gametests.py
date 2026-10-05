#!/usr/bin/env python3
"""Compile and run Forge GameTests in a separate JVM with its actual exit code."""
import json
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
wrapper = root / ("gradlew.bat" if os.name == "nt" else "gradlew")
subprocess.run([str(wrapper), "writeGameTestLaunch", *sys.argv[1:]], cwd=root, check=True)
launch = json.loads((root / "build/gametest-launch.json").read_text())
environment = os.environ.copy()
environment.update(launch["environment"])
result = subprocess.run([launch["executable"], *launch["args"]], cwd=launch["cwd"], env=environment)
sys.exit(result.returncode)
