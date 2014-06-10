#!/bin/bash
read -p "Press [Enter] key to start upload bigFile..."
echo
echo "java -jar client.jar writeAll /bigFile myBigFile"
java -jar client.jar writeAll /bigFile myBigFile
echo
read -p "Press [Enter] key to start download bigFile..."
echo
echo "java -jar client.jar readAll /bigFile downloadedBigFile"
java -jar client.jar readAll /bigFile downloadedBigFile
echo
