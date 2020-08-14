package com.justec.pillowalcohol.fragment.connect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.callback.BleGattCallback;
import com.justec.blemanager.callback.BleMtuChangedCallback;
import com.justec.blemanager.callback.BleScanCallback;
import com.justec.blemanager.exception.BleException;
import com.justec.blemanager.messageEvent.Disconnect;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.utils.BleLog;
import com.justec.common.Interface.dataCallbackInterface;
import com.justec.common.event.ToastUtil;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.activity.MainActivity;
import com.justec.pillowalcohol.adapter.DeviceAdapter;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.socketcontrol.SocketInterface;
import com.justec.socketcontrol.SocketManager;
import com.justec.socketcontrol.SocketStatus;
import com.justec.socketcontrol.WebClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static java.security.AccessController.getContext;

public class ConnectFragment extends BaseMainFragment implements dataCallbackInterface{
    @BindView(R.id.btn_scan)
    Button btnScan;
    @BindView(R.id.list_device)
    RecyclerView recyclerView;
    @BindView(R.id.lin_content)
    LinearLayout linContent;

    private static final String TAG = ConnectFragment.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int MaxCount = 10;

    private Animation operatingAnim;
    // 自定义RecyclerView
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private WebClient mClient;

    private static final int OverTimeMsg = 0X00;

//    String[] indicate_items = {getActivity().getResources().getString(R.string.Network_Connect)};
    private AlertDialog alertDialog1;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_connect;
    }

    public static ConnectFragment newInstance() {
        Bundle args = new Bundle();
        ConnectFragment fragment = new ConnectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        mDeviceAdapter = new DeviceAdapter();
        // 点击事件监听
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }
        });
        recyclerView.setAdapter(mDeviceAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissions(); //进入界面就开始实现刷新
        showConnectedDevice();//界面资源初始化就刷新蓝牙设备列表
    }

    @OnClick({R.id.btn_scan,R.id.ll_indicata})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                if (btnScan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btnScan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;

            case R.id.ll_indicata:
                ShowDialog();
                break;
        }
    }

    private void ShowDialog(){
        try {
            String[] indicate_items = {getActivity().getResources().getString(R.string.Network_Connect)};
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setItems(indicate_items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((ConnectControlFragment) getParentFragment()).triggler(2);//切换到网络界面
//                    Common.getInstance().setSwitchFragment(true);
                }
            });
            alertDialog1 = alertBuilder.create();
            alertDialog1.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.indicate_show));
            alertDialog1.show();

            Window dialogWindow = alertDialog1.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);

            lp.x = 40; // 新位置X坐标
            lp.y = 180; // 新位置Y坐标
            lp.width = 380; // 宽度
            lp.height = 200; // 高度
