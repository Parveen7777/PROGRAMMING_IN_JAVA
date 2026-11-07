#!/usr/bin/env bash
# Usage: ./run.sh /path/to/mysql-connector-j.jar    (DB_URL / DB_USER / DB_PASS may be set)
set -e
JAR=${1:?"give the path of the MySQL Connector/J jar"}
mkdir -p out results
javac -d out src/*.java
java -cp out GenerateLegacyFile legacy_passes.csv 100000 | tee results/generate_output.txt
mysql_clear() { :; }   # table is truncated by the programs below as needed
java -cp out:$JAR InspectDb       | tee results/metadata_output.txt
java -cp out:$JAR MigrationApp legacy_passes.csv | tee results/migration_output.txt
java -cp out:$JAR ConcurrentCounters legacy_passes.csv | tee results/concurrency_output.txt
