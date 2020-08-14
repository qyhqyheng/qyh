package com.justec.blemanager.messageEvent;

public class ReadCalibrationData {
    float CalibrationValue ;
    String dataValue;
    String difCode;

    public ReadCalibrationData(float CalibrationValue,String dataValue,String difCode){
        this.CalibrationValue = CalibrationValue;
        this.dataValue = dataValue;
        this.difCode = difCode;
    }

    public float getCalibrationValue() {
        return CalibrationValue;
    }

    public void setCalibrationValue(float CalibrationValue) {
        this.CalibrationValue = CalibrationValue;
    }
    public String getDataValue() {
        return dataValue;
    }
    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public String getDifCode() {
        return difCode;
    }

    public void setDifCode(String difCode) {
        this.difCode = difCode;
    }
}
