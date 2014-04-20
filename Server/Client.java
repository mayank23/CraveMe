import java.io.*;
import java.net.*;

public class Client{

public static void main(String[] args){	

int port = Integer.parseInt(args[0]);
try{
Socket socket = new Socket("sslab03.cs.purdue.edu",port);
PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
pw.println("{\"option\":\"get_all_recipes\"}");
System.out.println(in.readLine());
}
catch(Exception e)
{
e.printStackTrace();
}


}
}
