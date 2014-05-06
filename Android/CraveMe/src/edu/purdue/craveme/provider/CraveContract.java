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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for
 * {@link com.example.android.basicsyncadapter.provider.FeedProvider}.
 */
public class CraveContract {
    private CraveContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "edu.purude.craveme";

    /**
     * Base URI. (content://com.example.android.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "recipe"-type resources..
     */
    private static final String PATH_RECIPES = "recipes";
    
    /**
     * Path component for "ingredient"-type resources..
     */
    private static final String PATH_INGREDIENTS = "ingredients";
    
    private static final String PATH_DIRECTIONS = "directions";

    /**
     * Columns supported by "entries" records.
     */
    public static class Recipe implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.craveme.recipes";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.craveme.recipe";

        /**
         * Fully qualified URI for "recipe" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        /**
         * Table name where records are stored for "recipe" resources.
         */
        public static final String TABLE_NAME = "recipe";
        
        /**
         * Recipe ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
        
        /**
         * Title of recipe
         */
        public static final String COLUMN_NAME_TITLE = "title";
        
        /**
         * Length recipe takes to be finished, in minutes
         */
        public static final String COLUMN_NAME_LENGTH = "length";
        
        /**
         * Path to a picture of the finished recipe
         */
        public static final String COLUMN_NAME_PIC_PATH = "pic_path";
        
        /**
         * Url of recipe picture
         */
        public static final String COLUMN_NAME_PIC_URL = "pic_url";
        
        /**
         * ID of user who created the recipe (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_USER_ID = "user_id";
    }
    
    public static class Ingredient implements BaseColumns {
    	/**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.craveme.ingredients";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.craveme.ingredient";

        /**
         * Fully qualified URI for "ingredient" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENTS).build();
        
        /**
         * Table name where records are stored for "recipe" resources.
         */
        public static final String TABLE_NAME = "ingredient";
        
        /**
         * Name of ingredient
         */
        public static final String COLUMN_NAME_NAME = "name";
        
        /**
         * ID of recipe ingredient is tied to
         */
        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
        
    }
    
    public static class Direction implements BaseColumns {
    	/**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.craveme.directions";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.craveme.direction";

        /**
         * Fully qualified URI for "ingredient" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DIRECTIONS).build();
        
        /**
         * Table name where records are stored for "recipe" resources.
         */
        public static final String TABLE_NAME = "directions";
        
        /**
         * Name of direction
         */
        public static final String COLUMN_NAME_DIRECTION = "direction";
        
        public static final String COLUMN_NAME_NUMBER = "number";
        /**
         * ID of recipe
         */
        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
    }
}