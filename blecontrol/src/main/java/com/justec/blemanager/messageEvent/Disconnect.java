package com.justec.blemanager.messageEvent;

public class Disconnect {
    //断开连接的时间
    String disConnectTime;

    public Disconnect(String disConnectTime) {
        this.disConnectTime = disConnectTime;
    }

    public String getDisConnectTime() {
        return disConnectTime;
    }

    public void setDisConnectTime(String disConnectTime) {
        this.disConnectTime = disConnectTime;
    }
}
