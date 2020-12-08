package com.example.TSN;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Objects;

public class AlertList {

    private static final String TAG = "AlertList";
    ArrayList<Alert> alertArrayList;
    public AlertList() {
        alertArrayList = new ArrayList<Alert>();
    }

    void inputAlert(int x) {
        long time = System.currentTimeMillis();
        Alert temp = new Alert(x,time);
        int i = alertArrayList.indexOf(temp);
        if (i!=-1)
            alertArrayList.remove(i);
        alertArrayList.add(temp);
    }
    void check() {
        while (alertArrayList.size() > 0) {
            Alert temp = alertArrayList.get(0);
            if ((System.currentTimeMillis() - temp.time ) > 5000) {
                alertArrayList.remove(0);
            }
            else break;
        }
    }
    void initList() {
        if (this.alertArrayList != null && alertArrayList.size() > 0) {
            alertArrayList = new ArrayList<Alert>();
        }
    }
    String showTxt() {
        if(alertArrayList.size() > 0) {
            String txt = "";
            ArrayList<Integer> situationArrayList = new ArrayList<Integer>();
            for (Alert alert : alertArrayList) {
                for (int i = 0 ; i < Situations.situations.size() ; i++ ) {
                    DangerSituations ds = Situations.situations.get(i);
                    int t = ds.search(alert.index);
                    if(t > -1 && !situationArrayList.contains(i)) {
                        situationArrayList.add(i);
                        if (i==0) txt = ds.getLabel() + txt;
                        else txt = ds.getLabel() +"\n" + txt;
                        break;
                    }
                }
            }
            return txt;
        }
        return "";
    }
    private class Alert {
        long time;
        int index;
        Alert(int index, long time) {
            this.time = time;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Alert alert = (Alert) o;
            return index == alert.index;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }

}