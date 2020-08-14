package com.justec.blemanager.messageEvent;

public class NotificationDialog {

    //1:开始校准...   2:校准完成
    String NotificationTag;
    String paraSetting ="";

    boolean isSuccess ;

    public NotificationDialog(String NotificationTag) {
        this.NotificationTag = NotificationTag;
    }
    public NotificationDialog(String paraSetting,boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.paraSetting = paraSetting;
    }

    public String getNotificationTag() {
        return NotificationTag;
    }

    public void setNotificationTag(String NotificationTag) {
        this.NotificationTag = NotificationTag;
    }
    public boolean getSuccess() {
        return isSuccess;
    }
    public void setSuccess(boolean success) {
        isSuccess = success;
    }
    public String getParaSetting() {
        return paraSetting;
    }
    public void setParaSetting(String paraSetting) {
        this.paraSetting = paraSetting;
    }
}
