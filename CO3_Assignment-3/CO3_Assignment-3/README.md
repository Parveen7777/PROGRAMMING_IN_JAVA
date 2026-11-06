# CSA09-<RegisterNumber>-Assignment3 — Programming in Java (CO3)

| | Student A |
|---|---|
| Name | PARVEEN | 
| Register number | 192462005 |  

**Java version used:** OpenJDK 21 (any JDK 17+ works). Check with `java -version`.

## How to compile and run
Every task folder is self-contained (default package). From the repository root you can run everything with `./run_all.sh`,
or per task, e.g. `cd Task01 && javac *.java && java ATM`.

| Task | Folder | Compile & run | Result (captured output is in the `output_*.txt` files) |
|---|---|---|---|
| Q1 ATM exceptions | `Task01` | `javac *.java && java ATM` ; `java UncaughtDemo` ; `java ErrorCodeATM` | 8 tests hit 6 different catch paths, `finally` ran 8/8 times. Wrong catch order = compile error (`CatchOrderError.java.txt`). Throwing an exception with stack trace ≈ 7,300 ns vs error code ≈ 20 ns. |
| Q2 Exam portal | `Task02/ExamPortal`, `Task02/CompleteTheCode`, `Task02/Alternative` | `javac *.java && java ExamPortalDemo` / `java StorageDemo` / `java UncheckedAndCost` | Chaining keeps `IOException` as cause; lossy version gives `getCause() == null`. StorageDemo prints the expected 3 lines. |
| Q3 Resource safety | `Task03` | `javac *.java && java SalesReport` ; `java SuppressedDemo` | Buggy manual version leaks 2 handles and writes an empty report; manual-finally and try-with-resources leak 0 and give identical reports. |
| Q4 Threads | `Task04` | `javac *.java && java Scanning` ; `./run10.sh` ; `java RunVsStart` ; `java DaemonDemo` ; `java PriorityDemo` ; `java ExecutorScan` | Finish order varies between runs; total always 126. Fixed pool scales ≈ n/p. |
| Q5 Race conditions | `Task05` | `javac *.java && java SeatBooking` ; `java SeatBookingUnsafe` ; `java RaceTest` ; `java Strategies` | Synchronized: always 10 seats. `RaceTest` (unsynchronized, widened window) sold 10–29 seats. |
| Q6 Deadlock / producer-consumer | `Task06` | `javac *.java && java TokenCounter` ; `java TokenCounterTrace` ; `java DeadlockDemo` ; `java Alternatives` | Sum 210. `if` instead of `while` gave a wrong sum in 11 of 30 runs. Deadlock detected, lock ordering fixes it. |
| Q7 Fault-tolerant processor | `Task07` | `javac *.java && java ResultProcessor` ; `java Benchmark` ; `java AltProcessors` | Hand-checked set matches. 10,000 files: summary equals generator ground truth for pools of 1/2/4/8. |

## Notes
* `screenshots/` in each task folder is empty on purpose: **take your own screenshots by running each program on your machine** and paste them there.
* Task07 generates its 10,000 test files at run time in a temp folder, so they are not stored in the repository. The 11-file hand-checked set is in `Task07/data/`.
* Timings were measured on a 1-CPU-core container; your numbers will differ (compare shapes, not absolute values).
* `./commit_each_task.sh` creates one commit per task.
