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

package edu.purdue.craveme;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import edu.purdue.craveme.net.RecipeParser;
import edu.purdue.craveme.provider.CraveContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String SERVER_HOSTNAME = "data.cs.purdue.edu";
    private static final int SERVER_PORT = 9012;

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[] {
            CraveContract.Recipe._ID,
            CraveContract.Recipe.COLUMN_NAME_RECIPE_ID,
            CraveContract.Recipe.COLUMN_NAME_USER_ID,
            CraveContract.Recipe.COLUMN_NAME_TITLE,
            CraveContract.Recipe.COLUMN_NAME_LENGTH,
            CraveContract.Recipe.COLUMN_NAME_PIC_URL
    };
    
    private static final String[] INGREDIENTS_PROJECTION = new String[] {
    		CraveContract.Ingredient._ID,
    		CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID,
    		CraveContract.Ingredient.COLUMN_NAME_NAME
    };
    
    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_RECIPE_ID = 1;
    public static final int COLUMN_USER_ID = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_LENGTH = 4;
    public static final int COLUMN_PIC_URL = 5;
    
    public static final int INGREDIENT_COLUMN_ID = 0;
    public static final int INGREDIENT_COLUMN_RECIPE_ID = 1;
    public static final int INGREDIENT_COLUMN_NAME = 2;
    
    // Constants representing sync options
    private static final String REQUEST_OPTION = "option";
    private static final String OPTION_RECIPES = "get_all_recipes";

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            InputStream stream = null;
            Socket s = null;
            try {
                Log.i(TAG, "Streaming data from network: " + SERVER_HOSTNAME + ":" + SERVER_PORT);
                s = getSocket(SERVER_HOSTNAME, SERVER_PORT);
                stream = requestRecipes(s);
                updateLocalRecipeData(stream, syncResult);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if(s != null) {
                	s.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (JSONException e) {
        	e.printStackTrace();
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (NoSuchElementException e) {
        	Log.e("TAG", "Error reading form network: " + e.toString());
        	syncResult.stats.numIoExceptions++;
        	return;
        }
        Log.i(TAG, "Network synchronization complete");
    }
    
    /**
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public void updateLocalRecipeData(final InputStream stream, final SyncResult syncResult)
            throws IOException, RemoteException,
            OperationApplicationException, JSONException {
        final RecipeParser feedParser = new RecipeParser();
        final ContentResolver contentResolver = getContext().getContentResolver();

        Log.i(TAG, "Parsing stream as Recipe object");
        final List<RecipeParser.Recipe> entries = feedParser.parse(stream);
        Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<Integer, RecipeParser.Recipe> entryMap = new HashMap<Integer, RecipeParser.Recipe>();
        for (RecipeParser.Recipe e : entries) {
            entryMap.put(e.id, e);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = CraveContract.Recipe.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        int recipeId;
        String title;
        int length;
        String photoUrl;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            recipeId = c.getInt(COLUMN_RECIPE_ID);
            title = c.getString(COLUMN_TITLE);
            length = c.getInt(COLUMN_LENGTH);
            photoUrl = c.getString(COLUMN_PIC_URL);
            RecipeParser.Recipe match = entryMap.get(recipeId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
            	RecipeParser.Recipe entry = entryMap.get(recipeId);
                entryMap.remove(recipeId);
                // Check to see if the entry needs to be updated
                Uri existingUri = CraveContract.Recipe.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.title != null && !match.title.equals(title)) ||
                        (match.time != 0 && match.time != length) ||
                        (match.photoUrl != null && !match.photoUrl.equals(photoUrl))) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(CraveContract.Recipe.COLUMN_NAME_TITLE, title)
                            .withValue(CraveContract.Recipe.COLUMN_NAME_LENGTH, length)
                            .withValue(CraveContract.Recipe.COLUMN_NAME_PIC_URL, photoUrl)
                            .build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
                //Check ingredients
                Uri ingredientsUri = CraveContract.Recipe.getIngredientsURI(recipeId);
                Cursor cIngredients = contentResolver.query(ingredientsUri, INGREDIENTS_PROJECTION, null, null, null);
            	HashSet<String> ingredientsSet = new HashSet<String>();
            	for(String ingredient : entry.ingredients) {
            		ingredientsSet.add(ingredient);
            	}
            	String curIngredient;
                while (cIngredients.moveToNext()) {
                	curIngredient = cIngredients.getString(INGREDIENT_COLUMN_NAME);
                	boolean contained = ingredientsSet.contains(curIngredient);
                	//delete ingredients not in the current
                	if(!contained) {
                		Log.i(TAG, "Scheduling delete: name=" + curIngredient);
                		batch.add(ContentProviderOperation.newDelete(ingredientsUri)
                				.withSelection("name=?", new String[]{Integer.toString(recipeId)})
                				.build());
                	}
                	else {
                		ingredientsSet.remove(curIngredient);
                	}
                	
                }
                for(String ingredient : ingredientsSet) {
                	Log.i(TAG, "Scheduling insert: name=" + ingredient);
                	batch.add(ContentProviderOperation.newInsert(ingredientsUri)
                			.withValue(CraveContract.Ingredient.COLUMN_NAME_NAME, ingredient)
                			.withValue(CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID, recipeId)
                			.build());
                }
                cIngredients.close();
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = CraveContract.Recipe.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (RecipeParser.Recipe e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);
            //TODO: create local photo
            batch.add(ContentProviderOperation.newInsert(CraveContract.Recipe.CONTENT_URI)
                    .withValue(CraveContract.Recipe.COLUMN_NAME_RECIPE_ID, e.id)
                    .withValue(CraveContract.Recipe.COLUMN_NAME_USER_ID, e.userId)
                    .withValue(CraveContract.Recipe.COLUMN_NAME_TITLE, e.title)
                    .withValue(CraveContract.Recipe.COLUMN_NAME_LENGTH, e.time)
                    .withValue(CraveContract.Recipe.COLUMN_NAME_PIC_URL, e.photoUrl)
                    .build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(CraveContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                CraveContract.Recipe.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    /**
     * Given a hostname + port, sets up a connection and gets a socket
     */
    private Socket getSocket(final String hostname, int port) throws IOException {
    	Socket s = new Socket();
    	SocketAddress remoteAddr = new InetSocketAddress(hostname, port);
    	s.setSoTimeout(NET_READ_TIMEOUT_MILLIS);
    	s.connect(remoteAddr, NET_CONNECT_TIMEOUT_MILLIS);
    	return s;
    }
    
    /**
     * Send a recipe request through a socket to the server
     * @param s the socket
     * @return the socket's input stream after writing the request to and closing the output stream
     * @throws IOException
     * @throws JSONException
     */
    private InputStream requestRecipes(Socket s) throws IOException, JSONException {
    	OutputStreamWriter wr = new OutputStreamWriter(s.getOutputStream());
    	JSONObject request = new JSONObject();
    	request.put(REQUEST_OPTION, OPTION_RECIPES);
    	wr.write(request.toString());
    	wr.append('\n');
    	wr.flush();
    	return s.getInputStream();
    }
}
