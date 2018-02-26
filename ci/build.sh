#!/bin/bash
# fail if any commands fails
set -e

./gradlew clean assemble createDebugTestCoverage  --stacktrace
