echo "Building..."
javac -cp .:mysql-connector-java-5.1.30-bin.jar Server.java 
java -cp .:mysql-connector-java-5.1.30-bin.jar Server 
