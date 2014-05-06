/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.purdue.craveme.net;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class parses generic Atom feeds.
 *
 * <p>Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 *
 * <p>An example of an Atom feed can be found at:
 * http://en.wikipedia.org/w/index.php?title=Atom_(standard)&oldid=560239173#Example_of_an_Atom_1.0_feed
 */
public class RecipeParser {

	private static final String FIELD_RECIPES = "recipes";
	private static final String FIELD_ID = "id";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_STEPS = "steps";
	private static final String FIELD_INGREDIENTS = "ingredients";
	private static final String FIELD_TIME = "time";
	private static final String FIELD_PHOTO_URL = "photo_url";
	private static final String FIELD_USER_ID = "user_id";
	private static final String FIELD_DIRECTIONS = "steps";

	private static final String FIELD_RESPONSE = "response";
	private static final String VALUE_SUCCESS = "success";
	
    /** Parse a recipe json object, returning a collection of recipe objects.
     *
     * @param in Recipe json, as a stream.
     * @return List of {@link edu.purdue.craveme.net.RecipeParser.Recipe} objects.
     * @throws java.io.IOException on I/O error.
     * @throws JSONException on parse error
     */
    public List<Recipe> parse(InputStream in)
            throws IOException, JSONException {
    	Scanner scanner = null;
        try {
            scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            String jsonStr = scanner.next();
            JSONObject json = new JSONObject(jsonStr);
            if(json.getString(FIELD_RESPONSE).equals(VALUE_SUCCESS)) {
            	JSONArray recipesJson = new JSONArray(json.getString(FIELD_RECIPES));
            	ArrayList<Recipe> recipes = new ArrayList<Recipe>(recipesJson.length());
            	for(int i = 0; i < recipesJson.length(); ++i) {
            		JSONObject recipeJson = recipesJson.getJSONObject(i);
            		int id = recipeJson.getInt(FIELD_ID);
            		int userId = recipeJson.getInt(FIELD_USER_ID);
            		String title = recipeJson.getString(FIELD_TITLE);
            		String steps = recipeJson.getString(FIELD_STEPS);
            		JSONArray ingredientsJson = recipeJson.getJSONArray(FIELD_INGREDIENTS);
            		String[] ingredients = new String[ingredientsJson.length()];
            		for(int j = 0; j < ingredients.length; ++j) {
            			ingredients[j] = ingredientsJson.getString(j);
            		}
            		JSONArray directionsJson = recipeJson.getJSONArray(FIELD_DIRECTIONS);
            		String[] directions = new String[directionsJson.length()];
            		for(int j = 0; j < directions.length; ++j) {
            			directions[j] = directionsJson.getString(j);
            		}
            		int time = recipeJson.getInt(FIELD_TIME);
            		String photoUrl = recipeJson.getString(FIELD_PHOTO_URL);
            		Recipe recipe = new Recipe(id, userId, title, steps, ingredients, directions, time, photoUrl);
            		recipes.add(recipe);
            	}
            	return recipes;
            }
            else {
            	throw new JSONException("response failed");
            }
            
        } finally {
        	if(scanner != null) {
        		scanner.close();
        	}
            in.close();
        }
    }

    

    /**
     * This class represents a single recipe from the JSON recipe array
     *
     * <p>It includes all recipe data members
     */
    public static class Recipe {
        public final int id;
        public final int userId;
        public final String title;
        public final String steps;
        public final String[] ingredients;
        public final String[] directions;
        public final int time;
        public final String photoUrl;
        
        Recipe(int id, int userId, String title, String steps, String[] ingredients, String[] directions, int time, String photoUrl) {
        	this.id = id;
        	this.userId = userId;
        	this.title = title;
        	this.steps = steps;
        	this.ingredients = ingredients;
        	this.directions = directions;
        	this.time = time;
        	this.photoUrl = photoUrl;
        }
    }
}
