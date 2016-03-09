package com.example.masommer.mapster;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by agmal_000 on 08.03.2016.
 */
class PopupCursorAdapter extends CursorAdapter {
<<<<<<< HEAD
=======

    private static final int LAT_TAG = 1;
    private static final int LONG_TAG = 2;
>>>>>>> refs/remotes/origin/master
    public PopupCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.lvItem);
        String item = cursor.getString(cursor.getColumnIndexOrThrow("suggest_text_1"));
        textView.setText(item);
        textView.setTag(R.string.lat_tag, cursor.getString(cursor.getColumnIndexOrThrow("LATITUDE")));
        textView.setTag(R.string.long_tag, cursor.getString(cursor.getColumnIndexOrThrow("LONGITUDE")));
    }
}
