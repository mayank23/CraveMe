package com.example.craveme2;


import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.google.gson.Gson;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MealActivity extends ActionBarActivity implements LoaderCallbacks<String> {

	TextView desc;
	TextView title;
	ImageView image;
	EditText commentBox;
	Button comment;
	TextView comments;
	
	private static final String HOSTNAME = "data.cs.purdue.edu";
    private static final int PORT = 9012;
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    private static final Gson gson = new Gson();
	
    public static class AddCommentReq {
    	String option = "add_comment_meal";
    	int meal_id;
    	String text;
    	String user_name = "mayank23";
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		desc = (TextView) findViewById(R.id.desc);
		image = (ImageView) findViewById(R.id.image);
		title = (TextView) findViewById(R.id.title);
		commentBox = (EditText) findViewById(R.id.comment_box);
		comment = (Button) findViewById(R.id.comment_button);
		comments = (TextView) findViewById(R.id.comments);
		comment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(commentBox.getText().length() > 0) {
					if(comments.getText().toString().equals("No Comments")) {
						comments.setText("");
					}
					final String text = commentBox.getText().toString();
					comments.append("mayank23: " + text);
					comments.append("\n");
					commentBox.setText("");
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							//update comment
							try {
								Socket s = new Socket();
						    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
						    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
						    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
						    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
						    	AddCommentReq req = new AddCommentReq();
						    	req.meal_id = MainActivity.theMeal.id;
						    	req.text = text;
						    	wr.write(gson.toJson(req ));
						    	wr.append('\n');
						    	wr.flush();
						    	s.close();
							}
							catch(Exception e) {
								e.printStackTrace();
							}
							return null;
						}
						
					}.execute();

				}
			}
			
		});
		desc.setText(MainActivity.theMeal.description);
		getSupportLoaderManager().initLoader(0, null, new ImageLoad());
		getSupportLoaderManager().initLoader(1, null, this);
		title.setText(MainActivity.theMeal.title + " - " + MainActivity.theMeal.category);
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
	    craves.setTitle(Integer.toString(MainActivity.theMeal.craves));
	    nots.setTitle(Integer.toString(MainActivity.theMeal.nots));
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
        		MainActivity.theMeal.craves++;
        		options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theMeal.craves));
        		status = CRAVED;
        		break;
        	case CRAVED:
        		item.getIcon().clearColorFilter();
        		MainActivity.theMeal.craves--;
        		options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theMeal.craves));
        		status = NONE;
        		break;
        	case NOT:
	            options.findItem(R.id.action_not).getIcon().clearColorFilter();
	            item.getIcon().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
	            MainActivity.theMeal.nots--;
	            MainActivity.theMeal.craves++;
	            options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theMeal.craves));
	            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theMeal.nots));
        		status = CRAVED;
        		break;
        	}
        	break;
        case R.id.action_not:
        	switch(status) {
        	case NONE:
        		item.getIcon().mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        		MainActivity.theMeal.nots++;
	            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theMeal.nots));
        		status = NOT;
        		break;
        	case CRAVED:
        		item.getIcon().mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
	            options.findItem(R.id.action_crave).getIcon().clearColorFilter();
	            MainActivity.theMeal.craves--;
	            MainActivity.theMeal.nots++;
	            options.findItem(R.id.craves).setTitle(Integer.toString(MainActivity.theMeal.craves));
	            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theMeal.nots));
        		status = NOT;
        		break;
        	case NOT:
	            item.getIcon().mutate().clearColorFilter();
        		status = NONE;
        		MainActivity.theMeal.nots--;
	            options.findItem(R.id.nots).setTitle(Integer.toString(MainActivity.theMeal.nots));
        		break;
        	}
        	break;
	    default:
	            return super.onOptionsItemSelected(item);
	    }
	    new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				//update crave / not count
				try {
					Socket s = new Socket();
			    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
			    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
			    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
			    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
			    	SetCraves setCraves = new SetCraves();
			    	setCraves.meal_id = MainActivity.theMeal.id;
			    	setCraves.craves = MainActivity.theMeal.craves;
			    	wr.write(gson.toJson(setCraves));
			    	wr.append('\n');
			    	wr.flush();
			    	s.close();
			    	s = new Socket();
			    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
			    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
			    	wr = new OutputStreamWriter(s.getOutputStream());
			    	SetNots setNots = new SetNots();
			    	setNots.meal_id = MainActivity.theMeal.id;
			    	setNots.nots = MainActivity.theMeal.nots;
			    	wr.write(gson.toJson(setNots));
			    	wr.append('\n');
			    	wr.flush();
			    	s.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return null;
			}
    		
    	}.execute();
    	return true;
	}
	
	public static class SetCraves {
		String option = "set_craves";
		int meal_id;
		int craves;
	}
	
	public static class SetNots {
		String option = "set_nots";
		int meal_id;
		int nots;
	}
	
	private class ImageLoad implements LoaderCallbacks<Bitmap> {

		@Override
		public Loader<Bitmap> onCreateLoader(int arg0, Bundle arg1) {
			return new BitmapLoader(MealActivity.this, MainActivity.theMeal.photo_url, 512, 512);
		}

		@Override
		public void onLoadFinished(Loader<Bitmap> arg0, Bitmap bmp) {
			image.setImageBitmap(bmp);
		}

		@Override
		public void onLoaderReset(Loader<Bitmap> arg0) {
			
		}
		
	}
	
	public static class Comment {
		String user_name;
		String text;
	}
	public static class GetCommentsResp {
		Comment[] comments;
	}
	public static class GetCommentsReq {
		String option = "get_comments_meal";
		int meal_id;
	}

	@Override
	public Loader<String> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<String>(this) {

			@Override
			public String loadInBackground() {
				try {
				Socket s = new Socket();
		    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
		    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
		    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
		    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
		    	GetCommentsReq req = new GetCommentsReq();
		    	req.meal_id = MainActivity.theMeal.id;
		    	wr.write(gson.toJson(req ));
		    	wr.append('\n');
		    	wr.flush();
		    	GetCommentsResp resp = gson.fromJson(new InputStreamReader(s.getInputStream()), GetCommentsResp.class);
		    	s.close();
		    	StringBuilder build = new StringBuilder();
		    	for(Comment comment : resp.comments) {
		    		build.append(comment.user_name + ": " + comment.text);
		    		build.append('\n');
		    	}
				return build.toString();
				}
				catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			@Override
			public void onStartLoading() {
				forceLoad();
			}
			
		};
	}

	@Override
	public void onLoadFinished(Loader<String> arg0, String arg1) {
		if(arg1 == null) {
			comments.setText("No Comments");
		}
		else {
			comments.setText(arg1);
		}
	}

	@Override
	public void onLoaderReset(Loader<String> arg0) {
	}
}
