package com.justec.pillowalcohol.event;

import android.net.Uri;

import com.justec.socketcontrol.SocketManager;

public class Common {
    private String SerizableName = "";
    private Boolean AlamDateEnable = true;
    private Boolean VibrateEnable = true;
    private String SonesName_Items;
    private Uri SonesUrl_Items;
    private String AlarmValue;
    private String SoftVersion;
    private int AlarmLimit;
    private String TotalTestTime="";
    private Boolean SwitchFragment = false;

    private static volatile Common instance = null;
    private Common (){}
    public static Common getInstance(){
        if(instance==null){
            synchronized(Common.class){
                if(instance==null){
                    instance=new Common();
                }
            }
        }
        return instance;
    }

    public String getSerizableName() {
        return SerizableName;
    }

    public void setSerizableName(String serizableName) {
        SerizableName = serizableName;
    }

    public Boolean getAlamDateEnable() {
        return AlamDateEnable;
    }

    public void setAlamDateEnable(Boolean alamDateEnable) {
        AlamDateEnable = alamDateEnable;
    }

    public Boolean getVibrateEnable() {
        return VibrateEnable;
    }

    public void setVibrateEnable(Boolean vibrateEnable) {
        VibrateEnable = vibrateEnable;
    }

    public String getSonesName_Items() {
        return SonesName_Items;
    }

    public void setSonesName_Items(String sonesName_Items) {
        SonesName_Items = sonesName_Items;
    }

    public Uri getSonesUrl_Items() {
        return SonesUrl_Items;
    }

    public void setSonesUrl_Items(Uri sonesUrl_Items) {
        SonesUrl_Items = sonesUrl_Items;
    }

    public String getAlarmValue() {
        return AlarmValue;
    }

    public void setAlarmValue(String alarmValue) {
        AlarmValue = alarmValue;
    }

    public String getSoftVersion() {
        return SoftVersion;
    }

    public void setSoftVersion(String softVersion) {
        SoftVersion = softVersion;
    }

    public int getAlarmLimit() {
        return AlarmLimit;
    }

    public void setAlarmLimit(int alarmLimit) {
        AlarmLimit = alarmLimit;
    }

    public String getTotalTestTime() {
        return TotalTestTime;
    }

    public void setTotalTestTime(String totalTestTime) {
        TotalTestTime = totalTestTime;
    }

    public Boolean getSwitchFragment() {
        return SwitchFragment;
    }

    public void setSwitchFragment(Boolean switchFragment) {
        SwitchFragment = switchFragment;
    }
}
