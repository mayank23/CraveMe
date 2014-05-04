
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class ExampleClient{

public static void main(String[] args){	

if(args.length<3)
{
	
System.out.println("Usage: java Client portnum hostname option(register_user,upload_photo,get_all_recipes,post_meal,get_meal,vote_meal,vote_recipe)");
System.exit(1);
}
int port = Integer.parseInt(args[0]);

try{

Socket socket = new Socket(args[1],port);
PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
if(args[2].equals("upload_photo")){

OutputStream out = socket.getOutputStream();

// now actually write the file
File file = new File("test.jpg");
FileInputStream in = new FileInputStream("test.jpg");
// need to send option, file name, and meal_id to upload to server
String x = "{\"option\":\"photo_upload\",\"file_name\":\"test.jpg\",\"meal_id\":3}";
pw.println(x);

IOUtils.copy(in, out);
in.close();
socket.shutdownOutput();
System.out.println(reader.readLine());
socket.close();
}
else
	if(args[2].equals("get_photo"))
	{
		// downloads photo//
		pw.println("{\"option\":\"get_photo\",\"server_file_path\":\"photos/photos_2101375_10314433_5858_test.jpg\"}");
		File file = new File("got_photo.jpg");
		FileOutputStream outs = new FileOutputStream(file);
		InputStream input = socket.getInputStream();
		IOUtils.copy(input, outs);
		outs.close();
		socket.shutdownInput();
		
	}
else
	if(args[2].equals("register_user"))
	{

		String y ="{\"option\":\"register_user\",\"user_name\":\"mayank2333\",\"password\":\"dsf\",\"email\":\"email@gmail.com\"}";
		pw.println(y);
		System.out.println(reader.readLine());
		
	}
	else
		if(args[2].equals("get_all_recipes"))
		{
			pw.println("{\"option\":\"get_all_recipes\"}");
			System.out.println(reader.readLine());
		}
		else
			if(args[2].equals("post_meal"))
			{
				pw.println("{\"option\":\"post_meal\",\"user_name\":\"mayank23\",\"title\":\"best meal in the world\",\"description\":\"fifa meal night\",\"category\":\"american\",\"recipe_id\":-1}");
				System.out.println(reader.readLine());

			}
			else
		
				if(args[2].equals("login"))
				{
					pw.println("{\"option\":\"login\",\"user_name\":\"mayank23\",\"password\":\"dsf\"}");
					System.out.println("from server:"+reader.readLine());
				}else
				if(args[2].equals("get_meal"))
				{
					pw.println("{\"option\":\"get_single_meal\",\"meal_id\":3}");
					System.out.println("from server:"+reader.readLine());
					
				}
				else
					if(args[2].equals("vote_meal"))
					{
						// vote on a mean. 
						pw.println("{\"option\":\"vote_meal\",\"meal_id\":1,\"vote_option\":\"not\"}");
						System.out.println(reader.readLine());
					}
					else
						if(args[2].equals("vote_recipe"))
						{
							// vote on recipe
							pw.println("{\"option\":\"vote_recipe\",\"recipe_id\":1,\"vote_option\":\"crave\"}");
							System.out.println(reader.readLine());
						}
						else
							if(args[2].equals("upload_recipe"))
							{
								pw.println("{\"option\":\"upload_recipe\", \"ingredients\" : [\"food1\"],\"steps\":[\"<>html>step1\"],\"user_id\":-1,\"title\":\"title\" ,\"time\":15,\"photo_url\":\"http://url.com\"}");
								System.out.println(reader.readLine());
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
