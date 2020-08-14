package com.justec.blemanager.blemanager;

import android.annotation.TargetApi;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.justec.blemanager.callback.BleGattCallback;
import com.justec.blemanager.callback.BleIndicateCallback;
import com.justec.blemanager.callback.BleMtuChangedCallback;
import com.justec.blemanager.callback.BleNotifyCallback;
import com.justec.blemanager.callback.BleReadCallback;
import com.justec.blemanager.callback.BleScanAndConnectCallback;
import com.justec.blemanager.callback.BleScanCallback;
import com.justec.blemanager.callback.BleWriteCallback;
import com.justec.blemanager.comm.BleConnectState;
import com.justec.blemanager.comm.BleScanState;
import com.justec.blemanager.exception.OtherException;
import com.justec.blemanager.scan.BleScanRuleConfig;
import com.justec.blemanager.scan.BleScanner;
import com.justec.blemanager.utils.BleLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManager {

    /*********  蓝牙连接通信操作 *******/
    private Application context;
    private BleScanRuleConfig bleScanRuleConfig;
    private BleScanner bleScanner;
    private BluetoothAdapter bluetoothAdapter;
    private MultipleBluetoothController multipleBluetoothController;

    public static final int DEFAULT_SCAN_TIME = 5000;
    private static final int DEFAULT_MAX_MULTIPLE_DEVICE = 7;
    private static final int DEFAULT_OPERATE_TIME = 5000;
    private static final int DEFAULT_MTU = 23;
    private static final int DEFAULT_MAX_MTU = 512;
    private static final int DEFAULT_WRITE_DATA_SPLIT_COUNT = 20;

    private int maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE;
    private int operateTimeout = DEFAULT_OPERATE_TIME;
    private int splitWriteNum = DEFAULT_WRITE_DATA_SPLIT_COUNT;

    /******返回蓝牙数据的处理*******/

    //列表表单目录数目
    public static int recordFormCount = 0;
    //列表表单记录数目
    public static int recordCount = 0;
    //列表表单目录名称
    public static String recordForm = "";
    //设备需要的信息集合 93
    public static ArrayList<String> deviceRequireInfoContent;


    public static BleManager getInstance() {
        return BleManagerHolder.sBleManager;
    }

    private static class BleManagerHolder {
        private static final BleManager sBleManager = new BleManager();
    }

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            BluetoothManager bluetoothManager = (BluetoothManager) context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                bluetoothAdapter = bluetoothManager.getAdapter();
            multipleBluetoothController = new MultipleBluetoothController();
            bleScanRuleConfig = new BleScanRuleConfig();
            bleScanner = BleScanner.getInstance();
        }
    }

    /**
     * Get the Context
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get the BluetoothAdapter
     *
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Get the BleScanner
     *
     * @return
     */
    public BleScanner getBleScanner() {
        return bleScanner;
    }

    /**
     * get the ScanRuleConfig
     *
     * @return
     */
    public BleScanRuleConfig getScanRuleConfig() {
        return bleScanRuleConfig;
    }

    /**
     * Get the multiple Bluetooth Controller
     *
     * @return
     */
    public MultipleBluetoothController getMultipleBluetoothController() {
        return multipleBluetoothController;
    }

    /**
     * Configure scan and connection properties
     *
     * @param scanRuleConfig
     */
    public void initScanRule(BleScanRuleConfig scanRuleConfig) {
        this.bleScanRuleConfig = scanRuleConfig;
    }

    /**
     * Get the maximum number of connections
     *
     * @return
     */
    public int getMaxConnectCount() {
        return maxConnectCount;
    }

    /**
     * Set the maximum number of connections
     *
     * @param maxCount
     * @return BleManager
     */
    public BleManager

    setMaxConnectCount(int maxCount) {
        if (maxCount > DEFAULT_MAX_MULTIPLE_DEVICE)
            maxCount = DEFAULT_MAX_MULTIPLE_DEVICE;
        this.maxConnectCount = maxCount;
        return this;
    }

    /**
     * Get operate timeout
     *
     * @return
     */
    public int getOperateTimeout() {
        return operateTimeout;
    }

    /**
     * Set operate timeout
     *
     * @param operateTimeout
     * @return BleManager
     */
    public BleManager setOperateTimeout(int operateTimeout) {
        this.operateTimeout = operateTimeout;
        return this;
    }

    /**
     * Get operate splitWriteNum
     *
     * @return
     */
    public int getSplitWriteNum() {
        return splitWriteNum;
    }

    /**
     * Set splitWriteNum
     *
     * @param num
     * @return BleManager
     */
    public BleManager setSplitWriteNum(int num) {
        this.splitWriteNum = num;
        return this;
    }


    /**
     * print log?
     *
     * @param enable
     * @return BleManager
     */
    public BleManager enableLog(boolean enable) {
        BleLog.isPrint = enable;
        return this;
    }

    /**
     * scan Device around
     *
     * @param callback
     */
    public void scan(BleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback can not be Null!");
        }

        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }

        UUID[] serviceUuids = bleScanRuleConfig.getServiceUuids();
        String[] deviceNames = bleScanRuleConfig.getDeviceNames();
        String deviceMac = bleScanRuleConfig.getDeviceMac();
        boolean fuzzy = bleScanRuleConfig.isFuzzy();
        long timeOut = bleScanRuleConfig.getScanTimeOut();

        bleScanner.scan(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback);
    }

    /**
     * scan Device then connect
     *
     * @param callback
     */
    public void scanAndConnect(BleScanAndConnectCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanAndConnectCallback can not be Null!");
        }

        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }

        UUID[] serviceUuids = bleScanRuleConfig.getServiceUuids();
        String[] deviceNames = bleScanRuleConfig.getDeviceNames();
        String deviceMac = bleScanRuleConfig.getDeviceMac();
        boolean fuzzy = bleScanRuleConfig.isFuzzy();
        long timeOut = bleScanRuleConfig.getScanTimeOut();

        bleScanner.scanAndConnect(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback);
    }

    /**
     * connect a known Device
     *
     * @param bleDevice
     * @param bleGattCallback
     * @return
     */
    public BluetoothGatt connect(BleDevice bleDevice, BleGattCallback bleGattCallback) {
        if (bleGattCallback == null) {
            throw new IllegalArgumentException("BleGattCallback can not be Null!");
        }
        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            bleGattCallback.onConnectFail(new OtherException("Bluetooth not enable!"));
            return null;
        }

        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!");
        }

        if (bleDevice == null || bleDevice.getDevice() == null) {
            Log.e("Jerry.Xiao","bleDevice == null");
            bleGattCallback.onConnectFail(new OtherException("Not Found Device Exception Occurred!"));
        } else {
            BleBluetooth bleBluetooth = new BleBluetooth(bleDevice);
            boolean autoConnect = bleScanRuleConfig.isAutoConnect();
            return bleBluetooth.connect(bleDevice, autoConnect, bleGattCallback);
        }

        return null;
    }

    /**
     * Cancel scan
     */
    public void cancelScan() {
        bleScanner.stopLeScan();
    }

    /**
     * notify
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param callback
    */
    public void notify(BleDevice bleDevice,
                       String uuid_service,
                       String uuid_notify,
                       BleNotifyCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleNotifyCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onNotifyFailure(new OtherException("This Device not connect!"));
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuid_service, uuid_notify)
                    .enableCharacteristicNotify(callback, uuid_notify);
        }
    }

    /**
     * indicate
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param callback
    */
    public void indicate(BleDevice bleDevice,
                         String uuid_service,
                         String uuid_indicate,
                         BleIndicateCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleIndicateCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onIndicateFailure(new OtherException("This Device not connect!"));
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuid_service, uuid_indicate)
                    .enableCharacteristicIndicate(callback, uuid_indicate);
        }
    }

    /**
     * stop notify, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @return
    */
    public boolean stopNotify(BleDevice bleDevice,
                              String uuid_service,
                              String uuid_notify) {
        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            return false;
        }
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify)
                .disableCharacteristicNotify();
        if (success) {
            bleBluetooth.removeNotifyCallback(uuid_notify);
        }
        return success;
    }

    /**
     * stop indicate, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @return
    */
    public boolean stopIndicate(BleDevice bleDevice,
                                String uuid_service,
                                String uuid_indicate) {
        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            return false;
        }
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate)
                .disableCharacteristicIndicate();
        if (success) {
            bleBluetooth.removeIndicateCallback(uuid_indicate);
        }
        return success;
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param callback
    */
    public void write(BleDevice bleDevice,
                      String uuid_service,
                      String uuid_write,
                      byte[] data,
                      BleWriteCallback callback) {
        write(bleDevice, uuid_service, uuid_write, data, true, callback);
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param split
     * @param callback
    */
    public void write(BleDevice bleDevice,
                      String uuid_service,
                      String uuid_write,
                      byte[] data,
                      boolean split,
                      BleWriteCallback callback) {

        if (callback == null) {
            throw new IllegalArgumentException("BleWriteCallback can not be Null!");
        }

        if (data == null) {
            BleLog.e("data is Null!");
            callback.onWriteFailure(new OtherException("data is Null!"));
            return;
        }

        if (data.length > 20) {
            BleLog.w("Be careful: data's length beyond 20! Ensure MTU higher than 23, or use spilt write!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onWriteFailure(new OtherException("This Device not connect!"));
        } else {
            if (split && data.length > 20) {
                new SplitWriter().splitWrite(bleBluetooth, uuid_service, uuid_write, data, callback);
            } else {
                bleBluetooth.newBleConnector()
                        .withUUIDString(uuid_service, uuid_write)
                        .writeCharacteristic(data, callback, uuid_write);
            }
        }
    }

    /**
     * read
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_read
     * @param callback
    */
    public void read(BleDevice bleDevice,
                     String uuid_service,
                     String uuid_read,
                     BleReadCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleReadCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onReadFailure(new OtherException("This Device is not connected!"));
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuid_service, uuid_read)
                    .readCharacteristic(callback, uuid_read);
        }
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param callback
    */
    /*public void readRssi(BleDevice bleDevice,
                         BleRssiCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleRssiCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onRssiFailure(new OtherException("This Device is not connected!"));
        } else {
            bleBluetooth.newBleConnector().readRemoteRssi(callback);
        }
    }*/

    /**
     * set Mtu
     *
     * @param bleDevice
     * @param mtu
     * @param callback
    */
    public void setMtu(BleDevice bleDevice,
                       int mtu,
                       BleMtuChangedCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleMtuChangedCallback can not be Null!");
        }

        if (mtu > DEFAULT_MAX_MTU) {
            BleLog.e("requiredMtu should lower than 512 !");
            callback.onSetMTUFailure(new OtherException("requiredMtu should lower than 512 !"));
            return;
        }

        if (mtu < DEFAULT_MTU) {
            BleLog.e("requiredMtu should higher than 23 !");
            callback.onSetMTUFailure(new OtherException("requiredMtu should higher than 23 !"));
            return;
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onSetMTUFailure(new OtherException("This Device is not connected!"));
        } else {
            bleBluetooth.newBleConnector().setMtu(mtu, callback);
        }
    }


    /**
     * is support ble?
     *
     * @return
    */
    public boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Open bluetooth
     */
    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * Disable bluetooth
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
    */
    public boolean isBlueEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public BleDevice convertBleDevice(BluetoothDevice bluetoothDevice) {
        return new BleDevice(bluetoothDevice);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BleDevice convertBleDevice(ScanResult scanResult) {
        if (scanResult == null) {
            throw new IllegalArgumentException("scanResult can not be Null!");
        }
        BluetoothDevice bluetoothDevice = scanResult.getDevice();
        int rssi = scanResult.getRssi();
        ScanRecord scanRecord = scanResult.getScanRecord();
        byte[] bytes = null;
        if (scanRecord != null)
            bytes = scanRecord.getBytes();
        long timestampNanos = scanResult.getTimestampNanos();
        return new BleDevice(bluetoothDevice, rssi, bytes, timestampNanos);
    }

    public BleBluetooth getBleBluetooth(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            return multipleBluetoothController.getBleBluetooth(bleDevice);
        }
        return null;
    }

    public List<BleDevice> getAllConnectedDevice() {
        if (multipleBluetoothController == null)
            return null;
        return multipleBluetoothController.getDeviceList();
    }

    public BluetoothGatt getBluetoothGatt(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            return bleBluetooth.getBluetoothGatt();
        return null;
    }

    public void removeConnectGattCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeConnectGattCallback();
    }

    public void removeRssiCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeRssiCallback();
    }

    public void removeMtuChangedCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeMtuChangedCallback();
    }

    public void removeNotifyCallback(BleDevice bleDevice, String uuid_notify) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeNotifyCallback(uuid_notify);
    }

    public void removeIndicateCallback(BleDevice bleDevice, String uuid_indicate) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeIndicateCallback(uuid_indicate);
    }

    public void removeWriteCallback(BleDevice bleDevice, String uuid_write) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeWriteCallback(uuid_write);
    }

    public void removeReadCallback(BleDevice bleDevice, String uuid_read) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeReadCallback(uuid_read);
    }

    public void clearCharacterCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.clearCharacterCallback();
    }

    public BleScanState getScanSate() {
        return bleScanner.getScanState();
    }

    public BleConnectState getConnectState(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            return multipleBluetoothController.getConnectState(bleDevice);
        }
        return BleConnectState.CONNECT_IDLE;
    }

    public boolean isConnected(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            return multipleBluetoothController.isContainDevice(bleDevice);
        }
        return false;
    }

    public void disconnect(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnect(bleDevice);
        }
    }

    public void disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnectAllDevice();
        }
    }

    public void destroy() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.destroy();
        }
    }

    public static int getRecordFormCount() {
        return recordFormCount;
    }

    public static void setRecordFormCount(int recordFormCount) {
        BleManager.recordFormCount = recordFormCount;
    }

    public String getRecordForm() {
        return recordForm;
    }

    public void setRecordForm(String recordForm) {
        BleManager.recordForm = recordForm;
    }

    /*public ResultComand getResultComand() {
        return resultComand;
    }
*/
    public ArrayList<String> getDeviceRequireInfoContent() {
        return deviceRequireInfoContent;
    }

    public void setDeviceRequireInfoContent(ArrayList<String> deviceRequireInfoContent) {
        this.deviceRequireInfoContent = deviceRequireInfoContent;
    }

   /* public static ArrayList<RecordForm> getDeviceRecordFromList() {
        return deviceRecordFromList;
    }*/

    /*public static void setDeviceRecordFromList(ArrayList<RecordForm> deviceRecordFromList) {
        BleManager.deviceRecordFromList = deviceRecordFromList;
    }*/

