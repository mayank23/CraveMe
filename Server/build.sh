echo "Building..."
javac -cp ".:mysql-connector-java-5.1.30-bin.jar:java-json.jar"  Server.java MessageProtocol.java

echo "Started Server..."
java -cp ".:mysql-connector-java-5.1.30-bin.jar:java-json.jar"  Server


