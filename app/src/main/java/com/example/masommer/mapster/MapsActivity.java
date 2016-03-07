package com.example.masommer.mapster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
<<<<<<< HEAD
=======
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
>>>>>>> Martin
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

<<<<<<< HEAD
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener {
=======
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMapClickListener, LocationListener {
>>>>>>> Martin

    private GoogleMap mMap;
    private final int MY_PERMISSION_LOCATION_ACCESS = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker at ucsb and move the camera
        LatLng ucsb = new LatLng(34.412573495197854, -119.8470050096512);
        mMap.addMarker(new MarkerOptions().position(ucsb).title("Marker at UCSB"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ucsb));
        mMap.setOnMapClickListener(this);
        //Add north hall to map
        LatLng northHallPos = new LatLng(34.415151360789565, -119.84659027319226);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.raw.north_hall))
                .position(northHallPos, 120f, 90f);
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

        // Associate searchable configuration with the SearchView
        // SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLocationChanged(Location var1){

    }

}
