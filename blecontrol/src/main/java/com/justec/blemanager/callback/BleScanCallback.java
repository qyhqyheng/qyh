package com.justec.blemanager.callback;


import com.justec.blemanager.blemanager.BleDevice;

import java.util.List;



public abstract class BleScanCallback {

    public abstract void onScanStarted(boolean success);

    public abstract void onScanning(BleDevice result);

    public abstract void onScanFinished(List<BleDevice> scanResultList);

    public void onLeScan(BleDevice bleDevice){}
}
