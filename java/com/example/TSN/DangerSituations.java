package com.example.TSN;

import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class DangerSituations {
    String label;
    boolean dangerLevel;
    ArrayList<Integer> dangerClass;
    public DangerSituations(String label2, boolean x, ArrayList<Integer> arrayList) {
        this.label = label2;
        setDangerLevel(x);
        dangerClass = new ArrayList<Integer>(arrayList);
        Collections.sort(dangerClass);
    }

    int search(int index) {

        if(dangerLevel == false) return -1;
        return dangerClass.indexOf(index);
    }
    void setDangerLevel(boolean x) {this.dangerLevel = x;}
    public String getLabel() {
        return label;
    }
    boolean getDangerLevel() {return this.dangerLevel;}
    String saveStringFormat() {
        String temp = "";
        temp += label + ":";
        temp += dangerLevel +":";
        temp += dangerClass.get(0);
        for(int i=1;i<dangerClass.size();i++) {
            temp += "," + dangerClass.get(i);
        }
        return temp;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DangerSituations that = (DangerSituations) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(label);
        }
        return 0;
    }
}
