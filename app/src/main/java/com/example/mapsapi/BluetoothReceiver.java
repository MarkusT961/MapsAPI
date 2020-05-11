package com.example.mapsapi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BluetoothReceiver extends BroadcastReceiver {
    private View view; //activity che vuole gli update;
    private LinearLayout linearLayout;
    public BluetoothReceiver(View view) {
        this.view = view;
        linearLayout = (LinearLayout) view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
            final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
            view.post(new Runnable() {
                @Override
                public void run() {
                    TextView textView = new TextView(view.getContext());
                    if (bluetoothDevice != null) {
                        textView.setText(rssi + " "+ bluetoothDevice.getAddress());
                    }
                    linearLayout.addView(textView);
                }
            });

            try {
                Log.d("Bluetooht signal", String.valueOf(rssi));
                Log.d("Bluetooth",bluetoothDevice.getAddress());
                Log.d("Bluetooth", String.valueOf(bluetoothDevice.getBondState()));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            view.post(new Runnable() {
                @Override
                public void run() {
                    TextView textView = new TextView(view.getContext());
                    textView.setText("FINISH DISCOVERY");
                    linearLayout.addView(textView);
                }
            });
        }
    }
}
