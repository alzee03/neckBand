package com.example.TSN;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Alerting extends AppCompatActivity {
    BluetoothService bluetoothService;
    boolean isService = false;
    boolean stillRemain = true;
    Thread mshowThread = null;
    TextView tv;
    TextView tv2;
    private static final String TAG = "Alerting";

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothBinder mbinder = (BluetoothService.BluetoothBinder) service;
            bluetoothService = mbinder.getService();    //서비스 객체전달
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alert);
        tv = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isService) {
            Intent intent = new Intent(Alerting.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            stillRemain = true;
            mshowThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (stillRemain) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String temp = bluetoothService.getTxt();
                                if (temp.equals("")) stillRemain = false;
                                tv.setText(temp);
                            }
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "run: sleep error", e);
                        }
                    }
                    finish();
                }
            });

            mshowThread.start();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isService) {
            Intent intent = new Intent(Alerting.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            stillRemain = true;
            mshowThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(stillRemain) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String temp = bluetoothService.getTxt();
                                if (temp.equals("")) stillRemain = false;
                                tv.setText(temp);
                            }
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "run: sleep error", e);
                        }
                        finish();
                    }
                }
            });
            mshowThread.start();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (isService) {
            unbindService(conn);
            isService = false;
        }
        if (stillRemain) {
            stillRemain = false;
        }
        Log.d(TAG, "onStop: onstopcalled");
    }
}
