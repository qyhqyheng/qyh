package com.justec.blemanager.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HexUtil {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        if (data == null)
            return null;
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }


    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    public static String formatHexString(byte[] data) {
        return formatHexString(data, false);
    }

    public static String formatHexString(byte[] data, boolean addSpace) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
            if (addSpace)
                sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String extractData(byte[] data, int position) {
        return HexUtil.formatHexString(new byte[]{data[position]});
    }

    //将16进制String转化为10进制Int
    public static int formatHexStringTo10Int(String hexString) throws NumberFormatException {
//        formatHexStringTo10String(hexString)
        return Integer.valueOf(hexString, 16);
    }

    //16进制String转化为10机制String
    public static String formatHexStringTo10String(String hexString) throws NumberFormatException {
        return Integer.valueOf(hexString, 16).toString();
    }

    // 计算16进制的10进制的值
    public static String formatHexStringTo10Value(String hexString) {
        int result = 0;
        for (int i = 0; i <= hexString.length() / 2; i = i + 2) {
            result = result + Integer.valueOf(hexString.substring(i, i + 2), 16)
                    * (int)Math.pow(16, i);//pow：Java 幂运算
        }
        return String.valueOf(result);
    }



    // 10进制String转化为16进制String
//    public static String format10StringToHexString(String intString) throws NumberFormatException {
//        return String.format("%02X", Integer.valueOf(intString).byteValue());//两位16进制数
//    }

    // 10进制String转化为16进制String (转为两位)
    public static String format10StringToHexString(String intString) throws NumberFormatException {
        int test=Integer.valueOf(intString);

        String verifyResult_1 = Integer.toHexString(test / 256);//翻转移位
        for (int i = verifyResult_1.length(); i < 2; i++) {
            verifyResult_1 = "0" + verifyResult_1; //补零
        }
        String verifyResult_2 = Integer.toHexString(test % 256);//翻转移位
        for (int i = verifyResult_2.length(); i < 2; i++) {
            verifyResult_2 = "0" + verifyResult_2;   //补零
        }
        return verifyResult_1 + verifyResult_2;
    }
    // 10进制String转化为16进制String (转为一位)

    public static String format10StringToHex(String intString) throws NumberFormatException {
        int test=Integer.valueOf(intString);
        String verifyResult_2 = Integer.toHexString(test % 256);//翻转移位
        for (int i = verifyResult_2.length(); i < 2; i++) {
            verifyResult_2 = "0" + verifyResult_2;   //补零
        }
        return  verifyResult_2;
    }
    // 10进制String转化为16进制int
    public static int format10StringTo16HexInt(String intString) throws NumberFormatException {
        //return Integer.parseInt(intString,16);
        return Integer.valueOf(format10StringToHexString(intString));
    }

    // 10进制Int转化为16进制String
    public static String format10IntTo16HexString(String intString) throws NumberFormatException {
        //return Integer.parseInt(intString,16);
        return Integer.toHexString(Integer.valueOf(intString));
    }

    // 10进制Int转化为16进制String
    public static String format10IntTo16HexString(int intString) throws NumberFormatException {
        //return Integer.parseInt(intString,16);
        return Integer.toHexString(intString);
    }

    //16进制String转化为10进制char
    public static String formatHexStringTo10Char(String hexString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexString.length() - 1; i += 2) {
            String output = hexString.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    //16进制字符串转化为GBK的String
    public static String formatHexStringToGBKString(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        byte[] baKeyword = new byte[hexString.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        hexString.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            hexString = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return hexString;
    }

    //确保精度为小数点3三位
    public static  double parseDataWithPointFour(String content) {
        return new BigDecimal(content).setScale(3,
                BigDecimal.ROUND_DOWN)//BigDecimal.ROUND_DOWN 直接去掉多余的位数
                .doubleValue();

    }

    /**
     *  转换16进制的小端
     * @param wCRC 目标10进制int值
     * @return
     */
    public static String verifyNum(int wCRC) {
//		int wCRC = 0xFFFF;
        String verifyResult_1,verifyResult_2;
        verifyResult_1 = Integer.toHexString(wCRC % 256);// 翻转移位
        for (int i = verifyResult_1.length(); i < 2; i++) {
            verifyResult_1 = "0" + verifyResult_1; // 补零
        }
//        System.err.println("hex verifyResult_1 " + verifyResult_1);
        verifyResult_2 = Integer.toHexString(wCRC / 256);// 翻转移位
        for (int i = verifyResult_2.length(); i < 2; i++) {
            verifyResult_2 = "0" + verifyResult_2; // 补零
        }
//        System.err.println("hex verifyResult_2 " + verifyResult_2);
        return verifyResult_2+verifyResult_1;//高位在前
    }


    /**
     * 获取当前时间
     * SimpleDateFormat : yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(d);
    }
    /**
     * 获取当前时间以固定模式
     * SimpleDateFormat : yyyyMMddHHmmss
     * @return
     */
    public static String getFixedCurrentTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(d);
    }

    /**
     * @author JerryXiao
     * @param scr
     * @return 转化后的
     */
    public static String StrToDate(String scr){
        if(scr.length()<10){return "";}
        StringBuffer str = new StringBuffer(scr);
        str.insert(12, ":");
        str.insert(10, ":");
        str.insert(8, " ");
        str.insert(6, "-");
        str.insert(4, "-");
        String marStrNew = str.toString();
        return marStrNew;
    }

    /**
     * 获取当前时间，以一定时间日期格式拿到对应的时间String
     * SimpleDateFormat : yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime(String formatScr){
        SimpleDateFormat sdf = new SimpleDateFormat(formatScr);
        String date = sdf.format(System.currentTimeMillis());
        return date;
    }
    //16进制转10进制
    public static String hexStr2Str(String hexStr)
    {
        if(hexStr == null){
            Log.e("Utils","数据为空");
            return null;
        }
        String str = "0123456789abcdef";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        String myStr ="";
        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);

            if(n <10){
                myStr += "0"+bytes[i];//十位补零
            }else{

                myStr+=bytes[i];
            }
        }
        return "20"+myStr;//例：结果显示180809101223 所以加了头部20 显示2018
    }

    public static String StrToHex(String hexStr){
        String str = "0123456789abcdef";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n,DigTemp,TenDigTemp;
        String myStr = "";
        for (int i = 1; i < hexs.length/2; i++)//年号2018中20不处理
        {
            String s= String.valueOf(hexs[2 * i])+ String.valueOf(hexs[2 * i+1]);
            n = Integer.parseInt(s);
            if(n <10){
                myStr += s;//十位补零
            }else{
                TenDigTemp = n / 16;
                DigTemp = n % 16;
                if(DigTemp >9){
                    myStr+= String.valueOf(TenDigTemp)+str.charAt(DigTemp);
                }else{
                    myStr+= String.valueOf(TenDigTemp)+ String.valueOf(DigTemp);
                }
            }
        }
//        Log.d("TAG","myStr   =  "+myStr);
        return myStr;
    }
    public static String asciiToStrforTime(String value){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<value.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = value.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }
        Log.d("Jerry.Xiao","sb.toString() = "+sb.toString());
        return "20"+sb.toString();//时间显示1810241947 加20显示完全
    }
    public static String asciiToStr(String value){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<value.length()-1; i+=2 ){
            //grab the hex in pairs
            String output = value.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);
            temp.append(decimal);
        }
        return sb.toString();//时间显示1810241947 加20显示完全
    }
    public static String StrAsciiToHex(String str) {
        char[] chars = str.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        Log.d("Jerry.Xiao","hex.toString() = "+hex.toString());
        return hex.toString();
    }
    /*int chrtodec(char chr)

    {
        int value=0;
        //先全部将小写转换为大写
        if((chr>='a')&&(chr<='z'))

            chr= (char) (chr-32);
        //将字符转化成相应的数字
        if((chr>='0')&&(chr<='9'))
            value=chr-48;
        else if((chr>='A')&&(chr<='Z'))
            value=chr-65+10;
        return value;
    }*/

}