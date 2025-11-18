#!/bin/bash
set -e
export DBPath="test.db"

java \
  --enable-native-access=ALL-UNNAMED \
  -cp "out:lib/*" \
  scheduler.Scheduler