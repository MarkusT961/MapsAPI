package com.example.mapsapi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    static int REQUEST_ENABLE_BT = 1;
    BluetoothReceiver receiver;
    LinearLayout linear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        linear = findViewById(R.id.linearbluetooth);
        receiver = new BluetoothReceiver(linear); //in modo da fare update a questa view;
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //per scoprire quando finisce la ricerca
        registerReceiver(receiver,filter);
        if(bluetoothAdapter == null){ //DISPOSITIVO NON HA IL BLUETOOTH
            Log.d("Bluetooth", "Bluetooth not found");
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.disable();
        }
    }

    public void activatebluetooth(View view) {
        if(!bluetoothAdapter.isEnabled()){
            Log.d("Bluetooth","non attivato");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            return;
        }
        linear.removeAllViews();

        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                activatebluetooth(linear);
            }
        }
    }
}
