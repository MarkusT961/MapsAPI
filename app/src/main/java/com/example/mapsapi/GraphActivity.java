package com.example.mapsapi;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {
private GraphView graphViewLat;
    private ArrayList<LocationGraph> locationgraphs;
    private GraphView graphViewLong;
    private TextView textView;
    private double thirdpercmetri;
    private double novantpercidista;
    private int bit; //Se == 0 Grafici di Google se == 1 Grafici con layer gio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        textView = findViewById(R.id.textView);
        graphViewLat = findViewById(R.id.graphlat);
        graphViewLat.getViewport().setScalable(true);
        graphViewLat.getViewport().setScrollable(true);
        graphViewLat.getViewport().setScalableY(true);
        graphViewLat.getViewport().setScrollableY(true);
        graphViewLong = findViewById(R.id.graphlong);
        graphViewLong.getViewport().setScalable(true);
        graphViewLong.getViewport().setScrollable(true);
        graphViewLong.getViewport().setScalableY(true);
        graphViewLong.getViewport().setScrollableY(true);
        locationgraphs= (ArrayList<LocationGraph>) getIntent().getSerializableExtra("Value");
        TextView texttitle = findViewById(R.id.textTitle);
        bit = getIntent().getIntExtra("Bit",0);
        if(bit == 0)
            texttitle.setText("Dati Google");
        if(bit == 1)
            texttitle.setText("Dati GioLayer");
        if(bit == 2)
            texttitle.setText("Dati Kalman + Giolayer");
      new ErrorTask().execute(locationgraphs);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void saveExperiemnt(View view) throws IOException {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        FileOutputStream out = openFileOutput(currentDate+ "d"+System.currentTimeMillis(),MODE_PRIVATE);
        String body = "EXPERIMENT";
        if(bit == 0)
            body = body + " CON GOOGLE\n";
        if(bit == 1)
            body = body + " CON GIOLAYER\n";
        if(bit == 2)
            body = body + " CON KALMAN + GIOLAYER\n";
        out.write(body.getBytes());
        for(LocationGraph locationGraph : locationgraphs){
            if(bit == 0) {
                body = body + "Lat Real : " + locationGraph.getLatreal() + " Long Real: " + locationGraph.getLongreal() + "\n"
                        + "Lat Google: " + locationGraph.getLatgoogle() + " Long Google: " + locationGraph.getDlonggoogle() +
                        "\n\n";
            }
            else {
                body = body + "Lat Real : " + locationGraph.getLatreal() + " Long Real: " + locationGraph.getLongreal() + "\n"
                        + "Lat gio: " + locationGraph.getLatgoogle() + " Long gio: " + locationGraph.getDlonggoogle() +
                        "\n\n";
            }

        }
        out.write((body + "\n 3° Quartile: "+thirdpercmetri + " 90° Percittile: "+novantpercidista).getBytes());
        Toast.makeText(this,"Esperimento Salvato",Toast.LENGTH_LONG).show();
    }

    private class ErrorTask extends AsyncTask<ArrayList<LocationGraph>,Void,Void> {

        @Override
        protected Void doInBackground(ArrayList<LocationGraph>... arrayLists) {
            ArrayList<LocationGraph> locationGraphs = arrayLists[0];
            ArrayList<Double> errLat = new ArrayList<>();
            ArrayList<Double> errLong = new ArrayList<>();
            ArrayList<Float> errdistance = new ArrayList<>();
            for(int i  = 0 ; i< locationGraphs.size();i++){
                errLat.add(Math.abs(locationGraphs.get(i).getLatgoogle()-locationGraphs.get(i).getLatreal()));
                errLong.add(Math.abs(locationGraphs.get(i).getDlonggoogle()-locationGraphs.get(i).getLongreal()));
                Location start = new Location("Start");
                start.setLatitude(Math.ceil(locationgraphs.get(i).getLatgoogle()*10000000)/10000000);
                start.setLongitude(Math.ceil(locationgraphs.get(i).getDlonggoogle()*10000000)/10000000);
                Location end = new Location("End");
                end.setLatitude(Math.ceil(locationgraphs.get(i).getLatreal()*10000000)/10000000);
                end.setLongitude(Math.ceil(locationgraphs.get(i).getLongreal()*10000000)/10000000);

                errdistance.add(end.distanceTo(start));
                Log.d("DISTANCE ERRORE", String.valueOf(errdistance.get(i)) + " "+ start.toString() + " "+ end.toString());
            }
            //3th quartile
            errLat.sort(new ComparatorDouble());
            errLong.sort(new ComparatorDouble());
            errdistance.sort(new Comparator<Float>() {
                @Override
                public int compare(Float aFloat, Float t1) {
                    return aFloat.compareTo(t1);
                }
            });


            Log.d("Errore distance ordinato",errdistance.toString());


            thirdpercmetri=percitl(errdistance,(float)0.75);
            novantpercidista = percitl(errdistance,(float)0.90);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            textView.setText("3° quart: "+thirdpercmetri + "\n90° perc: "+ novantpercidista);
            if(locationgraphs != null){
                Thread th = new Thread(new AnimationGraph(graphViewLat,graphViewLong, (ArrayList<LocationGraph>) locationgraphs.clone()));
                th.start();
            }
        }
    }

    //riceve dati gia ordinati
    private double percitl(ArrayList dati,float percentile) {
        int k = 0;
        try {
            k = Integer.parseInt(String.valueOf(percentile*dati.size()-1));
        } catch (NumberFormatException e) {
            //K non è intero si arrotonda per eccesso
            k= (int) Math.ceil(percentile*dati.size())-1; //Devo togliere -1 perchè l'array ha indice da 0 a N
            Log.d("Intero","no " + k);
            return (float) dati.get(k);
        }

        float i = ((float)dati.get(k) +(float)dati.get(k + 1))/2;
        return i;
    }


    private class ComparatorDouble implements Comparator<Double>{

        @Override
        public int compare(Double aDouble, Double t1) {
            return aDouble.compareTo(t1);
        }
    }
}
