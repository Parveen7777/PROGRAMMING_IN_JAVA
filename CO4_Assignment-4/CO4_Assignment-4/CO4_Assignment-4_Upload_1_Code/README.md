# CSA0902 – Programming in Java · Assignment 4 (CO4) – Event-Driven GUI Design

| | |
|---|---|
| **Name** | PARVEEN |
| **Register Number** | 192462005 |
| **Course** | Programming in Java – course code CSA0902 (CSA09) |
| **Java version** | JDK 17 or later (developed and tested on OpenJDK 21) |
| **Libraries** | AWT, Swing, JDBC. Task 6 on MySQL needs *MySQL Connector/J* (`mysql-connector-j-8.x.jar`) |

## Repository layout
```
Task01/  GUI design review  – bad design (bad/), FeeService + FeeWindow redesign, tests, benchmark
Task02/  Outpatient desk    – AWT event-driven window + event-trace demo
Task03/  Library search     – nested BorderLayout/FlowLayout (AWT) + GridBagLayout alternative
Task04/  DrawPad forensics  – defective, half-fixed, fixed (Complete-the-Code), BufferedImage alternative, compiler-error demo
Task05/  Converter          – four handler styles x two requirement versions, Complete-the-Code, shared-Action alternative
Task06/  Event registration – layered Swing + JDBC application, SQL script, benchmark
tools/   Evidence.java      – drives the GUIs with java.awt.Robot and writes screenshots
screenshots/                – output screenshots (each referenced in the report)
```

## How to compile and run (Windows / Linux / macOS)
Open a terminal in the task folder. `javac` and `java` must be on the PATH.

| Task | Compile | Run |
|---|---|---|
| 1 | `javac *.java` | `java FeeWindow` · tests: `java FeeServiceTest` · analysis: `java DispatchBenchmark` |
| 2 | `javac *.java` | `java AppointmentDesk` · event trace: `java TraceDemo` |
| 3 | `javac *.java` | `java LibrarySearch` · alternative: `java LibrarySearchGridBag` |
| 4 | `javac *.java` | `java DrawPadBuggy` (bug) · `java DrawPadHalfFixed` · `java DrawPad` (fixed) · `java DrawPadBuffered` · `java RepaintBenchmark` |
| 4.4 | `javac compile_error_demo/DrawPadTypo.java` | *expected to fail* – copy the compiler message |
| 5 | `javac Rates.java Converter.java ConverterSharedAction.java` | `java Converter` · `java ConverterSharedAction` |
| 5.1 | `cd v1 && javac *.java` | `java ConverterA_Frame` (also `B_Inner`, `C_Anonymous`, `D_Lambda`) |
| 5.2 | `cd v2 && javac *.java` | same class names – version with Enter key + dialog |
| 6 | `javac -d out $(find src -name "*.java")` (Windows: see `Task06/README.md`) | `java -cp out com.csa09.events.App` |

### Task 6 with MySQL
1. Run `Task06/sql/schema.sql` in MySQL Workbench / mysql client.
2. Copy `db.properties.example` to `db.properties` and set your user/password.
3. `java -cp "out;mysql-connector-j-8.x.jar" com.csa09.events.App` (use `:` instead of `;` on Linux/macOS).
Without `db.properties` the program starts in clearly labelled **DEMO mode** (in-memory data, same rules).

## Result of each task (fill in after YOU run it)
| Task | Result |
|---|---|
| 1 | Redesigned window validates input, runs the bank call in a `SwingWorker`, uses `PreparedStatement`; 11/11 service tests pass |
| 2 | Doctor list refreshes from a `Map` on department change; event order traced and recorded in `Task02/trace_output.txt` |
| 3 | Results area grows on resize, search row and buttons keep their size (3 screenshots) |
| 4 | Defect = `mouseclicked` (lower-case c) – fixed; dots survive minimise/restore; compiler message recorded |
| 5 | Four handler styles implemented; change effort measured with `diff` (`Task05/diff_summary.txt`) |
| 6 | Register / filter / paginate / export; duplicates rejected by `UNIQUE (student_reg, event_id)` |

## Commit plan (one commit per task at least)
Run `bash commit_each_task.sh` once **after** `git init` and adding your remote – or commit manually while you work:
`git add Task01 && git commit -m "Task 1: redesign fee window with FeeService"` … and so on.
