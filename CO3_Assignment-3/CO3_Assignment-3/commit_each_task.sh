#!/bin/bash
# Creates one commit per task so the commit history shows progressive work.
git init 2>/dev/null
git add README.md .gitignore run_all.sh commit_each_task.sh diagrams && git commit -m "Add README, scripts and diagrams"
for t in 1 2 3 4 5 6 7; do
  d=Task0$t
  git add $d && git commit -m "Task $t: code, input files and outputs"
done
echo "Now: git branch -M main && git remote add origin <YOUR_REPO_URL> && git push -u origin main"
