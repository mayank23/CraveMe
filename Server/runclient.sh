#!/bin/bash
echo "Building..."
javac ExampleClient.java
echo "Running..."
java ExampleClient $1 $2
