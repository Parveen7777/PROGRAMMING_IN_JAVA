#!/usr/bin/env bash
# Compile and run every Task B1 program.  Usage: ./run.sh
set -e
mkdir -p out results
javac -d out src/*.java
echo "=== PassStore (Complete the Code) ===";   java -cp out PassStore               | tee results/passstore_output.txt
echo "=== PassStoreTest ===";                   java -cp out PassStoreTest           | tee results/test_output.txt
echo "=== BufferBenchmark (takes a while) ==="; java -cp out BufferBenchmark         | tee results/buffer_output.txt
echo "=== GrowthBenchmark ===";                 java -Xmx512m -cp out GrowthBenchmark | tee results/growth_output.txt
