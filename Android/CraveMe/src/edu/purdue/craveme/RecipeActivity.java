package edu.purdue.craveme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

public class RecipeActivity extends ActionBarActivity {
	
	public static final String ARG_RECIPE_ID = "recipe_id";
	public static final String ARG_RECIPE_NAME = "recipe_name";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_recipe);
		Intent i = getIntent();
		if(i.getAction().equals(Intent.ACTION_VIEW)) {
			int recipeId = i.getIntExtra(ARG_RECIPE_ID, -1);
			String recipeName = i.getStringExtra(ARG_RECIPE_NAME);
			if(recipeId == -1 || recipeName == null) {
				throw new RuntimeException("ACTION_VIEW missing recipe id or recipe name!");
			}
			// update the main content by replacing fragments
	        FragmentManager fragmentManager = getSupportFragmentManager();
	        fragmentManager.beginTransaction()
	                .replace(R.id.container, RecipeFragment.newInstance(recipeId, recipeName))
	                .commit();
	        getSupportActionBar().setTitle(recipeName);
		}
		else {
			throw new RuntimeException("Unknown intent type to RecipeActivity: " + i.getAction());
		}
		
	}
	
	public static Intent getViewIntent(Context context, int recipeId, String recipeName) {
		Intent intent = new Intent(context, RecipeActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra(ARG_RECIPE_NAME, recipeName);
		intent.putExtra(ARG_RECIPE_ID, recipeId);
		return intent;
	}
}
