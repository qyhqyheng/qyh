package com.justec.pillowalcohol.event;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ServiceManager {
private static final String URL ="http://121.15.182.147:8089/";
    private static final String NAME_SPACE ="http://tempuri.org/";
    private static final String methodName ="UploadDate ";

    //单例模式
    public static ServiceManager getInstance() {
        return ServiceManager.ServiceManagerHolder.mserviceManager;
    }

    private static class ServiceManagerHolder {
        private static final ServiceManager mserviceManager = new ServiceManager();
    }
    /**
     * 请求webservice的步骤
     * @param dataList  传给webservice的参数。
     * @return
     */
    public  String uploadToWebService(ArrayList<String[]> dataList,String startTime,String SN) {
        Log.d("Jerry.Xiao","uploadToWebService");
        String result = "";
        //（1）创建HttpTransportSE对象，该对象用于调用WebService操作
        HttpTransportSE httpTransportSE = new HttpTransportSE(URL,90*1000);
        //（2）创建SoapSerializationEnvelope对象
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        //（3）创建SoapObject对象，创建该对象时需要传入所要调用的Web Service的命名空间和WebService方法名
        SoapObject request = new SoapObject(NAME_SPACE, methodName);
        //(4) //填入需要传入的参数
      /* Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            String key = (String) iterator.next();
            String value = map.get(key);
            request.addProperty(key,value);
        }*/
      for(int i =0;i<dataList.size();i++){
          request.addProperty("strJson",dataList.get(i).toString());
      }
        //request.addProperty("strJson",dataList);
        request.addProperty("starttime",startTime);
        request.addProperty("sn",SN);

        //（5）调用SoapSerializationEnvelope的setOutputSoapObject()方法，或者直接对bodyOut属性赋值，
        //将前两步创建的SoapObject对象设为SoapSerializationEnvelope的传出SOAP消息体
        envelope.bodyOut = request;
//        envelope.setOutputSoapObject(request);
        try {
            //（6）调用对象的call()方法，并以SoapSerializationEnvelope作为参数调用远程的web service
            httpTransportSE.call(NAME_SPACE+methodName, envelope);//调用
            if (envelope.getResponse() != null) {
                result = envelope.getResponse().toString().trim();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        //解析该对象，即可获得调用web service的返回值
        return result;
    }

    public String[] setStrJson(String time, String value){
        String[] str = {"Time:"+ time,"VALUE:"+ value};
        return str;
    }

    public ArrayList<String[]> getJsonList(ArrayList<String> strTime,ArrayList<String> strValue){
        ArrayList<String[]> strJsonList = new ArrayList<>();
        for(int i =0;i<strTime.size();i++){
            strJsonList.add(setStrJson(strTime.get(i),strValue.get(i)));
        }
        return strJsonList;
    }

}
