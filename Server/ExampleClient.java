
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

public class ExampleClient{

public static void main(String[] args){	

if(args.length<3)
{
	
System.out.println("Usage: java Client portnum hostname option(register_user,upload_photo,get_all_recipes,post_meal)");
System.exit(1);
}
int port = Integer.parseInt(args[0]);

try{

Socket socket = new Socket(args[1],port);
PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);

if(args[3].equals("upload_photo")){
BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
OutputStream out = socket.getOutputStream();
// now actually write the file
int count=0;
byte[] buffer = new byte[1024];
File file = new File("test2.jpg");
FileInputStream in = new FileInputStream("test2.jpg");

long length = file.length();

String x = "{\"option\":\"photo_upload\",\"file_name\":\"test2.jpg\",\"length\":"+length+",\"email\":\"email@gmail.com\"}";
pw.println(x);


long total =0;
System.out.println("about to write");
while ((count = in.read(buffer)) >= 0) {
	
	System.out.print("writing: "+total);
     	out.write(buffer,0,count);
     	total += count;
      
        System.out.println("flushed");
        if(total == length)
        {
        	break;
        }
	}
out.flush();
out.close();
in.close();
}
else
	if(args[3].equals("register_user"))
	{

		String y ="{\"option\":\"register_user\",\"user_name\":\"mayank23\",\"password\":\"dsf\",\"email\":\"email@gmail.com\"}";
		pw.println(y);
	}
	else
		if(args[3].equals("get_all_recipes"))
		{
			pw.println("{\"option\":\"get_all_recipes\"}");

		}
		else
			if(args[3].equals("post_meal"))
			{
				pw.println("{\"option\":\"post_meal\",\"user_name\":\"mayank23\",\"title\":\"best meal in the world\",\"description\":\"fifa meal night\",\"category\":\"american\",\"recipe_id\":-1}");

			}
			else
			{
				System.out.println("unrecognized option!");
			}

}
catch(Exception e)
{
System.out.println("Server is offline!\n\n");
e.printStackTrace();

}


}
}
