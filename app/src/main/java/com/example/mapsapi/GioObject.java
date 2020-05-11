package com.example.mapsapi;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

class GioObject  implements Parcelable {
    private ArrayList scanResults;
    private Location locationgoogle;
    private double timestamp;
    private LatLng locationreal;
    private LatLng locationkalman;


    protected GioObject(Parcel in) {
        scanResults = in.readArrayList(ScanResult.class.getClassLoader());
        locationgoogle = in.readParcelable(Location.class.getClassLoader());
        timestamp = in.readDouble();
        locationreal = in.readParcelable(LatLng.class.getClassLoader());
        distancefromA = in.readDouble();
        velocityavg = in.readDouble();
        timedt = in.readDouble();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(scanResults);
        dest.writeParcelable(locationgoogle, flags);
        dest.writeDouble(timestamp);
        dest.writeParcelable(locationreal, flags);
        dest.writeDouble(distancefromA);
        dest.writeDouble(velocityavg);
        dest.writeDouble(timedt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GioObject> CREATOR = new Creator<GioObject>() {
        @Override
        public GioObject createFromParcel(Parcel in) {
            return new GioObject(in);
        }

        @Override
        public GioObject[] newArray(int size) {
            return new GioObject[size];
        }
    };

    public double getTimedt() {
        return timedt;
    }

    private double distancefromA;
    private double velocityavg;
    private double timedt;
    private LatLng locationgio;

    public double getDistancefromA() {
        return distancefromA;
    }

    public double getVelocityavg() {
        return velocityavg;
    }

    public GioObject(List<ScanResult> scanResults,LatLng locationkalman,LatLng locationgio, Location locationgoogle, LatLng locationreal, double timestamp,double timedt, double distancefromA, double velocityavg) {
        this.scanResults = (ArrayList<ScanResult>) scanResults;
        this.locationgoogle = locationgoogle;
        this.locationreal = locationreal;
        this.timestamp= timestamp; //Il tempo della locazione di google
        this.distancefromA=distancefromA;
        this.velocityavg=velocityavg;
        this.timedt=timedt; //tempo frazionato
        this.locationgio=locationgio;
        this.locationkalman=locationkalman;
    }

    public LatLng getLocationgio() {
        return locationgio;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public Location getLocationgoogle() {
        return locationgoogle;
    }

    public LatLng getLocationkalman() {
        return locationkalman;
    }

    public LatLng getLocationreal() {
        return locationreal;
    }

    public double getTimestamp() {
        return timestamp;
    }

}
