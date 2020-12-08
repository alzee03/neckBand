package com.example.TSN;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Situations {
    static ArrayList<DangerSituations> situations = new ArrayList<DangerSituations>();
    public Situations() {
        //file load
        File optionDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ClassData");
        String line = null;
        if(!optionDir.exists()) {
            optionDir.mkdir();
        }
        File optionFile = new File(optionDir.getAbsolutePath() + "/option.txt");
        addSituation("동물 울음 소리",true,new ArrayList<Integer>(Arrays.asList(69, 70, 71, 72, 73, 74)));
        addSituation("차 소리, 경적 소리",true,new ArrayList<Integer>(Arrays.asList(294, 295, 296, 297, 298, 299, 300, 301, 305, 306, 307, 308, 309, 310, 311, 312, 314, 315, 320, 321, 302, 303, 304, 313, 392, 337, 338, 339, 340, 341, 342, 342, 343, 344, 345, 346, 347)));
        addSituation("사이렌 소리",true,new ArrayList<Integer>(Arrays.asList(316, 317, 318, 319, 390, 391)));
        addSituation("열차 소리",true,new ArrayList<Integer>(Arrays.asList(322, 323, 324, 325, 326, 327, 328)));
        addSituation("노크 소리",true,new ArrayList<Integer>(Arrays.asList(348, 351, 352, 353, 354, 355)));
        addSituation("공사 소음",true,new ArrayList<Integer>(Arrays.asList(412, 413, 414, 415, 416, 417,418, 419)));
        addSituation("초인종 소리",true,new ArrayList<Integer>(Arrays.asList(349, 350, 382, 383, 384, 385, 386, 387, 388, 389, 393, 394, 395)));
        addSituation("폭발음",true,new ArrayList<Integer>(Arrays.asList(420, 421, 422, 423, 424, 425, 426, 427, 428, 429, 430)));
        addSituation("아이 울음 소리",true,new ArrayList<Integer>(Arrays.asList(14, 20)));
    }
    boolean addSituation(String label,boolean x, ArrayList<Integer> arrayList) {
        DangerSituations temp = new DangerSituations(label,x,arrayList);
        if(!situations.contains(temp)) {
            situations.add(temp);
            return true;
        }
        return false;
    }
    static int findSituation(String label, boolean level) {
        DangerSituations temp = new DangerSituations(label, level, new ArrayList<Integer>());
        return situations.indexOf(temp);
    }
    static void setDangerLevel(String label, boolean level) {
        int i = findSituation(label, level);
        situations.get(i).setDangerLevel(level);
    }
    static void saveOptions() {
        //file save
        File optionDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ClassData");
        if(!optionDir.exists()) {
            optionDir.mkdir();
        }
        File optionFile = new File(optionDir.getAbsolutePath() + "/option.txt");
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(optionFile.getAbsolutePath(),false));
            for (DangerSituations ds : situations) {
                buf.append(ds.saveStringFormat());
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
