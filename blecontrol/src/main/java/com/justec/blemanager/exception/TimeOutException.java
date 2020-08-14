package com.justec.blemanager.exception;


public class TimeOutException extends BleException {

    public TimeOutException() {
        super(ERROR_CODE_TIMEOUT, "Timeout Exception Occurred!");
    }

}
