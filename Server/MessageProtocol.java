

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.*;




public class MessageProtocol {

String message = null;

public MessageProtocol(String message)
{
	this.message = message;
	
	
}
public JSONObject parseMessage() throws Exception
{
	// parse the message.
	JSONObject json = new JSONObject(this.message);
	String option = json.get("option").toString();
	
	if(option.equals("login"))
	{
		JSONObject output = Work.login(json);
		return output;
		
	}else
	// what to do
	if(option.equals("get_all_recipes"))
	{
		JSONObject output = Work.recipes_all(json);
		return output;
	}
	else
		if(option.equals("register_user"))
		{
			JSONObject output = Work.registerUser(json);
			return output;
		}
		else
			if(option.equals("post_meal"))
			{
				JSONObject output  = Work.postMeal(json);
				return output;
			}
			else
				if(option.equals("get_all_meals"))
				{
					JSONObject output = Work.getAllMeals(json);
					return output;
				}
			else
				if(option.equals("get_single_meal"))
				{
					JSONObject output = Work.getSingleMeal(json);
					return output;
				}
				else
					if(option.equals("vote_meal"))
					{
						JSONObject output = Work.voteMeal(json);
						return output;
					}
					else
						if(option.equals("vote_recipe"))
						{
							JSONObject output = Work.voteRecipe(json);
							return output;
						}
						else
							if(option.equals("upload_recipe"))
							{
								JSONObject output = Work.uploadRecipe(json);
								return output;
							}
							else
								if(option.equals("get_comments_meal"))
								{
									JSONObject output = Work.getComments(json);
									return output;
								}
								else
									if(option.equals("add_comment_meal"))
									{
										JSONObject output = Work.addComment(json);
										return output;
									}
									else
										if(option.equals("set_craves"))
										{
											
											JSONObject output = Work.setCraves(json);
											return output;
										}
										else
											if(option.equals("set_nots"))
											{
												JSONObject output = Work.setNots(json);
												return output;
											}
		
			// else
			return null;
	
	
	
}

	

}
class Work{
	
