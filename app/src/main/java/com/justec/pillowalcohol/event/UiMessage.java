package com.justec.pillowalcohol.event;

public class UiMessage {
    private int type;
    private String msg;

    public UiMessage(int type,String msg){
        this.type = type;
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
