package com.example.mapsapi;

import com.google.android.gms.maps.model.LatLng;

public class CalculateCoordinate {
    private static final double EARTHRADIUS = 6366198;


    /**
     * Move a LatLng-Point into a given distance and a given angle (0-360,
     * 0=North).
     */
    public static LatLng moveByDistance(LatLng startGp, double distance,
                                        double angle) {
        /*
         * Calculate the part going to north and the part going to east.
         */
        double arc = Math.toRadians(angle);
        double toNorth = distance * Math.cos(arc);
        double toEast = distance * Math.sin(arc);
        double lonDiff = meterToLongitude(toEast, startGp.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startGp.latitude + latDiff, startGp.longitude
                + lonDiff);
    }

    private static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        double degrees = Math.toDegrees(rad);
        return degrees;
    }

    private static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        double degrees = Math.toDegrees(rad);
        return degrees;
    }

    public static double angleFromCoordinate(double lat1, double long1, double lat2,
                                       double long2) {
        double dLon = (long2 - long1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        //brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

        return brng;
    }
}
