#!/bin/bash
echo "Start presentation"
echo
echo "Part 1: Standard work"
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
read -p "Press [Enter] key to continue..."
echo
echo "Part 2: Master server breakdown"
echo
echo "Master server 2 is shutting down. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "Master server 0 is shutting down. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar writeAll /uploadedFile2.txt myFile.txt"
java -jar client.jar writeAll /uploadedFile2.txt myFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar readAll /uploadedFile2.txt downloadedFile2.txt"
java -jar client.jar readAll /uploadedFile2.txt downloadedFile2.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "Master server 0 is starting up. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "Master server 2 is starting up. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "Part 3: Data server breakdown"
echo
echo "Data server 0 is shutting down. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar writeAll /uploadedFile3.txt myFile.txt"
java -jar client.jar writeAll /uploadedFile3.txt myFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar readAll /uploadedFile3.txt downloadedFile3.txt"
java -jar client.jar readAll /uploadedFile3.txt downloadedFile3.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "Data server 1 is shutting down. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar writeAll /uploadedFile4.txt myFile.txt"
java -jar client.jar writeAll /uploadedFile4.txt myFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar readAll /uploadedFile.txt downloadedFile4.txt"
java -jar client.jar readAll /uploadedFile.txt downloadedFile4.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "Data server 0 is starting up. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar writeAll /uploadedFile5.txt myFile.txt"
java -jar client.jar writeAll /uploadedFile5.txt myFile.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "java -jar client.jar readAll /uploadedFile5.txt downloadedFile5.txt"
java -jar client.jar readAll /uploadedFile5.txt downloadedFile5.txt
echo
read -p "Press [Enter] key to continue..."
echo
echo "Data server 1 is starting up. Please wait..."
echo
read -p "Press [Enter] key to continue..."
echo
echo "Cleaning"
echo rm myFile.txt downloadedFile*.txt 
rm myFile.txt downloadedFile*.txt 
echo "End presentation"

