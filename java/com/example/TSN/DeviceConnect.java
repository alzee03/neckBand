package com.example.TSN;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.UUID;

public class DeviceConnect extends PreferenceActivity {

    private static final String TAG = "Device_Connect";

    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    SharedPreferences pref;
    boolean isChecked = false;

    BluetoothService bluetoothService;
    boolean isService = false;
    static boolean isConnectionError = false;
    static boolean[] classes = new boolean[9];

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            bluetoothService = binder.getService();    //서비스 객체전달
            isService = true;
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
        addPreferencesFromResource(R.xml.set);

        pref = getPreferenceManager().getSharedPreferences();
        checkClass();
        Situations.setDangerLevel("동물 울음 소리", classes[0]);
        Situations.setDangerLevel("차 소리, 경적 소리", classes[1]);
        Situations.setDangerLevel("사이렌 소리", classes[2]);
        Situations.setDangerLevel("열차 소리", classes[3]);
        Situations.setDangerLevel("노크 소리", classes[4]);
        Situations.setDangerLevel("공사 소음", classes[5]);
        Situations.setDangerLevel("초인종 소리", classes[6]);
        Situations.setDangerLevel("폭발음", classes[7]);
        Situations.setDangerLevel("아이 울음 소리", classes[8]);
    }

    public void checkClass() {
        if (pref.getBoolean("class1", true))
            classes[0] = true;
        else
            classes[0] = false;

        if (pref.getBoolean("class2", true))
            classes[1] = true;
        else
            classes[1] = false;

        if (pref.getBoolean("class3", true))
            classes[2] = true;
        else
            classes[2] = false;

        if (pref.getBoolean("class4", true))
            classes[3] = true;
        else
            classes[3] = false;

        if (pref.getBoolean("class5", true))
            classes[4] = true;
        else
            classes[4] = false;

        if (pref.getBoolean("class6", true))
            classes[5] = true;
        else
            classes[5] = false;

        if (pref.getBoolean("class7", true))
            classes[6] = true;
        else
            classes[6] = false;

        if (pref.getBoolean("class8", true))
            classes[7] = true;
        else
            classes[7] = false;

        if (pref.getBoolean("class9", true))
            classes[8] = true;
        else
            classes[8] = false;

    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("deviceOnOff")) {
                isChecked = pref.getBoolean("deviceOnOff", false);
                if (isChecked) {
                    //기기와 연결 On
                    bluetoothOn();
                }
                else {
                    //기기와 연결 Off
                    bluetoothOff();
                }
            }
            switch (key){
                case "class1":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[0])
                        classes[0] = isChecked;
                    Situations.setDangerLevel("동물 울음 소리", classes[0]);
                case "class2":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[1])
                        classes[1] = isChecked;
                    Situations.setDangerLevel("차 소리, 경적 소리", classes[1]);
                case "class3":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[2])
                        classes[2] = isChecked;
                    Situations.setDangerLevel("사이렌 소리", classes[2]);
                case "class4":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[3])
                        classes[3] = isChecked;
                    Situations.setDangerLevel("열차 소리", classes[3]);
                case "class5":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[4])
                        classes[4] = isChecked;
                    Situations.setDangerLevel("노크 소리", classes[4]);
                case "class6":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[5])
                        classes[5] = isChecked;
                    Situations.setDangerLevel("공사 소음", classes[5]);
                case "class7":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[6])
                        classes[6] = isChecked;
                    Situations.setDangerLevel("초인종 소리", classes[6]);
                case "class8":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[7])
                        classes[7] = isChecked;
                    Situations.setDangerLevel("폭발음", classes[7]);
                case "class9":
                    isChecked = pref.getBoolean(key, true);
                    if(isChecked!=classes[8])
                        classes[8] = isChecked;
                    Situations.setDangerLevel("아이 울음 소리", classes[8]);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!isService) {
            Intent intent = new Intent(DeviceConnect.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isService) {
            Intent intent = new Intent(DeviceConnect.this, BluetoothService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
        Log.d(TAG, "onRestart: called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isService) {
            unbindService(conn);
            isService = false;
        }
        Log.d(TAG, "onStop: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void bluetoothOn() {
        Log.d( TAG, "Initalizing Bluetooth adapter...");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        }
        else {
            Log.d(TAG, "Initialisation successful.");
            showPairedDevicesListDialog();
        }
    }

    void bluetoothOff() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            bluetoothService.bluetoothOff();
            mBluetoothAdapter = null;
            showErrorDialog("블루투스꺼짐");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                //BlueTooth is now Enabled
                showPairedDevicesListDialog();

            }
            if (resultCode == RESULT_CANCELED) {
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }
    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }
    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }
    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( TAG, "socket create failed " + e.getMessage());
            }

            //mConnectionStatus.setText("connecting...");
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, String.valueOf((mBluetoothSocket.getConnectionType())));
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }

                return false;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if ( isSuccess ) {
                bluetoothService.connected(mConnectedDeviceName, mBluetoothSocket);

                Interpreter interpreter = getTfliteInterpreter("yamnet.tflite");
                bluetoothService.setInterpreter(interpreter);
            }
            else{
                isConnectionError = true;
                Log.d( TAG,  "Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }

    private Interpreter getTfliteInterpreter(String modelPath) {

        try{
            MappedByteBuffer tfliteModel
                    = FileUtil.loadMappedFile(DeviceConnect.this,
                    "yamnet.tflite");
            Interpreter tflite = new Interpreter(tfliteModel);
            return tflite;
        } catch (IOException e){
            Log.e("tfliteSupport", "Error reading model", e);
        }

        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}