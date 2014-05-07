package com.example.craveme2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

import org.json.JSONObject;

import com.google.gson.Gson;

import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SurfaceView hack = new SurfaceView(this);
		hack.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
		hack.setVisibility(View.GONE);
		setContentView(hack);

		ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);

	    	Tab tab = actionBar.newTab()
                .setText("meals")
                .setTabListener(new TabListener<MealListFragment>(
                        this, "meals", MealListFragment.class));
	    	actionBar.addTab(tab);
	    	
		   tab = actionBar.newTab()
		                       .setText("recipes")
		                       .setTabListener(new TabListener<RecipelistFragment>(
		                               this, "recipes", RecipelistFragment.class));
		    actionBar.addTab(tab);
	
		    
		    tab = actionBar.newTab()
		    			   .setText("post meal")
		    			   .setTabListener(new TabListener<MealPost>(
		    					   this, "meals_post", MealPost.class));
		    actionBar.addTab(tab);
	    /*
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new RecipelistFragment()).commit();
		}
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static Gson gson = new Gson();
	
	private static final String HOSTNAME = "data.cs.purdue.edu";
    private static final int PORT = 9012;
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    
    public static Recipe theRecipe;
    public static Meal theMeal;
    public static byte[] thePicture;
    
	
    public static class RecipeResponse {
    	String response;
    	Recipe[] recipes;
    }
	static class Recipe {
        public int id;
        public int user_id;
        public String title;
        public String[] steps;
        public String[] ingredients;
        public int time;
        public int craves;
        public int nots;
        public String photo_url;
        
        @Override
        public String toString() {
        	return title;
        }
	}
	
	public static class RecipeListLoader extends AsyncTaskLoader<Recipe[]> {

		private Recipe[] recipes;
		public RecipeListLoader(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Recipe[] loadInBackground() {
			try {
				Socket s = new Socket();
		    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
		    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
		    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
		    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
		    	JSONObject request = new JSONObject();
		    	request.put("option", "get_all_recipes");
		    	wr.write(request.toString());
		    	wr.append('\n');
		    	wr.flush();
		    	recipes = gson.fromJson(new InputStreamReader(s.getInputStream()), RecipeResponse.class).recipes;
		    	s.close();
		    	return recipes;
	    	}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
	    	
		}
		
		@Override
		public void onStartLoading() {
			if(recipes != null) {
				deliverResult(recipes);
			}
			else {
				forceLoad();
			}
		}
		
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class RecipelistFragment extends ListFragment implements LoaderCallbacks<Recipe[]> {

		private ArrayAdapter<Recipe> adapter;
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			adapter = new ArrayAdapter<Recipe>(getActivity(), android.R.layout.simple_list_item_1);
			getListView().setOnItemClickListener(new OnItemClickListener() {

		            @Override
		            public void onItemClick(AdapterView<?> arg0, View view, int position,
		                    long id) {
		            	Intent i = new Intent();
		            	theRecipe = adapter.getItem(position);
		            	i.setClass(getActivity(), RecipeActivity.class);
		            	startActivity(i);
		            }
			});
			getLoaderManager().initLoader(0, null, this);
		}
		
		@Override
		public Loader<Recipe[]> onCreateLoader(int i, Bundle args) {
			return new RecipeListLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<Recipe[]> loader, Recipe[] recipes) {
			if(recipes != null) {
				adapter.clear();
				for(int i = 0; i < recipes.length; ++i) {
					adapter.add(recipes[i]);
				}
				setListAdapter(adapter);
			}
		}

		@Override
		public void onLoaderReset(Loader<Recipe[]> loader) {
			adapter.clear();
		}
	}
	
	public static class GetMealRequest {
		String option = "get_all_meals";
	}
	
	public static class Meal {
		int id;
		String photo_url;
		int craves;
		String user_name;
		String title;
		String category;
		String description;
		int nots;
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	public static class GetMealResponse {
		Meal[] meals;
		String response;
	}
	
	public static class MealListLoader extends AsyncTaskLoader<Meal[]> {

		private Meal[] meals;
		public MealListLoader(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Meal[] loadInBackground() {
			try {
				Socket s = new Socket();
		    	SocketAddress remoteAddr = new InetSocketAddress(HOSTNAME, PORT);
		    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
		    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
		    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
		    	
		    	wr.write(gson.toJson(new GetMealRequest()));
		    	wr.append('\n');
		    	wr.flush();
		    	meals = gson.fromJson(new InputStreamReader(s.getInputStream()), GetMealResponse.class).meals;
		    	s.close();
		    	return meals;
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
		
	}
	public static class MealListFragment extends ListFragment implements LoaderCallbacks<Meal[]> {

		private ArrayAdapter<Meal> adapter;
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			adapter = new ArrayAdapter<Meal>(getActivity(), android.R.layout.simple_list_item_1);
			getListView().setOnItemClickListener(new OnItemClickListener() {

		            @Override
		            public void onItemClick(AdapterView<?> arg0, View view, int position,
		                    long id) {
		            	Intent i = new Intent();
		            	theMeal = adapter.getItem(position);
		            	i.setClass(getActivity(), MealActivity.class);
		            	startActivity(i);
		            }
			});
			getLoaderManager().initLoader(0, null, this);
		}
		
		@Override
		public Loader<Meal[]> onCreateLoader(int i, Bundle args) {
			return new MealListLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<Meal[]> loader, Meal[] meals) {
			if(meals != null) {
				adapter.clear();
				for(int i = 0; i < meals.length; ++i) {
					adapter.add(meals[i]);
				}
				setListAdapter(adapter);
			}
		}

		@Override
		public void onLoaderReset(Loader<Meal[]> loader) {
			adapter.clear();
		}
	}
	public static class MealPost extends Fragment {
		private SurfaceView surfaceView;
		private Camera camera;
		private SurfaceHolder holder;
		
		@Override
		public void onResume() {
			super.onResume();
			getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
		}
		
		@Override
		public void onPause() {
			super.onPause();
			getActivity().getWindow().setFormat(PixelFormat.UNKNOWN);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			
			surfaceView = new SurfaceView(getActivity().getApplicationContext());
			holder = surfaceView.getHolder();
			holder.addCallback(new Callback() {

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					camera = Camera.open();

		             try {
		                  camera.setPreviewDisplay(holder);  
		             } catch (IOException exception) {  
		                   camera.release();  
		                   camera = null;  
		             }
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
					camera.setDisplayOrientation(90);
                    camera.startPreview();
				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					camera.stopPreview();
		            camera.release();
		            camera = null;
				}
				
			});
			
			surfaceView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Camera.Parameters params = camera.getParameters();
					params.setRotation(90);
					camera.setParameters(params);
					camera.takePicture(null, null, null, new PictureCallback() {

						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							Intent i = new Intent();
							thePicture = data;
							i.setClass(getActivity(), PostMealActivity.class);
							startActivity(i);
						}
						
					});
				}
				
			});
			return surfaceView;
		}
 	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final ActionBarActivity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(ActionBarActivity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}
}
