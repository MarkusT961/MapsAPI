package com.example.mapsapi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import gio.UniversityCoordinate;

public class DistanceCalculateTask extends AsyncTask<Void,Integer,Boolean> {
    private ArrayList<LatLng> markerarray;
    private final ArrayList<LocationTime> location;
    private final Context context;
    private ArrayList<Double> time;
    private ArrayList<GioObject> gioObjects;
    private double dt;
    private ModelAdapter modelAdapter;
    private ViewGroup root;
    private ImageView mapview;
    public DistanceCalculateTask(ImageView mapView,Context context, ViewGroup root, ArrayList<LatLng> markerarray, ArrayList<LocationTime> location, ArrayList<Double> time, float dt, ArrayList<GioObject> gioObjects, ModelAdapter modelAdapter){
        this.location = location;
        this.time = time;
        this.dt=dt;
        this.mapview=mapView;
        this.gioObjects=gioObjects;
        this.modelAdapter=modelAdapter;
        this.root = root;
        this.context = context;
        this.markerarray=markerarray;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        if(location.size()>0) { //almeno una posizione la devo aver presa
            int index =0;
            LatLng start = markerarray.get(index);
            LatLng end = markerarray.get(index+1);
            Location startloc = new Location("Start");
            startloc.setLatitude(start.latitude);
            startloc.setLongitude(start.longitude);
            Location endloc = new Location("end");
            endloc.setLatitude(end.latitude);
            endloc.setLongitude(end.longitude);
            double distancereal = startloc.distanceTo(endloc);
            double bearing = CalculateCoordinate.angleFromCoordinate(start.latitude, start.longitude,
                    end.latitude, end.longitude);
                //CALCOLO VELOCITA MEDIA
                double velocityavg = (distancereal / (time.get(index) / 1000));
                double timetemp = 0;
            for(LocationTime loc : location){
                if(loc.getTime()>time.get(index)){
                    index++;
                     start = markerarray.get(index);
                     end = markerarray.get(index+1);
                    startloc = new Location("Start");
                    startloc.setLatitude(start.latitude);
                    startloc.setLongitude(start.longitude);
                     endloc = new Location("end");
                    endloc.setLatitude(end.latitude);
                    endloc.setLongitude(end.longitude);
                    distancereal = startloc.distanceTo(endloc);
                    bearing = CalculateCoordinate.angleFromCoordinate(start.latitude, start.longitude,
                            end.latitude, end.longitude);
                    velocityavg = (distancereal / (time.get(index) / 1000));
                    timetemp = time.get(index-1);
                }
                double distancefromA = (velocityavg * ((loc.getTime()-timetemp )/ 1000));
                //AGGIUNGO ALL'array della matrice nell activity
                LatLng real = CalculateCoordinate.moveByDistance(new LatLng(start.latitude, start.longitude), distancefromA, bearing);
                LatLng realposition = new LatLng(Math.ceil(real.latitude * 10000000) / 10000000, Math.ceil(real.longitude * 10000000) / 10000000);
                gioObjects.add(new GioObject(loc.getListwifi(), loc.getLocationkalman(), loc.getLocationGio(), loc.getLocation(), realposition, loc.getTime(), loc.getTime(), distancefromA, velocityavg));
            }

        }
        Log.d("Size",location.size() +" "+ gioObjects.size());
            return true;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        ArrayList<LocationGraph> coordinategraph= new ArrayList<>();
        modelAdapter.SetUpdate(gioObjects);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.WHITE);
        shape.setStroke(2, Color.BLACK);
        for(GioObject gioObject : gioObjects){
             TextView imageView = new TextView(context);
             float[] res = getCoordinateXY(gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude);
             imageView.setX(res[0]);
             imageView.setY(res[1]);
             imageView.setText("  ");
             imageView.setBackground(ContextCompat.getDrawable(context,R.drawable.circlereal));
             TextView imageView1 = new TextView(context);
            TextView imageView2 = new TextView(context);
            TextView imageView3 = new TextView(context);

            res = getCoordinateXY(gioObject.getLocationgio().latitude,gioObject.getLocationgio().longitude);
            imageView1.setX(res[0]);
            imageView1.setY(res[1]);
            imageView1.setText("  ");
            imageView1.setBackground(ContextCompat.getDrawable(context,R.drawable.circlegoogle));


            res = getCoordinateXY(gioObject.getLocationgoogle().getLatitude(),gioObject.getLocationgoogle().getLongitude());
            imageView2.setX(res[0]);
            imageView2.setY(res[1]);
            imageView2.setText("  ");
            imageView2.setBackground(ContextCompat.getDrawable(context,R.drawable.circle));

            res = getCoordinateXY(gioObject.getLocationkalman().latitude,gioObject.getLocationkalman().longitude);
            imageView3.setX(res[0]);
            imageView3.setY(res[1]);
            imageView3.setText("  ");
            imageView3.setBackground(ContextCompat.getDrawable(context,R.drawable.circlekalman));

            root.addView(imageView);
            root.addView(imageView1);
            root.addView(imageView2);
            root.addView(imageView3);
            coordinategraph.add(new LocationGraph(gioObject.getLocationgoogle().getLatitude(),gioObject.getLocationgoogle().getLongitude(),gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude,gioObject.getTimedt()));
        }
        Intent i = new Intent(context, GraphActivity.class);
        i.putExtra("Value",coordinategraph)
            .putExtra("Bit",0);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }


    private float[] getCoordinateXY(double lat,double longi){
        double distanceLatA = UniversityCoordinate.NORDOVEST[0]-UniversityCoordinate.SUDOVEST[0];
        double distanceLongA = UniversityCoordinate.NORDEST[1] - UniversityCoordinate.NORDOVEST[1];
        float distanceLatinPixel = mapview.getHeight();
        float distanceLonginPixel = mapview.getWidth();
        double latdifference = UniversityCoordinate.NORDOVEST[0]-lat;
        double longdifference = longi-UniversityCoordinate.NORDOVEST[1];
        Log.d("MAPVIEW",mapview.getTop() + " " +mapview.getLeft());
        float ypixel = (float) (((distanceLatinPixel * latdifference)/distanceLatA) + mapview.getTop());
        float xpixel = (float) (((distanceLonginPixel * longdifference)/distanceLongA) + mapview.getLeft());
        float[] res = {xpixel,ypixel};
        return res;
    }
}
