package com.example.mapsapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder>{

private ArrayList<GioObject> gioObjects;
public ModelAdapter( ArrayList<GioObject> gioObjects){
    this.gioObjects = gioObjects;
}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater =LayoutInflater.from(context);
        View nameview = inflater.inflate(R.layout.layout,parent,false);
        return new ViewHolder(nameview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textLat.setText(String.format("Lat Google: %s", gioObjects.get(position).getLocationgoogle().getLatitude()));
        holder.textLong.setText(String.format("Long Google: %s", String.valueOf(gioObjects.get(position).getLocationgoogle().getLongitude())));
        holder.textvelocity.setText(String.format("Velocity: %s", String.valueOf(gioObjects.get(position).getVelocityavg())));
        holder.texttime.setText(String.format("Time G: %s", gioObjects.get(position).getTimestamp()/1000));
        holder.textdistancefromA.setText(String.format("Distance: %s", gioObjects.get(position).getDistancefromA()));
        holder.textdt.setText(String.format("Time dt: %s", gioObjects.get(position).getTimedt() / 1000));
        holder.textwifi.setText(gioObjects.get(position).getScanResults().toString());
        holder.textLatreal.setText(String.format("Lat Real:%s", gioObjects.get(position).getLocationreal().latitude));
        holder.textLongreal.setText(String.format("Long Real:%s", gioObjects.get(position).getLocationreal().longitude));
        holder.textlatgio.setText(String.format("Lat Gio:%s", gioObjects.get(position).getLocationgio().latitude));
        holder.textlonggio.setText(String.format("Long Gio:%s", gioObjects.get(position).getLocationgio().longitude));

        //Da finire con reallong e reallat
    }

    @Override
    public int getItemCount() {
        return this.gioObjects.size();
    }

    //Cancella tutto e aggiorna il modeladapter per refresh della UI
    public void clearUpdate(){
        this.gioObjects.clear();
        this.notifyDataSetChanged();
    }

    public void SetUpdate(ArrayList<GioObject> gioObjects){
        this.gioObjects=gioObjects;
        this.notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView textLatreal;
        private  TextView textLongreal;
        private  TextView textvelocity;
        private  TextView texttime;
        private TextView textLat;
        private TextView textLong;
        private TextView textwifi;
        private TextView textdt;
        private TextView textdistancefromA;
        private TextView textlatgio;
        private TextView textlonggio;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textLat = itemView.findViewById(R.id.textLat);
            textLong = itemView.findViewById(R.id.textLong);
            textlatgio = itemView.findViewById(R.id.textlatgio);
            textlonggio = itemView.findViewById(R.id.textlonggio);
            textLatreal= itemView.findViewById(R.id.textlatreal);
            textLongreal= itemView.findViewById(R.id.textlongreal);
            textvelocity=itemView.findViewById(R.id.textvelocity);
            texttime=itemView.findViewById(R.id.textime);
            textwifi= itemView.findViewById(R.id.textpand);
            textdistancefromA=itemView.findViewById(R.id.textdistancereal);
            textdt=itemView.findViewById(R.id.texttimedt);

        }
    }
}
