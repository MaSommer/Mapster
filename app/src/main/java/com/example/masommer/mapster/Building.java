package com.example.masommer.mapster;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by MaSommer on 07.03.16.
 */
public class Building {

    private HashMap<String, LatLng> rooms = new HashMap<String, LatLng>();
    private String name;


    public Building(String name){
        this.name = name;
    }

    public HashMap<String, LatLng> getBuilding(){
        return rooms;
    }

    public void addRoom(Room room){
        rooms.put(room.getRoomNumber(), room.getLatLng());
    }

}
