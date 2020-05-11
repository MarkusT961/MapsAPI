package com.example.mapsapi;


import java.io.Serializable;

public class LocationGraph implements Serializable { //Classe utilizzata per passare dati al grafico
   private double latgoogle,dlonggoogle,latreal, longreal, temp; //temp è il tempo frazionato.

    public double getLatgoogle() {
        return latgoogle;
    }

    public double getDlonggoogle() {
        return dlonggoogle;
    }

    public double getLatreal() {
        return latreal;
    }

    public double getLongreal() {
        return longreal;
    }

    public double getTemp() {
        return temp;
    }

    public LocationGraph(double latgoogle, double dlonggoogle, double latreal, double longreal, double temp) {
        this.latgoogle = latgoogle;
        this.dlonggoogle = dlonggoogle;
        this.latreal = latreal;
        this.longreal = longreal;
        this.temp = temp;
    }
}
