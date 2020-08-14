package com.justec.blemanager.messageEvent;

import com.justec.blemanager.utils.BleLog;

/**
 * Created by jerry.xiao 2018-10-12
 */
public class ResultCommand {

        //设备返回命令码
        //对蓝牙返回的数据进行处理
        public static final String WRITE_CALIBRATION_VALUE = "c1";
        public static final String READ_UPLOAD_DATA = "a0";
        public static final String WRITE_ALARM_UPLOAD = "a1";
        public static final String ALARM_OPEN_CLOSE = "a2";
        public static final String WRITE_TIME_CAL = "c2";

        public static final String WRITE_SETTING_SAMPLE_FREQUENCE = "80";
        public static final String WRITE_SETTING_DATA_UPLOAD_SPACE = "81";
        public static final String WRITE_ALARM_LIMITE = "84";
        public static final String WRITE_PRODUCT_NAME= "60";
        public static final String WRITE_PRODUCT_SERIAL= "61";
        public static final String WRITE_SOFTWARE_VERSION = "63";
        public static final String WRITE_BLUTH_NAME = "64";

        public static final String READ_PRODUCT_SERIAL= "11";
        public static final String READ_PRODUCT_NAME = "10";
        public static final String READ_SOFTWARE_VERSION = "13";

        public static final String READ_SAMPLE_FREQUENCE = "30";
        public static final String READ_UPLOAD_DATA_SPACE = "31";
        public static final String READ_ALARM_LIMITE = "34";
        public static final String READ_CALIBRATION_FACTOR = "33";
        public static final String READ_CALIBRATION_ZERO = "36";

        public static final String DEVICE_MAIN = "00";
        public static final String DEVICE_SLAVE = "01";

        //帧头
        String resultstart;
        //长度
        String commandLen;
        //命令码
        String comandCode;
        //Tag 标识命令类型
        String tag;
        //内容
        String resultComandContent;
        //校验码
        String verifyCode;

    public ResultCommand(String resultstart, String commandLen, String comandCode,
            String resultComandContent, String verifyCode) {
            this.resultstart = resultstart;
            this.commandLen = commandLen;
            this.comandCode = comandCode;
            this.resultComandContent = resultComandContent;
            this.verifyCode = verifyCode;

            switch (comandCode) {
            case READ_PRODUCT_NAME:
                //读产品名称
                this.tag = READ_PRODUCT_NAME;
                break;
            case READ_PRODUCT_SERIAL:
                //读产品序列号
                this.tag = READ_PRODUCT_SERIAL;
                break;
            case READ_SOFTWARE_VERSION:
                //读产品软件版本
                this.tag = READ_SOFTWARE_VERSION;
                break;
            case WRITE_CALIBRATION_VALUE:
                //校准值命令
                this.tag = WRITE_CALIBRATION_VALUE;
            break;

            case WRITE_BLUTH_NAME:
                //修改蓝牙
                this.tag = WRITE_BLUTH_NAME;
                break;

            case READ_UPLOAD_DATA:
                //上传数据
                this.tag = READ_UPLOAD_DATA;
                break;
            case WRITE_TIME_CAL:
                this.tag = WRITE_TIME_CAL;     //时间校准
                break;
            case WRITE_ALARM_UPLOAD:
                //报警/报警关闭事件
                this.tag = WRITE_ALARM_UPLOAD;
                break;//
            case WRITE_SETTING_DATA_UPLOAD_SPACE:
                //设置数据上传间隔
                this.tag = WRITE_SETTING_DATA_UPLOAD_SPACE;
                break;
            case WRITE_SETTING_SAMPLE_FREQUENCE:
                //设置数据上传间隔
                this.tag = WRITE_SETTING_SAMPLE_FREQUENCE;
                break;
            case READ_ALARM_LIMITE:
                //读报警阈值
                this.tag = READ_ALARM_LIMITE;
                BleLog.e("TestingUpdataView----this.tag = READ_ALARM_LIMITE");
                break;
            case READ_CALIBRATION_FACTOR:
                //读校准系数
                this.tag = READ_CALIBRATION_FACTOR;
                break;
                case READ_CALIBRATION_ZERO:
                    this.tag = READ_CALIBRATION_ZERO;
                    break;
            case READ_SAMPLE_FREQUENCE:
                //读采样频率
                this.tag = READ_SAMPLE_FREQUENCE;
                break;
            case READ_UPLOAD_DATA_SPACE:
                //读上传间隔
                this.tag = READ_UPLOAD_DATA_SPACE;
                break;
            case WRITE_ALARM_LIMITE:
                //设置报警限值
                this.tag = WRITE_ALARM_LIMITE;
                break;
            case WRITE_PRODUCT_NAME:
                //设置产品名称
                this.tag = WRITE_PRODUCT_NAME;
                break;
            case WRITE_PRODUCT_SERIAL:
                //设置产品序列号
                this.tag = WRITE_PRODUCT_SERIAL;
                break;
            case WRITE_SOFTWARE_VERSION:
                //设置软件版本
                this.tag = WRITE_SOFTWARE_VERSION;
                break;

            case ALARM_OPEN_CLOSE:
                //报警声音开启/关闭事件
                this.tag = ALARM_OPEN_CLOSE;
                break;
            default:
            break;
            }
        }

        public String getTag() {
        return tag;
        }

        public void setTag(String tag) {
        this.tag = tag;
        }

    }

