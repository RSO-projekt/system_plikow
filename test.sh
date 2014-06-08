#!/bin/bash

check() {
	if [ $1 -eq 1 ]
	then
		echo -e "\e[32mPASS"
	else
		echo -e "\e[31mFAIL"
	fi
	echo -ne "\e[39m"
}

echo -n "1: Test connection                      "
result=$(java -jar client.jar ls | grep "Connecting .* OK" | wc -l)
check $result

if [ $result -ne 1 ]
then
	exit
fi

echo -n "2: Test lookup (empty dir)              "
result=$(java -jar client.jar ls / | grep "Chosen folder is empty" | wc -l)
check $result

echo -n "3: Test make directory                  "
result1=$(java -jar client.jar mkdir /testDir | grep "Done." | wc -l)
check $result

echo -n "4: Test upload file                     "
echo "My test file" > myFile.txt
result=$(java -jar client.jar writeAll /uploadFile.txt myFile.txt | grep "Done." | wc -l)
rm myFile.txt
check $result

echo -n "5: Test download file                   "
result=$(java -jar client.jar readAll /uploadFile.txt downloadFile.txt | grep "Done." | wc -l)
check $result

echo -n "6: Test downloaded file have size       "
if [ $result -eq 1 ]
then
	result=$(wc -c downloadFile.txt | grep "13 downloadFile.txt" | wc -l)
	rm downloadFile.txt
fi
check $result

echo -n "7: Test make file                       "
result=$(java -jar client.jar mkfile /testFile.txt | grep "Done." | wc -l)
check $result

echo -n "8: Test lookup (test directory exists)  "
result=$(java -jar client.jar ls / | grep "DIR  0\stestDir" | wc -l)
check $result

echo -n "9: Test lookup (test file exists)       "
result=$(java -jar client.jar ls / | grep "FILE 0\stestFile.txt" | wc -l)
check $result

echo -n "10: Test move                           "
result=$(java -jar client.jar mv /testFile.txt /testDir/testFile.txt | grep "Done." | wc -l)
check $result

echo -n "11: Test remove not empty dir           "
result=$(java -jar client.jar rm /testDir | grep "You cannot remove unempty directory" | wc -l)
check $result

echo -n "12: Test remove file                    "
result=$(java -jar client.jar rm /testDir/testFile.txt | grep "Done." | wc -l)
check $result

echo -n "13: Test remove uploaded file           "
result=$(java -jar client.jar rm /uploadFile.txt | grep "Done." | wc -l)
check $result

echo -n "14: Test remove directory               "
result=$(java -jar client.jar rm /testDir | grep "Done." | wc -l)
check $result

echo -n "15: Test lookup (empty dir)             "
result=$(java -jar client.jar ls / | grep "Chosen folder is empty" | wc -l)
check $result
