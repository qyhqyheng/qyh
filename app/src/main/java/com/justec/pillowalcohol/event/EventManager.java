package com.justec.pillowalcohol.event;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.callback.BleNotifyCallback;
import com.justec.blemanager.callback.BleWriteCallback;

import com.justec.blemanager.messageEvent.AnalyzeData;
import com.justec.blemanager.messageEvent.NotificationDialog;
import com.justec.blemanager.messageEvent.ProductInfoEvent;
import com.justec.blemanager.messageEvent.ReadCalibrationData;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.exception.BleException;
import com.justec.blemanager.messageEvent.SettingUploadSpace;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.HexUtil;
import com.justec.common.CommonLog;
import com.justec.common.Interface.dataCallbackInterface;
import com.justec.pillowalcohol.media.MediaManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.UUID;

public class EventManager {
    private BleDevice bleDevice;
    //蓝牙写操作需要的类
    public BluetoothGattService writeService;
    public BluetoothGattCharacteristic bleWriteChara;

    public BluetoothGattCharacteristic ble_notify_chara;
    public BluetoothGattService notify_service;
    public boolean isInitFinsh = false;//是否已经完成初始化
    private StringBuffer data_receive = new StringBuffer();

    public dataCallbackInterface callback;

    /******返回蓝牙数据的处理*******/
    //查看返回结果
    public ResultCommand resultCommand;

    //上传命令计时器
    private long timeRecord = 0;
    public int receiveCount=0;
    //上传一次的数据总和跟总个数
    private int receiveDataCount = 0;
    private float receiveSumCount=0;
    //默认设置的警戒线值
    private float ALERT_LIMIT_CONSTANT = 2;
    private boolean isAlertStatu = false;

    ArrayList<String> product_info = new ArrayList<String>();

    private int SpaceTime = 1;
    public boolean bGetData = false;
    boolean bZeroCal = false;

    //单例模式
    public static EventManager getInstance() {
        return EventManager.EventManagerHolder.sEventManager;
    }
    private static class EventManagerHolder {
        private static final EventManager sEventManager = new EventManager();
    }

    public int getSpaceTime() {
        return SpaceTime;
    }

    public void setSpaceTime(int spaceTime) {
        SpaceTime = spaceTime;
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }
    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
    /**
     * 计算指令的校验码
     * 封装填充发送指令
     */
    public String checkCRC(String verifyCode) {
        if (TextUtils.isEmpty(verifyCode)){
            BleLog.e("verifyCode cannnot be null");
        }
        return EventOperator.verifyCodeValied(verifyCode);
    }

