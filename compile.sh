#!/bin/bash
set -e

echo "Compiling Java sources..."

# Create output directory if missing
mkdir -p out

# Compile all .java files into out/
javac -cp "lib/*" -d out $(find src/main -name "*.java")

echo "Build successful. Classes are in ./out"