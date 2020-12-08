package com.example.TSN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.TSN.ml.Yamnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class BluetoothService extends Service {
    ConnectedTask mConnectedTask = null;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    boolean canClass =false;
    Thread mClassifyThread = null;
    Interpreter tensorflowInterpreter = null;
    AlertList alertList;
    int ID = getRan();
    boolean b = false;

    //receive audio data
    byte [] readBuffer = null;

    boolean prevExi = false;
    boolean canViv = true;

    String CHANNEL_ID = "bluetooth_classify_NDS";
    String txt = "";
    String prevTxt = "";

    IBinder mBinder = new BluetoothBinder();
    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private static final String TAG = "BluetoothService";

    public BluetoothService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alertList = new AlertList();

        mClassifyThread = new Thread(new Runnable() {
            private String TAG = "ClassThread";

            @Override
            public void run() {
                while(true) {
                    if(canClass && readBuffer!=null) {
                        doExample(readBuffer);
                    }
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try
        {
            if (mConnectedTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                mConnectedTask.cancel(true);
                mConnectedTask = null;
            }
        }
        catch (Exception e) {
        }
    }
    void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Classify Service App", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("TSN App")
                .setContentText(txt)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());

    }
    void updateNotification() {
        if (txt.equals("")) {
            if (prevExi) {
                //초기화
                Notification notification = getInitNotiBuilder();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, notification);
                prevExi = false;
                prevTxt="";
                //canViv = true;
            }
            else {
                return;
            }
        }
        else {
            if (prevTxt.equals(txt))
                return;
            Notification notification = getNotificationBuilder();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notification);
            prevExi = true;
            prevTxt = new String(txt);
        }
    }
    Notification getInitNotiBuilder() {
        CharSequence title = getText(R.string.app_name);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Classify Service App", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("TSN")
                .setContentText("경고알림창")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent);


        return builder.getNotification();
    }
    Notification getNotificationBuilder() {
        CharSequence title = getText(R.string.app_name);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class), 0);
        String temp[] = txt.split("\n");
        String content="";
        for (int i=1;i<temp.length;i++) {
            if (i==temp.length-1) {
                content = content + temp[i];
            }
            else {
                content = content + temp[i] + ", ";
            }
        }
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Classify Service App", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }


        builder.setContentTitle(temp[0])
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent);
        return builder.getNotification();
    }

    int getRan() {
        return new Random().nextInt();
    }
    void connected( String name, BluetoothSocket socket ) {
        mConnectedDeviceName = name;
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
        b = true;
    }
    //service 에서 bluetoothOff
    void bluetoothOff() {
        if (b) {
            Log.d(TAG, "bluetoothOff: called");
            mConnectedDeviceName = null;
            b = false;
            mConnectedTask.cancel(true);
            mConnectedTask = null;

            canClass = false;
            txt = "";
            updateNotification();
        }
    }
    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                b=false;
                Log.e(TAG, "socket not created", e );
            }

            Log.d( TAG, "connected to "+mConnectedDeviceName);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            readBuffer = new byte[88200];
            int readBufferPosition = 0;

            while (true) {
                if ( isCancelled() ) return false;
                try {
                    int bytesAvailable = mInputStream.available();
                    if(bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);

                        if(readBufferPosition == 88200) {
                            canClass = false;
                            byte[] temp = new byte[readBuffer.length];
                            System.arraycopy(readBuffer,bytesAvailable,temp,0,readBuffer.length - bytesAvailable);
                            System.arraycopy(packetBytes,0,temp,readBuffer.length - bytesAvailable, bytesAvailable);
                            readBuffer = temp;
                            canClass = true;
                        }
                        else {
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                readBuffer[readBufferPosition++] = b;
                                if (readBufferPosition == 88200 && mClassifyThread.getState() != Thread.State.RUNNABLE) {
                                    mClassifyThread.start();
                                    break;
                                }
                            }
                        }

                    }
                } catch (IOException e) {

                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }

        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {

            Log.d(TAG, "onProgressUpdate: " + recvMessage[0]);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            Log.d(TAG, "onPostExecute: isSucess = " + isSucess);
            super.onPostExecute(isSucess);

            if ( !isSucess ) {

                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            closeSocket();
        }

        void closeSocket(){

            try {

                readBuffer = null;
                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");
                b=false;
            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg){

            msg += "\n";

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e );
            }

        }
    }
    void setInterpreter (Interpreter interpreter) {
        tensorflowInterpreter = interpreter;
    }
    private float[][] toInputFormat(short[] shorts) {
        float[][] temp = new float[1][15600];
        for (int i = 0;i<15600;i++) {
            temp[0][i] = ((float) shorts[i]) / 32768;
        }
        return temp;
    }
    void doExample(byte[] bytes) {
        byte[] rawData = bytes;
        short[] shortData = bytesToshort(rawData);
        short[] resampledData = resampling(shortData);
        doClassify(resampledData);
    }
    private short[] bytesToshort(byte[] readBuffer) {
        short[] temp = new short[(int)(readBuffer.length / 2)];
        ByteBuffer bb = ByteBuffer.wrap(readBuffer);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int j = 0;
        for (int i=0;i<temp.length;i++) {
            temp[i] = bb.getShort();
        }
        return temp;
    }
    private short[] resampling(short[] shorts) {
        int length = shorts.length;
        double rsrate = 2.75625;
        double i = 0.0;
        short[] temp = new short[(int)(length  / 44100 * 16000)];
        int shortindex=0;
        while (i < (double)(shorts.length)) {
            int j = (int)Math.round(i);
            try{
                if(j >= 44100) break;
                temp[shortindex++] = shorts[j];

            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "resampling: arrayindexoutofbounds" , e);
                System.exit(-1);
            }
            i+=rsrate;
        }
        short[] result = new short[15600];
        System.arraycopy(temp,0,result,0,15600);
        return result;
    }
    private void doClassify(short[] shorts) {
        /**
         * Input [15600] 16000 sampling, 0.975sec
         * Output1 [521]
         * Output2 [96,64]
         * */
        List<Score> scores = new ArrayList<>();
        Map<Integer,Object> map_of_indices_to_outputs = new HashMap<>();

        float[][] inputs = toInputFormat(shorts);
        float[][] predictions = new float[1][521];
        float[][] spectrogram = new float[96][64];
        map_of_indices_to_outputs.put(0,predictions);
        map_of_indices_to_outputs.put(1,spectrogram);

        if (tensorflowInterpreter == null) {
            Log.d(TAG, "doClassify: no interpreter!!");
            return;
        }
        try {
            tensorflowInterpreter.runForMultipleInputsOutputs(inputs, map_of_indices_to_outputs);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"TENSORFLOW LITE 모델이 유효하지 않음.");
        }
        for(int i=0;i<521;i++) {
            scores.add(new Score(i,predictions[0][i]));
        }

        Collections.sort(scores);
        Score temp = scores.get(scores.size()-1);
        int i = 1;
        while(temp.getScore() > 0.4) {
            alertList.inputAlert(temp.getIndex());
            temp = scores.get(scores.size()-1-i++);
        }
        alertList.check();
        txt = alertList.showTxt();
        if (txt != null ) {
            updateNotification();
        }
    }

    void initList() {
        //if (alertList != null)
        if(readBuffer != null && readBuffer.length==88200) {
            for (int i = 0; i<88200;i++) {
                readBuffer[i]=0;
            }
        }
        this.alertList.initList();
    }
    public String getTxt() {
        if(this.txt != null)
            return txt;
        return "";
    }
    public int getID() {
        return ID;
    }
    boolean isB() {
        return this.b;
    }
    public void bOn() {
        this.b = true;
    }
    void bOff() {
        this.b = false;
    }
}
class Score implements Comparable<Score> {
    int index;
    float score;

    Score(int index, float score) {
        this.index = index;
        this.score = score;
    }

    float getScore(){
        return this.score;
    }
    int getIndex(){
        return this.index;
    }
    @Override
    public int compareTo(Score s) {
        if (this.score < s.getScore()){
            return -1;
        } else if (this.score > s.getScore()) {
            return 1;
        }
        return 0;
    }

}