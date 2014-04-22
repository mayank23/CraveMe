echo "Building..."
javac -cp ".:mysql-connector-java-5.1.30-bin.jar:java-json.jar:commons-io-2.4.jar"  Server.java MessageProtocol.java

echo "Started Server..."
java -cp ".:mysql-connector-java-5.1.30-bin.jar:java-json.jar:commons-io-2.4.jar"  Server


