package com.example.masommer.mapster;

import android.content.Context;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

/**
 * Created by MaSommer on 08.03.16.
 */

public class PopUpMenuEventHandle implements PopupMenu.OnMenuItemClickListener {
    Context context;
    public PopUpMenuEventHandle(Context context){
        this.context =context;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId()==R.id.favourites){

            Toast.makeText(context,"login is user",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
