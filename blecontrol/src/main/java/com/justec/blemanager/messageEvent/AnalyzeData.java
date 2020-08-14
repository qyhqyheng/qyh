package com.justec.blemanager.messageEvent;

public class AnalyzeData {
    String data ;
    int count;

    int uploadTimeSpace;
    public AnalyzeData(String data,int count){
        this.data = data;
        this.count = count;
    }
    public AnalyzeData(int uploadTimeSpace){
        this.uploadTimeSpace = uploadTimeSpace;
    }
    public AnalyzeData(){
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }


    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public int getUploadTimeSpace() {
        return uploadTimeSpace;
    }
    public void setUploadTimeSpace(int uploadTimeSpace) {
        this.uploadTimeSpace = uploadTimeSpace;
    }
}
