package com.justec.pillowalcohol.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.fragment.MainFragment;
import com.justec.pillowalcohol.media.MediaManager;

import java.lang.reflect.Field;

import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends SupportActivity {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    String SoftVersion;

    String[] name_items;
    Uri[] url_items;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        if (findFragment(MainFragment.class) == null) {
            loadRootFragment(R.id.fl_container, MainFragment.newInstance());
        }
//        MediaAction.getInstance().SetSonesResource();
        MediaManager.getInstance().SetSonesResource();
        SystemClock.sleep(100);
        verifyStoragePermissions(this);
        InitialData();
    }

    private void InitialData(){
        try {
            GetSonesResource();
            PackageManager packageManager = this.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            SoftVersion = getResources().getString(R.string.Version_Name)+packageInfo.versionName;
            Common.getInstance().setSoftVersion(SoftVersion);

            preferences = this.getSharedPreferences("ParaSetting", this.MODE_PRIVATE);

            boolean bAlarm = preferences.getBoolean("isAlarm",false);
            boolean bVibrate = preferences.getBoolean("isVibrate",false);
            int alarmValue = preferences.getInt("alarmValue",20);
            int ItemIndex = preferences.getInt("ItemIndex",1);

            MediaManager.getInstance().InitialParame(bVibrate,bAlarm,ItemIndex,alarmValue);
            MediaManager.getInstance().Prepare(ItemIndex);
            BleLog.e("MediaAction----InitialData---bAlarm="+bAlarm+"   bVibrate="+bVibrate
                    +"   alarmValue="+alarmValue+"   ItemIndex="+ItemIndex);

            if(Common.getInstance().getSonesName_Items().equals("null")){
                Common.getInstance().setSonesName_Items(name_items[0]);
            }

        }catch (Exception e){
            BleLog.d("InitialData---"+e.toString());
        }
    }

    private void GetSonesResource(){
        try {
            Field[] fields = R.raw.class.getDeclaredFields();
            int rawId;
            int fields_size;
            fields_size = fields.length;

            name_items = new String[fields_size];
            url_items = new Uri[fields_size];

            for(int i=0;i<fields_size;i++){
                try {
                    name_items[i] = fields[i].getName();
                    rawId = fields[i].getInt(R.raw.class);
                    url_items[i] = Uri.parse("android.resource://"+this.getPackageName()+"/"+ rawId);
                }catch(Exception e){}
            }
        }catch (Exception e){
            BleLog.d("GetSonesResource---"+e.toString());
        }
    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        // 设置横向(和安卓4.x动画相同)
        return new DefaultHorizontalAnimator();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BleLog.e("ParaSettintFragment----onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //清楚指定的mac记录
//        SQLite.delete(RecordForm.class)
//                .where(RecordForm_Table.recordFormDeviceNum
//                        .eq(EventManger.getInstance().getBleDevice().getMac()))
//                .execute();

        //清除界面ReceivceFragment_1,SearchReasultFragment_1数据 Jerry.Xiao0907
      /*  EventBusActivityScope.getDefault(this).postSticky(new DisConnectInstatntInfo());
        EventBus.getDefault().postSticky(new DisConnectReceiveFormInfo());//断开连接

        if (BleManager.getInstance().isBlueEnable()) {
            //断开连接
            BleManager.getInstance().disconnectAllDevice();
            //BleManager.getInstance().disableBluetooth();
        }
        //清除EventManger应用实例
        EventManger.getInstance().destroy();
*/
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
// Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
// We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
   /* private boolean isAlarm = false;
    private boolean isVibrate = false;
    public boolean getIsVibrate() {
        return isVibrate;
    }
    public boolean getIsAlarm() {
        return isAlarm;
    }

    public void setLimiteValue(boolean isVibrate,boolean isAlarm) {
        this.isVibrate = isVibrate;
        this.isAlarm = isAlarm;
    }*/
}
