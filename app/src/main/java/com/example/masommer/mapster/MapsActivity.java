package com.example.masommer.mapster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener {


    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;

    private ArrayList<Building> buildingList = new ArrayList<Building>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //createBuildingList();

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
        Log.i("bs", ""+bs);

        LatLng northHallPos = new LatLng(34.415130260789565, -119.84671038419226);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bs)
                .position(northHallPos, 130f, 95f);
        mMap.addGroundOverlay(newarkMap);
        //Zoom in to UCSB campus
        CameraPosition cp = new CameraPosition.Builder()
                .target(northHallPos)
                .zoom(15)
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

}
