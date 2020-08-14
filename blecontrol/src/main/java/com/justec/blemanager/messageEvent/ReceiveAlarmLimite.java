package com.justec.blemanager.messageEvent;

public class ReceiveAlarmLimite {

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    int value;

    public ReceiveAlarmLimite(int value){
        this.value = value;
    }

}
