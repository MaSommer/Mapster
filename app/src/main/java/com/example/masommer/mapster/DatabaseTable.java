package com.example.masommer.mapster;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class DatabaseTable extends ListActivity{

    private static final String TAG = "RoomDatabase";

    public static final String COL_ROOM = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String COL_LAT = "LATITUDE";
    public static final String COL_LONG = "LONGITUDE";

    private static final String DATABASE_NAME = "database";
    private static final String FTS_VIRTUAL_TABLE = "FTSdatabase";
    private static final int DATABASE_VERSION = 1;



    private final DatabaseOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();


    public DatabaseTable(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }


    public Cursor getWordMatches(String query, String[] columns) {

        String selection = COL_ROOM + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    public Cursor getRoom(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[]{rowId};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(COL_ROOM, COL_ROOM);
        map.put(COL_LAT, COL_LAT);
        map.put(COL_LONG,COL_LONG);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, "rowid AS "+
                SearchManager.SUGGEST_COLUMN_INTENT_DATA);
        return map;
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_ROOM + ", " +
                        COL_LAT + ", " +
                        COL_LONG + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadAll();
        }




        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void loadAll() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadLocations();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }



        private void loadLocations() throws IOException {
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.room_and_buildings);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = line.split("\\s+");
                    try{
                        long id = addLocation(strings[0].trim(), strings[1].trim(), strings[2].trim());
                        if (id < 0) {
                            Log.e(TAG, "unable to add location: " + strings[0].trim());
                        }
                    }catch(Exception e){
                        Log.e("Database", "Unable to load location: "+e);
                    }
                }
            } finally {
                reader.close();
            }
        }

        public long addLocation(String room, String lat, String lon) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ROOM, room);
            initialValues.put(COL_LAT, lat);
            initialValues.put(COL_LONG, lon);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

    }


}
