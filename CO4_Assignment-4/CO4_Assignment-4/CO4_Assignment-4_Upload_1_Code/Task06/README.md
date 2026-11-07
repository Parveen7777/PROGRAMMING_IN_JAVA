# Task 6 – Campus Event Registration (Swing + JDBC)

Packages: `model` (record) · `dao` (JDBC + in-memory) · `service` (rules) · `ui` (Swing) · `App` (entry point).

Linux/macOS:  `javac -d out $(find src -name "*.java")`
Windows:      `dir /s /b src\*.java > sources.txt` then `javac -d out @sources.txt`

Run (demo mode):    `java -cp out com.csa09.events.App`
Run (MySQL):        `java -cp "out;mysql-connector-j-8.x.jar" com.csa09.events.App`  (Linux/macOS use `:`)
Benchmark:          `java -cp out com.csa09.events.LoadBenchmark`
SQL script:         `sql/schema.sql`
