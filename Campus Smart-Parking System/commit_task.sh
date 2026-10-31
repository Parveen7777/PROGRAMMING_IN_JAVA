#!/bin/bash
# Usage: ./commit_task.sh 03   -> commits only Task03 with a clear message. Run it as you finish each task.
n=$1; git add Task$n README.md 2>/dev/null
git commit -m "Task $n: completed code, output and notes" && echo "Committed Task $n"
