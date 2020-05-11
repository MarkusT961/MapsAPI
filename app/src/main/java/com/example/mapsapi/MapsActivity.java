package com.example.mapsapi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.apache.commons.math3.filter.KalmanFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import gio.LocalizationGioAisle;
import gio.UniversityCoordinate;


public class MapsActivity extends AppCompatActivity  {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private RecyclerView recyclerview;
    private int bitclick = 0;
    private int bitstarrecording = 0;
    private FusedLocationProviderClient fused = null;
    private LocationRequest request;
    private LocationCallback locationcallback;
    private ModelAdapter modeladapter;
    private TextView textLog;
    private int idmenu = 0;
    private Menu menu;

    private WifiManager wifiManager;
    private ArrayList<GioObject> tempgioobject; //Array temporanea che viene inviato all'activity del grafico, viene settato quando viene aggiunto o cambiato il modeladapterter
    private ArrayList<ArrayList<GioObject>> matrixgioobcts = new ArrayList<>();
    private ArrayList<LocationTime> arraylocation=new ArrayList<>();
    private double time;
    private TextView textdt;
    private LocalizationGioAisle giolocalization;
    private LocalizationGioAisle giolocalizationkalman;
    private ImageView mapview;
    private ImageView imageposition,imagepositiongio,imagepositionkalman;
    private Handler handler = new Handler();
    private float[] lastTouchDownXY = new float[2];
    private ArrayList<LatLng> markerLatLng = new ArrayList<>();
    private int numchild;
    private CoordinatorLayout root;
    private int bitgraph=0;
    private KalmanFilterImp kalmanFilter;
    private double dt =System.currentTimeMillis();
    private double latinz=0,longinz=0;
    private double latitudine, longitudine;
    private double vx,vy;
    private double templat=0, templong;
    private double latigoogle,longgoogle;
    private ArrayList<Double> arraytempi=new ArrayList<>();
    private int bittempo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_maps);
        root = findViewById(R.id.root);
        numchild = root.getChildCount();
        imageposition = findViewById(R.id.position);
        imagepositiongio=findViewById(R.id.positiongio);
        imagepositionkalman=findViewById(R.id.positionkalman);
        mapview = findViewById(R.id.mapView);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.getX();
                    lastTouchDownXY[1] = event.getY();
                }
                // let the touch event pass on to whoever needs it
                return false;
            }
        };
        mapview.setOnTouchListener(touchListener);
        mapview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onMapLongClick(lastTouchDownXY[0],lastTouchDownXY[1]);
                return true;
            }
        });

        recyclerview = findViewById(R.id.recycler);
        recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
        textLog = findViewById(R.id.textLog);
        textdt= findViewById(R.id.texttimedt);
        giolocalization=new LocalizationGioAisle();
        giolocalizationkalman=new LocalizationGioAisle();

        if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permesso negato", Toast.LENGTH_LONG).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            Toast.makeText(getApplicationContext(), "Permesso eseguito", Toast.LENGTH_LONG).show();
            //Per il fusedlocation
            request = new LocationRequest()
                    .setInterval(5000)
                    .setFastestInterval(2000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationcallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    onLocationChanged(locationResult.getLastLocation());
                }
            };
            //ACTIVATE FUSEDLOCATION
            fusedActive();
            //WIFI MANAGER
            wifiManager= (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //INIZIALIZZO IL MODEL ADAPTER
            modeladapter=new ModelAdapter(new ArrayList<GioObject>()); //vuoto
            recyclerview.setAdapter(modeladapter);


        }
    }


    private void onLocationChanged(Location location) {
        synchronized (this) {
            if (kalmanFilter == null) {
                latinz = location.getLatitude();
                longinz = location.getLongitude();
                kalmanFilter = new KalmanFilterImp(location.getLatitude(), location.getLongitude(), 3);
               // Thread th = new Thread(new KalmanThread());
                //th.start();
                dt = System.currentTimeMillis();
                latigoogle = location.getLatitude();
                longgoogle = location.getLongitude();
                return;
            }
            latigoogle = location.getLatitude();
            longgoogle = location.getLongitude();
            float temp = (float) (System.currentTimeMillis() - dt);
            dt = System.currentTimeMillis();
            double[] cord = giolocalization.getCoordinate(location.getLatitude(), location.getLongitude());
            vx = (longgoogle - longinz) / (temp / 1000);
            vy = (latigoogle - latinz) / (temp / 1000);
            latinz = latigoogle;
            longinz = longgoogle;
            Log.d("kalman", dt + " " + vy + " " + vx);
            double[] result = kalmanFilter.correctAndStimate(new double[]{vy,vx},new double[]{latigoogle,longgoogle});
            double[] cordkalman  = giolocalizationkalman.getCoordinate(result[0],result[1]);
            textLog.setText("Accuratezza: " + location.getAccuracy() + "Velocità: " + location.getSpeed());

            float[] res = getCoordinateXY(location.getLatitude(), location.getLongitude());
            imageposition.setY(res[1]);
            imageposition.setX(res[0]);
            float[] resgio = getCoordinateXY(cord[0],cord[1]);
            imagepositiongio.setY(resgio[1]);
            imagepositiongio.setX(resgio[0]);
            float[] reskalman = getCoordinateXY(cordkalman[0],cordkalman[1]);
            imagepositionkalman.setY(reskalman[1]);
            imagepositionkalman.setX(reskalman[0]);
            if (bitstarrecording != 0) {//recording activate
                LocationTime locationTime = new LocationTime(location, System.currentTimeMillis()-time, wifiManager.getScanResults());
                locationTime.setLocationGio(cord[0], cord[1]); //Gio Position
                locationTime.setLocationKalman(cordkalman[0],cordkalman[1]); //Kalman + Gio Position
                arraylocation.add(locationTime);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuinflater = getMenuInflater();
        menuinflater.inflate(R.menu.first, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                if(bitstarrecording==1){//sto registrando quindi stoppo la registrazione
                    addPosition(findViewById(R.id.root));
                 }
                AlertDialog.Builder alert = new AlertDialog.Builder(this)
                        .setTitle("Cancellazione totale dei risultati")
                        .setMessage("Sei sicuro di voler cancellare tutti i dati?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Elimino tutti gli elementi dell'adapter
                                menu.removeGroup(1);
                               matrixgioobcts.clear();
                               idmenu=0;
                               modeladapter.clearUpdate();
                               //elimino tutti i marker
                            }
                        })
                        .setNegativeButton("No", null);
                alert.show();
                break;
            case R.id.graph:
                if(tempgioobject== null)
                    break;
                showDialog();
               break;
            default:
                tempgioobject=matrixgioobcts.get(item.getItemId());
                modeladapter = new ModelAdapter(tempgioobject);
                recyclerview.setAdapter(modeladapter);
                modeladapter.notifyDataSetChanged();
                for(GioObject gioObject : tempgioobject){
                    TextView imageView = new TextView(this);
                    //imageView.setBackground(shape);
                    float[] res = getCoordinateXY(gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude);
                    imageView.setX(res[0]);
                    imageView.setY(res[1]);
                    imageView.setText("  ");
                    imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.circlereal));
                    TextView imageView1 = new TextView(this);
                    //imageView.setBackground(shape);
                    res = getCoordinateXY(gioObject.getLocationgoogle().getLatitude(),gioObject.getLocationgoogle().getLongitude());
                    imageView1.setX(res[0]);
                    imageView1.setY(res[1]);
                    imageView1.setBackground(ContextCompat.getDrawable(this,R.drawable.circle));
                    imageView1.setText("  ");

                    TextView imageView2 = new TextView(this);
                    res = getCoordinateXY(gioObject.getLocationgio().latitude,gioObject.getLocationgio().longitude);
                    imageView2.setX(res[0]);
                    imageView2.setY(res[1]);
                    imageView2.setText("  ");
                    imageView2.setBackground(ContextCompat.getDrawable(this,R.drawable.circlegoogle));

                    TextView imageView3 = new TextView(this);
                    res = getCoordinateXY(gioObject.getLocationkalman().latitude,gioObject.getLocationkalman().longitude);
                    imageView3.setX(res[0]);
                    imageView3.setY(res[1]);
                    imageView3.setText("  ");
                    imageView3.setBackground(ContextCompat.getDrawable(this,R.drawable.circlekalman));


                    root.addView(imageView);
                    root.addView(imageView1);
                    root.addView(imageView2);
                    root.addView(imageView3);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void onMapLongClick(float x, float y) {
        TextView first = new TextView(getApplicationContext());
        first.setY(y+mapview.getTop());
        first.setX(x+mapview.getLeft());
        first.setTextSize(20);
        double[] res;
        switch (markerLatLng.size()){
            case 0:
                first.setText("A");
                root.addView(first);
                 res = getCoordinateMap(x,y);
                 Log.d("Coordinate A",res[0] + " "+res[1]);
                markerLatLng.add(new LatLng(res[0],res[1]));
                break;
            case 1:
                first.setText("B");
                root.addView(first);
                res = getCoordinateMap(x,y);
                markerLatLng.add(new LatLng(res[0],res[1]));
                break;
            case 2:
                first.setText("C");
                root.addView(first);
                res = getCoordinateMap(x,y);
                markerLatLng.add(new LatLng(res[0],res[1]));
                break;
            case 3:
                first.setText("D");
                root.addView(first);
                res = getCoordinateMap(x,y);
                markerLatLng.add(new LatLng(res[0],res[1]));
                break;

                default:
                    new AlertDialog.Builder(this)
                            .setTitle("Errore")
                            .setMessage("Segnaposti gia inseriti").show();
        }
    }

    public void showButton(View view) {
        if (bitclick == 0) {//non è stato cliccato
            final FloatingActionButton btnfirst = findViewById(R.id.floatingfused);
            final FloatingActionButton btnsecond  = findViewById(R.id.floatinggps);
            btnfirst.show();
            btnsecond.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnfirst.hide();
                    btnsecond.hide();
                    bitclick=0;
                }
            },5000);
            bitclick++;
        }
    }

    public void fusedActive() {
        if (fused == null) {
            fused = LocationServices.getFusedLocationProviderClient(this);
            fused.requestLocationUpdates(request, locationcallback, null);

            View root = findViewById(R.id.root);
            Snackbar.make(root, "FusedLocation Activated", Snackbar.LENGTH_LONG).show();
        }
    }


    public void addPosition(View view) {
        if(markerLatLng.size()<2){
            new AlertDialog.Builder(this).setTitle("ERRORE")
                    .setMessage("Devi prima inserire i segnaposti").show();
            return;
        }
        if(textdt.getText().toString().equals("")){
            new AlertDialog.Builder(this).setTitle("ERRORE")
                    .setMessage("Devi prima inserire il dt").show();
            return;
        }
        if (bitstarrecording == 0) {
            bitstarrecording++;
            bittempo = 0;
            Snackbar.make(root, "START Recording", Snackbar.LENGTH_LONG).show();
            menu.add(1, idmenu, 0, "Prova: " + (idmenu + 1)); //GroupId = 0, così da eliminarli facilmente
            tempgioobject = new ArrayList<GioObject>();
            matrixgioobcts.add(tempgioobject);
            idmenu++;
            arraylocation.clear();
            time = System.currentTimeMillis();
        }
        else {
            if(bittempo != markerLatLng.size()-2){
                bittempo++;
                arraytempi.add(System.currentTimeMillis()-time);
                return;
            }
            double endTime= System.currentTimeMillis()-time;
            arraytempi.add(endTime);
            bitstarrecording = 0;
            Snackbar.make(root,"STOP Recording",Snackbar.LENGTH_LONG).show();
            AsyncTask<Void, Integer, Boolean> task = new DistanceCalculateTask(mapview,getApplicationContext(),root,markerLatLng, arraylocation, arraytempi, Integer.valueOf(textdt.getText().toString()), matrixgioobcts.get(matrixgioobcts.size() - 1), modeladapter).execute();
        }
    }

    public void expandCard(View view) {
        TextView text = view.findViewById(R.id.textpand);
        Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.textanim);
        view.setAnimation(anim);
        if(text.getVisibility()==View.GONE)
            text.setVisibility(View.VISIBLE);
        else {
            text.setVisibility(View.GONE);
            modeladapter.notifyDataSetChanged();
        }
    }


    public void removeAllMarker(View view) {
        for(int i = 8 ; i<root.getChildCount();i++) {
            root.removeViewAt(i);
            i--;
        }
        markerLatLng.clear();

    }

    public void printResult(View view) {
        if(matrixgioobcts.isEmpty()){
            Toast.makeText(this,"Non ci sono prove",Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salvataggio dati esperimento");
        builder.setMessage("Vuoi salvare tutti i dati dell'esperimento?");
        builder.setNegativeButton("No",null);
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String currentDate = "Total"+new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())+System.currentTimeMillis();
                FileOutputStream out=null;
                try {
                    out = openFileOutput(currentDate,MODE_PRIVATE);
                    int j=0;
                    for(ArrayList<GioObject> gioObjects : matrixgioobcts){
                        j++;
                        String text = "Prova: "+ j + "\n";
                        out.write(text.getBytes());
                        for(GioObject gio : gioObjects){
                            String body =  "R " + gio.getLocationreal().latitude + "  " + gio.getLocationreal().longitude + "\n"
                                    + "G " + gio.getLocationgoogle().getLatitude() + "  " + gio.getLocationgoogle().getLongitude() + "\n"
                                    + "Gio " + gio.getLocationgio().latitude + "  "+gio.getLocationgio().longitude+"\n"
                                    + "K " + gio.getLocationgio().latitude + "  "+gio.getLocationgio().longitude+"\n"
                                    + "T "+gio.getTimestamp()+"\n"
                                    + "V "+gio.getVelocityavg()+"\n"
                                    + "I "+ markerLatLng.get(0).latitude+"  "+markerLatLng.get(0).longitude +"\n"
                                    + "dt " + gio.getTimedt()+"\n";
                        out.write(body.getBytes());
                        }
                        out.write("############################\n".getBytes());
                    }
                    out.flush();
                    out.close();
                    Toast.makeText(getApplicationContext(),"Prova salvata in ",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.create().show();
    }



    private double[] getCoordinateMap(float x, float y){
        //PROPORZIONE X(double: lat) : X( pixel) = Y(double: Lat) : Y (pixel)
        // Y(double: lat) = distanza lattudine * distanzanew pixel) / distanza pixel

        double distanceLatA = UniversityCoordinate.NORDOVEST[0]-UniversityCoordinate.SUDOVEST[0];
        double distanceLongA = UniversityCoordinate.NORDEST[1] - UniversityCoordinate.NORDOVEST[1];
        float distanceLatinPixel = mapview.getHeight();
        float distanceLonginPixel = mapview.getWidth();

        double newlat = (distanceLatA * y)/distanceLatinPixel;
        double newlong = (distanceLongA* x)/distanceLonginPixel;
        Log.d("GIOLOC","DistanceLongA "+distanceLongA + " DistanceLongPixel "+ distanceLonginPixel + "Distancenew Pixel:"+x);
        double[] res ={UniversityCoordinate.NORDOVEST[0]-newlat,newlong+UniversityCoordinate.NORDOVEST[1]};
        return res;
    }

    private float[] getCoordinateXY(double lat,double longi){
        double distanceLatA = UniversityCoordinate.NORDOVEST[0]-UniversityCoordinate.SUDOVEST[0];
        double distanceLongA = UniversityCoordinate.NORDEST[1] - UniversityCoordinate.NORDOVEST[1];
        float distanceLatinPixel = mapview.getHeight();
        float distanceLonginPixel = mapview.getWidth();
        double latdifference = UniversityCoordinate.NORDOVEST[0]-lat;
        double longdifference = longi-UniversityCoordinate.NORDOVEST[1];
        float ypixel = (float) (((distanceLatinPixel * latdifference)/distanceLatA) + mapview.getTop());
        float xpixel = (float) (((distanceLonginPixel * longdifference)/distanceLongA) + mapview.getLeft());
        float[] res = {xpixel,ypixel};
        return res;
    }

    private void showDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Scegli")
                .setMessage("Scegli quale grafico vuoi visualizzare")
                .setPositiveButton("Google", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent  = new Intent(getApplicationContext(),GraphActivity.class);
                        ArrayList<LocationGraph> arrlocationgraph = new ArrayList<>();
                        for(GioObject gioObject : tempgioobject ){
                            if(bitgraph==0)
                                arrlocationgraph.add(new LocationGraph(gioObject.getLocationgoogle().getLatitude(),gioObject.getLocationgoogle().getLongitude(),gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude,gioObject.getTimedt()));
                        }
                        intent.putExtra("Value",arrlocationgraph)
                        .putExtra("Bit",0);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("GioLayer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent  = new Intent(getApplicationContext(),GraphActivity.class);
                        ArrayList<LocationGraph> arrlocationgraph = new ArrayList<>();
                        for(GioObject gioObject : tempgioobject ){
                            arrlocationgraph.add(new LocationGraph(gioObject.getLocationgio().latitude,gioObject.getLocationgio().longitude,gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude,gioObject.getTimedt()));
                        }
                        intent.putExtra("Value",arrlocationgraph)
                        .putExtra("Bit",1);
                        startActivity(intent);
                    }
                })
                .setNeutralButton("Kalman + GioLayer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent  = new Intent(getApplicationContext(),GraphActivity.class);
                        ArrayList<LocationGraph> arrlocationgraph = new ArrayList<>();
                        for(GioObject gioObject : tempgioobject ){
                            arrlocationgraph.add(new LocationGraph(gioObject.getLocationkalman().latitude,gioObject.getLocationkalman().longitude,gioObject.getLocationreal().latitude,gioObject.getLocationreal().longitude,gioObject.getTimedt()));
                        }
                        intent.putExtra("Value",arrlocationgraph)
                                .putExtra("Bit",2);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

    /*public class KalmanThread implements Runnable {

        @Override
        public void run() {
            while(true) {
                synchronized (this) {
                    double[] filterresult = kalmanFilter.correctAndStimate(new double[]{vy, vx}, new double[]{latitudine, longitudine});
                    double[] cord = giolocalization.getCoordinate(Math.ceil(filterresult[0] * 10000000) / 10000000, Math.ceil(filterresult[1] * 10000000) / 10000000);
                    float[] res1 = getCoordinateXY(cord[0], cord[1]);
                    imagepositiongio.setY(res1[1]);
                    imagepositiongio.setX(res1[0]);
                    if (bitstarrecording != 0) {//recording activated
                        Location location = new Location("google");
                        location.setLatitude(latigoogle);
                        location.setLongitude(longgoogle);
                        LocationTime locationTime = new LocationTime(location, System.currentTimeMillis() - time, wifiManager.getScanResults());
                        locationTime.setLocationGio(cord[0], cord[1]);
                        arraylocation.add(locationTime);
                    }
                }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }
    }*/
}
