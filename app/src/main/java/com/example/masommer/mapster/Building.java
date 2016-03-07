package com.example.masommer.mapster;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by MaSommer on 07.03.16.
 */
public class Building {

    private HashMap<String, LatLng> building = new HashMap<String, LatLng>();


    public Building(HashMap<String, LatLng> building){
        this.building = building;
    }

    public HashMap<String, LatLng> getBuilding(){
        return building;
    }

}
