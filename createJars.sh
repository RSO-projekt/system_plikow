#!/bin/bash
rm -r bin
mkdir bin
cp src/properties.conf .
echo "Creating Client jar"
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/client/*.java src/gen-java/rso/at/*.java 2> javac.log
jar cfm client.jar src/impl/client/MANIFEST.MF bin/rso/*

echo "Creating MasterServer jar"
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/server/master/*.java src/gen-java/rso/at/*.java 2> javas.log
jar cfm master-server.jar src/impl/server/master/MANIFEST.MF bin/rso/*

echo "Creating DataServer jar"
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/server/data/*.java src/gen-java/rso/at/*.java 2> javas.log
jar cfm data-server.jar src/impl/server/data/MANIFEST.MF bin/rso/*

echo "Done"

