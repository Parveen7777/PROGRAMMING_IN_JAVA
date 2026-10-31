#!/bin/bash
# Runs every program and saves its text output next to the code (take screenshots of these runs too).
set -e
( cd Task02 && javac CastDemo.java && java CastDemo > output.txt )
( cd Task03 && javac ParkingFacility.java && java ParkingFacility > output.txt )
( cd Task11 && javac -d out $(find src -name "*.java") && java -cp out parking.app.TestRunner > output.txt )
( cd Task04 && javac -cp ../Task11/out TicketDemo.java && java -cp ../Task11/out:. TicketDemo > output.txt )
( cd Task06 && javac CtorTrace.java && java CtorTrace > output.txt )
( cd Task08 && javac FeeDemo.java && java FeeDemo > output.txt )
( cd Task12 && javac -d out $(find src -name "*.java") && java -cp out parking.app.Day4Tests > output_day4.txt && java -cp out parking.app.Main < input.txt > output_main.txt )
echo "Done. Now take screenshots and commit task by task."
