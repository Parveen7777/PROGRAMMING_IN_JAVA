#!/usr/bin/env bash
# Creates a separate commit for every task so the history shows progressive work.
# Usage:  git init && git remote add origin https://github.com/<you>/CSA09-192462005-Assignment4.git
#         bash commit_each_task.sh && git branch -M main && git push -u origin main
set -e
git add README.md .gitignore && git commit -m "Add README and project layout" || true
git add Task01 && git commit -m "Task 1: diagnose poor design, redesign with FeeService and SwingWorker"
git add Task02 && git commit -m "Task 2: outpatient desk event model, handlers and event trace"
git add Task03 && git commit -m "Task 3: library search screen with nested layouts"
git add Task04 && git commit -m "Task 4: DrawPad forensics, repair, BufferedImage alternative"
git add Task05 && git commit -m "Task 5: four event-handling styles and shared Action"
git add Task06 && git commit -m "Task 6: layered Swing + JDBC event registration application"
git add tools screenshots && git commit -m "Add evidence tool and screenshots"
git log --oneline
