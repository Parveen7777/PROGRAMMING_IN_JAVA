#!/usr/bin/env bash
# Usage: ./run.sh /path/to/mysql-connector-j.jar      (set DB_URL / DB_USER / DB_PASS if needed)
set -e
JAR=${1:?"give the path of the MySQL Connector/J jar"}
mkdir -p out results
javac -d out src/*.java
java -cp out:$JAR LoginDemo               | tee results/login_output.txt
java -cp out      DamagedFileDemo         | tee results/damaged_file_output.txt
java -cp out:$JAR ValidationCostBenchmark | tee results/validation_cost_output.txt
