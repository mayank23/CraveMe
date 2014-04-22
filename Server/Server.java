
import java.net.*;
import java.nio.CharBuffer;
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
				
				 server = new ServerSocket(9012);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				server = null;
			}
			 System.out.println("going to while");
			while(server != null && true){
				
				try{
						 Socket clientSocket = server.accept();    
						 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						 
						 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						 String message = in.readLine();
						 JSONObject json = new JSONObject(message);
						 System.out.println("message:"+message+"|"+json.toString());
						 InputStream inf = clientSocket.getInputStream();
						 
						 if(json.getString("option").equals("photo_upload"))
						 {
							 System.out.println("about to read file");
							 String file_name = "photos/photos_"+json.getString("file_name");
							 PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
							 long length = json.getLong("length");
							 
							 // photo upload, mode.
							 // start reading from the server
							 byte[] buffer = new byte[1024];
							 
							 File file = new File(file_name);
							 OutputStream outs = new FileOutputStream(file);
							 int count=0;
							
							 long total =0;
							 while((count = inf.read(buffer)) >= 0 && total !=length )
							 {
								 System.out.println("total : "+total);
								 
								outs.write(buffer	, 0, count);
								
								
								total += count;
							 }
							 outs.flush();
							 inf.close();
							 outs.close();
							 System.out.println("stored on server");
							 out.println("got the file: "+file_name );
						 }else{
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
				}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
		
	
		
	}



}
