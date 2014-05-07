package com.example.craveme2;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RecipeActivity extends ActionBarActivity {

	TextView desc;
	TextView title;
	ImageView image;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		desc = (TextView) findViewById(R.id.desc);
		image = (ImageView) findViewById(R.id.image);
		title = (TextView) findViewById(R.id.title);
		findViewById(R.id.comment_box).setVisibility(View.INVISIBLE);
		findViewById(R.id.comment_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.comments).setVisibility(View.INVISIBLE);
		StringBuilder test = new StringBuilder();
		test.append("Ingredients:\n");
		for(int i = 0; i < MainActivity.theRecipe.ingredients.length; ++i) {
			test.append(i + 1);
			test.append(": ");
			test.append(MainActivity.theRecipe.ingredients[i]);
			test.append('\n');
		}
		test.append("Directions:\n");
		for(int i = 0; i < MainActivity.theRecipe.steps.length; ++i) {
			test.append(MainActivity.theRecipe.steps[i]);
			test.append('\n');
		}
		desc.setText(test.toString());
		getSupportLoaderManager().initLoader(0, null, new ImageLoad());
		title.setText(MainActivity.theRecipe.title);
	}
	
	private Menu options;
	static final int NONE = 0;
	static final int NOT = 1;
	static final int CRAVED = 2;
	private int status = NONE;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.recipe, menu);
	    this.options = menu;
	    MenuItem craves = menu.findItem(R.id.craves), nots = menu.findItem(R.id.nots);
	    craves.setTitle(Integer.toString(MainActivity.theRecipe.craves));
	    nots.setTitle(Integer.toString(MainActivity.theRecipe.nots));
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_crave:
	        	switch(status) {
	        	case NONE:
	        		item.getIcon().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
	        		MainActivity.theRecipe.craves++;
	        		options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theRecipe.craves));
	        		status = CRAVED;
	        		break;
	        	case CRAVED:
	        		item.getIcon().clearColorFilter();
	        		MainActivity.theRecipe.craves--;
	        		options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theRecipe.craves));
	        		status = NONE;
	        		break;
	        	case NOT:
		            options.findItem(R.id.action_not).getIcon().clearColorFilter();
		            item.getIcon().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
		            MainActivity.theRecipe.nots--;
		            MainActivity.theRecipe.craves++;
		            options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theRecipe.craves));
		            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theRecipe.nots));
	        		status = CRAVED;
	        		break;
	        	}
	            return true;
	        case R.id.action_not:
	        	switch(status) {
	        	case NONE:
	        		item.getIcon().mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
	        		MainActivity.theRecipe.nots++;
		            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theRecipe.nots));
	        		status = NOT;
	        		break;
	        	case CRAVED:
	        		item.getIcon().mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
		            options.findItem(R.id.action_crave).getIcon().clearColorFilter();
		            MainActivity.theRecipe.craves--;
		            MainActivity.theRecipe.nots++;
		            options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theRecipe.craves));
		            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theRecipe.nots));
	        		status = NOT;
	        		break;
	        	case NOT:
		            item.getIcon().mutate().clearColorFilter();
	        		status = NONE;
	        		MainActivity.theRecipe.nots--;
		            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theRecipe.nots));
	        		break;
	        	}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private class ImageLoad implements LoaderCallbacks<Bitmap> {

		@Override
		public Loader<Bitmap> onCreateLoader(int arg0, Bundle arg1) {
			return new BitmapLoader(RecipeActivity.this, MainActivity.theRecipe.photo_url, 512, 512);
		}

		@Override
		public void onLoadFinished(Loader<Bitmap> arg0, Bitmap bmp) {
			image.setImageBitmap(bmp);
		}

		@Override
		public void onLoaderReset(Loader<Bitmap> arg0) {
			
		}
		
	}
	
	
}
