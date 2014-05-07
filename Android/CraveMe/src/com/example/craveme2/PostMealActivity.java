package com.example.craveme2;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.json.JSONObject;

import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class PostMealActivity extends ActionBarActivity {

	ImageView image;
	EditText title;
	EditText desc;
	Spinner categories;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_meal);
		image = (ImageView) findViewById(R.id.image);
		title = (EditText) findViewById(R.id.title);
		desc = (EditText) findViewById(R.id.desc);
		categories = (Spinner) findViewById(R.id.categories);
		categories.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				new String[]{"American", "Mexican", "Asian", "Italian", "Indian"}));
		getSupportLoaderManager().initLoader(0, null, new ImageLoaderCallbacks());
	}
	
	private class ImageLoaderCallbacks implements LoaderCallbacks<Bitmap> {
		@Override
		public Loader<Bitmap> onCreateLoader(int arg0, Bundle arg1) {
			return new BitmapLoader(PostMealActivity.this, MainActivity.thePicture, 512, 512);
		}

		@Override
		public void onLoadFinished(Loader<Bitmap> arg0, Bitmap bitmap) {
			image.setImageBitmap(bitmap);
			
		}

		@Override
		public void onLoaderReset(Loader<Bitmap> arg0) {
			
		}
	}
	
	private static class PostMealRequest {
		String option = "post_meal";
		String user_name = "mayank23";
		int recipe_id = -1;
		String title;
		String description;
		String category;
	}
	
	private static class PostMealResponse {
		String user_name;
		int id;
		String response;
	}
	
	private static class PostImageRequest {
		String option = "photo_upload";
		String file_name = "file.jpg";
		int meal_id;
	}
	
	private static class PostImageResponse {
		String response;
	}

	private Menu options;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.postmeal, menu);
	    this.options = menu;
	    return true;
	}
	
	private static final String HOSTNAME = "data.cs.purdue.edu";
    private static final int PORT = 9012;
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    private static final Gson gson = new Gson();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    
	    case R.id.action_post:
	    	
	    	if(title.getText().toString().length() == 0 || desc.getText().toString().length() == 0) {
	    		Toast.makeText(this, "Fill in description and title", Toast.LENGTH_SHORT).show();
		    	break;
		    }
	    	final ProgressDialog diag = new ProgressDialog(this);
	    		diag.setTitle("Posting meal...");
	    		diag.setMessage("Working...");
	    		diag.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		diag.show();
	    	new AsyncTask<Void, Void, PostImageResponse>() {

				@Override
				protected PostImageResponse doInBackground(Void... params) {
					PostMealRequest req = new PostMealRequest();
					req.title = title.getText().toString();
					req.category = categories.getSelectedItem().toString();
					req.description = desc.getText().toString();
					if(req.title.length() == 0 || req.description.length() == 0) {
						return null;
					}
					PostMealResponse resp;
			    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
					try {
						Socket s = new Socket();
				    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
				    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
				    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
				    	wr.write(gson.toJson(req));
				    	wr.append('\n');
				    	wr.flush();
				    	resp = gson.fromJson(new InputStreamReader(s.getInputStream()), PostMealResponse.class);
				    	s.close();
			    	
					}
					catch(Exception ex) {
						ex.printStackTrace();
						return null;
					}
					try {
						PostImageRequest imgReq = new PostImageRequest();
				    	imgReq.meal_id = resp.id;
				    	Socket s = new Socket();
				    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
				    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
				    	OutputStream out = s.getOutputStream();
				    	byte[] jsonBytes = gson.toJson(imgReq).getBytes();
				    	out.write(jsonBytes);
				    	out.write('\n');
				    	Bitmap pictureJPG = BitmapFactory.decodeByteArray(MainActivity.thePicture, 0, MainActivity.thePicture.length);
				    	if(pictureJPG != null) {
				    		pictureJPG.compress(CompressFormat.JPEG, 50, out);
				    	}
				    	else {
				    		s.close();
				    		throw new RuntimeException("can't read image");
				    	}
				    	out.flush();
				    	s.close();
					}
					catch(Exception ex) {
						ex.printStackTrace();
						return null;
					}
					return null;
				}
				@Override
		    	protected void onPostExecute(PostImageResponse in) {
					Toast.makeText(PostMealActivity.this, "posted!" , Toast.LENGTH_SHORT).show();
					diag.dismiss();
		    		finish();
		    	}
	    		
	    	}.execute();
	    	
	    	
	    }
	    return true;
	}
	
	
	
}
