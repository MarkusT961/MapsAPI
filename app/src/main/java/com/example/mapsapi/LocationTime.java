package com.example.mapsapi;

import android.location.Location;
import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LocationTime {
    private Location location;
    private double time;
    private List<ScanResult> listwifi;
    private LatLng locationgio;
    private LatLng locationkalman;

    public LocationTime(Location location, double time,List<ScanResult> listwifi) {
        this.location = location;
        this.time = time;
        this.listwifi=listwifi;
    }

    public Location getLocation() {
        return location;
    }

    public double getTime() {
        return time;
    }
    public List<ScanResult> getListwifi(){
        return this.listwifi;
    }
    public LatLng getLocationGio(){
        return this.locationgio;
    }
    public LatLng getLocationkalman(){
        return this.locationkalman;
    }

    public void setLocationGio(double lat,double longi){
        LatLng latLng = new LatLng(lat,longi);
        this.locationgio = latLng;
    }
    public void setLocationKalman(double lat,double longi){
        LatLng latLng = new LatLng(lat,longi);
        this.locationkalman = latLng;
    }
}
