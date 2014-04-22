

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
	
	JSONObject json = new JSONObject(this.message);
	String option = json.get("option").toString();
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
			// else
			return null;
	
	
	
}

	

}
class Work{
	
	public static Connection conn = null;
	public static int ConnectToDB()
	{
		try {
			conn = DriverManager.getConnection("jdbc:mysql://data.cs.purdue.edu:50399/lab6", "my_user", "abc");
			
			return 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
			current.put("steps", result.getString("steps"));
			current.put("ingredients", result.getString("ingredients"));
			current.put("time", result.getInt("time"));
			current.put("photo_url", result.getString("photo_url"));
			current.put("user_id", result.getInt("user_id"));
			recipes.put(current);
			
		}
		json.put("recipes",recipes);
		
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
				return null;
			}
			else{
				// ok proceed
				try{
				String user_name = request.getString("user_name");
				String email = request.getString("email");
				String password = request.getString("password");
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO user(user_name,email,password) VALUES (?,?,?)");
				stmt.setString(1, user_name);
				stmt.setString(2, email);
				stmt.setString(3, password);
				stmt.executeUpdate();
				JSONObject output = new JSONObject();
				output.put("result", "success");
				output.put("email", email);
				output.put("user_name", user_name);
				return output;
				}
				catch(Exception e){
					// error
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
}
