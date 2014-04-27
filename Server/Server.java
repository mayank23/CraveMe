
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

public class Server extends Thread{
	static ServerSocket server;

	
	public static void main(String[] args)
	{
			
			try{
				
				 server = new ServerSocket(9012);
				 
			}
			catch(Exception e)
			{
				e.printStackTrace();
				server = null;
			}
			for(int x=0;x<5;x++)
			{
			Pool pool = new Pool(server,x+1);
			pool.start();
			}

			
	
		
	}


}

class Pool extends Thread{
		static ServerSocket server;
		int number=0;
		public Pool(ServerSocket server, int number){
			this.server = server;
			this.number = number;
		}
		public void run()
		{
			//thread pool
			while(true){
				
				try{
						 Socket clientSocket = server.accept();    
						 System.out.println("\nThread: "+number+"got the connection\n");
						 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
 
						 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						 String message = in.readLine();
						 System.out.println(message);
						 JSONObject json = new JSONObject(message);
						 System.out.println("message:"+message+"|"+json.toString());
 
						 InputStream inf = clientSocket.getInputStream();
						 if(json.getString("option").equals("photo_upload"))
						 {
							 
							 // naming file
							 String file_name = "photos/photos_"+(int)(Math.random()*2234211)+"_"+(int)(Math.random()*22342111)+"_"+(int)(Math.random()*22342)+"_"+json.getString("file_name");
							 System.out.println("file_name= "+file_name);
							 
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
						if(json.getString("option").equals("get_photo"))
						{
								
								 String server_file_path = json.getString("server_file_path");
								 // get the photo and write it to the socket.
								 synchronized(this){
								 FileInputStream fin = new FileInputStream(server_file_path);
								 OutputStream outstream = clientSocket.getOutputStream();
								 IOUtils.copy(fin, outstream);
								 fin.close();
								 outstream.close();
								 }
								 
								 
						}
						else
						 {
						System.out.println("input: "+message);
						 MessageProtocol protocol = new MessageProtocol(message);
						 JSONObject response =   protocol.parseMessage();
						 if(response == null)
						 {
							 response = new JSONObject();
							 response.put("response", "error");
							 out.println(response.toString());
							 System.out.println("response was null");
						 }
						 else{
							 out.println(response.toString());
							 // print to terminal
							 System.out.println(response.toString());
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
