
package com.example.masommer.mapster;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

/**
 * Provides access to the dictionary database.
 */
public class DatabaseProvider extends ContentProvider {
    String TAG = "DatabaseProvider";

    public static String AUTHORITY = "com.example.masommer.mapster.DatabaseProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/database");

    // MIME types used for searching words or looking up a single definition
    public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.example.masommer.mapster";
    public static final String DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.example.masommer.mapster";

    private DatabaseTable mDatabase;

    // UriMatcher stuff
    private static final int SEARCH_ROOMS = 0;
    private static final int GET_ROOM = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, "database", SEARCH_ROOMS);
        matcher.addURI(AUTHORITY, "database/#", GET_ROOM);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabase = new DatabaseTable(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case SEARCH_ROOMS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_ROOM:
                return getRoom(uri);
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        String[] columns = new String[] {
                BaseColumns._ID,
                DatabaseTable.COL_ROOM,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID}; //changed from DATA_ID

        return mDatabase.getWordMatches(query, columns);
    }

    private Cursor search(String query) {
        query = query.toLowerCase();
        String[] columns = new String[] {
                BaseColumns._ID,
                DatabaseTable.COL_ROOM,
                DatabaseTable.COL_LONG,
                DatabaseTable.COL_LAT};
//                DictionaryDatabase.KEY_DEFINITION};

        return mDatabase.getWordMatches(query, columns);
    }

    private Cursor getRoom(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns = new String[] {
                DatabaseTable.COL_ROOM,
                DatabaseTable.COL_LAT,
                DatabaseTable.COL_LONG};
//                DictionaryDatabase.KEY_DEFINITION};

        return mDatabase.getRoom(rowId, columns);
    }

    private Cursor refreshShortcut(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns = new String[] {"*"};
//                BaseColumns._ID,
//                DictionaryDatabase.KEY_WORD,
//                DictionaryDatabase.KEY_DEFINITION,
//                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
//                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

        return mDatabase.getRoom(rowId, columns);
    }

    /**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_ROOMS:
                return WORDS_MIME_TYPE;
            case GET_ROOM:
                return DEFINITION_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}