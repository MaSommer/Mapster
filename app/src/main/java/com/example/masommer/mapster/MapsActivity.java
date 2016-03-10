package com.example.masommer.mapster;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import android.app.SearchManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener, BlankFragment.OnFragmentInteractionListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;
    private final int DATABASE_LOADER = 0;
    private Marker roomMarker;
    private LocationManager lm;
    private String provider;
    private Marker marker;
    private boolean landscape;
    private Polyline newWalkingPolyline;
    private Polyline newDrivingPolyline;
    private Polyline newPolyline;
    private BlankFragment fragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private ArrayList directionPoints;
    private SharedPreferences prefs = null;

    private final int EDIT_MODE = 0;
    private final int NORMAL_MODE = 1;

    private int mode;

    private HashMap<String, LatLng> favourites;
    private ArrayList<Marker> favouritesMarkersList;

    private String directionMode;
    private int favNr;

    private boolean onMarkerClickRemove;

    private ListView listView;
    private ArrayList<Building> buildingList = new ArrayList<Building>();
    private ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private double currentCameraLongtitude;
    private double currentCameraLatitude;
    private double currentPositionLongtitude;
    private double currentPositionLatitude;
    private double roomMarkerLongtitude;
    private double roomMarkerLatitude;

    private double[] longtitudeList;
    private double[] latitudeList;

    private CameraPosition cameraPos;

    private boolean fragmentUpWhenRotationChanged;

    private DatabaseTable db;
    private android.location.LocationListener locationListener;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Toolbar editToolbar;
    private Marker markerToDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("fragmentUpWhenRotationChanged")) {
            fragment = new BlankFragment();
            fragment.show(getFragmentManager(), "Diag");
        }
        db = new DatabaseTable(this);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        provider = lm.getBestProvider(criteria, true);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (findViewById(R.id.sampleListView) != null) {
            landscape = true;
            listView = (ListView) findViewById(R.id.sampleListView);
            listView.setVisibility(View.GONE);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    listItems);
            listView.setAdapter(adapter);
        }
        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 4, locationListener);

        } catch (SecurityException sec) {
            sec.printStackTrace();
        }
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        onMarkerClickRemove = false;
        Log.i("i am her", "looser");
        loadFavourites();
        //deleteFile();
        mode = NORMAL_MODE;
        editToolbar = (Toolbar) findViewById(R.id.toolbar);
        editToolbar.setVisibility(View.GONE);
        editToolbar.setTitleTextColor(Color.WHITE);
        editToolbar.setTitle("Edit favourites");
        editToolbar.inflateMenu(R.menu.edit_favourites);



    }

    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        //System.out.print("tried to search!");
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            //System.out.print("HEIIIII");
            //String path = intent.getStringExtra(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
            //Log.i("path", intent.getData().getPath());
            Uri uri = intent.getData();
            roomFromSuggestion(uri);

//            Toast.makeText(getApplicationContext(), "VOILA!", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            Log.i("query", query);
            args.putString("QUERY", query);
            getSupportLoaderManager().restartLoader(DATABASE_LOADER,args,this);
            getSupportLoaderManager().initLoader(DATABASE_LOADER, args, this);
//            Cursor cursor = db.getWordMatches(query, null);
//
//            Intent new_intent = new Intent(this, DisplayResultActivity.class);
//            ArrayList<String> result = new ArrayList<>();
//            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//                // The Cursor is now set to the right position
//                result.add(cursor.getString(0));
//            }
//            new_intent.putExtra("RESULT", result);
//            MapsActivity.this.startActivity(intent);
            //String query = intent.getStringExtra(SearchManager.QUERY);
            //showResults(query);
        }
    }

    private void roomFromSuggestion(Uri uri) {
        Cursor c = managedQuery(uri, null, null, null, null);
        c.moveToFirst();
        int idIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_ROOM);
        int latIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_LAT);
        int longIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_LONG);
        double latitude = Double.parseDouble(c.getString(latIndex));
        double longitude = Double.parseDouble(c.getString(longIndex));
        String title = c.getString(idIndex);
        LatLng roomPoint = new LatLng(latitude,longitude);
        if (roomMarker != null){
            roomMarker.remove();
        }
        roomMarker = mMap.addMarker(new MarkerOptions().position(roomPoint));
        roomMarker.setTitle(title);
        zoomToRoom(roomMarker.getPosition());
    }

