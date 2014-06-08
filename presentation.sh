#!/bin/bash
echo "Start presentation"
echo
echo "java -jar client.jar ls /"
java -jar client.jar ls /
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar mkdir /testDir"
java -jar client.jar mkdir /testDir
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar writeAll /uploadedFile.txt myFile.txt"
echo "My test file" > myFile.txt
java -jar client.jar writeAll /uploadedFile.txt myFile.txt
rm myFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar readAll /uploadedFile.txt downloadedFile.txt"
java -jar client.jar readAll /uploadedFile.txt downloadedFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar mkfile /testFile.txt"
java -jar client.jar mkfile /testFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar ls /"
java -jar client.jar ls /
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar mv /testFile.txt /testDir/testFile.txt"
java -jar client.jar mv /testFile.txt /testDir/testFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar rm /testDir"
java -jar client.jar rm /testDir
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar rm /testDir/testFile.txt"
java -jar client.jar rm /testDir/testFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar rm /testDir"
java -jar client.jar rm /testDir
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar ls /"
java -jar client.jar ls /
echo
echo "End presentation"

