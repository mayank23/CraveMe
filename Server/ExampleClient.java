import java.io.*;
import java.net.*;

public class ExampleClient{

public static void main(String[] args){	

if(args.length<2)
{
System.out.println("Usage: java Client portnum hostname");
System.exit(1);
}
int port = Integer.parseInt(args[0]);

try{
Socket socket = new Socket(args[1],port);
PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
pw.println("{\"option\":\"get_all_recipes\"}");
System.out.println(in.readLine());
}
catch(Exception e)
{
System.out.println("Server is offline!\n\n");
e.printStackTrace();

}


}
}