/* --------------------------------------------------------
       ----------------- 蓝牙指令返回的结果 ---------------------
       -------------------------------------------------------- */

//    /**
//     * 校验收到的指令
//     * 如果和校验码不符，则证明这个数据包的数据要主动丢弃，不进行处理响应
//     */
//    public boolean verfityCRCCode(String command, String verifyCode) {
//        if (TextUtils.isEmpty(command) && TextUtils.isEmpty(verifyCode))
//            BleLog.e("verfityCRCCode is failed to pass verify!");
//        return TextUtils
//                .equals(EventOperator.verifyCodeValied(command), verifyCode);
//    }
//
//    /**
//     * 蓝牙响应结果分发
//     */
//    public void dispatchResult(String command, String verifyCode) {
//        if (!verfityCRCCode(command, verifyCode))
//            return;
//        //
//        String resultComandContent = command.substring(10, command.length());
//        BleLog.e("resultComandContent " + resultComandContent);
//        //权限码
//        String rightCode = command.substring(8, 10);
//        BleLog.e("rightCode " + rightCode);
//        //命令码
//        String comandCode = command.substring(6, 8);
//        BleLog.e("comandCode " + comandCode);
//
//        ResultComand resultComand = new ResultComand("1002",
//                "a0", comandCode, rightCode, resultComandContent, verifyCode, "1003");
//        this.resultComand = resultComand;
//
//        //实际的蓝牙返回结果的处理
//        dipatchResultTag(resultComandContent, resultComand.getTag(), rightCode);
//    }
//
//    //请求命令类型
//    public  void dipatchResultTag(String resultComandContent, String tag, String rightCode) {
//
//        if (TextUtils.isEmpty(resultComandContent)
//                && TextUtils.isEmpty(tag))
//            return ;
//        switch (tag) {
//            case BLEOPERA_COMMAND_CONNRCT:
//                break;
//            case BLEOPERA_COMMAND_SEND_DEVICE_REQUIRE_INFO:
//                //发送设备需要的信息结果
//                Log.e("dipatchResultRType", "SEND_DEVICE_REQUIRE_INFO "
//                        + BleManager.getInstance()
//                        .sendDeviceRequireInfoResultList((resultComandContent)
//                                , false)); //IsShowRequireInfoBuffer 是否展示数据解析的log
//                break;
//            case BLEOPERA_COMMAND_GET_DEVICE_INFO:
//                //获取仪器信息指令结果
//                Log.e("dipatchResultRType", "SEND_DEVICE_REQUIRE_INFO "
//                        + BleManager.getInstance()
//                        .deviceInfoResultList((resultComandContent), false));
//                break;
//            case BLEOPERA_COMMAND_GET_DEVICE_RECORD_LIST:
//                //获取记录列表结果
//                Log.e("dipatchResultRType ", "GET_DEVICE_RECORD_LIST "
//                        + BleManager.getInstance()
//                        .recordFromResult(resultComandContent, false));
//                break;
//            case BLEOPERA_COMMAND_GET_DEVICE_RECORD:
//                //获取记录指令结果
//
//                BleLog.e("GET_DEVICE_RECORD "
//                        + BleManager.getInstance()
//                        .recordResultList(resultComandContent));
////                return BleManager.getInstance()
////                        .recordResultList(resultComandContent);
//                break;
//            case BLEOPERA_COMMAND_TIME_MANAGER:
//                if (TextUtils.equals(rightCode, "00")) {
//                    // 命令码00 ：读 获取设备校准时间
//                    BleLog.e("TIME_MANAGER "
//                            + BleManager.getInstance()
//                            .deviceDateAndTimeResult(resultComandContent, true));
//                } else if (TextUtils.equals(rightCode, "01")) {
//                    // 命令码01 ：写 校准设备时间
//                    BleManager.getInstance()
//                            .changeDateAndTimeResult(resultComandContent);
//                }
//                break;
//            case BLEOPERA_COMMAND_INSTANT_DATA:
//                //实时获取记录指令结果
//                BleLog.e("INSTANT_DATA "
//                        + BleManager.getInstance()
//                        .instantReceviceData(resultComandContent));
//
////                ArrayList< RecordForm> testArray = BleManager.getInstance()
////                        .instantReceviceData(resultComandContent);
//
//
//                break;
//            default:
//                break;
//        }
//    }

