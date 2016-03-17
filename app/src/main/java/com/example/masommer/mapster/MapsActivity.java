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
import android.location.Location;
import android.location.LocationManager;

import android.app.SearchManager;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ActionMode;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;
    private final int DATABASE_LOADER = 0;
    private final int SUGG_DATABASE_LOADER = 1;
    private Marker roomMarker;
    private LocationManager lm;
    private Polyline newWalkingPolyline;
    private Polyline newDrivingPolyline;
    private InfoFragment fragment;
    private SharedPreferences prefs = null;

    private final int EDIT_MODE = 0;
    private final int NORMAL_MODE = 1;
    private boolean favoritesVisible = false;

    private int mode = NORMAL_MODE;

    private HashMap<String, LatLng> favourites;
    private HashMap<String, Marker> favouritesMarkersList = new HashMap<String, Marker>();


    private String directionMode;


    private boolean setupMap;
    private Menu mMenu;
    private Menu editMenu;
    private ListView listView;
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private double currentCameraLongtitude;
    private double currentCameraLatitude;
    private double roomMarkerLongtitude;
    private double roomMarkerLatitude;

    private double[] longtitudeList;
    private double[] latitudeList;

    private double[] drivingLatitude;
    private double[] drivingLongtitude;
    private double[] walkingLatitude;
    private double[] walkingLongtitude;


    private CameraPosition cameraPos;

    private boolean fragmentUpWhenRotationChanged;

    private android.location.LocationListener locationListener;
    private GoogleApiClient client;
    private Toolbar editToolbar;
    private Marker markerToDelete;

    private ActionMode mActionMode;
    private String roomMarkerTitle;


    private boolean walkingVisible;
    private boolean drivingVisible;
    private boolean searchWhileFavShow;
    private boolean pausedNoRotate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION_ACCESS);
        }
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        loadFavourites();

        if(savedInstanceState==null){
            setupMap=true;
            mapFragment.setRetainInstance(true); //first time oncreate is called
            mapFragment.getMapAsync(this); //set up map
        }else{
            mMap = mapFragment.getMap();
            mMap.setOnMarkerClickListener(this);
            buildMarkersFromFavoriteList();
        }

        if (findViewById(R.id.sampleListView) != null) {
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

        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            fragment = new InfoFragment();
            fragment.show(getFragmentManager(), "Diag");
            fragmentUpWhenRotationChanged = true;
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Log.i("i am her", "looser");
        editToolbar = (Toolbar) findViewById(R.id.toolbar);
        editToolbar.setVisibility(View.GONE);
        editToolbar.setTitleTextColor(Color.WHITE);
        editToolbar.setTitle("Edit favourites");
        editToolbar.inflateMenu(R.menu.edit_favourites);
    }

    public boolean checkAccessFineLocation(){
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //hideFavoritesClicked();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            //roomFromSuggestion(uri);
            Uri uri = intent.getData();
            Bundle args = new Bundle();
            args.putParcelable("URI", uri);
            getSupportLoaderManager().restartLoader(SUGG_DATABASE_LOADER, args, this);
            getSupportLoaderManager().initLoader(SUGG_DATABASE_LOADER, args, this);

        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            Log.i("query", query);
            args.putString("QUERY", query);
            getSupportLoaderManager().restartLoader(DATABASE_LOADER, args, this);
            getSupportLoaderManager().initLoader(DATABASE_LOADER, args, this);
        }
        iconifySearchView();
    }

    private void iconifySearchView(){
        if(mMenu!=null){
            SearchView sv = (SearchView) mMenu.findItem(R.id.action_search).getActionView();
            sv.onActionViewCollapsed();
        }
    }

    private void roomFromSuggestion(Cursor c) {
        c.moveToFirst();
        int idIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_ROOM);
        int latIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_LAT);
        int longIndex = c.getColumnIndexOrThrow(DatabaseTable.COL_LONG);
        double latitude = Double.parseDouble(c.getString(latIndex));
        double longitude = Double.parseDouble(c.getString(longIndex));
        String title = c.getString(idIndex);
        LatLng roomPoint = new LatLng(latitude, longitude);
        if (roomMarker != null) {
            roomMarker.remove();
        }
        roomMarker = mMap.addMarker(new MarkerOptions().position(roomPoint));
        roomMarker.setTitle(title);
        zoomToRoom(roomMarker.getPosition());
    }

    public void setupMapFirstTime(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION_ACCESS);
        }
        boolean permission = checkAccessFineLocation();
        if (permission){
            try {
                mMap.setMyLocationEnabled(true);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 4, locationListener);

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        if (currentCameraLongtitude != 0.0) {
            LatLng cameraPosition = new LatLng(currentCameraLatitude, currentCameraLongtitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition));
        } else {
            //Zoom in to UCSB campus
            CameraPosition cp = new CameraPosition.Builder()
                    .target(new LatLng(34.415135360789565, -119.84668038419226))
                    .zoom(16)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
        }
        mMap.setOnMarkerClickListener(this);
        enableOverlays();
        mMap.setBuildingsEnabled(false);
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        if (setupMap){
            setupMapFirstTime(googleMap);
        }
        buildMarkersFromFavoriteList();

    }

    private void enableOverlays() {
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

        LatLng ellisonPos = new LatLng(34.41556914042667, -119.84526090323925);
        Bitmap ellison = BitmapFactory.decodeResource(getResources(), R.raw.ellison_hall2);
        BitmapDescriptor ellison_bs = BitmapDescriptorFactory.fromBitmap(ellison);
        Log.i("bs", "" + ellison_bs);
        GroundOverlayOptions ellison_opts = new GroundOverlayOptions()
                .image(ellison_bs)
                .position(ellisonPos, 105f, 100f);
        mMap.addGroundOverlay(ellison_opts);

        LatLng buchananPos = new LatLng(34.41541854703484, -119.84456453472376);
        Bitmap buchanan = BitmapFactory.decodeResource(getResources(), R.raw.buchanan);
        BitmapDescriptor buch_bs = BitmapDescriptorFactory.fromBitmap(buchanan);
        Log.i("bs", "" + buch_bs);
        GroundOverlayOptions buch_opts = new GroundOverlayOptions()
                .image(buch_bs)
                .position(buchananPos, 55f, 67f);
        mMap.addGroundOverlay(buch_opts);

        LatLng campbellPos = new LatLng(34.416216843876514, -119.84528464956045);
        Bitmap campbell = BitmapFactory.decodeResource(getResources(), R.raw.campbell_hall2);
        BitmapDescriptor campbell_bs = BitmapDescriptorFactory.fromBitmap(campbell);
        Log.i("bs", "" + campbell_bs);
        GroundOverlayOptions campbell_opts = new GroundOverlayOptions()
                .image(campbell_bs)
                .position(campbellPos, 75f, 75f);
        mMap.addGroundOverlay(campbell_opts);

        LatLng libaryPos = new LatLng(34.413643969752266, -119.84552929899216);
        Bitmap libary = BitmapFactory.decodeResource(getResources(), R.raw.library);
        BitmapDescriptor libary_bs = BitmapDescriptorFactory.fromBitmap(libary);
        Log.i("bs", "" + campbell_bs);
        GroundOverlayOptions libary_opts = new GroundOverlayOptions()
                .image(libary_bs)
                .position(libaryPos, 85f, 175f);
        mMap.addGroundOverlay(libary_opts);


        Bitmap kerr = BitmapFactory.decodeResource(getResources(), R.raw.kerr_hall2);
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
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public void onMapClick(LatLng var1) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName())); //.getActionView()
        mSearchView.setSubmitButtonEnabled(false);
        mMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLocationChanged(Location var1) {

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
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        if (newWalkingPolyline == null && roomMarker != null && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng targetPos = roomMarker.getPosition();
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "walking");
            walkingVisible = true;
        } else if (newWalkingPolyline != null) {
            newWalkingPolyline.remove();
            newWalkingPolyline = null;
            walkingVisible = false;
        } else if (roomMarker == null){
            String data = "No target room specified.";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        }

    }

    public void onDirectionDriveClick(MenuItem item) {
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        if (newDrivingPolyline == null && roomMarker != null && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "driving");
            drivingVisible = true;
        } else if (newDrivingPolyline != null) {
            newDrivingPolyline.remove();
            newDrivingPolyline = null;
            drivingVisible = false;
        } else if (roomMarker == null){
            String data = "No target room specified.";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        }

    }

    public void onInfoClicked(MenuItem item) {
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        fragmentUpWhenRotationChanged = true;
        fragment = new InfoFragment();
        fragment.show(getFragmentManager(), "Diag");
        TextView tv = (TextView) findViewById(R.id.info_text);
        if(tv!=null){
            tv.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    public void onFavouritesClicked(MenuItem item) {
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        View menuItemView = findViewById(R.id.favorites);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup_favourites, popup.getMenu());
        MenuItem toggleFavs = popup.getMenu().findItem(R.id.toggle_favorites);
        if(favoritesVisible){
            toggleFavs.setTitle(R.string.hide_favorites);
        }else{
            toggleFavs.setTitle(R.string.show_favorites);
        }
        popup.show();
    }

    public void onDirectionsClicked(MenuItem item) {
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        View menuItemView = findViewById(R.id.direction);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup_direction, popup.getMenu());
        MenuItem toggleDirWalk = popup.getMenu().findItem(R.id.walking);
        MenuItem toggleDirDrive = popup.getMenu().findItem(R.id.driving);
        if(walkingVisible){
            toggleDirWalk.setTitle(R.string.hide_walking);
        }else{
            toggleDirWalk.setTitle(R.string.show_walking);
        }
        if(drivingVisible){
            toggleDirDrive.setTitle(R.string.hide_driving);
        }else{
            toggleDirDrive.setTitle(R.string.show_driving);
        }
        popup.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DATABASE_LOADER:
                String[] qry = {args.getString("QUERY")};
                // Returns a new CursorLoader
                return new android.support.v4.content.CursorLoader(
                        getApplicationContext(),   // Parent activity context
                        DatabaseProvider.CONTENT_URI,
                        null,
                        null,     // Projection to return
                        qry,            // No selection arguments
                        null             // Default sort order
                );
            case SUGG_DATABASE_LOADER:
                Uri uri = args.getParcelable("URI");
                return new android.support.v4.content.CursorLoader(
                        getApplicationContext(),   // Parent activity context
                        uri,
                        null,
                        null,     // Projection to return
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int cursorID = loader.getId();
        if(favoritesVisible){
            searchWhileFavShow=true;
        }else{
            searchWhileFavShow=false;
        }
        switch (cursorID){
            case DATABASE_LOADER:
                SearchView sv = (SearchView) findViewById(R.id.action_search);
                sv.clearFocus();
                iconifySearchView();
                showPopup(data);
                return;
            case SUGG_DATABASE_LOADER:
                roomFromSuggestion(data);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(fragment!=null){
            fragment.dismiss();
        }
        super.onSaveInstanceState(outState);
        hideFavoritesClicked();
        outState.putBoolean("searchWhileFavShow",searchWhileFavShow);
        outState.putBoolean("fragmentUpWhenRotationChanged", fragmentUpWhenRotationChanged);
        if (newDrivingPolyline != null){
            List<LatLng> drivingList = newDrivingPolyline.getPoints();
            drivingLatitude = new double[drivingList.size()];
            drivingLongtitude = new double[drivingList.size()];
            int i = 0;
            for (LatLng latLng:drivingList) {
                drivingLatitude[i] = latLng.latitude;
                drivingLongtitude[i] = latLng.longitude;
                i++;
            }
            newDrivingPolyline.remove();
            outState.putDoubleArray("drivingLatitude", drivingLatitude);
            outState.putDoubleArray("drivingLongtitude", drivingLongtitude);
        }
        if (newWalkingPolyline != null){
            List<LatLng> walkingList = newWalkingPolyline.getPoints();
            walkingLatitude = new double[walkingList.size()];
            walkingLongtitude= new double[walkingList.size()];
            int i = 0;
            for (LatLng latLng:walkingList) {
                walkingLatitude[i] = latLng.latitude;
                walkingLongtitude[i] = latLng.longitude;
                i++;
            }
            newWalkingPolyline.remove();
            outState.putDoubleArray("walkingLatitude", walkingLatitude);
            outState.putDoubleArray("walkingLongtitude", walkingLongtitude);
        }
        if(roomMarker!=null){
            LatLng roomPos = roomMarker.getPosition();
            outState.putString("roomTitle", roomMarker.getTitle());
            outState.putDouble("roomLat", roomPos.latitude);
            outState.putDouble("roomLong", roomPos.longitude);
            roomMarker.remove();
        }
        if(mode == EDIT_MODE){
            outState.putBoolean("edit_mode", true);
        }
        outState.putBoolean("favoritesVisible", favoritesVisible);
        outState.putBoolean("drivingVisible",drivingVisible);
        outState.putBoolean("walkingVisible",walkingVisible);
        clearMarkers();


    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        pausedNoRotate = true;
        roomMarkerTitle = savedInstanceState.getString("roomTitle");
        roomMarkerLongtitude = savedInstanceState.getDouble("roomLong");
        roomMarkerLatitude = savedInstanceState.getDouble("roomLat");
        searchWhileFavShow=savedInstanceState.getBoolean("searchWhileFavShow", false);
        if(roomMarkerTitle!=null){
            roomMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(roomMarkerLatitude,roomMarkerLongtitude)));
            roomMarker.setTitle(roomMarkerTitle);
        }
        boolean edit_mode = savedInstanceState.getBoolean("edit_mode", false);
        favoritesVisible = savedInstanceState.getBoolean("favoritesVisible", false);

        if(edit_mode){
            mode = EDIT_MODE;
            onEditFavouriteClicked(null);
        }else if(favoritesVisible){
            showFavoritesClicked();
            if(searchWhileFavShow){
                roomMarker.setVisible(true);
            }
        }

        drivingVisible = savedInstanceState.getBoolean("drivingVisible",false);
        walkingVisible= savedInstanceState.getBoolean("walkingVisible", walkingVisible);

        fragmentUpWhenRotationChanged = savedInstanceState.getBoolean("fragmentUpWhenRotationChanged", false);
        if(fragmentUpWhenRotationChanged){
            fragment = new InfoFragment();
            fragment.show(getFragmentManager(), "Diag");
        }

        walkingLongtitude = savedInstanceState.getDoubleArray("walkingLongtitude");
        walkingLatitude = savedInstanceState.getDoubleArray("walkingLatitude");
        drivingLongtitude = savedInstanceState.getDoubleArray("drivingLongtitude");
        drivingLatitude = savedInstanceState.getDoubleArray("drivingLatitude");

        PolylineOptions walkRectLine;
        PolylineOptions driveRectLine;

        if (walkingLatitude != null) {
            Log.i("direction latlist", ""+walkingLatitude.length);
            walkRectLine = new PolylineOptions().width(8).color(Color.RED);
            for (int i = 0; i < walkingLatitude.length; i++) {
                LatLng latLng = new LatLng(walkingLatitude[i], walkingLongtitude[i]);
                walkRectLine.add(latLng);
            }
            newWalkingPolyline = mMap.addPolyline(walkRectLine);
            Log.i("direction poly", ""+newWalkingPolyline);

        }
        if (drivingLatitude != null) {
            driveRectLine = new PolylineOptions().width(8).color(Color.BLUE);
            for (int i = 0; i < drivingLatitude.length; i++) {
                LatLng latLng = new LatLng(drivingLatitude[i], drivingLongtitude[i]);
                driveRectLine.add(latLng);
            }
            newDrivingPolyline = mMap.addPolyline(driveRectLine);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMap!=null){
            hideFavoritesClicked();
            favouritesMarkersList.clear();
            buildMarkersFromFavoriteList(); //markers must be rebuilt or else they will not draw after onPause
            if(roomMarker!=null){
                MarkerOptions roomOpts = new MarkerOptions().position(roomMarker.getPosition()).title(roomMarker.getTitle());
                roomMarker.remove();
                roomMarker=null;
                roomMarker = mMap.addMarker(roomOpts);
            }
            if(favoritesVisible||mode == EDIT_MODE){
                Log.i("onresume", "happened");
                showFavoritesClicked();
                if(roomMarker!=null){
                    if(!favouritesMarkersList.containsKey(roomMarker.getTitle()) && searchWhileFavShow && mode!=EDIT_MODE){
                        roomMarker.setVisible(true);
                    }
                }
            }
            if(mode==EDIT_MODE){
                onEditFavouriteClicked(null);
            }
            if(drivingVisible){
                PolylineOptions polyOpts = new PolylineOptions().width(8).color(Color.BLUE);
                polyOpts.addAll(newDrivingPolyline.getPoints());
                newDrivingPolyline.remove();
                newDrivingPolyline = mMap.addPolyline(polyOpts);
            }
            if(walkingVisible){
                PolylineOptions polyOpts = new PolylineOptions().width(8).color(Color.RED);
                polyOpts.addAll(newWalkingPolyline.getPoints());
                newWalkingPolyline.remove();
                newWalkingPolyline = mMap.addPolyline(polyOpts);
            }
        }

        if (cameraPos != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            cameraPos = null;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            currentCameraLatitude = mMap.getCameraPosition().target.latitude;
            currentCameraLongtitude = mMap.getCameraPosition().target.longitude;
        }
        //if (fragment != null) {
        //    fragment.dismiss();
        //    fragment = null;


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
            drivingVisible=false;
        }
        if (newWalkingPolyline != null) {
            newWalkingPolyline.remove();
            newWalkingPolyline = null;
            walkingVisible=false;
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

        final PopupCursorAdapter pcAdapter = new PopupCursorAdapter(lv.getContext(), cursor);
        lv.setAdapter(pcAdapter);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        popupWindow.setBackgroundDrawable(new ColorDrawable());

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (roomMarker != null) {
                    roomMarker.remove();
                }
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
        popupWindow.showAtLocation(findViewById(R.id.map_layout), Gravity.CENTER, 0, 0);
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


    public void onToggleFavouritesClicked(MenuItem item){
        if(favourites.isEmpty()){
            return;
        }
        if(favoritesVisible){
            hideFavoritesClicked();
        }else{
            showFavoritesClicked();
        }
        iconifySearchView();
        favoritesVisible = !favoritesVisible;
    }

    private void hideFavoritesClicked() {
        if(roomMarker!=null){
            roomMarker.setVisible(true);
        }
        if (favouritesMarkersList != null) {
            if (!favouritesMarkersList.isEmpty()) {
                for (Map.Entry<String, Marker> stringMarkerEntry : favouritesMarkersList.entrySet()) {
                    Marker mrk = stringMarkerEntry.getValue();
                    mrk.setVisible(false);
                }
            }
        }
    }

    private void clearMarkers(){
        if(roomMarker!=null){
            roomMarker.remove();
        }
        if (favouritesMarkersList != null) {
            if (!favouritesMarkersList.isEmpty()) {
                for (Map.Entry<String, Marker> stringMarkerEntry : favouritesMarkersList.entrySet()) {
                    Marker mrk = stringMarkerEntry.getValue();
                    mrk.remove();
                }
            }
        }
    }

    private void showFavoritesClicked(){
        if(roomMarker!=null){
            roomMarker.setVisible(false);
        }
        if (favourites.isEmpty()){
            String data = "No favorites to show";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Object o : favouritesMarkersList.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                Marker m = (Marker) entry.getValue();
                m.setVisible(true);
                builder.include(m.getPosition());
            }
            Log.i("favs", ""+favourites);
            Log.i("favs markers", ""+favouritesMarkersList);

            LatLngBounds bounds = builder.build();
            int padding = 150; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    public boolean enterFavEditMode(){
        if(favoritesVisible){ //exit "show favorites" if in this mode
            favoritesVisible=!favoritesVisible;
        }
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        if(sv!=null){
            sv.clearFocus();
        }
        if (favourites.isEmpty()){
            String data = "You have no favorites";
            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
            mode = NORMAL_MODE;
            return false;
        }
        if (roomMarker != null){
            roomMarker.setVisible(false);
        }
        Log.i("favs on edit", "" + favourites);
        showFavoritesClicked();
        String data = "Choose marker to edit...";
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
        return true;
    }


    public void onEditFavouriteClicked(MenuItem item) {
        mode = EDIT_MODE;
        boolean favsExist = enterFavEditMode();
        if (!favsExist) {
            return;
        }
        if (mActionMode != null) {
            return;
        }
        mActionMode = startActionMode(mActionModeCallback);
    }


    public void buildMarkersFromFavoriteList(){
        for (Object o : favourites.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Marker marker = mMap.addMarker(new MarkerOptions().position((LatLng) pair.getValue()).title((String) pair.getKey())
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            marker.setVisible(false);
            favouritesMarkersList.put(marker.getTitle(), marker);
        }
    }


    public void onAddFavouriteClicked(MenuItem item) {
        SearchView sv = (SearchView) findViewById(R.id.action_search);
        sv.clearFocus();
        if (roomMarker != null && !favourites.containsKey(roomMarker.getTitle())) {
            Log.i("this room has", "" + roomMarker.getTitle());
            favourites.put(roomMarker.getTitle(), roomMarker.getPosition());
            Log.i("# of favs before:", favouritesMarkersList.size() + "");
            Marker newFav = mMap.addMarker(new MarkerOptions().position(roomMarker.getPosition()).title(roomMarker.getTitle())
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            newFav.setVisible(false);
            favouritesMarkersList.put(newFav.getTitle(),newFav);
            Log.i("# of favs after: ",favouritesMarkersList.size()+"");
            saveToFavourites(roomMarker);
            String data = roomMarker.getTitle() + " was added to favorites";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
        } else if (roomMarker == null) {
            String data = "You need to specify a room first!";
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
            return;
        }
        else{
            String data = "Your favorites already contain the room "+roomMarker.getTitle();
            Toast.makeText(this, data,
                    Toast.LENGTH_LONG).show();
            return;
        }
        Log.i("favs", "" + favourites);
    }


    public void loadFavourites() {
        Log.i("loading started", "...");
        Context context = this.getApplicationContext();
        favourites = new HashMap<String, LatLng>();
        String filename = "favourites.txt";
        try {
            FileInputStream fis = context.openFileInput(filename);
            Log.i("c", "" + openFileInput(filename).getChannel());
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();
                    Log.i("line", line);
                    String[] roomInfo = line.split(" ");
                    String roomName = roomInfo[0];
                    double latitude = Double.parseDouble(roomInfo[1]);
                    double longtitude = Double.parseDouble(roomInfo[2]);
                    LatLng latLng = new LatLng(latitude, longtitude);
                    favourites.put(roomName, latLng);
                }
            } catch (IOException e) {
                Log.e("Load failed", "No favorites saved");
            }
        } catch (FileNotFoundException e) {
            Log.e("Load failed", "No favorites saved");
        }
    }

    public void saveToFavourites(Marker roomMarker) {
        Log.i("saving started", "...");
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

    public void removeMarkerFromMemory(Marker marker) {
        Log.i("removing started", "...");
        Context context = this.getApplicationContext();
        String filename = "favourites.txt";
        String tempFileName = "myTempFile.txt";
        File inputFile = new File(filename);
        File tempFile = new File(context.getFilesDir(), tempFileName);
        String sb = "";
        String lineToRemove = marker.getTitle() + " " + marker.getPosition().latitude + " "
                + marker.getPosition().longitude;
        FileOutputStream outputStream;
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            outputStream = openFileOutput(tempFileName, Context.MODE_PRIVATE);
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.equals(lineToRemove)) {
                    continue;
                }
                sb += currentLine + " ";
                outputStream.write((currentLine + System.getProperty("line.separator")).getBytes());
            }
            outputStream.close();
            fis.close();
            isr.close();
            reader.close();
            String[] string = sb.split(" ");
            FileOutputStream os;
            os = openFileOutput(filename, Context.MODE_PRIVATE);
            String kuk = "";
            for (int i = 0; i < string.length; i++) {
                if (i % 3 == 2) {
                    kuk += string[i];
                } else {
                    kuk += string[i] + " ";
                }
                if (i % 3 == 2) {
                    kuk += "\n";
                    os.write(kuk.getBytes());
                    kuk = "";
                }
            }
            os.close();
            tempFile.renameTo(inputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(mode==EDIT_MODE){
            editMenu.findItem(R.id.deleteFake).setVisible(false);
            editMenu.findItem(R.id.delete).setVisible(true);
            markerToDelete = marker;
        }

        return false;
    }

    public void deleteSelectedMarker(String title){
        removeMarkerFromMemory(markerToDelete);
        markerToDelete.remove();
        favourites.remove(title);
        favouritesMarkersList.remove(title);
        editMenu.findItem(R.id.deleteFake).setVisible(true);
        editMenu.findItem(R.id.delete).setVisible(false);
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode action_mode, Menu menu) {
            MenuInflater inflater = action_mode.getMenuInflater();
            inflater.inflate(R.menu.edit_favourites, menu);
            editMenu = menu;
            editMenu.findItem(R.id.deleteFake).setVisible(true);
            editMenu.findItem(R.id.delete).setVisible(false);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteSelectedMarker(markerToDelete.getTitle());
                default:
                    return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode action_mode) {
            hideFavoritesClicked();
            mActionMode = null;
            if (roomMarker != null) {
                roomMarker.setVisible(true);
            }
            mode = NORMAL_MODE;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onMapReady(mMap);
                    if (mMenu != null){
                        mMenu.findItem(R.id.direction).setVisible(true);
                        mMenu.findItem(R.id.zoomToEye).setVisible(true);
                    }

                } else {

                    if (mMenu != null){
                        mMenu.findItem(R.id.direction).setVisible(false);
                        mMenu.findItem(R.id.zoomToEye).setVisible(false);
                    }
                }
            }

        }
    }

}

