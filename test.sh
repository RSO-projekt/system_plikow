#!/bin/bash

check() {
	if [ $1 -eq 1 ]
	then
		echo PASS
	else
		echo FAIL
	fi
}

echo "1: Test connection"
result=$(java -jar client.jar ls | grep "Connecting .* OK" | wc -l)
check $result

if [ $result -eq 1 ]
then 
	java -jar client.jar rm /testDir/testFile.txt > /dev/null
	java -jar client.jar rm /testDir > /dev/null
else
	exit
fi

echo "2: Test make directory"
result1=$(java -jar client.jar mkdir /testDir | grep "Done." | wc -l)
check $result

echo "3: Test lookup"
result=$(java -jar client.jar ls /testDir | grep "Chosen folder is empty" | wc -l)
check $result

echo "4: Test make file"
result=$(java -jar client.jar mkfile /testDir/testFile.txt 10 | grep "Done." | wc -l)
check $result
