

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
	return null;
	
	
}

	

}
class Work{
	public static Connection conn = null;
	public static Connection ConnectToDB()
	{
		try {
			conn = DriverManager.getConnection("jdbc:mysql://data.cs.purdue.edu:50399/lab6", "my_user", "abc");
			
			return conn;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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
		ConnectToDB();
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
	
}
