package com.justec.pillowalcohol.dataBase;

public class ItemTime {

    private int itemId;
    private String itemInfo;
    private String timeTotal;
    private int itemCount;

    private String alarmLimite;

    public ItemTime(){}

    public ItemTime(String itemInfo, String timeTotal,int itemCount,String alarmLimite){
        this.itemInfo=itemInfo;
        this.timeTotal=timeTotal;
        this.itemCount=itemCount;
        this.alarmLimite=alarmLimite;
    }
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int id) {
        this.itemId = itemId;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(String itemInfo) {
        this.itemInfo = itemInfo;
    }

    public String getTimeTotal() {
        return timeTotal;
    }

    public void setTimeTotal(String timeTotal) {
        this.timeTotal = timeTotal;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getAlarmLimite() {
        return alarmLimite;
    }
    public void setAlarmLimite(String alarmLimite) {
        this.alarmLimite = alarmLimite;
    }
}
