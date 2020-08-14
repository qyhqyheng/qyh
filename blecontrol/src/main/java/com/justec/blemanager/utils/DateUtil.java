package com.justec.blemanager.utils;

import android.content.Context;

import com.justec.blemanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String getTimeToDay(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate =new Date(System.currentTimeMillis());//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }
    public static String getTimeToSecond(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate =new Date(System.currentTimeMillis());//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }

    public static String DateFormatToDay(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(dateStr);//提取格式中的日期
        String str1 = sdf1.format(date);
        return str1;
    }

    public static String DateFormatToSecond(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        Date date = sdf.parse(dateStr);//提取格式中的日期
        String str1 = sdf1.format(date);
        return str1;
    }

    public static String getTimeToHHmm(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date curDate =new Date(System.currentTimeMillis());//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }

    /*
     * for historyFragment
     * */

    /*
    * 获取X轴时间刻度值
    * */
    public static String getXlabelTime(long spaceTime){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(System.currentTimeMillis()+spaceTime);//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }
    public static String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate =new Date(System.currentTimeMillis());//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }

    public static String getLastTime(int i){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate =new Date(System.currentTimeMillis()-1000*i);//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }

    /*
    * 比较两个时间参数的时间间隔，返回格式为ms的单位时间差
    * */
    public static Long getTimeSpace(String startTime,String endTime)  {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = 0;
        try {
            time = Math.abs(sdf.parse(startTime).getTime() - sdf.parse(endTime).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  time;
    }



    /*
    * for historyShowFragment
    * */
    //return ms毫秒数
    public static Long getTimeToMS(String time)   {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timeToMS = 0;
        try {
            timeToMS = sdf.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  timeToMS;
    }
    //return hh:mm形式时间
    public static String getHistoryXlabel(long spaceTime,String time) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(getTimeToMS(time)+spaceTime);//获取当前时间
        String str  =  formatter.format(curDate);
        return str;
    }
}
