#!/bin/bash
rm -r bin
mkdir bin
cp src/properties.conf .
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/client/*.java src/gen-java/rso/at/*.java 2> javac.log
jar cfm client.jar src/impl/client/MANIFEST.MF bin/rso/*

javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" src/impl/server/master/*.java src/gen-java/rso/at/*.java 2> javas.log
jar cfm master-server.jar src/impl/server/master/MANIFEST.MF bin/rso/*

