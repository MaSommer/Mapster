package com.example.masommer.mapster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by agmal_000 on 07.03.2016.
 */
public class DisplayResultActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);
        ListView listView = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        ArrayList<String> result = intent.getStringArrayListExtra("RESULT");

    }
}
