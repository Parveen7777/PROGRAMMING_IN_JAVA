# CSA09-<RegisterNumber>-Assignment2  (Programming in Java – CO2)

| | |
|---|---|
| **Name** | `PARVEEN` |
| **Register Number** | `192462005` |
| **Course** | Programming in Java (CSA09) – Assignment 2, CO2 |
| **Java version** | OpenJDK 21 (any JDK 17+ works; `javac`/`java` must be on the PATH) |
| **Faculty collaborator** | add the course faculty as collaborator (or keep this repository public) |

> Rename this folder / repository to `CSA09-<RegisterNumber>-Assignment2` before pushing.

## Repository layout

```
Task01/  Q1  String vs StringBuilder vs StringBuffer (+ thread-safety demo)
Task02/  Q2  ArrayList vs LinkedList vs ArrayDeque (+ plain array)
Task03/  Q3  hashCode–equals contract  (Complete the Code: Attendance.java)
Task04/  Q4  HashMap vs TreeMap vs LinkedHashMap (+ Hashtable, sorted array)
Task05/  Q5  Generics, Comparable & Comparator  (Complete the Code: LeaderboardDemo.java)
Task06/  Q6  Queue & PriorityQueue triage        (Complete the Code: Triage.java)
Task07/  Q7  Day-4 design challenge – Diagnose, Redesign, Validate (pharmacy inventory)
Assignment2_Report.docx   (the written report for Q1–Q7)
```

Every Task folder contains: the Java source, `output_*.txt` (console output of the run), raw data as `*.csv`,
a `chart_*.png` where relevant, and an empty `screenshots/` folder for **your own** console screenshots.

## How to compile and run (run inside each Task folder)

| Task | Compile | Run |
|---|---|---|
| Task01 | `javac StringBench.java ThreadLogDemo.java` | `java -Xmx2g StringBench` then `java ThreadLogDemo` |
| Task02 | `javac ListBench.java` | `java -Xmx2g ListBench` |
| Task03 | `javac Attendance.java AttendanceBefore.java HashContractTests.java AlternativeBench.java` | `java AttendanceBefore` (TODO 1 not done → prints 3), `java Attendance` (TODO 1 done → prints 2), `java HashContractTests`, `java -Xmx2g AlternativeBench` |
| Task04 | `javac MedicineBench.java` | `java -Xmx2g MedicineBench` |
| Task05 | `javac LeaderboardDemo.java LeaderboardTests.java GenericDesign.java` | `java LeaderboardDemo`, `java LeaderboardTests`, `java -Xmx2g GenericDesign` |
| Task06 | `javac Triage.java TriageDesign.java` | `java Triage`, `java -Xmx2g TriageDesign` |
| Task07 | `javac PharmacyRedesign.java` | `java -Xmx3g PharmacyRedesign` |

Programs write their raw data to CSV files in the current folder. Timing results differ from machine to machine;
trends (growth rates, ratios) are what matter. Task01, Task03 and Task07 use `com.sun.management.ThreadMXBean`
to count allocated bytes (available in OpenJDK / Oracle JDK).

## Short result of each task

| Task | Result |
|---|---|
| Q1 | `String +` in a loop is O(n²): 37× slower and 142× more memory allocated than StringBuilder for a 1,000-field ticket; unsafe shared StringBuilder corrupted the log in 3 of 5 trials, StringBuffer / synchronized did not. |
| Q2 | ArrayList is the right default for a playlist (get k-th ≈ 1,200× faster than LinkedList at n = 200,000); ArrayDeque for the “Up Next” queue (ArrayList was ≈ 110× slower). |
| Q3 | Without `hashCode()` the program prints `Unique students: 3`; with TODO 1 completed it prints `Unique students: 2`. |
| Q4 | HashMap/LinkedHashMap lookups ≈ 40–210 ns vs TreeMap ≈ 1.8–2.5 µs at large n; LinkedHashMap chosen; code-order report needs an O(n log n) sort. |
| Q5 | Output `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`; faulty comparators (subtraction, no tie-breaker, never-zero, “close = equal”) detected. |
| Q6 | Output `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`; 5,000 patients/day cost ≈ 4 ms of CPU; starvation shown and fixed by aging. |
| Q7 | Pharmacy daily code-order report ≈ 7× faster (500,000 medicines) with a LinkedHashMap + incrementally merged sorted array; 62× fewer string comparisons. |

## Suggested commit history (at least one commit per task)

```bash
git init
git add Task01 && git commit -m "Q1: String/StringBuilder/StringBuffer experiments"
git add Task02 && git commit -m "Q2: ArrayList/LinkedList/ArrayDeque experiments"
git add Task03 && git commit -m "Q3: hashCode-equals contract tests and Attendance fix"
git add Task04 && git commit -m "Q4: HashMap/TreeMap/LinkedHashMap pharmacy inventory"
git add Task05 && git commit -m "Q5: generic Leaderboard and comparator tests"
git add Task06 && git commit -m "Q6: triage PriorityQueue, starvation and aging"
git add Task07 && git commit -m "Q7: design challenge - diagnose, redesign, validate"
git add README.md Assignment2_Report.docx && git commit -m "Add README and report"
git branch -M main
git remote add origin https://github.com/<username>/CSA09-<RegisterNumber>-Assignment2.git
git push -u origin main
```
