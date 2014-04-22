
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
							 
							 String file_name = "photos/photos_"+(int)(Math.random()*2234211)+"_"+(int)(Math.random()*22342111)+"_"+(int)(Math.random()*22342)+"_"+json.getString("file_name");
							 System.out.println("file_name= "+file_name);
							 long length = json.getLong("length");
							 // photo upload, mode.
							 // start reading from the server
							 File file = new File(file_name);
							 OutputStream outs = new FileOutputStream(file);
							 IOUtils.copy(inf, outs);
							 // only shut down inputstream from socket
							 clientSocket.shutdownInput();
							 // close the file output stream for writing to file from socket.
							 outs.close();
							 // now that we have stored the file on the server, update the database.
							 
							 json.put("server_file_path", file_name);
							 JSONObject output = Work.uploadFilePathToMeal(json);
							 if(output == null)
							 {
								 output = new JSONObject();
								 output.put("response", "error");
								 out.println(output.toString());
								 
							 }else{
							
							output.put("server_file_path", file_name); 
							 // return response to socket print writer.
							 out.println(output.toString());
							 System.out.println("stored on server");
							 // close print writer and socket
							 }
							 out.close();
							 clientSocket.close();
							
						 }
						 else
						 {
						 MessageProtocol protocol = new MessageProtocol(message);
						 JSONObject response =   protocol.parseMessage();
						 if(response == null)
						 {
							 response = new JSONObject();
							 response.put("response", "error");
							 out.println(response.toString());
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
