package edu.purdue.craveme;

import edu.purdue.craveme.provider.CraveContract;
import edu.purdue.craveme.provider.CraveContract.Direction;
import edu.purdue.craveme.provider.CraveContract.Ingredient;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

public class RecipeFragment extends Fragment implements
		LoaderCallbacks<Cursor> {
	
	private static final String TAG = "RecipeFragment";

	public static final String ARG_RECIPE_ID = "recipe_id";
	public static final String ARG_RECIPE_NAME = "recipe_name";
	
    /**
     * Cursor adapter for controlling ListView results.
     */
    private RecipeAdapter mAdapter;
    
    private String mRecipeName;
    private int mRecipeID;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] DIRECTIONS_PROJECTION = new String[]{
            CraveContract.Direction._ID,
            CraveContract.Direction.COLUMN_NAME_DIRECTION
    };
    
    private static final int DIRECTIONS_COL_ID = 0;
    private static final int DIRECTIONS_COL_DIRECTION = 1;
    
    private static final String[] INGREDIENTS_PROJECTION = new String[]{
    		Ingredient._ID,
    		Ingredient.COLUMN_NAME_NAME
    };
    
    private static final int INGREDIENTS_COL_ID = 0;
    private static final int INGREDIENTS_COL_NAME = 1;

    private static final int LOADER_INGREDIENTS = 0;
    private static final int LOADER_DIRECTIONS = 1;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeFragment() {}
    
    public static RecipeFragment newInstance(int recipeId, String recipeName) {
    	Bundle args = new Bundle();
    	args.putInt(ARG_RECIPE_ID, recipeId);
    	args.putString(ARG_RECIPE_NAME, recipeName);
    	RecipeFragment frag = new RecipeFragment();
    	frag.setArguments(args);
    	return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mRecipeName = args.getString(ARG_RECIPE_NAME);
        mRecipeID = args.getInt(ARG_RECIPE_ID);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       
        ExpandableListView list = new ExpandableListView(getActivity());
        mAdapter = new RecipeAdapter();
        list.setAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_INGREDIENTS, null, this);
        getLoaderManager().initLoader(LOADER_DIRECTIONS, null, this);
        return list;
    }

    /**
     * Query the content provider for data.
     *
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
		switch(i) {
		case LOADER_DIRECTIONS:
			 return new CursorLoader(getActivity(),  // Context
		                CraveContract.Direction.CONTENT_URI, // URI
		                DIRECTIONS_PROJECTION,                // Projection
		                Direction.COLUMN_NAME_RECIPE_ID + "=?",                           // Selection
		                new String[]{Integer.toString(mRecipeID)},                           // Selection args
		                CraveContract.Direction.COLUMN_NAME_NUMBER + " asc"); // Sort
		case LOADER_INGREDIENTS:
				return new CursorLoader(getActivity(),
						Ingredient.CONTENT_URI,
						INGREDIENTS_PROJECTION,
						Ingredient.COLUMN_NAME_RECIPE_ID + "=?",
						new String[]{Integer.toString(mRecipeID)},
						Ingredient.COLUMN_NAME_NAME + " asc");
		default:
			throw new RuntimeException("out of bounds");
		}
       
	}

	/**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		Log.i(TAG, cursor.getCount() + " ROWS");
		switch(cursorLoader.getId()) {
		case LOADER_INGREDIENTS:
			mAdapter.updateIngredients(cursor);
			break;
		case LOADER_DIRECTIONS:
			mAdapter.updateDirections(cursor);
			break;
		default:
			throw new RuntimeException("out of bounds");
		}
	}

	 /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		switch(cursorLoader.getId()) {
		case LOADER_INGREDIENTS:
			mAdapter.updateIngredients(null);
			break;
		case LOADER_DIRECTIONS:
			mAdapter.updateDirections(null);
			break;
		default:
			throw new RuntimeException("out of bounds");
		}
	}
	
	
	private class RecipeAdapter extends BaseExpandableListAdapter {

		private Cursor ingredients;
		private Cursor directions;
		private static final int INGREDIENTS_POS = 0;
		private static final int DIRECTIONS_POS = 1;
		@Override
		public int getGroupCount() {
			return 2;
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			switch(groupPosition) {
			case INGREDIENTS_POS:
				if(ingredients != null) {
					return ingredients.getCount();
				}
				else {
					return 0;
				}
			case DIRECTIONS_POS:
				if(directions != null) {
					return directions.getCount();
				}
				else {
					return 0;
				}
			default:
				throw new RuntimeException("out of bounds");
			}
			
		}
		@Override
		public Object getGroup(int groupPosition) {
			switch(groupPosition) {
			case INGREDIENTS_POS:
				return ingredients;
			case DIRECTIONS_POS:
				return directions;
			default:
				throw new RuntimeException("out of bounds");
			}
		}
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			switch(groupPosition) {
			case INGREDIENTS_POS:
				if(ingredients != null) {
					ingredients.moveToPosition(childPosition);
					return ingredients;
				}
				else {
					return null;
				}
			case DIRECTIONS_POS:
				if(directions != null) {
					directions.moveToPosition(childPosition);
					return directions;
				}
				else {
					return null;
				}
			default:
				throw new RuntimeException("out of bounds");
			}
		}
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			switch(groupPosition) {
			case INGREDIENTS_POS:
				if(ingredients != null) {
					ingredients.moveToPosition(childPosition);
					return ingredients.getInt(INGREDIENTS_COL_ID);
				}
				else {
					return -1;
				}
			case DIRECTIONS_POS:
				if(directions != null) {
					directions.moveToPosition(childPosition);
					return directions.getInt(DIRECTIONS_COL_ID);
				}
				else {
					return -1;
				}
			default:
				throw new RuntimeException("out of bounds");
			}
		}
		@Override
		public boolean hasStableIds() {
			return true;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView tv;
			if(convertView != null) {
				tv = (TextView) convertView;
			}
			else {
				tv = new TextView(getActivity());
			}
			switch(groupPosition) {
			case INGREDIENTS_POS:
				tv.setText("Ingredients");
				break;
			case DIRECTIONS_POS:
				tv.setText("Directions");
				break;
			default:
				throw new RuntimeException("out of bounds");
			}
			return tv;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView tv;
			if(convertView != null) {
				tv = (TextView) convertView;
			}
			else {
				tv = new TextView(getActivity());
			}
			switch(groupPosition) {
			case INGREDIENTS_POS:
				ingredients.moveToPosition(childPosition);
				tv.setText(ingredients.getString(INGREDIENTS_COL_NAME));
				break;
			case DIRECTIONS_POS:
				directions.moveToPosition(childPosition);
				tv.setText(directions.getString(DIRECTIONS_COL_DIRECTION));
				break;
			default:
				throw new RuntimeException("out of bounds");
			}
			return tv;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
		
		public void updateIngredients(Cursor ingredients) {
			this.ingredients = ingredients;
			notifyDataSetChanged();
		}
		
		public void updateDirections(Cursor directions) {
			this.directions = directions;
			notifyDataSetChanged();
		}
		
		
	}

}