	public static Connection conn = null;
	public static int ConnectToDB()
	{
		try {
			conn = DriverManager.getConnection("jdbc:mysql://data.cs.purdue.edu:50399/lab6", "xxxx", "xxxx");

			return 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("error for db");
			e.printStackTrace();
			return -1;
		}
	
	}
	public static void CloseConnection()
	{
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JSONObject recipes_all(JSONObject request)
	{
		int error = ConnectToDB();
		if(error == -1)
		{
			return null;
		}
		try{
		JSONObject json = new JSONObject();
		JSONArray recipes = new JSONArray();
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recipes");
		
		ResultSet result  =stmt.executeQuery();
		while(result.next())
		{
			JSONObject current = new JSONObject();
			current.put("id",result.getInt("id"));
			current.put("title", result.getString("title"));
			JSONArray steps = new JSONArray(result.getString("steps"));
			current.put("steps", steps);
			JSONArray ingredients = new JSONArray(result.getString("ingredients"));
			
			current.put("ingredients", ingredients);
			current.put("time", result.getInt("time"));
			current.put("photo_url", result.getString("photo_url"));
			current.put("user_id", result.getInt("user_id"));
			current.put("craves",result.getInt("craves"));
			current.put("nots", result.getInt("nots"));
			recipes.put(current);
			
		}
		json.put("recipes",recipes);
		json.put("response", "success");
		
		CloseConnection();
		return json;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			CloseConnection();
			return null;
		}
		
		
	}
	/*
	 * 
	 *	Register User Function
	 *	needs user_name, email, password in request object
	 *	returns a json object with success, else returns null
	 * 
	 */
	public static JSONObject registerUser(JSONObject request)
	{
		if(request.has("user_name") && request.has("email") && request.has("password"))
		{
			int error = ConnectToDB();
			if(error == -1)
			{
				// error in connection.
				System.out.println("error in connect to db");
				return null;
			}
			else{
				// ok proceed
				try{
				String user_name = request.getString("user_name");
				String email = request.getString("email");
				String password = request.getString("password");
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO user(user_name,email,password) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, user_name);
				stmt.setString(2, email);
				stmt.setString(3, password);
				stmt.executeUpdate();
				// get the generated id of the user
				// get the if of the user.
				JSONObject output = new JSONObject();
				output.put("response", "success");
				output.put("email", email);
				output.put("user_name", user_name);
				ResultSet keys;
				keys = stmt.getGeneratedKeys();
				if(keys.next())
				{
					output.put("user_id", keys.getInt(1));
				}else{
					return null;
				}
				
				return output;
				}
				catch(Exception e){
					// error
					e.printStackTrace();
				}
				CloseConnection();
				return null;
			}
		}
		else{
			// request does not have all the params
			return null;
		}
		
	}
	/*
	 *	Post Meal
	 *	needs user_name, title, description, category, recipe id.
	 *  // returns json object with id of meal, and user will call another function to actually upload the file.
	 * 
	 */
	public static JSONObject postMeal(JSONObject request)
	{
		if(request.has("user_name") && request.has("title") && request.has("category") && request.has("recipe_id"))
		{
			// ok
			int error = ConnectToDB();
			if(error == -1)
			{
				return null;
			}
			else{
			// ok
			try{
			System.out.println(request.toString());
			String user_name = request.getString("user_name");
			String title = request.getString("title");
			String category = request.getString("category");
			int recipe_id = request.getInt("recipe_id");
			String description = request.getString("description");
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO meals (user_name,title,category,recipe_id,description) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS); 
			stmt.setString(1, user_name);
			stmt.setString(2, title);
			stmt.setString(3, category);
			stmt.setInt(4, recipe_id);
			stmt.setString(5, description);
			stmt.executeUpdate();
			ResultSet keys = stmt.getGeneratedKeys();
			int meal_id = -1;
			if(keys.next())
			{
				meal_id = keys.getInt(1);
			}
			if(meal_id == -1)
			{	// error
				return null;
			}
			JSONObject output = new JSONObject();
			output.put("user_name", user_name);
			output.put("id", meal_id);
			output.put("response", "success");
			keys.close();
			CloseConnection();
			return output;
			
			}
			catch(Exception e)
			{
				CloseConnection();
				e.printStackTrace();
				return null;
			}
			}
		}
		// else return null
		return null;
		
	}
	
	public static JSONObject uploadFilePathToMeal(JSONObject request)
	{

		try{
		ConnectToDB();
		int meal_id = request.getInt("meal_id");
		String server_file_path = request.getString("server_file_path");
		PreparedStatement stmt = conn.prepareStatement("UPDATE meals SET photo_url=? WHERE id=?");
		System.out.println("id: "+meal_id +"and server_file_path: "+server_file_path);
		stmt.setString(1, server_file_path);
		stmt.setInt(2, meal_id);
		stmt.executeUpdate();
		JSONObject response = new JSONObject();
		response.put("response", "success");
		CloseConnection();
		return response;
		}
		catch(Exception e)
		{
			CloseConnection();
			e.printStackTrace();
			return null;		
		}
	
		
	}
	
	
	public static JSONObject getSingleMeal(JSONObject request)
	{
		int error = ConnectToDB();
		if(error == -1)
		{// error in connection to mysql
			return null;
		}
		// else proceed
		try{
			int meal_id = request.getInt("meal_id");
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM meals WHERE id=?");
			stmt.setInt(1,meal_id);
			ResultSet rs = stmt.executeQuery();
			JSONObject output = new JSONObject();
			if(rs.next())
			{
				output.put("id", rs.getInt("id"));
				output.put("photo_url", rs.getString("photo_url"));
				output.put("description", rs.getString("description"));
				output.put("recipe_id", rs.getInt("recipe_id"));
				output.put("category", rs.getString("category"));
				output.put("views", rs.getInt("views"));
				output.put("title", rs.getString("title"));
				output.put("craves"	, rs.getInt("craves"));
				output.put("nots", rs.getInt("nots"));
				output.put("user_name", rs.getString("user_name"));
			
			}
			
			CloseConnection();
			return output;
		}
		catch(Exception e)
		{
		e.printStackTrace();	
			CloseConnection();
			return null;

		}
		
	}
	
	 // voting on meals function
	public static JSONObject voteMeal(JSONObject request)
	{
		int error = ConnectToDB();
		if(error == -1)
		{
			return null;
		}
		else
		{
			// proceed
			try{
				String SQL = "UPDATE meals SET ";
				int meal_id = request.getInt("meal_id");
				if(request.get("vote_option").equals("crave"))
				{
					SQL += " craves = craves+1 ";
				}
				else
					if(request.getString("vote_option").equals("not"))
					{
						SQL +=" nots = nots+1 ";
					}
				SQL += " WHERE id=?";
				System.out.println(SQL);
				PreparedStatement stmt = conn.prepareStatement(SQL);
				stmt.setInt(1, meal_id);
				stmt.executeUpdate();
				JSONObject response = new JSONObject();
				response.put("response", "success");
				CloseConnection();
				return response;
			}catch(Exception e){
				e.printStackTrace();
				CloseConnection();
				return null;
			}
		}
		
		
		
	}
	// voting on recipes
	 // voting function
		public static JSONObject voteRecipe(JSONObject request)
		{
			int error = ConnectToDB();
			if(error == -1)
			{
				return null;
			}
			else
			{
				// proceed
				try{
					String SQL = "UPDATE recipes SET ";
					int recipe_id = request.getInt("recipe_id");
					if(request.get("vote_option").equals("crave"))
					{
						SQL += " craves = craves+1 ";
					}
					else
						if(request.getString("vote_option").equals("not"))
						{
							SQL +=" nots = nots+1 ";
						}
					SQL += " WHERE id=?";
					System.out.println(SQL);
					PreparedStatement stmt = conn.prepareStatement(SQL);
					stmt.setInt(1, recipe_id);
					stmt.executeUpdate();
					JSONObject response = new JSONObject();
					response.put("response", "success");
					CloseConnection();
					return response;
				}catch(Exception e){
					e.printStackTrace();
					CloseConnection();
					return null;
				}
			}
			
			
			
		}
		// upload a recipe
		public static JSONObject uploadRecipe(JSONObject request)
		{
			int error = ConnectToDB();
			if(error == -1)
			{
				return null;
			}
			try{
				
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO recipes (user_id, steps, photo_url, ingredients, title, time) VALUES (?,?,?,?,?,?) ");
				stmt.setInt(1, request.getInt("user_id"));
				stmt.setString(2, request.getJSONArray("steps").toString());
				stmt.setString(3, request.getString("photo_url"));
				stmt.setString(4, request.getJSONArray("ingredients").toString());
				stmt.setString(5, request.getString("title"));
				stmt.setInt(6, request.getInt("time"));
				stmt.executeUpdate();
				CloseConnection();
				JSONObject response = new JSONObject();
				response.put("response", "success");
				return response;
				
			}catch(Exception e)
			{
				e.printStackTrace();
				CloseConnection();
				JSONObject response = new JSONObject();
				try {
					response.put("response", e.toString());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return response;
			}

		}
// login user
		// needs the username and password
		public static JSONObject login(JSONObject request)
		{
			int error = ConnectToDB();
			JSONObject output = new JSONObject();
			if(error ==-1)
			{
				System.out.println("cannot connect to db");
				return null;
			}else{
				try{
					
					// actual login
					String username = request.getString("user_name");
					String password = request.getString("password");
					PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE user_name=? AND password=?");
					stmt.setString(1, username);
					stmt.setString(2, password);
					ResultSet result =stmt.executeQuery();
					
					if(result.next())
					{
						
						output.put("username", result.getString("user_name"));
						output.put("email", result.getString("email"));
						output.put("user_id", result.getInt("id"));
						output.put("response", "success");
						System.out.println(output.toString());
						CloseConnection();
						return output;
					}else{
						System.out.println("user with user_name: "+username+" not found");
						output.put("response", "user not found");
						CloseConnection();
						return output;
					}
					
					
				
					
					
					
					
					
				}catch(Exception e)
				{
					CloseConnection();
					e.printStackTrace();
					try {
						output.put("response", e.toString());
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return output;
				}
			}
			

		}

public static JSONObject getAllMeals(JSONObject request)
{
	
	ConnectToDB();
	JSONObject output = new JSONObject();
	try{
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM meals");
		ResultSet result = stmt.executeQuery();
		JSONArray array = new JSONArray();
		while(result.next())
		{
			JSONObject current = new JSONObject();
			current.put("id", result.getInt("id"));
			current.put("photo_url", result.getString("photo_url"));
			current.put("craves", result.getInt("craves"));
			current.put("user_name", result.getString("user_name"));
			current.put("category", result.getString("category"));
			current.put("title", result.getString("title"));
			current.put("description", result.getString("description"));
			current.put("nots", result.getInt("nots"));
			array.put(current);
		}
		output.put("meals", array);
		System.out.println("sending to client:\n\n:"+ output.toString());
		return output;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		try {
			output.put("response", e.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return output;
		
	}
	
	
}

// add comment
public static JSONObject addComment(JSONObject request)
{
	
	int error = ConnectToDB();
	JSONObject response = new JSONObject();
	
	if(error == -1)
	{
		return null;
	}
	else{
		
		try{
	
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO comments_meals (meal_id,text,user_name) VALUES (?,?,?)");
			stmt.setInt(1, request.getInt("meal_id"));
			stmt.setString(2,request.getString("text") );
			stmt.setString(3, request.getString("user_name"));
			stmt.executeUpdate();
			
			response.put("response", "success");
			
			System.out.println("added text:"+request.getString("text"));
			return response;
		}
		catch(Exception e)
		{
			try {
				response.put("response", e.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
			return response;
		}
		
		
		
	}
	
}
public static JSONObject getComments(JSONObject request)
{
	
	ConnectToDB();
	JSONObject response = new JSONObject();
	try{
		
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM comments_meals WHERE meal_id = ?");
		stmt.setInt(1, request.getInt("meal_id"));
		ResultSet result = stmt.executeQuery();
		JSONArray array = new JSONArray();
		while(result.next())
		{
			JSONObject current = new JSONObject();
			current.put("text", result.getString("text"));
			current.put("user_name", result.getString("user_name"));
			array.put(current);
		}
		response.put("comments", array);
		
		return response;
		
	}
	catch(Exception e)
	{
		try {
			response.put("response", e.toString());
			return response;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			
			e1.printStackTrace();
			return null;
		}
		
	}

}

public static JSONObject setCraves(JSONObject request)
{
	ConnectToDB();
	JSONObject response = new JSONObject();
	try{
		
		PreparedStatement stmt = conn.prepareStatement("UPDATE meals SET craves = ? WHERE id = ?");
		stmt.setInt(1, request.getInt("craves"));
		stmt.setInt(2, request.getInt("meal_id"));
		stmt.executeUpdate();
		
		response.put("response", "success");
		return response;
	}
	catch(Exception e)
	{
		try {
			response.put("response", e.toString());
			return response;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
	

}
public static JSONObject setNots(JSONObject request)
{
	ConnectToDB();
	JSONObject response = new JSONObject();
	try{
		
		PreparedStatement stmt = conn.prepareStatement("UPDATE meals SET nots = ? WHERE id = ?");
		stmt.setInt(1, request.getInt("nots"));
		stmt.setInt(2, request.getInt("meal_id"));
		stmt.executeUpdate();
		response.put("repsonse","success" );
		return response;
	}
	catch(Exception e)
	{
		try {
			response.put("response", e.toString());
			return response;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
	

}


}
