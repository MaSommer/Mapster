package com.example.masommer.mapster;

import android.Manifest;
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
import android.widget.SearchView;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener {


    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;
    private Marker roomMarker;
    private LocationManager lm;
    private String provider;

    private ArrayList<Building> buildingList = new ArrayList<Building>();



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
        //createBuildingList();
        //placeMarkers();

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
        Log.i("bitmap", ""+bitmap);
        BitmapDescriptor bs = BitmapDescriptorFactory.fromBitmap(bitmap);
        Log.i("bs", "" + bs);

        LatLng northHallPos = new LatLng(34.415135360789565, -119.84668038419226);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bs)
                .position(northHallPos, 131f, 99f);
        mMap.addGroundOverlay(newarkMap);


        Bitmap kerr = BitmapFactory.decodeResource(getResources(), R.raw.kerr_hall);
        Log.i("bitmap", ""+kerr);
        BitmapDescriptor kerr_bs = BitmapDescriptorFactory.fromBitmap(kerr);
        Log.i("bs", "" + kerr_bs);
        LatLng kerrHallPos = new LatLng(34.41455997621988,-119.84687080011797);
        GroundOverlayOptions newark = new GroundOverlayOptions()
                .image(kerr_bs)
                .position(kerrHallPos, 93.8f, 69f);
        mMap.addGroundOverlay(newark);

        //Zoom in to UCSB campus
        CameraPosition cp = new CameraPosition.Builder()
                .target(northHallPos)
                .zoom(20)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        //Set my location
        try{
            mMap.setMyLocationEnabled(true);

        }
        catch (SecurityException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public void onMapClick(LatLng var1) {
        Log.i("lat", "" + var1.latitude);
        Log.i("long", "" + var1.longitude);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLocationChanged(Location var1){

    }

    public void createBuildingList(){
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard,"file.txt");

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
                if (createNewBuilding){
                    building = new Building(output[0]);
                    createNewBuilding = false;
                }
                if (line.isEmpty()){
                    createNewBuilding = true;
                    buildingList.add(building);
                }
                else{
                    double latitude = Double.parseDouble(output[2]);
                    double longtitude = Double.parseDouble(output[3]);
                    LatLng latLng = new LatLng(latitude, longtitude);
                    Room room = new Room(output[0], output[1], latLng);
                    building.addRoom(room);
                }


            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            //You'll need to add proper error handling here
        }

    }

    public void placeMarkers(){
        for (Building building : buildingList) {
            Iterator it = building.getBuilding().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Log.i("key", ""+pair.getKey());
                Log.i("value", ""+pair.getValue());
                LatLng sydney = (LatLng) pair.getValue();
                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at" + pair.getKey()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    public void onZoomToMarkersClick(MenuItem item) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(roomMarker!=null && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

                Location myLocation = lm.getLastKnownLocation(provider);
                builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            }else{
                return;
            }
        builder.include(roomMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }
}
