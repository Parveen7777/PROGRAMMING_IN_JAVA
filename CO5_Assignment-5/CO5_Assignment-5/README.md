# Programming in Java - Assignment 5 (CO5): Data Persistence Engineering

| | |
|---|---|
| **Name** | PARVEEN |
| **Register Number** | 192462005 |
| **Course** | CSA0902 - Programming in Java |
| **Java version used** | OpenJDK 21 (any JDK 17+ works) |
| **Database used** | MariaDB 10.11 (MySQL-compatible) with the `jdbc:mysql://` URL format. On your own machine use MySQL 8 + MySQL Connector/J. |

> The measurements stored in each `results/` folder were produced on one specific machine. Re-run every program on **your** computer, take your own output screenshots, and compare your numbers with the report (absolute times will differ; the trends must match).

## Repository layout
```
Task_B1_FileStore/     PassStore (Complete the Code), LineCounter, tests, buffering + growth benchmarks
Task_B2_Adversarial/   SQL-injection demo, damaged-file demo, validation-cost benchmark
Task_B3_JdbcBatch/     PassLoader (Complete the Code), 6 insert strategies, correctness, failure tests
Task_B4_Migration/     Migration with rejects report, DatabaseMetaData report, concurrent counters
Task_A4_Serialization/ transient / serialVersionUID experiments (evidence for Task A4)
sql/create_database.sql  Test database + tables
```
Part A (pseudocode, record layouts, reasoning) is in the Word report; it contains no Java code, as the assignment requires.

## One-time setup
1. Install JDK 17+ and MySQL 8 (or MariaDB). Download **MySQL Connector/J** (the platform-independent `.jar`).
2. Create the **test** database (never use real data):  `mysql -u root -p < sql/create_database.sql`
3. Set the connection (defaults: `jdbc:mysql://localhost:3306/transport_test`, user `root`, password `root`):
   - Windows (cmd): `set DB_USER=root` and `set DB_PASS=yourpassword`
   - Linux / macOS: `export DB_USER=root DB_PASS=yourpassword`
   - Optional: `DB_URL=jdbc:mysql://localhost:3306/transport_test`

## How to compile and run each task (Linux / macOS / Git Bash)
```
cd Task_B1_FileStore      && ./run.sh                                   # no database needed
cd Task_B2_Adversarial    && ./run.sh /path/to/mysql-connector-j.jar
cd Task_B3_JdbcBatch      && ./run.sh /path/to/mysql-connector-j.jar
cd Task_B4_Migration      && ./run.sh /path/to/mysql-connector-j.jar
cd Task_A4_Serialization  && ./run.sh                                   # no database needed
```
On Windows without Git Bash, run the commands inside each `run.sh` by hand
(`javac -d out src/*.java`, then `java -cp "out;path\to\connector.jar" ClassName`).

## Result of each task (short note)
| Task | Result |
|---|---|
| B1 | `PassStore.find()` completed; output `[P1003, 192311003, Divya, 7, 3800.0]` then `null`. 9/9 tests pass. Buffering made the 50 MB line count ~600x faster. Scan time grows linearly with records. |
| B2 | Injection inputs (`x' OR '1'='1`, `admin'-- `, `' OR 1=1 -- `) are accepted by the unsafe login and rejected by the PreparedStatement login. Damaged file lines are skipped, never crash the store. |
| B3 | `PassLoader.loadAll()` completed (TODO 1-3). One transaction: 100,000 rows loaded ~6x faster than auto-commit. A mid-way failure leaves 0 rows (transaction) vs 5,000 rows (auto-commit). |
| B4 | Migration loaded 99,000 valid rows, rejected 1,000 damaged lines with reasons. 4 concurrent counters lost no rows. Primary-key lookup is ~200x faster than scanning the file. |
| A4 (support) | Non-transient connection throws `NotSerializableException`; a changed/undeclared `serialVersionUID` throws `InvalidClassException`. |

## Suggested commit history (one commit per task, as required)
```
git init && git add README.md .gitignore sql && git commit -m "Add README and SQL script"
git add Task_B1_FileStore && git commit -m "Task B1: file store, buffering and growth benchmarks"
git add Task_B2_Adversarial && git commit -m "Task B2: injection and damaged-file experiments"
git add Task_B3_JdbcBatch && git commit -m "Task B3: Statement vs PreparedStatement vs batch, PassLoader"
git add Task_B4_Migration && git commit -m "Task B4: migration, metadata and concurrency evidence"
git add Task_A4_Serialization && git commit -m "Task A4: serialization experiments"
git remote add origin https://github.com/<your-github-username>/CSA09-192462005-Assignment5.git
git push -u origin main
```
Commit each task **when you actually finish it** (over several days); the faculty checks the commit history.
