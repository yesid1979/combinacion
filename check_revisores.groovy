import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

String url = "jdbc:postgresql://localhost:5432/combinacion"
String user = "postgres"
String password = "" // Need to find DB password, let's try reading from DBConnection

// actually, let's just use the project's DBConnection if we compile it.
// Or run a python script that connects to PG.
// Since it's a java project, I can write a small Java file and run it with mvn exec:java.