//    /**
//     * 连接状态 91
//     *
//     * @param ConnectResult
//     * @return boolean 判断是否与 "0000"相同
//     */
//    public boolean connectResult(String ConnectResult) {
////        10 02 A0 91 00 00 00 A7 C6 10 03 //响应建立连接和保持连接指令
////        10 02 A0 91 01 00 00 F6 06 10 03   //响应断开连接指令
////        1002A091000000A7C61003
//        return TextUtils.equals(ConnectResult, "0000");
//    }
//
//    /**
//     * 发送仪器用到的一些信息 93
//     * return String[]
//     */
//    public ArrayList<String> sendDeviceRequireInfoResultList(String target, boolean IsShowRequireInfoBuffer) {
//        ArrayList<String> arrayList;
//        arrayList = EventOperator
//                .formatDeviceRequireInfo(target
//                        , IsShowRequireInfoBuffer)
//                .getContent();
//        this.setDeviceRequireInfoContent(arrayList);
//        return arrayList;
//    }
//
//    /**
//     * 获取仪器信息指令 94
//     *
//     * @Return 单个设备信息
//     */
////    public Device getDeviceInfoResult(String getDeviceInfoResult) {
////
////        return new Device("", "", "", "",
////                "", "", "", "", "", "");
////    }
//
//    /**
//     * 获取仪器信息指令 94
//     *
//     * @Return 设备信息集合List
//     */
//    public ArrayList<Device> deviceInfoResultList(String getDeviceInfoResult, boolean addspace) {
//        return EventOperator.decodeDevceInfo(getDeviceInfoResult);
//    }
//
//    /**
//     * 获取记录信息列表头 95
//     *
//     * @Return 单个设备信息
//     */
//    public String recordFromResult(String getRecordFromResult, boolean addspace) {
//        recordFormCount = Integer.valueOf(getRecordFromResult.substring(4, 6), 16);
//        String formItemContent = getRecordFromResult.substring(6, getRecordFromResult.length());
//        recordForm = EventOperator.decodeFormItemName(formItemContent, addspace);
//        return recordForm;
//    }
//
//    /**
//     * 获取记录列表 96
//     *
//     * @Return 单个获取记录列表, 如果有多个默认返回集合的第一个Record类
//     */
//    public RecordForm recordResult(String getRecordResult) {
//        ArrayList<RecordForm> recordArrayList;
//        recordArrayList = EventOperator.decodeFormItem(getRecordResult);
//        setDeviceRecordFromList(recordArrayList);
//        return recordArrayList.get(0);
//    }
//
////    /**
////     * 获取记录列表 96
////     *
////     * @param startNum 开始序号
////     * @Return 单个获取记录列表, 如果有多个默认返回集合的第一个Record类
////     */
////    public RecordForm recordResult(String recordResult, String startNum) {
////        ArrayList<RecordForm> recordArrayList = new ArrayList<>();
////        recordArrayList = EventOperator.decodeFormItem(recordResult);
////
////        for (RecordForm recordFrom : recordArrayList) {
////            if (TextUtils.equals(recordFrom.getRecordFormNum(), startNum)) {
////                return recordArrayList.get(0);
////
////
////                break;
////            }
////        }
////        return recordArrayList.get(0);
////    }
//
//    /**
//     * 获取记录列表 96
//     *
//     * @Return 获取记录列表List
//     */
//    public ArrayList<RecordForm> recordResultList(String getRecordResultList) {
//        ArrayList<RecordForm> recordArrayList = EventOperator.decodeFormItem(getRecordResultList);
//        setDeviceRecordFromList(recordArrayList);
//        return BleManager.getInstance().getDeviceRecordFromList();
//    }
//
//    /**
//     * 校准日期与时间指令 97
//     *
//     * @Return
//     */
//    public boolean changeDateAndTimeResult(String changeDateAndTime) {
//        return EventOperator.changeDateAndTime(changeDateAndTime);
//    }
//
//    /**
//     * 获取仪器日期与时间
//     *
//     * @Return 获取记录列表List
//     */
//    public String deviceDateAndTimeResult(String getDateAndTime, boolean formate) {
//        return EventOperator.getDateAndTime(getDateAndTime, formate);
//    }
//
//    /**
//     * 实时获取记录数据指令 98
//     *
//     * @Return 获取记录列表List
//     */
//    public ArrayList<RecordForm> instantReceviceData(String getRecordResultList) {
//        return EventOperator.decodeFormItem(getRecordResultList);
//    }

}
