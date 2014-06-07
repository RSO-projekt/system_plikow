#!/bin/bash
rm -r bin
mkdir bin
cp src/properties.conf .

echo "Compiling java files"
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/*.java src/impl/client/*.java src/impl/server/master/*.java src/impl/server/data/*.java src/gen-java/rso/at/*.java 2> javas.log
cd bin

echo "Creating Client jar"
jar cfm client.jar ../src/impl/client/MANIFEST.MF impl/Configuration.class rso/at/*.class impl/client/*.class
mv client.jar ..

echo "Creating MasterServer jar"
jar cfm master-server.jar ../src/impl/server/master/MANIFEST.MF impl/Configuration.class rso/at/*.class impl/server/master/*.class 
mv master-server.jar ..

echo "Creating DataServer jar"
jar cfm data-server.jar ../src/impl/server/data/MANIFEST.MF impl/Configuration.class rso/at/*.class impl/server/data/*.class 
mv data-server.jar ..

cd ..
echo "Done"