    public void initDeviceChara(BleDevice bleDevice, boolean isInitRespone) {
        try {
            BleLog.e("initDeviceChara---------");
            isInitFinsh = !isInitRespone;
            if (callback != null)
                this.callback = callback;//回调
            //获取信息，BluetoothGatt，BluetoothGattService，
            this.setBleDevice(bleDevice);

            BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

            notify_service = gatt
                    .getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            if (notify_service == null) {
                BleLog.e("Default notify_service maybe not allowed.");
                return;
            }
            ble_notify_chara = notify_service
                    .getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"));
            if (ble_notify_chara == null) {
                BleLog.e("Default ble_notify_chara maybe not allowed.");
                return;
            }
//        //发送命令
            writeService = gatt
                    .getService(UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"));
            if (writeService == null) {
                BleLog.e("Default write_service maybe not allowed.");
                return;
            }

            bleWriteChara = writeService
                    .getCharacteristic(UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb"));

            if (bleWriteChara == null) {
                BleLog.e("Default ble_write_chara maybe not allowed.");
                return;
            }
            //初始化监听通知特征值改变，读取指令返回的结果
            readFormCommandResult(bleDevice
                    , ble_notify_chara.getService().getUuid().toString()
                    , ble_notify_chara.getUuid().toString());
            Thread.sleep(1000);

            BleLog.e("TestingUpdataView---postSticky---MSG_DATA_INITIAL");
            EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_INITIAL,""));

            String ProductCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_PRODUCT_NAME);
            EventManager.getInstance().sendComand(ProductCode);

//            String AlarmCode = EventManager.getInstance().ReadCommandCode(ResultCommand.WRITE_ALARM_UPLOAD);
//            EventManager.getInstance().sendComand(AlarmCode);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //初始化监听通知特征值改变，读取指令返回的结果
    public void readFormCommandResult(final BleDevice bleDevice,
                                      String uuid_service,
                                      String uuid_notify) {
        //适当延时配合硬件的连接问题
        SystemClock.sleep(300);

        //初始化监听通知特征值改变，读取指令返回的结果
        BleManager.getInstance().notify(bleDevice
                , uuid_service
                , uuid_notify
                , new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        BleLog.i("onNotifySuccess");
                        //获取上传时间间隔
                        String SpaceTime = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_UPLOAD_DATA_SPACE);
                        EventManager.getInstance().sendComand(SpaceTime);

                        String SampleFrequence = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_SAMPLE_FREQUENCE);
                        EventManager.getInstance().sendComand(SampleFrequence);

                        SystemClock.sleep(500);
                        String commandCode = EventManager.getInstance().WriteCommandCode("0001", ResultCommand.WRITE_SETTING_DATA_UPLOAD_SPACE);
                        BleLog.e("WRITE_SETTING_DATA_UPLOAD_SPACE----sendComand = "+commandCode);
                        EventManager.getInstance().sendComand(commandCode);
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        BleLog.e("onNotifyFailure");
                    }
                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String result = HexUtil.formatHexString(data, false);
                        data_receive.append(result);
                        String commandCode = data_receive.toString().substring(6, 8);
//                        BleLog.e(" TestingBle------data_receive = "+data_receive+" commandCode = "+commandCode);
                        BleLog.e(" TestValue------Data_Receive = "+data_receive.toString());
                        if(data_receive.toString().contains("88aa1433")){
                            BleLog.e(" Calibrate---DataReceive = "+data_receive.toString());
                        }
                        dealwithResultTag(data_receive.toString(),commandCode);
                        data_receive.setLength(0);//清空resultBuffer
                    }
        });
    }

    public void sendComand(final String commandCode) {
        BleManager.getInstance().write(bleDevice, writeService.getUuid().toString(),
                bleWriteChara.getUuid().toString(), HexUtil.hexStringToBytes(commandCode),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        BleLog.i("write success, current: " + current + " total: " + total
                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                        BleLog.e(" TestValue------sendComand = "+commandCode);
                        if(commandCode.equals("88aa06330542")){
                            BleLog.e("Calibrate---sendComand = "+commandCode);
                        }
                        else if(commandCode.contains("88aa13c100")){
                            bZeroCal = true;
                            BleLog.e("Calibrate---Write = "+commandCode);
                        }
//                        BleLog.e("TestingBle---------SendcommandCode = "+commandCode);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        BleLog.e(exception.toString());
                        BleLog.e("onWriteFailure------");
                    }
                });
    }

    /**
    * 写操作命令格式 （Ascall码转16进制字符）
    * */

    public String WriteAscallCommandCode(String data,String code){
        int dataLen = HexUtil.StrAsciiToHex(data).length()/2;
        String CommandLen =Integer.toHexString(5+dataLen+1);//由于命令帧头2位，CRC 2位，命令码1位是固定的，所以固定为5位,自身1位
        for (int i = CommandLen.length(); i < 2; i++) {
            CommandLen = "0" + CommandLen; //如果一位则补零
        }
        String mCRC = checkCRC(CommandLen+code+HexUtil.StrAsciiToHex(data));
        //完整命令
        String writeCommand = "88aa"+CommandLen+code+HexUtil.StrAsciiToHex(data)+mCRC;
        BleLog.e("writeCommand = "+writeCommand);
        return writeCommand;
    }

    /*
    * 写操作命令格式 （十进制转16进制）
    * */
    public String WriteCommandCode(String data,String code){
        int dataLen;
        String data2Hex ;
        if(data.length()==4){
            dataLen = HexUtil.format10StringToHexString(data).length();
            data2Hex = HexUtil.format10StringToHexString(data);
        }else {
            dataLen = HexUtil.format10StringToHex(data).length();
            data2Hex = HexUtil.format10StringToHex(data);
        }
        BleLog.e("writeCommand---data2Hex="+data2Hex+"   dataLen="+dataLen);
        String CommandLen =String.valueOf(5+dataLen/2+1);//由于命令帧头2位，CRC 2位，命令码1位是固定的，所以固定为5位,自身1位
        for (int i = CommandLen.length(); i < 2; i++) {
            CommandLen = "0" + CommandLen; //如果一位则补零
        }
        String mCRC = checkCRC(CommandLen+code+data2Hex);
        //完整命令
        String writeCommand = "88aa"+CommandLen+code+data2Hex+mCRC;
        BleLog.e("writeCommand = "+writeCommand);
        return writeCommand;
    }
    /*
     * 写操作命令格式 （参数中包含16进制及10进制的混合命令）
     * */
    public String WriteFixedCommandCode(String data,String code){
        BleLog.e("data = "+data);
        int dataLen = data.length();
        String CommandLen = HexUtil.format10StringToHex(String.valueOf(6+dataLen/2));//由于命令帧头2位，CRC 2位，命令码1位是固定的，所以固定为5位,自身1位
        for (int i = CommandLen.length(); i < 2; i++) {
            CommandLen = "0" + CommandLen; //如果一位则补零
        }
        String mCRC = checkCRC(CommandLen+code+data);
        //完整命令
        String writeCommand = "88aa"+CommandLen+code+data+mCRC;
        BleLog.e("writeCommand = "+writeCommand);
        return writeCommand;
    }

    /*
    * 读操作命令格式
    * */
    public String ReadCommandCode(String code){
        String CommandLen =String.valueOf(5+1);//由于命令帧头2位，CRC 2位，命令码1位是固定的，所以固定为5位,自身1位
        for (int i = CommandLen.length(); i < 2; i++) {
            CommandLen = "0" + CommandLen; //如果一位则补零
        }
        String mCRC = checkCRC(CommandLen+code);
        String readCommand = "88aa"+CommandLen+code+mCRC;   //完整命令
        BleLog.e("readCommand = "+readCommand);
        return readCommand;
    }

    /* --------------------------------------------------------
 ----------------- 蓝牙指令返回的结果 ---------------------
 -------------------------------------------------------- */
    //对蓝牙数据进行分发处理
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean dealwithResultTag(String result,String code) {
        if (TextUtils.isEmpty(result))
            return false;
        //数据分发
        return dealwithResult(result.substring(0, result.length() - 4),
                result.substring(result.length() - 4, result.length()),code);
    }

    /**
     * 校验收到的指令
     * 如果和校验码不符，则证明这个数据包的数据要主动丢弃，不进行处理响应
     */
    public boolean verfityCRC(String command, String verifyCode) {
        if (TextUtils.isEmpty(command) && TextUtils.isEmpty(verifyCode))
            BleLog.e("checkCRC is failed to pass verify!");
        return TextUtils
                .equals(EventOperator.verifyCodeValied(command), verifyCode);
    }

    /**
     * 蓝牙响应结果分发处理结果
     */

    public boolean dealwithResult(String command, String verifyCode,String code) {
        String CommandValue = command.substring(4,command.length());
        if (!verfityCRC(CommandValue, verifyCode)) {
            BleLog.e("verfityCRC fail");
            return false;
        }
        //resultComandContent为有效数据(剔除crc，帧头，数据长度等)
//        String resultComandContent = command.substring(8, command.length());  //旧仪器
        String resultComandContent = command.substring(8, command.length());
        BleLog.e("resultComandContent " + resultComandContent+"    CommandValue ="+CommandValue);
        String commandLen = command.substring(4,6);
        String comandCode = command.substring(6, 8);   //命令码
        ResultCommand resultComand = new ResultCommand("88aa",commandLen,comandCode, resultComandContent, verifyCode);
        this.resultCommand = resultComand;
        dipatchResultTag(resultComandContent, resultComand.getTag());    //实际的蓝牙返回结果的处理
        return true;
    }

    public void dipatchResultTag(String resultComandContent, String tag) {
        BleLog.e("tag =" + tag+"    resultComandContent="+resultComandContent);
        if(tag.equals("34"))
            BleLog.e("TestingUpdataView------dipatchResultTag");
        if (TextUtils.isEmpty(resultComandContent) && TextUtils.isEmpty(tag))
            return;
        switch (tag) {
            case ResultCommand.WRITE_CALIBRATION_VALUE:
                processCalibrationResult(resultComandContent);
                break;
            case ResultCommand.WRITE_TIME_CAL:
                TimeCal(resultComandContent);
                break;
            case ResultCommand.READ_UPLOAD_DATA:
//                String resultComandContent = resultComandContent.substring(4,resultComandContent.length());
                if(bGetData)
                  dealUploadData(resultComandContent);
                break;
            case ResultCommand.READ_CALIBRATION_FACTOR:
                readCalibrationResult(resultComandContent,ResultCommand.READ_CALIBRATION_FACTOR);
                break;
            case ResultCommand.READ_CALIBRATION_ZERO :
                readCalibrationResult(resultComandContent,ResultCommand.READ_CALIBRATION_ZERO);
                break;
            case ResultCommand.READ_UPLOAD_DATA_SPACE:
                readUploadDataSpace(resultComandContent);
                break;
            case ResultCommand.READ_SAMPLE_FREQUENCE:
                readSampleFrequence(resultComandContent);
                break;
            case ResultCommand.READ_PRODUCT_NAME:
                String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_PRODUCT_SERIAL);
                EventManager.getInstance().sendComand(commandCode);
                getProductName(resultComandContent);
                break;
            case ResultCommand.READ_PRODUCT_SERIAL:
                String commandSerialCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_SOFTWARE_VERSION);
                EventManager.getInstance().sendComand(commandSerialCode);
                getProductSerial(resultComandContent);
                break;
            case ResultCommand.READ_SOFTWARE_VERSION:
                getSoftwareVersion(resultComandContent);
                break;
            case ResultCommand.READ_ALARM_LIMITE:
                getAlarmLimite(resultComandContent);
                break;
            case ResultCommand.WRITE_ALARM_LIMITE:
                BleLog.e("MediaAction----WRITE_ALARM_LIMITE");
                setAlarmLimite(resultComandContent);
                EventManager.getInstance().sendComand(ReadCommandCode(ResultCommand.READ_ALARM_LIMITE));
                break;

            case ResultCommand.WRITE_ALARM_UPLOAD:
                BleLog.d("WRITE_ALARM_UPLOAD---resultComandContent="+resultComandContent);
                break;

            case ResultCommand.WRITE_SETTING_SAMPLE_FREQUENCE:
                BleLog.e("WRITE_SETTING_SAMPLE_FREQUENCE---resultComandContent="+resultComandContent);
                break;
            case ResultCommand.WRITE_SETTING_DATA_UPLOAD_SPACE:
                BleLog.e("WRITE_SETTING_DATA_UPLOAD_SPACE---resultComandContent="+resultComandContent);
                break;

            case ResultCommand.ALARM_OPEN_CLOSE:
                //ToDo 报警开关事件处理
                DealWithAlarData(resultComandContent);
                BleLog.d("ALARM_OPEN_CLOSE---resultComandContent="+resultComandContent);
                break;

            case ResultCommand.WRITE_BLUTH_NAME:
                Log.e("WRITE_BLUTH_NAME","===藍牙設置---resultComandContent="+resultComandContent);
                //ToDo 报警开关事件处理
                EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_WRITE_BLUTH_NAME,resultComandContent));
                break;
            default:
                break;
        }
    }

    private void TimeCal(String resultComand){
        try {
            if(!resultComand.equals("01")){
                String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.WRITE_TIME_CAL);
                EventManager.getInstance().sendComand(commandCode);
                BleLog.e("TimeCal-=--------resultComand="+resultComand);
            }

        }
        catch (Exception e){
            BleLog.e("TimeCal------"+e.toString());
        }
    }

    /**
     * 处理0xA0命令，获取报警关闭事件
     * */
    private void DealWithAlarData(String resultComandContent){
        String result;
        if(resultComandContent.contains("1")){
            result = "1";
        }else {
            result = "0";
        }
        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_AlarmEnAble,result));
    }

    /*
    * 处理0x10命令，获取产品名称
    * */
    private void getProductName(String resultComandContent){
        String pdName = HexUtil.asciiToStr(resultComandContent);
        product_info.add(pdName);
        BleLog.e("pdName = "+ pdName);
        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_PRUNAME,pdName));
    }
    /*
     * 处理0x11命令，获取产品名称
     * */
    private void getProductSerial(String resultComandContent){
        String pdSerial = HexUtil.asciiToStr(resultComandContent);
        product_info.add(pdSerial);
        BleLog.e("pdSerial = "+ pdSerial);
        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_SERIZABLE,pdSerial));
    }
    /*
     * 处理0x13命令，获取产品名称
     * */
    private void getSoftwareVersion(String resultComandContent){
        String pdSoftwareVer = HexUtil.asciiToStr(resultComandContent);
        product_info.add(pdSoftwareVer);
        BleLog.e("pdSoftwareVer = "+ pdSoftwareVer);
        EventBus.getDefault().postSticky(new ProductInfoEvent(product_info));
        product_info.clear();
    }
    /*
     * 处理自动上传命令0xA0获取到的数据
     * */
    private void dealUploadData(String resultComandContent){
        String result = resultComandContent.substring(4,resultComandContent.length());
        String device = resultComandContent.substring(0,2);  //ResultCommand.DEVICE_MAIN
//        BleLog.e("Testing----dealUploadData000---resultComandContent="+resultComandContent+"   result="+result+"   device="+device);
        if(device.compareTo(ResultCommand.DEVICE_MAIN)==0){      //主机
//            BleLog.e("Testing----dealUploadData111---result="+result);
//            analyzeUploadData(result);
            GetResultCount(result);
            if(getPerDataAverage(result)>ALERT_LIMIT_CONSTANT){
                if(!isAlertStatu)
//                    EventBus.getDefault().postSticky(new SettingUploadSpace((float) 1.0));
                isAlertStatu = true;
                //ResultUploadData(receiveSumCount,receiveDataCount);
            }else {
                if(isAlertStatu){
                    //重新获取上传时间间隔当从超警戒状态恢复到正常状态时
                    String commandCode = ReadCommandCode(ResultCommand.READ_UPLOAD_DATA_SPACE);
                    sendComand(commandCode);
                    isAlertStatu = false;
                }
            }
//            ResultUploadData(receiveSumCount,receiveDataCount);
//            BleLog.e("Testing------dealUploadData222-----result = "+result+"    receiveCount = "+receiveCount);
            EventBus.getDefault().postSticky(new AnalyzeData(result,receiveCount));
//            receiveCount++;
        }
    }
    /*
    * 处理校准命令0xC1
    * */
    private void processCalibrationResult(String resultComandContent) {
        String statu = resultComandContent.substring(0,2);
        if(TextUtils.equals(statu, "01")){
            BleLog.e("Calibrate---开始校准");
        }
        else if(TextUtils.equals(statu, "02")){
            BleLog.e(" Calibrate---校准完成---bZeroCal="+bZeroCal);
            EventBus.getDefault().postSticky(new NotificationDialog("2"));
            //setting success,and send message for get calibration factor
            if(bZeroCal){
                bZeroCal = false;
                String commandCode1 = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_CALIBRATION_ZERO);
                EventManager.getInstance().sendComand(commandCode1);
            }else {
                String commandCode = ReadCommandCode(ResultCommand.READ_CALIBRATION_FACTOR);
                sendComand(commandCode);
            }

        }else {
            EventBus.getDefault().postSticky(new NotificationDialog("0"));
            BleLog.e("Unknow error !");
        }
    }
    /*
     * 处理自动上传命令0xA0 得到每一次数据的平均值
     * */
    private float getPerDataAverage(String resultComandContent) {
        String contentValue = resultComandContent.substring(2,resultComandContent.length());
        int PerDataCount =contentValue.length()/2;
        float PerReceiveSum = 0;

        for(int i=0;i<contentValue.length();i=i+2 ){
            String perData = contentValue.substring(i,i+2);
            float temp = HexUtil.formatHexStringTo10Int(perData);
            float realValue = temp/100;
            PerReceiveSum = PerReceiveSum+temp;
        }
        //EventBus.getDefault().postSticky(new AnalyzeData(receiveSumCount/receiveDataCount,receiveCount));
        BleLog.d("PerReceiveSum/PerDataCount = "+PerReceiveSum/PerDataCount);
        return PerReceiveSum/PerDataCount;
    }

    private void GetResultCount(String result){
        try {
            if(result.length()%2!=0){
                receiveCount = 0;
                return ;
            }
            receiveCount = result.length()/2;
        }
        catch (Exception e){
            CommonLog.e("GetResultCount------"+e.toString());
        }
    }

    /*
     * 处理自动上传命令0xA0 得出上传总值跟个数
     * */
    private void analyzeUploadData(String resultComandContent) {
//        String contentValue = resultComandContent.substring(2,resultComandContent.length());
        String contentValue = resultComandContent;
        receiveDataCount = receiveDataCount+contentValue.length()/2;
        for(int i=0;i<contentValue.length();i=i+2 ){
            String perData = contentValue.substring(i,i+2);
            float temp = HexUtil.formatHexStringTo10Int(perData);
            float realValue = temp/100;
            receiveSumCount = receiveSumCount+temp;
        }
        //EventBus.getDefault().postSticky(new AnalyzeData(receiveSumCount/receiveDataCount,receiveCount));*/
        BleLog.e("Testing----analyzeUploadData----contentValue = "+contentValue+" receiveDataCount = "+receiveDataCount+"    receiveSumCount = "+receiveSumCount);
    }
    /*
     * 处理自动上传命令0xA0 计算出平均值传值界面显示
     * */
    private void ResultUploadData(float sum,int count){
        //总数清零
        receiveDataCount = 0;
        receiveSumCount = 0;
        BleLog.d("ResultUploadData000-----sum = "+sum+"     count="+count);
        float  average_value = sum/count;
        BleLog.d("ResultUploadData000-----average_value = "+average_value);
//        EventBus.getDefault().postSticky(new AnalyzeData(average_value,receiveCount));
    }
    /*
     * 处理读取上传时间间隔命令0x31
     * */
    private void readUploadDataSpace(String value){
        String valueSpace = HexUtil.formatHexStringTo10String(value);
        //callback.dataCallback(valueSpace);
        BleLog.e("getUploadSpace---DATA_UPLOAD_SPACE---valueSpace = "+valueSpace);
        EventBus.getDefault().postSticky(new SettingUploadSpace(Integer.valueOf(valueSpace)));
    }

    private void readSampleFrequence(String value){
        String valueSpace = HexUtil.formatHexStringTo10String(value);
        //callback.dataCallback(valueSpace);
        BleLog.e("readSampleFrequence---SAMPLE_FREQUENCE---valueSpace = "+valueSpace);
    }

    /*
     * 处理校准
     * */
    private void  readCalibrationResult(String content,String dif) {
        if(content==null){
            BleLog.d("readCalibrationResult error");
            return;
        }
        String resultComandContent = content.substring(0,4);
        float calibrationValue = HexUtil.formatHexStringTo10Int(resultComandContent);
        String dataValue = content.substring(4,content.length());
        BleLog.e("Calibrate---readCalibrationResult---dataValue = "+dataValue+"   dif="+dif);
        EventBus.getDefault().postSticky(new ReadCalibrationData(calibrationValue/100,HexUtil.asciiToStrforTime(dataValue),dif));
    }

    /*
     * 处理读取报警限值命令0x34
     * */
    private void getAlarmLimite(String value){
        int valueSpace = HexUtil.formatHexStringTo10Int(value);
        MediaManager.getInstance().mediaInterface.AlarmValue(valueSpace);
//        Common.getInstance().setAlarmValue(String.valueOf(valueSpace));
        ALERT_LIMIT_CONSTANT = (float) valueSpace;
        BleLog.e("TestingUpdataView---getAlarmLimite---ALERT_LIMIT_CONSTANT = "+ALERT_LIMIT_CONSTANT+"---callback="+callback);
        if(callback!=null){
            callback.dataCallback(String.valueOf(valueSpace));
            callback.OverTime(true);
            bGetData = true;
        }
    }
    /*
     * 处理设置报警限值命令0x84
     * */
    private void setAlarmLimite(String value){
        BleLog.d("value = "+value);
        if(value.equals("01"))
            EventBus.getDefault().postSticky(new NotificationDialog("10001",true));

    }
    public void initCallback(dataCallbackInterface callback){
        if (callback != null)
            this.callback = callback;//回调
    }
    public void destroy() {
        if (callback != null)
            callback = null;
    }
}
