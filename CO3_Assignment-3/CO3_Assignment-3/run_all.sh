#!/bin/bash
# Compile and run every task from the repository root. Needs JDK 17+ (javac on PATH).
set -e
run() { # dir  mainclass...
  d=$1; shift
  echo "################ $d ################"
  (cd "$d" && javac *.java && for m in "$@"; do echo "--- java $m"; java $m || true; done; rm -f *.class)
}
run Task01 ATM UncaughtDemo ErrorCodeATM
run Task02/ExamPortal ExamPortalDemo
run Task02/CompleteTheCode StorageDemo
run Task02/Alternative UncheckedAndCost
run Task03 SalesReport SuppressedDemo
run Task04 Scanning RunVsStart DaemonDemo PriorityDemo ExecutorScan
run Task05 SeatBooking SeatBookingUnsafe RaceTest Strategies
run Task06 TokenCounter TokenCounterTrace DeadlockDemo Alternatives
run Task07 ResultProcessor Benchmark AltProcessors
