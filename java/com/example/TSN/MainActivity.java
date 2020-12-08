package com.example.TSN;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    Toolbar tb;
    ActionBar ab;
    private static final String TAG = "MainActivity";
    Thread mcheckThread = null;

    BluetoothService bluetoothService;
    boolean isService = false;
    boolean canAlert = false;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            bluetoothService = binder.getService();    //서비스 객체전달
            isService = true;
            if (bluetoothService.isB()) {
                canAlert = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);

        Intent intent = new Intent(getApplicationContext(),BluetoothService.class);
        if (Build.VERSION.SDK_INT >=26) {
            getApplicationContext().startForegroundService(intent);
        }
        else {
            getApplicationContext().startService(intent);
        }

        Log.d(TAG, "onCreate: " + isService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.settings, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent;

        switch (item.getItemId()){
            case R.id.close:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure to EXIT?");
                builder.setTitle("EXIT")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                                stopService(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("EXIT");
                alert.show();
                break;
            case R.id.setting:
                intent = new Intent(getApplicationContext(), DeviceConnect.class);
                startActivity(intent);
                break;
        }

       return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isService) {
            Intent intent = new Intent(MainActivity.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isService) {
            Intent intent = new Intent(MainActivity.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);

            if (bluetoothService.isB()) {
                canAlert = true;
            }

            mcheckThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: CheckThreadRun");
                    while(canAlert) {
                        if (!bluetoothService.getTxt().equals("")) {
                            //인텐트로 넘어가기
                            Intent intent1 = new Intent(getApplicationContext(), Alerting.class);
                            startActivity(intent1);
                            canAlert = false;
                        }
                    }
                    Log.d(TAG, "run: CheckThreadEnd");

                }
            });
            mcheckThread.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isService) {
            unbindService(conn);
            isService = false;
        }
        Log.d(TAG, "onStop: onstopcalled");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
