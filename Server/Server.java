
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



import org.apache.commons.io.IOUtils;
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
							 
							 String file_name = "photos/photos_"+Math.random()*22342+"_"+Math.random()*45643+"_"+Math.random()*10000+"_"+json.getString("file_name");
							
							 long length = json.getLong("length");
							 
							 // photo upload, mode.
							 // start reading from the server
							 byte[] buffer = new byte[1024];
							 
							 File file = new File(file_name);
							 OutputStream outs = new FileOutputStream(file);
							 IOUtils.copy(inf, outs);
							 inf.close();
							 outs.close();
							 out.println("got the file: "+file_name );
							 out.close();
							 System.out.println("stored on server");
							
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
						 out.flush();
						 out.close();
						 }
				}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
		
	
		
	}



}
