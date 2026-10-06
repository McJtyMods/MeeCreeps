#!/usr/bin/env python3
"""Compile and run the NeoForge integration tests."""
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
wrapper = root / ("gradlew.bat" if os.name == "nt" else "gradlew")
sys.exit(subprocess.run([str(wrapper), "runGameTestServer", *sys.argv[1:]], cwd=root).returncode)
