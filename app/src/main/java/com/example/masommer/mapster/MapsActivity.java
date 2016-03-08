package com.example.masommer.mapster;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import android.app.SearchManager;

import android.net.Uri;
import android.os.AsyncTask;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener {


    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;
    private Marker roomMarker;
    private LocationManager lm;
    private String provider;
    private Marker marker;
    private boolean landscape;

    private ListView listView;
    private ArrayList<Building> buildingList = new ArrayList<Building>();
    private ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    private ArrayList<String> listItems=new ArrayList<String>();
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

            adapter=new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    listItems);
            listView.setAdapter(adapter);
        }



    }

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
        mMap.setOnMapClickListener(this);
        //Add north hall to map
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
                .position(southHallPos,133f, 143f);
        mMap.addGroundOverlay(sh_newark);

        //Zoom in to UCSB campus
        CameraPosition cp = new CameraPosition.Builder()
                .target(northHallPos)
                .zoom(16)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        //Set my location
        try {
            mMap.setMyLocationEnabled(true);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (currentCameraLongtitude != 0.0){
            LatLng cameraPosition = new LatLng(currentCameraLatitude, currentCameraLongtitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition));
        }
        //roomMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(34.415370973562936, -119.84701473265886)));
        if (latitudeList != null){
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(currentPositionLatitude, currentPositionLongtitude);
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "walking");            /*ArrayList<LatLng> directionPoints = new ArrayList<LatLng>();
            for (int i = 0; i < directionPoints.size(); i++) {
                LatLng point = new LatLng(latitudeList[i], longtitudeList[i]);
                directionPoints.add(point);
            }
            Polyline newPolyline;
            GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.RED);
            for (int i = 0; i < directionPoints.size(); i++) {
                rectLine.add((LatLng) directionPoints.get(i));
            }
            newPolyline = mMap.addPolyline(rectLine);*/
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
        for (LatLng latLng: markerPoints) {
            s+=latLng.latitude + " " + latLng.longitude + "\n";
        }
        Log.i("string", s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
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
        Polyline newPolyline;
        GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.RED);
        longtitudeList = new double[directionPoints.size()];
        latitudeList = new double[directionPoints.size()];
        for (int i = 0; i < directionPoints.size(); i++) {
            rectLine.add((LatLng) directionPoints.get(i));
            LatLng point = (LatLng) directionPoints.get(i);
            longtitudeList[i] = point.longitude;
            latitudeList[i] = point.latitude;
        }
        newPolyline = mMap.addPolyline(rectLine);
    }

    public void onZoomToMarkersClick(MenuItem item) {
        if (landscape){
            listView.setVisibility(View.VISIBLE);
            listItems.add("NH1111 : ");
            adapter.notifyDataSetChanged();
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (roomMarker != null && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location myLocation = lm.getLastKnownLocation(provider);
            builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        } else {
            return;
        }
        builder.include(roomMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void onDirectionClick(MenuItem item){
        if (roomMarker != null && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)){
            Location location = mMap.getMyLocation();
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng targetPos = new LatLng(roomMarker.getPosition().latitude, roomMarker.getPosition().longitude);
            currentPositionLongtitude = currentPos.longitude;
            currentPositionLatitude = currentPos.latitude;
            findDirections(currentPos.latitude, currentPos.longitude, targetPos.latitude, targetPos.longitude, "walking");
        }
        else{
            return;
        }

    }

    @Override
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
    }

    @Override
    public void onResume(){
        super.onResume();
        if (cameraPos != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            cameraPos = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mMap != null){
            currentCameraLatitude = mMap.getCameraPosition().target.latitude;
            currentCameraLongtitude = mMap.getCameraPosition().target.longitude;
        }

    }
}

