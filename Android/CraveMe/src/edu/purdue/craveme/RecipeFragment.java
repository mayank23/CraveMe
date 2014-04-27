package edu.purdue.craveme;

import edu.purdue.craveme.provider.CraveContract;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;

public class RecipeFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	
	private static final String TAG = "RecipeFragment";

	public static final String ARG_RECIPE_ID = "recipe_id";
	public static final String ARG_RECIPE_NAME = "recipe_name";
	
    /**
     * Cursor adapter for controlling ListView results.
     */
    private SimpleCursorAdapter mAdapter;
    
    private String mRecipeName;
    private int mRecipeID;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            CraveContract.Ingredient._ID,
            CraveContract.Ingredient.COLUMN_NAME_NAME
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    /** Column index for _ID */
    private static final int COLUMN_ID = 0;
    /** Column index for name */
    private static final int COLUMN_NAME = 1;

    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            CraveContract.Ingredient.COLUMN_NAME_NAME
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
    };

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                    // Let SimpleCursorAdapter handle other fields automatically
                    return false;
            }
        });
        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
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
        return new CursorLoader(getActivity(),  // Context
                CraveContract.Recipe.getIngredientsURI(mRecipeID), // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                CraveContract.Ingredient.COLUMN_NAME_NAME + " asc"); // Sort
	}

	/**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		Log.i(TAG, cursor.getCount() + " ROWS");
        mAdapter.changeCursor(cursor);
	}

	 /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mAdapter.changeCursor(null);
	}

}
