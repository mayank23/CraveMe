
import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/* java server */
import org.json.JSONObject;

public class Server{
	
	public static void main(String[] args)
	{
			ServerSocket server;
			try{
				
				 server = new ServerSocket(9000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				server = null;
			}
			
			while(server != null && true){
				
				try{
						 Socket clientSocket = server.accept();    
						 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
						 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						 String message = in.readLine();
						 MessageProtocol protocol = new MessageProtocol(message);
						 JSONObject response =   protocol.parseMessage();
						 if(response == null)
						 {
							 out.println("ERROR");
						 }
						 else{
							 out.println(response.toString());
						 }
				}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
		
	
		
	}



}
