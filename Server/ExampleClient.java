
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

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


BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
OutputStream out = socket.getOutputStream();




// now actually write the file
int count=0;
byte[] buffer = new byte[1];
File file = new File("test.txt");
FileInputStream in = new FileInputStream("test.txt");

long length = file.length();

String x = "{\"option\":\"photo_upload\",\"file_name\":\"test.txt\",\"length\":"+length+",\"email\":\"email@gmail.com\"}\n";
pw.println(x);


long total =0;
System.out.println("about to write");
while ((count = in.read(buffer)) >= 0) {
	
	System.out.print("writing: "+total);
     	out.write(buffer,0,count);
     	total += count;
       out.flush();
        System.out.println("flushed");
        if(total == length)
        {
        	break;
        }
	}
out.close();





//String y ="{\"option\":\"register_user\",\"user_name\":\"mayank23\",\"password\":\"dsf\",\"email\":\"email@gmail.com\"}\n";
//pw.println(y);
//pw.println("{\"option\":\"register_user\",\"user_name\":\"mayank23\",\"password\":\"dsf\",\"email\":\"email@gmail.com\"}");
//pw.println("{\"option\":\"post_meal\",\"user_name\":\"mayank23\",\"title\":\"best meal in the world\",\"description\":\"fifa meal night\",\"category\":\"american\",\"recipe_id\":-1}");

}
catch(Exception e)
{
System.out.println("Server is offline!\n\n");
e.printStackTrace();

}


}
}
