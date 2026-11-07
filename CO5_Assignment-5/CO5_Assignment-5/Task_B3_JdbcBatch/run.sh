#!/usr/bin/env bash
# Usage: ./run.sh /path/to/mysql-connector-j.jar    (DB_URL / DB_USER / DB_PASS may be set)
# Table bus_pass must exist: run sql/create_database.sql first.
set -e
JAR=${1:?"give the path of the MySQL Connector/J jar"}
mkdir -p out results
javac -d out src/*.java
java -cp out:$JAR B3Correctness  | tee results/correctness_output.txt
java -cp out:$JAR B3FailureTest  | tee results/failure_output.txt
java -cp out:$JAR B3Benchmark    | tee results/benchmark_output.txt
