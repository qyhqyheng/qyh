package com.justec.blemanager.messageEvent;

public class SettingUploadSpace {

    float spaceTime = 0;
    public  SettingUploadSpace(float spaceTime){
        this.spaceTime = spaceTime;
    }
    public float getSpaceTime() {
        return spaceTime;
    }

    public void setSpaceTime(float spaceTime) {
        this.spaceTime = spaceTime;
    }
}
