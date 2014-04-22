#!/bin/bash
echo "Building..."
javac -cp ".:commons-io-2.4.jar" ExampleClient.java
echo "Running..."
java -cp ".:commons-io-2.4.jar" ExampleClient $1 $2 $3