//            lp.alpha = 0.7f; // 透明度
            dialogWindow.setAttributes(lp);
            dialogWindow.setDimAmount(0);//设置昏暗度为0
            dialogWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.indicate_show));

            try {
                Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                mAlert.setAccessible(true);
                Object mAlertController = mAlert.get(alertDialog1);
                Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                mMessage.setAccessible(true);
                TextView mMessageView = (TextView) mMessage.get(mAlertController);
                mMessageView.setTextColor(Color.BLACK);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            BleLog.d("ShowDialog---"+e.toString());
        }
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clear();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.add(bleDevice);
        }
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clear();//清楚蓝牙扫描的集合结果
                btnScan.setText(getString(R.string.stop_scan));
                //防止误触多次，导致多次刷新蓝牙命令，按钮不可用
                btnScan.setEnabled(false);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                BleLog.d("bleDevice.name="+bleDevice.getName());
                mDeviceAdapter.add(bleDevice);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                //防止误触多次，导致多次刷新蓝牙命令，按钮可用了
                btnScan.setEnabled(true);
                btnScan.setText(getString(R.string.start_scan));
                //停止扫描
                //BleManager.getInstance().cancelScan();//Jerry.Xiao
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d("Jerry.Xiao","onStartConnect");
                if(progressDialog==null){
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle(bleDevice.getName());
                    progressDialog.show();
                }
            }

            @Override
            public void onConnectFail(BleException exception) {
                Log.e("Jerry.Xiao","onConnectFail");
                btnScan.setText(getString(R.string.start_scan));
                mDeviceAdapter.clear();
                if(progressDialog!=null){
                    progressDialog.dismiss();
                    progressDialog=null;
                }
                Toast.makeText(getContext(), getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d("Jerry.Xiao","onConnectSuccess");
                if(progressDialog!=null){
                    progressDialog.dismiss();
                    progressDialog=null;
                }
//                mDeviceAdapter.add(mDeviceAdapter.addDevice(bleDevice));
                mDeviceAdapter.replace(mDeviceAdapter.addDevice(bleDevice));
                //readRssi(bleDevice);
                setMtu(bleDevice, 23);
                EventBus.getDefault().postSticky("00001");//通知结果页面发送请求
                ((ConnectControlFragment) getParentFragment()).triggler(1);//切换到结果页
            }
            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.e("Jerry.Xiao","onDisConnected");
                BleManager.getInstance().cancelScan();
                if(progressDialog!=null){
                    progressDialog.dismiss();
                    progressDialog=null;
                }
//                mDeviceAdapter.replace(mDeviceAdapter.addDevice(bleDevice));
                mDeviceAdapter.clear();

//                if (isActiveDisConnected) {
//                    Toast.makeText(getContext(), getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getContext(), getString(R.string.disconnected), Toast.LENGTH_LONG).show();
//                    ObserverManager.getInstance().notifyObserver(bleDevice);
//                }

                //设置device清空
                EventManager.getInstance().setBleDevice(null);
                EventBus.getDefault().postSticky(new Disconnect(getCurrentTime()));
                Toast.makeText(getContext(), getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_VIBRATE_STOP,""));
            }

            @Override
            public void onReConnect(BleDevice bleDevice) {
                Log.d("Jerry.Xiao","onReConnect");
                if(progressDialog!=null){
                    progressDialog.dismiss();
                    progressDialog=null;
                }
                if(bleDevice != null)
                    connect(bleDevice);
            }
        });
    }

    @Subscribe(sticky = true)
    public void DispatchInitial(UiMessage uiMessage){
        switch (uiMessage.getType()){
            case UiEvent.MSG_DATA_INITIAL:
                BleLog.e("TestingUpdataView---MSG_DATA_INITIAL---DispatchInitial----BleDevice ="+EventManager.getInstance().getBleDevice());
                if(EventManager.getInstance().getBleDevice()!=null) {
                    EventManager.getInstance().initCallback(this);    //读取报警限值
                    TimeChange();
                    String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_ALARM_LIMITE);
                    EventManager.getInstance().sendComand(commandCode);
                    BleLog.e("TestingUpdataView---MSG_DATA_INITIAL---sendComand ="+commandCode);
                }
                break;
        }
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i(TAG, "onsetMTUFailure" + exception.toString());
            }
            @Override
            public void onMtuChanged(int mtu) {
                Log.i(TAG, "onMtuChanged: " + mtu);
            }
        });
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(getActivity(), deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getActivity().finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                startScan();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mOverTimeThread);
        BleManager.getInstance().cancelScan();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void dataCallback(String data) {
        BleLog.e("TestingUpdataView------dataCallback===data="+data);   //数据外发
        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_ALARMDATA_LIMITE,data));
    }

    @Override
    public void OverTime(boolean over) {
        if(!over){
            BleLog.e("TestingUpdataView------OverTime---");
            String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_ALARM_LIMITE);
            EventManager.getInstance().sendComand(commandCode);
        }else {
            handler.removeCallbacks(mOverTimeThread);
        }
    }

    private Runnable mOverTimeThread = new Runnable() {
        public void run() {
//            BleLog.e("TestingUpdataView---mOverTimeThread");
            TimeChange();
        }
    };

    private void TimeChange(){
        Message message =Message.obtain();
        message.what = OverTimeMsg;
        handler.sendMessage(message);
//        BleLog.e("TestingUpdataView---TimeChange---sendMessage");
        handler.postDelayed(mOverTimeThread, 500);
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OverTimeMsg:
                    BleLog.e("TestingUpdataView---handler---OverTimeMsg");
                    EventManager.getInstance().callback.OverTime(false);
                    break;
            }
        }
    };
}