//    private void showResults(String query) {
//
//        Cursor cursor = new CursorLoader(getApplicationContext(),DatabaseProvider.CONTENT_URI, null, null,
//                new String[]{query}, null);
//        if (cursor == null) {
//            // There are no results
//            //mTextView.setText(getString(R.string.no_results, new Object[]{query}));
//        } else {
//            // Display the number of results
//            int count = cursor.getCount();
//            //String countString = getResources().getQuantityString(R.plurals.search_results,
//            //        count, new Object[] {count, query});
//            //mTextView.setText(countString);
//            Toast.makeText(MapsActivity.this, "WOW: You found "+count+" results!", Toast.LENGTH_SHORT).show();
//            // Specify the columns we want to display in the result
////            String[] from = new String[] { DictionaryDatabase.KEY_WORD,
////                    DictionaryDatabase.KEY_DEFINITION };
////
////            // Specify the corresponding layout elements where we want the columns to go
////            int[] to = new int[] { R.id.word,
////                    R.id.definition };
////
////            // Create a simple cursor adapter for the definitions and apply them to the ListView
////            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
////                    R.layout.result, cursor, from, to);
////            mListView.setAdapter(words);
////
////            // Define the on-click listener for the list items
////            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////
////                @Override
////                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                    // Build the Intent used to open WordActivity with a specific word Uri
////                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
////                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
////                            String.valueOf(id));
////                    wordIntent.setData(data);
////                    startActivity(wordIntent);
////                }
////            });
//        }
//    }
    /*private void showResults(String query) {

        Cursor cursor = new android.support.v4.content.CursorLoader(getApplicationContext(),DatabaseProvider.CONTENT_URI, null, null,
=======
=======
>>>>>>> Martin
>>>>>>> master
/*    private void showResults(String query) {

        CursorLoader cursor = new android.support.v4.content.CursorLoader(getApplicationContext(),DatabaseProvider.CONTENT_URI, null, null,
=======
    /*private void showResults(String query) {

        Cursor cursor = new android.support.v4.content.CursorLoader(getApplicationContext(),DatabaseProvider.CONTENT_URI, null, null,
>>>>>>> master
=======
    /*private void showResults(String query) {

        Cursor cursor = new android.support.v4.content.CursorLoader(getApplicationContext(),DatabaseProvider.CONTENT_URI, null, null,
>>>>>>> master
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> Martin
=======
>>>>>>> Martin
>>>>>>> master
                new String[]{query}, null);
        if (cursor == null) {
            // There are no results
            //mTextView.setText(getString(R.string.no_results, new Object[]{query}));
        } else {
            // Display the number of results
            int count = cursor.getCount();
            //String countString = getResources().getQuantityString(R.plurals.search_results,
            //        count, new Object[] {count, query});
            //mTextView.setText(countString);
            Toast.makeText(MapsActivity.this, "WOW: You found "+count+" results!", Toast.LENGTH_SHORT).show();
            // Specify the columns we want to display in the result
//            String[] from = new String[] { DictionaryDatabase.KEY_WORD,
//                    DictionaryDatabase.KEY_DEFINITION };
//
//            // Specify the corresponding layout elements where we want the columns to go
//            int[] to = new int[] { R.id.word,
//                    R.id.definition };
//
//            // Create a simple cursor adapter for the definitions and apply them to the ListView
//            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
//                    R.layout.result, cursor, from, to);
//            mListView.setAdapter(words);
//
//            // Define the on-click listener for the list items
//            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    // Build the Intent used to open WordActivity with a specific word Uri
//                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
//                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
//                            String.valueOf(id));
//                    wordIntent.setData(data);
//                    startActivity(wordIntent);
//                }
//            });
        }
    }*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION_ACCESS);
        }


        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMapClickListener(this);
        //Ad    d north hall to map
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.north_hall_v3);
        Log.i("bitmap", "" + bitmap);
        BitmapDescriptor bs = BitmapDescriptorFactory.fromBitmap(bitmap);
        Log.i("bs", "" + bs);

        LatLng northHallPos = new LatLng(34.415135360789565, -119.84668038419226);

        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bs)
                .position(northHallPos, 131f, 99f);
        mMap.addGroundOverlay(newarkMap);

        Bitmap phelps = BitmapFactory.decodeResource(getResources(), R.raw.phelps);
        BitmapDescriptor phelps_bs = BitmapDescriptorFactory.fromBitmap(phelps);
        Log.i("bs", "" + phelps_bs);
        LatLng phelpsPos = new LatLng(34.416163805390714, -119.8443453662777);
        GroundOverlayOptions phelps_opts = new GroundOverlayOptions()
                .image(phelps_bs)
                .position(phelpsPos, 146f, 130f);
        mMap.addGroundOverlay(phelps_opts);


        Bitmap kerr = BitmapFactory.decodeResource(getResources(), R.raw.kerr_hall);
        Log.i("bitmap", "" + kerr);
        BitmapDescriptor kerr_bs = BitmapDescriptorFactory.fromBitmap(kerr);
        Log.i("bs", "" + kerr_bs);
        LatLng kerrHallPos = new LatLng(34.41455997621988, -119.84687080011797);
        GroundOverlayOptions newark = new GroundOverlayOptions()
                .image(kerr_bs)
                .position(kerrHallPos, 93.8f, 69f);
        mMap.addGroundOverlay(newark);

        Bitmap sh = BitmapFactory.decodeResource(getResources(), R.raw.south_hall);
        Log.i("bitmap", "" + sh);
        BitmapDescriptor sh_bs = BitmapDescriptorFactory.fromBitmap(sh);
        Log.i("bs", "" + sh_bs);
        LatLng southHallPos = new LatLng(34.41369886374294, -119.84712543206453);
        GroundOverlayOptions sh_newark = new GroundOverlayOptions()
                .image(sh_bs)
                .position(southHallPos, 133f, 143f);
        mMap.addGroundOverlay(sh_newark);

        //Zoom in to UCSB campus
        CameraPosition cp = new CameraPosition.Builder()
                .target(northHallPos)
                .zoom(16)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        mMap.setOnMarkerClickListener(this);
        //for testing
        //roomMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(34.41447398728048, -119.8470713943243)));

        //Set my location
        try {
            mMap.setMyLocationEnabled(true);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (currentCameraLongtitude != 0.0) {
            LatLng cameraPosition = new LatLng(currentCameraLatitude, currentCameraLongtitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition));
        }
        //roomMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(34.415370973562936, -119.84701473265886)));
        if (latitudeList != null) {
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(currentPositionLatitude, currentPositionLongtitude);
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "walking");
        }
        //marker = mMap.addMarker(new MarkerOptions().position(new LatLng(34.415370973562936, -119.84701473265886)));
        //createBuildingList();
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public void onMapClick(LatLng var1) {
        //marker.remove();
        Context context = getApplicationContext();
        markerPoints.add(var1);
        //marker = mMap.addMarker(new MarkerOptions().position(var1));
        String s = "";
        for (LatLng latLng : markerPoints) {
            s += latLng.latitude + " " + latLng.longitude + "\n";
        }
        Log.i("string", s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        //getMenuInflater().inflate(R.menu.edit_favourites, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLocationChanged(Location var1) {

    }

    public void createBuildingList() {
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard, "file.txt");

        StringBuilder text = new StringBuilder();
        InputStream is;

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.room_and_buildings);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean createNewBuilding = true;
            Building building = new Building("luring");
            while ((line = reader.readLine()) != null) {
                String[] output;
                output = line.split(" ");
                if (createNewBuilding) {
                    building = new Building(output[0]);
                    createNewBuilding = false;
                }
                Log.i("Line", "" + output);
                if (line.equals("STOP")) {
                    createNewBuilding = true;
                    buildingList.add(building);
                } else {
                    double latitude = Double.parseDouble(output[2]);
                    double longtitude = Double.parseDouble(output[3]);
                    LatLng latLng = new LatLng(latitude, longtitude);
                    Room room = new Room(output[0], output[1], latLng);
                    building.addRoom(room);
                }


            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            //You'll need to add proper error handling here
        }

    }

    public void placeMarkers() {
        for (Building building : buildingList) {
            Log.i("building", "" + building);
            Iterator it = building.getBuilding().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Log.i("key", "" + pair.getKey());
                Log.i("value", "" + pair.getValue());
                LatLng pos = (LatLng) pair.getValue();
                Log.i("pos", "" + pos);
                Log.i("map", "" + mMap);
                mMap.addMarker(new MarkerOptions().position(pos));
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }


    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
        this.directionMode = mode;
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }

    public void handleGetDirectionsResult(ArrayList directionPoints) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        PolylineOptions rectLine;
        if (directionMode.equals("walking")) {
            rectLine = new PolylineOptions().width(8).color(Color.RED);
        } else {
            rectLine = new PolylineOptions().width(8).color(Color.BLUE);

        }
        longtitudeList = new double[directionPoints.size()];
        latitudeList = new double[directionPoints.size()];
        for (int i = 0; i < directionPoints.size(); i++) {
            rectLine.add((LatLng) directionPoints.get(i));
            LatLng point = (LatLng) directionPoints.get(i);
            longtitudeList[i] = point.longitude;
            latitudeList[i] = point.latitude;
        }
        if (directionMode.equals("walking")) {
            newWalkingPolyline = mMap.addPolyline(rectLine);
        } else {
            newDrivingPolyline = mMap.addPolyline(rectLine);
        }
        Location myLocation = mMap.getMyLocation();
        builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        if (directionPoints != null) {
            for (int i = 0; i < directionPoints.size(); i++) {
                LatLng point = (LatLng) directionPoints.get(i);
                builder.include(point);
            }
        }
        builder.include(roomMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void onZoomToMarkersClick(MenuItem item) {
        /*if (landscape){
            listView.setVisibility(View.VISIBLE);
            listItems.add("NH1111 : ");
            adapter.notifyDataSetChanged();
        }*/
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (roomMarker != null && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location myLocation = mMap.getMyLocation();
            builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        } else {
            return;
        }
        builder.include(roomMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void onDirectionWalkClick(MenuItem item) {
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (newWalkingPolyline == null && roomMarker != null && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            currentPositionLongtitude = currentPos.longitude;
            currentPositionLatitude = currentPos.latitude;
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "walking");
        } else if (newWalkingPolyline != null) {
            newWalkingPolyline.remove();
            newWalkingPolyline = null;
        } else if (roomMarker == null){
            String data = "No target are specified";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
            return;
        }

    }

    public void onDirectionDriveClick(MenuItem item) {
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (newDrivingPolyline == null && roomMarker != null && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            currentPositionLongtitude = currentPos.longitude;
            currentPositionLatitude = currentPos.latitude;
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "driveing");
        } else if (newDrivingPolyline != null) {
            newDrivingPolyline.remove();
            newDrivingPolyline = null;
        } else if (roomMarker == null){
        String data = "No target are specified";
        Toast.makeText(this, data,
                Toast.LENGTH_LONG).show();
        return;
    }

    }

    public void onInfoClicked(MenuItem item) {
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        fragmentUpWhenRotationChanged = true;
        fragment = new BlankFragment();
        fragment.show(getFragmentManager(), "Diag");
    }

    public void onFavouritesClicked(MenuItem item) {
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        View menuItemView = findViewById(R.id.favourites);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup_favourites, popup.getMenu());
        popup.show();
    }

    public void onDirectionsClicked(MenuItem item) {
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        View menuItemView = findViewById(R.id.direction);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup_direction, popup.getMenu());
        popup.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] qry = {args.getString("QUERY")};
        switch (id) {
            case DATABASE_LOADER:
                // Returns a new CursorLoader
                return new android.support.v4.content.CursorLoader(
                        getApplicationContext(),   // Parent activity context
                        DatabaseProvider.CONTENT_URI,
                        null,
                        null,     // Projection to return
                        qry,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.i("morro", data.getCount() + "");
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        showPopup(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("roomMarkerLongtitude", roomMarkerLongtitude);
        outState.putDouble("roomMarkerLatitude", roomMarkerLatitude);
        outState.putDouble("currentCameraLongtitude", currentCameraLongtitude);
        outState.putDouble("currentCameraLatitude", currentCameraLatitude);
        outState.putDoubleArray("longtitudeList", longtitudeList);
        outState.putDoubleArray("latitudeList", latitudeList);
        outState.putDouble("currentPositionLongtitude", currentPositionLongtitude);
        outState.putDouble("currentPositionLatitude", currentPositionLatitude);
        outState.putBoolean("fragmentUpWhenRotationChanged", fragmentUpWhenRotationChanged);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        roomMarkerLongtitude = savedInstanceState.getDouble("roomMarkerLongtitude");
        roomMarkerLatitude = savedInstanceState.getDouble("roomMarkerLatitude");
        currentCameraLatitude = savedInstanceState.getDouble("currentCameraLatitude");
        currentCameraLongtitude = savedInstanceState.getDouble("currentCameraLongtitude");
        latitudeList = savedInstanceState.getDoubleArray("latitudeList");
        longtitudeList = savedInstanceState.getDoubleArray("longtitudeList");
        currentPositionLongtitude = savedInstanceState.getDouble("currentPositionLongtitude");
        currentPositionLatitude = savedInstanceState.getDouble("currentPositionLatitude");
        fragmentUpWhenRotationChanged = savedInstanceState.getBoolean("fragmentUpWhenRotationChanged");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraPos != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            cameraPos = null;
        }
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            fragment = new BlankFragment();
            fragment.show(getFragmentManager(), "Diag");
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            currentCameraLatitude = mMap.getCameraPosition().target.latitude;
            currentCameraLongtitude = mMap.getCameraPosition().target.longitude;
        }
        if (fragment != null && fragment.isVisible()) {
            fragment.dismiss();
        }

    }

    public void onClickFragmentOk(View v) {
        fragmentUpWhenRotationChanged = false;
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }

    public void zoomToRoom(LatLng latLng) {
        if (newDrivingPolyline != null) {
            newDrivingPolyline.remove();
            newDrivingPolyline = null;
        }
        if (newWalkingPolyline != null) {
            newWalkingPolyline.remove();
            newWalkingPolyline = null;
        }
        CameraPosition cp = new CameraPosition.Builder()
                .target(latLng)
                .zoom(19.9f)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    public void showPopup(Cursor cursor) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ListView lv = (ListView) popupView.findViewById(R.id.listView);

        //add header
        TextView list_title = new TextView(this);
        list_title.setText(R.string.result_header);
        list_title.setTextSize(20);
        list_title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        list_title.setTextColor(getResources().getColor(R.color.primaryText,getTheme()));
        list_title.setBackgroundColor(getResources().getColor(R.color.colorPrimary,getTheme()));
        lv.addHeaderView(list_title);

        final PopupCursorAdapter pcAdapter = new PopupCursorAdapter(lv.getContext(), cursor);
        lv.setAdapter(pcAdapter);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //LinearLayout lin = (LinearLayout) view;
                //TODO fix layout, remove shortest path if existing
                roomMarker.remove();
                TextView tv = (TextView) view.findViewById(R.id.lvItem);
                String building = "" + tv.getText();
                String latitude = (String) tv.getTag(R.string.lat_tag);
                String longitude = (String) tv.getTag(R.string.long_tag);
                LatLng pos = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                roomMarker = mMap.addMarker(new MarkerOptions().position(pos).title(building));
                zoomToRoom(pos);
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(findViewById(R.id.map_layout),Gravity.CENTER,0,0);
        //popupWindow.showAsDropDown(findViewById(R.id.action_search),0,20, Gravity.CENTER_HORIZONTAL);
        /*
        http://stackoverflow.com/questions/18461990/pop-up-window-to-display-some-stuff-in-a-fragment
        https://guides.codepath.com/android/Populating-a-ListView-with-a-CursorAdapter#attaching-the-adapter-to-a-listview
        */
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.masommer.mapster/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.masommer.mapster/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void onAddFavouriteClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        onMarkerClickRemove = false;
        if (roomMarker != null && !favourites.containsKey(roomMarker.getTitle())) {
            if (roomMarker != null) {
                Log.i("this room has", ""+roomMarker.getTitle());
                favourites.put(roomMarker.getTitle(), roomMarker.getPosition());
                saveToFavourites(roomMarker);
                String data = roomMarker.getTitle() + " is added to favourites";
                Toast.makeText(this, data,
                        Toast.LENGTH_LONG).show();
                roomMarker.remove();
                roomMarker = null;
            }
        }
        else if (roomMarker == null){
            String data = "You need to specify a room first!";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
            return;
        }
        else{
            String data = "Your favourites already consist of the room "+roomMarker.getTitle();
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("favs", "" + favourites);
    }

    public void onShowFavouritesClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        favouritesMarkersList = new ArrayList<Marker>();
        if (favourites.isEmpty()){
            String data = "No favourites to show";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        }
        else{
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            Iterator it = favourites.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                Marker marker = mMap.addMarker(new MarkerOptions().position((LatLng) pair.getValue()).title((String) pair.getKey()));
                favouritesMarkersList.add(marker);
                builder.include(marker.getPosition());

            }
            LatLngBounds bounds = builder.build();
            int padding = 150; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    public void onHideFavouritesClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        if (favouritesMarkersList != null) {
            if (favouritesMarkersList.isEmpty()) {
                //No favourites to hide
            } else {
                for (Marker marker : favouritesMarkersList) {
                    marker.remove();
                }
            }
        }
    }

    public void onEditFavouriteClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        Log.i("favs on edit", "" + favourites);
        if (roomMarker != null){
            roomMarker.remove();
            roomMarker = null;
        }
        favouritesMarkersList = new ArrayList<Marker>();
        mode = EDIT_MODE;
        editToolbar.setVisibility(View.VISIBLE);
        Iterator it = favourites.entrySet().iterator();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Marker marker = mMap.addMarker(new MarkerOptions().position((LatLng) pair.getValue()).title((String) pair.getKey()));
            Log.i("pair", "key: " +pair.getKey().toString() + " value: " + pair.getValue().toString());
            Log.i("marker", ""+marker);
            favouritesMarkersList.add(marker);
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 150; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        String data = "Choose marker to edit or press add button to add room to favourites";
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
    }

    public void onArrowBackClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        editToolbar.setVisibility(View.GONE);
        onMarkerClickRemove = false;
        mode = NORMAL_MODE;
        onHideFavouritesClicked(item);
    }

    public void onDeleteClicked(MenuItem item){
        SearchView sv = (SearchView)findViewById(R.id.action_search);
        sv.clearFocus();
        onMarkerClickRemove = true;
        Log.i("markerToDelete", "" + markerToDelete);
        if (markerToDelete != null){
            markerToDelete.remove();
            favourites.remove(markerToDelete.getTitle());
            favouritesMarkersList.remove(markerToDelete);
            removeMarkerFromMemory(markerToDelete);
            markerToDelete = null;
        }
    }

    public void loadFavourites(){
        Log.i("loading started", "...");
        Context context = this.getApplicationContext();
        favourites = new HashMap<String, LatLng>();
        String filename = "favourites.txt";
        try{
            FileInputStream fis = context.openFileInput(filename);
            Log.i("c", "" + openFileInput(filename).getChannel());
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            try{
                while ((line = bufferedReader.readLine()) != null) {
                    String[] roomInfo = line.split(" ");
                    String roomName = roomInfo[0];
                    double latitude = Double.parseDouble(roomInfo[1]);
                    double longtitude = Double.parseDouble(roomInfo[2]);
                    LatLng latLng = new LatLng(latitude, longtitude);
                    favourites.put(roomName, latLng);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Log.i("markers loaded", "" + favourites);
    }

    public void saveToFavourites(Marker roomMarker){
        Log.i("saving started", "...");
        Context context = this.getApplicationContext();
        String filename = "favourites.txt";
        String content = roomMarker.getTitle() + " " + roomMarker.getPosition().latitude + " "
                + roomMarker.getPosition().longitude + System.getProperty("line.separator");
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("saving finished", "!");
    }

    public void removeMarkerFromMemory(Marker marker){
        Log.i("removing started", "...");
        Context context = this.getApplicationContext();
        String filename = "favourites.txt";
        String tempFileName = "myTempFile.txt";
        File inputFile = new File(filename);
        File tempFile = new File(context.getFilesDir(), tempFileName);
        String sb = "";
        boolean successful = false;
        String lineToRemove = marker.getTitle() + " " + marker.getPosition().latitude + " "
                + marker.getPosition().longitude;
        FileOutputStream outputStream;
        try{
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            outputStream = openFileOutput(tempFileName, Context.MODE_PRIVATE);
            String currentLine;
            Log.i("jass", "kjepp");
            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                Log.i("line", trimmedLine + " line to remove: " + lineToRemove);
                if(trimmedLine.equals(lineToRemove)) {
                    Log.i("found it", "removed");
                    continue;
                }
                sb += currentLine + " ";
                outputStream.write((currentLine + System.getProperty("line.separator")).getBytes());
            }
            outputStream.close();
            fis.close();
            isr.close();
            reader.close();
            Log.i("sb", sb);
            String[] string = sb.split(" ");
            FileOutputStream os;
            os = openFileOutput(filename, Context.MODE_PRIVATE);
            String kuk = "";
            Log.i("string", ""+Arrays.toString(string));
            for (int i = 0; i < string.length; i++) {
                kuk+= string[i] + " ";
                Log.i("kuk", kuk);
                Log.i("i", ""+i);
                if (i%3 == 2){
                    Log.i("rass", "rasshol");
                    kuk+= "\n";
                    os.write(kuk.getBytes());
                    kuk = "";
                }
            }
            os.close();
            tempFile.renameTo(inputFile);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (successful){
            String data = "Marker successfully removed from memory";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        }
        else{
            String data = "Marker failed to remove from memory";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        }
        loadFavourites();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("marker", "clicked, mode:" + mode);
        if (mode == EDIT_MODE){
            Log.i("marker", "marker to be delete placed");
            markerToDelete = marker;
        }
        else if (!onMarkerClickRemove && mode == EDIT_MODE){

        }
        return false;
    }

    public void deleteFile(){
        String path = "/data/user/0/com.example.masommer.mapster/app_favourites.txt";
        String filename = "favourites.txt";
        try {
            File f=new File(getFilesDir(), filename);
            Log.i("path", ""+getDir(filename, Context.MODE_PRIVATE));
            Log.i("file to delete", "" + f);
            boolean delete = f.delete();
            Log.i("deleted", ""+delete);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        loadFavourites();
    }
}

