package com.justec.pillowalcohol.event;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.HexUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EventOperator {

    //校验值
    private static final int s_crc16_talble[] =
            {
                    0x0000, 0xCC01, 0xD801, 0x1400, 0xF001, 0x3C00, 0x2800, 0xE401,
                    0xA001, 0x6C00, 0x7800, 0xB401, 0x5000, 0x9C01, 0x8801, 0x4400
            };


    //单例模式
    public static EventOperator getInstance() {
        return EventOperatorHolder.eventOperatorManger;
    }

    private static class EventOperatorHolder {
        private static final EventOperator eventOperatorManger = new EventOperator();
    }

    //初始化
    public void init(Application app) {

//            BleManager.getInstance().getBleBluetooth();
//            if (context == null && app != null) {
//            context = app;
//            BluetoothManager bluetoothManager = (BluetoothManager) context
//                    .getSystemService(Context.BLUETOOTH_SERVICE);
//            if (bluetoothManager != null)
//                bluetoothAdapter = bluetoothManager.getAdapter();
//            multipleBluetoothController = new MultipleBluetoothController();
//            bleScanRuleConfig = new BleScanRuleConfig();
//            bleScanner = BleScanner.getInstance();
//        }
    }

    //处理帧帧尾
    public static String resultCommandContent(String str) {
        if ((TextUtils.isEmpty(str)) &&
                (str.length() > 0))
            throw new RuntimeException("data cannot be null 0.0");
        str = str.substring(8);
        str = str.substring(0, str.length() - 4);
        return str;
    }

    /**
     * 校验码获取
     *
     * @param command
     * @return 返回16进制的String，校验码
     */
    public static String verifyCodeValied(String command) {
        int wCRC = 0xFFFF;
        char chChar = 0;
        String verifyResult_1 = "", verifyResult_2 = "";
        // i = i + 2 移位,硬件需要两位字节标识
        for (int i = 0; i < command.length() - 1; i = i + 2) {
            int temp = Integer.valueOf(command.substring(i, i + 2), 16);
            chChar = (char) temp;
            wCRC = s_crc16_talble[(chChar ^ wCRC) & 15] ^ (wCRC >> 4);
            wCRC = s_crc16_talble[((chChar >> 4) ^ wCRC) & 15] ^ (wCRC >> 4);
        }
//        verifyResult_1 = Integer.toHexString(wCRC % 256);//翻转移位
//        for (int i = verifyResult_1.length(); i < 2; i++) {
//            verifyResult_1 = "0" + verifyResult_1;   //补零
//        }
//        verifyResult_2 = Integer.toHexString(wCRC / 256);//翻转移位
//        for (int i = verifyResult_2.length(); i < 2; i++) {
//            verifyResult_2 = "0" + verifyResult_2; //补零
//        }
//        return verifyResult_1 + verifyResult_2;
        Log.e("Jerry.Xiao","wCRC = "+HexUtil.verifyNum(wCRC));
        return HexUtil.verifyNum(wCRC);
    }

}
