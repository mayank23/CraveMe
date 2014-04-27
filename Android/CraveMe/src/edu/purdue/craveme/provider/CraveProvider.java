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

package edu.purdue.craveme.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import edu.purdue.craveme.common.db.SelectionBuilder;

public class CraveProvider extends ContentProvider {
    CraveDatabase mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = CraveContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /recipes
     */
    public static final int ROUTE_RECIPES = 1;

    /**
     * URI ID for route: /recipes/{ID}
     */
    public static final int ROUTE_RECIPES_ID = 2;
    
    /**
     * URI ID for route: /ingredients
     */
    public static final int ROUTE_INGREDIENTS = 3;
    
    /**
     * URI ID for route: /ingredients/{ID}
     */
    public static final int ROUTE_INGREDIENTS_ID = 4;
    
    /**
     * URI ID for route: /recipes/{ID}/ingredients
     */
    public static final int ROUTE_RECIPE_INGREDIENTS = 5;
    
    
    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "recipes", ROUTE_RECIPES);
        sUriMatcher.addURI(AUTHORITY, "recipes/#", ROUTE_RECIPES_ID);
        sUriMatcher.addURI(AUTHORITY, "recipes/#/ingredients", ROUTE_RECIPE_INGREDIENTS);
        sUriMatcher.addURI(AUTHORITY, "ingredients", ROUTE_INGREDIENTS);
        sUriMatcher.addURI(AUTHORITY, "ingredients/#", ROUTE_INGREDIENTS_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new CraveDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_RECIPES:
                return CraveContract.Recipe.CONTENT_TYPE;
            case ROUTE_RECIPES_ID:
                return CraveContract.Recipe.CONTENT_ITEM_TYPE;
            case ROUTE_RECIPE_INGREDIENTS:
            	return CraveContract.Ingredient.CONTENT_TYPE;
            case ROUTE_INGREDIENTS:
            	return CraveContract.Ingredient.CONTENT_TYPE;
            case ROUTE_INGREDIENTS_ID:
            	return CraveContract.Ingredient.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_RECIPES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(CraveContract.Recipe._ID + "=?", id);
            case ROUTE_RECIPES:
                // Return all known entries.
                builder.table(CraveContract.Recipe.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_RECIPE_INGREDIENTS:
            	//Return all ingredients for a specific recipe
            	id = uri.getPathSegments().get(1);
            	builder.table(CraveContract.Ingredient.TABLE_NAME)
            		   .where(selection, selectionArgs)
            		   .where(CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID + "=?", id);
            	c = builder.query(db, projection, sortOrder);
            	 // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_INGREDIENTS_ID:
                // Return a single entry, by ID.
                id = uri.getLastPathSegment();
                builder.where(CraveContract.Ingredient._ID + "=?", id);
            case ROUTE_INGREDIENTS:
            	// Return all known entries.
                builder.table(CraveContract.Ingredient.TABLE_NAME)
                       .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;            	
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_RECIPES:
                long id = db.insertOrThrow(CraveContract.Recipe.TABLE_NAME, null, values);
                result = Uri.parse(CraveContract.Recipe.CONTENT_URI + "/" + id);
                break;
            case ROUTE_RECIPES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_RECIPE_INGREDIENTS:
            	String recipeId = uri.getPathSegments().get(1);
            	values.put(CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID, recipeId);
            	id = db.insertOrThrow(CraveContract.Ingredient.TABLE_NAME, null, values);
            	result = Uri.parse(CraveContract.Ingredient.CONTENT_URI + "/" + id);
            	break;
            case ROUTE_INGREDIENTS:
            	id = db.insertOrThrow(CraveContract.Ingredient.TABLE_NAME, null, values);
            	result = Uri.parse(CraveContract.Ingredient.CONTENT_URI + "/" + id);
            	break;
            case ROUTE_INGREDIENTS_ID:
            	throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_RECIPES:
                count = builder.table(CraveContract.Recipe.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_RECIPES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(CraveContract.Recipe.TABLE_NAME)
                       .where(CraveContract.Recipe._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            case ROUTE_RECIPE_INGREDIENTS:
            	id = uri.getPathSegments().get(1);
            	count = builder.table(CraveContract.Ingredient.TABLE_NAME)
            			.where(CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID + "=?", id)
            			.where(selection, selectionArgs)
            			.delete(db);
            	break;
            case ROUTE_INGREDIENTS:
            	count = builder.table(CraveContract.Ingredient.TABLE_NAME)
                .where(selection, selectionArgs)
                .delete(db);
            case ROUTE_INGREDIENTS_ID:
            	id = uri.getLastPathSegment();
                count = builder.table(CraveContract.Ingredient.TABLE_NAME)
                       .where(CraveContract.Ingredient._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an entry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_RECIPES:
                count = builder.table(CraveContract.Recipe.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_RECIPES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(CraveContract.Recipe.TABLE_NAME)
                        .where(CraveContract.Recipe._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_RECIPE_INGREDIENTS:
            	id = uri.getPathSegments().get(1);
            	count = builder.table(CraveContract.Ingredient.TABLE_NAME)
            			.where(CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID + "=?", id)
            			.where(selection, selectionArgs)
            			.update(db, values);
            	break;
            case ROUTE_INGREDIENTS:
            	count = builder.table(CraveContract.Ingredient.TABLE_NAME)
                .where(selection, selectionArgs)
                .update(db, values);
            	break;
            case ROUTE_INGREDIENTS_ID:
            	id = uri.getLastPathSegment();
                count = builder.table(CraveContract.Ingredient.TABLE_NAME)
                        .where(CraveContract.Ingredient._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class CraveDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "craveme.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "recipe" table. */
        private static final String SQL_CREATE_RECIPES =
                "CREATE TABLE " + CraveContract.Recipe.TABLE_NAME + " (" +
                        CraveContract.Recipe._ID + TYPE_INTEGER + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_RECIPE_ID + TYPE_INTEGER + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_USER_ID + TYPE_INTEGER + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_LENGTH + TYPE_INTEGER + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_PIC_URL + TYPE_TEXT + COMMA_SEP +
                        CraveContract.Recipe.COLUMN_NAME_PIC_PATH + TYPE_TEXT + COMMA_SEP +
                        "PRIMARY KEY(" + CraveContract.Recipe._ID + ")" + COMMA_SEP +
                        "UNIQUE(" + CraveContract.Recipe.COLUMN_NAME_RECIPE_ID + "))";
        
        /** SQL statement to create the "ingredients" table. */
        private static final String SQL_CREATE_INGREDIENTS =
        		"CREATE TABLE " + CraveContract.Ingredient.TABLE_NAME + " (" +
        				CraveContract.Ingredient._ID + TYPE_INTEGER + COMMA_SEP +
        				CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID + TYPE_INTEGER + COMMA_SEP +
        				CraveContract.Ingredient.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
        				"PRIMARY KEY(" + CraveContract.Ingredient._ID + ")" + COMMA_SEP +
        				"FOREIGN KEY(" + CraveContract.Ingredient.COLUMN_NAME_RECIPE_ID + ") REFERENCES " + CraveContract.Recipe.TABLE_NAME + "(" + CraveContract.Recipe.COLUMN_NAME_RECIPE_ID + "))"; 

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_RECIPES =
                "DROP TABLE IF EXISTS " + CraveContract.Recipe.TABLE_NAME;
        
        /** SQL statement to drop "ingredient" table. */
        private static final String SQL_DELETE_INGREDIENTS =
        		"DROP TABLE IF EXISTS " + CraveContract.Ingredient.TABLE_NAME;

        public CraveDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_RECIPES);
            db.execSQL(SQL_CREATE_INGREDIENTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_RECIPES);
            db.execSQL(SQL_DELETE_INGREDIENTS);
            onCreate(db);
        }
    }
}
