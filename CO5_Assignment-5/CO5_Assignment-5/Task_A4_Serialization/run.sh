#!/usr/bin/env bash
set -e
mkdir -p out results
javac -d out src/*.java
java -cp out TransientDemo      | tee results/transient_output.txt
java -cp out SerializationBench | tee results/serialization_bench_output.txt
echo "--- serialVersionUID experiment ---" | tee results/versioning_output.txt
cd versioning
for v in v1 v2_same_uid v3_no_uid; do mkdir -p ../out/$v; javac -d ../out/$v $v/*.java; done
( cd ../out/v1 && java -cp . Main && mv residents_v1.ser ../ ) | tee -a ../results/versioning_output.txt
echo "v2 (phone added, SAME serialVersionUID):" | tee -a ../results/versioning_output.txt
( cd ../out && java -cp v2_same_uid Main ) | tee -a ../results/versioning_output.txt
echo "v3 (phone added, NO serialVersionUID declared):" | tee -a ../results/versioning_output.txt
( cd ../out && java -cp v3_no_uid Main ) | tee -a ../results/versioning_output.txt
