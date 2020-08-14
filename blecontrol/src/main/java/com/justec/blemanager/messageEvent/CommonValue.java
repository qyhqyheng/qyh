package com.justec.blemanager.messageEvent;

import java.util.ArrayList;
import java.util.LinkedList;

public class CommonValue {

    int value = 0;
    String strPara;
    String startTime;
    String strValue;
    String limiteValue;
    ArrayList<Float> listData = new ArrayList<>();


    public CommonValue(ArrayList<Float> listData, String startTime, String strValue,String limiteValue){
        this.listData = listData;
        this.startTime = startTime;
        this.strValue = strValue;
        this.limiteValue = limiteValue;
    }
    public String getStrPara() {
        return strPara;
    }
    public void setStrPara(String strPara) {
        this.strPara = strPara;
    }

    public String getStrValue() {
        return strValue;
    }
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public ArrayList<Float> getListData() {
        return listData;
    }
    public void setListData(ArrayList<Float> listData) {
        this.listData = listData;
    }

    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public String getLimiteValue() {
        return limiteValue;
    }
    public void setLimiteValue(String limiteValue) {
        this.limiteValue = limiteValue;
    }
}
