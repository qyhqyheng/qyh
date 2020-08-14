package com.justec.blemanager.messageEvent;

import java.util.ArrayList;

public class ProductInfoEvent {

    ArrayList<String> info ;
    public ProductInfoEvent(ArrayList<String> info){
        this.info = info;
    }
    public ArrayList<String> getInfo() {
        return info;
    }
    public void setInfo(ArrayList<String> info) {
        this.info = info;
    }

}
