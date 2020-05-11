package com.example.mapsapi;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class AnimationGraph implements Runnable {
    private GraphView graphViewLat;
    private GraphView graphViewLong;
    private ArrayList<LocationGraph> locationGraphs;
    LineGraphSeries<DataPoint> seriesgoogleLat=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesrealLat=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesgoogleLong=new LineGraphSeries<>();
    LineGraphSeries<DataPoint> seriesrealLong=new LineGraphSeries<>();

    public AnimationGraph(GraphView graphViewLat,GraphView graphViewLong,ArrayList<LocationGraph> locationGraphs) {
        this.graphViewLat=graphViewLat;
        this.graphViewLong=graphViewLong;
        this.locationGraphs=locationGraphs;
        seriesrealLat.setColor(Color.BLUE);
        seriesgoogleLat.setColor(Color.RED);
        graphViewLat.addSeries(seriesgoogleLat);
        graphViewLat.addSeries(seriesrealLat);
        seriesrealLong.setColor(Color.BLUE);
        seriesgoogleLong.setColor(Color.RED);
        graphViewLong.addSeries(seriesgoogleLong);
        graphViewLong.addSeries(seriesrealLong);

    }

    @Override
    public void run() {
        for(final LocationGraph locationGraph : locationGraphs){
            graphViewLong.post(new Runnable() {
                @Override
                public void run() {
                    seriesrealLat.appendData(new DataPoint(locationGraph.getTemp(),locationGraph.getLatreal()),true,300);
                    seriesgoogleLat.appendData(new DataPoint(locationGraph.getTemp(),locationGraph.getLatgoogle()),true,300);
                    seriesrealLong.appendData(new DataPoint(locationGraph.getTemp(),locationGraph.getLongreal()),true,300);
                    seriesgoogleLong.appendData(new DataPoint(locationGraph.getTemp(),locationGraph.getDlonggoogle()),true,300);
                }
            });
        }
        graphViewLong.post(new ZoomOutGraph(graphViewLong));
        graphViewLat.post(new ZoomOutGraph(graphViewLat));
    }
    public class ZoomOutGraph implements Runnable{
        private GraphView graphView;

        public ZoomOutGraph(GraphView graphView) {
            this.graphView = graphView;
        }

        @Override
        public void run() {
            double maxX= graphView.getSeries().get(0).getHighestValueX(); //lo faccio su una sola serie perchè il tempo è uguale per tutti
            double minX = graphView.getSeries().get(0).getLowestValueX();
            graphView.getViewport().setXAxisBoundsManual(true);
            graphView.getViewport().setMinX(minX);
            graphView.getViewport().setMaxX(maxX);

        }
    }
}
