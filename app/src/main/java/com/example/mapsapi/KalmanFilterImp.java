package com.example.mapsapi;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;

public class KalmanFilterImp {
    private RealMatrix A;
    //  control input
    private RealMatrix B;

    private RealMatrix H;

    private RealMatrix Q;

    private RealMatrix R;
    private RealMatrix initialerror;
    private KalmanFilter filter;
    private double dt; //in secondi
    public KalmanFilterImp(double latinz, double longinz,double dt){
        this.dt = dt;
        A = new Array2DRowRealMatrix(new double[][] {{ 1d,0d },{0d,1d}});
        B = new Array2DRowRealMatrix(new double[][] {{dt,0d},{0d,dt}});
        H = new Array2DRowRealMatrix(new double[][] {{1d,0d},{0d,1d}});
        Q = new Array2DRowRealMatrix(new double[][] {{ 0.5d,0d },{0d,0.5d}});
        R = new Array2DRowRealMatrix(new double[][] {{ 2d,0d },{0d,2d}});
        initialerror = new Array2DRowRealMatrix(new double[][] {{ 2d,0d },{0d,2d}});

        ProcessModel pm = new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[] {latinz,longinz}),initialerror);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        this.filter = new KalmanFilter(pm, mm);
    }

    public double[] correctAndStimate(double[] predict,double[] mis){
        filter.predict(predict);
        filter.correct(mis);
        return filter.getStateEstimation();
    }
}
