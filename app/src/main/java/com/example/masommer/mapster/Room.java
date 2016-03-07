package com.example.masommer.mapster;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MaSommer on 07.03.16.
 */
public class Room {

    private String roomNumber;
    private String building;
    private LatLng latLng;


    public Room(String building, String roomNumber, LatLng pos){
        this.roomNumber = roomNumber;
        this.building = building;
        this.latLng = latLng;
    }


    public String getRoomNumber(){
        return roomNumber;
    }

    public LatLng getLatLng(){
        return latLng;
    }


}
