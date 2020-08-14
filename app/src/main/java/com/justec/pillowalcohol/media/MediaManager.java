package com.justec.pillowalcohol.media;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.activity.MainActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sunny.Qin 2020/3/4
 * */

public class MediaManager{

    public boolean bValibrate;
    public boolean bAlarm;
    public int SonesIndex;
    public int AlarmValue;
    public boolean LimiteFlag = false;
    public boolean PlayStopFlag = false;
    public boolean TestStatus = false;
    boolean HighAlarmStatus = false;    //是否处于报警状态

    public List<SonesConfigue> SonesList;
    int SonesSize = 0;
    Uri uri;
    MediaPlayer mediaPlayer;
    String[] name_items;

    Vibrator vibrator;
    Context mContext;
    float TestData ;
    long[] patter = {1000, 1000};

    private static volatile MediaManager instance;
    public MediaInterface mediaInterface;
    private MediaManager(){}
    public static MediaManager getInstance(){
        if(instance==null){
            synchronized (MediaManager.class){
                if(instance==null){
                    instance = new MediaManager();
                }
            }
        }
        return instance;
    }

    public void InitialParame(boolean bValibrate,boolean bAlarm,int SonesIndex,int AlarmValue){
        mediaInterface.ValibrateChecked(bValibrate);
        mediaInterface.AlarmChecked(bAlarm);
        mediaInterface.SonesIndexChecked(SonesIndex);
        mediaInterface.AlarmValue(AlarmValue);
    }

    public void initCallback(){
        MediaInterface CallBack = new MediaInterface() {
            @Override
            public void ValibrateChecked(boolean Checked) {
                bValibrate = Checked;
                BleLog.e("MediaManager---MediaManager---initCallback---ValibrateChecked-----bValibrate = "+bValibrate);
            }

            @Override
            public void AlarmChecked(boolean Checked) {
                bAlarm = Checked;
                BleLog.e("MediaManager---initCallback----AlarmChecked---bAlarm = "+bAlarm);
            }

            @Override
            public void SonesIndexChecked(int Index) {
                SonesIndex = Index;
                BleLog.e("MediaManager---initCallback----SonesIndexChecked---SonesIndex = "+SonesIndex);
            }

            @Override
            public void AlarmValue(int Value) {
                AlarmValue = Value;
                BleLog.e("MediaManager---initCallback---AlarmValue = "+AlarmValue);
            }

            @Override
            public void getContext(Context context) {
                mContext = context;
                vibrator = (Vibrator)mContext.getSystemService(MainActivity.VIBRATOR_SERVICE);
                BleLog.e("MediaManager---initCallback---mContext = "+mContext);
            }

            @Override
            public void TestingStatus(boolean status) {
                TestStatus = status;
                BleLog.e("MediaManager---initCallback---TestStatus = "+TestStatus);
            }

            @Override
            public void TestingData(float data) {
                TestData = data;
                BleLog.e("MediaManager---initCallback---TestData = "+TestData);
            }
        };
        if(CallBack!=null){
            this.mediaInterface = CallBack;
        }
    }

    public void Prepare(int source){
        try {
            Stop();
            mediaPlayer = new MediaPlayer();
            if(source>=1 && source<=SonesList.size()){
                uri = SonesList.get(source-1).getUri();
                BleLog.e("MediaManager---Prepare------getUri="+uri);
                if(uri!=null){
                    mediaPlayer.setDataSource(MainActivity.context, uri);
                    mediaPlayer.prepare();
                    BleLog.e("MediaManager---Prepare------uri="+uri);
                }
            }
            SonesSize = SonesList.size();
        }
        catch (Exception e){
            BleLog.e("Prepare-------"+e.toString());
        }
    }

    public void Start(Boolean Single){
        if(mediaPlayer==null)
            return;
        if(Single){
            if(HighAlarmStatus)
                return;
            mediaPlayer.start();
        }else{
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        BleLog.e("MediaManager------Start------");
    }

    public void Stop(){
        if(mediaPlayer==null)
            return;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        BleLog.e("MediaManager------Stop------");
    }

    public void SetSonesResource() {
        try {
            Field[] fields = R.raw.class.getDeclaredFields();
            int rawId;
            int fields_size;
            fields_size = fields.length;
            name_items = new String[fields_size];
            SonesList = new ArrayList<>();
            for (int i = 0; i < fields_size; i++) {
                try {
                    rawId = fields[i].getInt(R.raw.class);
                    name_items[i] = fields[i].getName();
                    SonesList.add(new SonesConfigue(fields[i].getName(),Uri.parse("android.resource://" + MainActivity.context.getPackageName() + "/" + rawId)));
                }
                catch (Exception e) {}
            }
            BleLog.e("MediaManager------SetSonesResource------SonesList="+SonesList);
        }
        catch (Exception e){
            BleLog.e("GetSonesResource------"+e.toString());
        }
    }

    public String[] getName_items(){
        return name_items;
    }

    public void MediaAction(float upload){
        try {
            synchronized (this){
                TestData = upload;
                if(TestData>=Integer.valueOf(AlarmValue)){
                    HighAlarmStatus = true;
                    BleLog.e("MediaManager---MediaAction---达到报警警戒线=bValibrate="+bValibrate+"   bAlarm="+bAlarm +"   StopPlayFlag="+PlayStopFlag+"    LimiteFlag="+LimiteFlag);
                    if(bValibrate){
                        Prepare(SonesIndex);
                        if(!PlayStopFlag){
                            PlayStopFlag = true;
                            vibrator.vibrate(patter, 0);
                            BleLog.e("MediaManager---MediaAction000---响铃");
                        }else {
                            PlayStopFlag = false;
                            BleLog.e("MediaManager---MediaAction000---之前有播放音乐然后停止音乐转而响铃！");
//                            MediaAction(TestData);
                        }
                    }
                    else {
                        vibrator.cancel();
                        if(bAlarm){
                            if(!PlayStopFlag){
                                PlayStopFlag = true;
                                Start(false);
                                BleLog.e("MediaManager---MediaAction000---播放音乐");
                            }else{
                                 if(!mediaPlayer.isPlaying()){
                                     Start(false);
                                 }
                            }
                        }
                        else {
                            if(PlayStopFlag){
//                                vibrator.cancel();
//                                Start(false);
                                Prepare(SonesIndex);
                                PlayStopFlag = false;
                                BleLog.e("MediaManager---MediaAction000---之前正在响铃或者播放音乐，然后立即停止！");
                            }
                        }
                    }
                    LimiteFlag = true;
                }
                else {
                    if(LimiteFlag){
                        LimiteFlag = false;
                        vibrator.cancel();
                        Prepare(SonesIndex);
                    }
                    PlayStopFlag = false;
                    HighAlarmStatus = false;
                }
            }
        }
        catch (Exception e){
            BleLog.e("isMediaAction-----------"+e.toString());
        }
    }

    public void StopMedia(){
        vibrator.cancel();
        Prepare(SonesIndex);
    }
}
