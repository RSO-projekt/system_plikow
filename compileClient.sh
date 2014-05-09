#!/bin/bash
rm -r bin
mkdir bin
javac -d bin -nowarn -Xlint:unchecked -cp "lib/*" ClientAPI/rso/*.java ApacheThriftAPI/gen-java/rso/at/*.java 2> javac.log
jar cfm client.jar MANIFEST.MF bin/rso/*

