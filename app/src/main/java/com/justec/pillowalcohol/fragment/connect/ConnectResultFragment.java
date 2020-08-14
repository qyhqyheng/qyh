package com.justec.pillowalcohol.fragment.connect;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.messageEvent.Disconnect;
import com.justec.blemanager.utils.BleLog;
import com.justec.common.Interface.dataCallbackInterface;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;


public class ConnectResultFragment extends BaseMainFragment{

    @BindView(R.id.tv_mac)
    TextView tvDeviceMac;
    @BindView(R.id.tv_name)
    TextView tvCurrentDeviceName;
    @BindView(R.id.im_state)
    ImageView imState;
    @BindView(R.id.tv_current_device_status)
    TextView tvCurrentDeviceStatus;
    @BindView(R.id.btn_disconnect)
    Button btnDisconnect;


    private static BleDevice mbleDevice;

    private static boolean isInited = false;//初始化这个Fragment,证明绝对要发送初始化指令，默认为false


    //进度条
    private ProgressDialog progressDialog;
    private int TAG_INSTANT_RECEIVE = 10004;//


    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_connect_result;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public static ConnectResultFragment newInstance() {
        Bundle args = new Bundle();
        ConnectResultFragment fragment = new ConnectResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.btn_disconnect)
    public void onClick() {

//        SQLite.update(RecordForm.class).set(RecordForm_Table.recordFormDeviceNum.eq("")).execute();
        //清楚数据库指定的mac记录
//        SQLite.delete(RecordForm.class)
//                .where(RecordForm_Table.recordFormDeviceNum
//                        .eq(EventManger.getInstance().getBleDevice().getMac()))
//                .execute();

        create_disconnect_dialog();
    }

    /**
     * @author Jerry.Xiao
     * @time 2018/8/30
     * @class describe 控制界面onPause(),OnStop()界面资源操作
     * @param
     */

     @Override
     public void onSupportVisible() {
         super.onSupportVisible();
        //为测试界面TestFragment获取数据做准备
         BleLog.e("onSupportVisible = ");

        if(BleManager.getInstance().getAllConnectedDevice().size() > 0) {
         /*
            mbleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
            //设备名字以及当前连接状态
            tvCurrentDeviceName.setText(EventManager.getInstance().getBleDevice().getName());
            tvDeviceMac.setText(EventManager.getInstance().getBleDevice().getMac());
            imState.setVisibility(View.VISIBLE);*/

        }else{
            tvCurrentDeviceName.setText(getResources().getString(R.string.connected_device_unknown));
            imState.setVisibility(View.INVISIBLE);
            //Fragment 控制跳转
            ((ConnectControlFragment) getParentFragment()).triggler(0);//切换到连接界面
        }

     }
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        //获取屏幕尺寸/密度
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        //BleLog.e("屏幕尺寸：宽度 = " + w_screen + "高度 = " + h_screen + " 密度 = " + dm.densityDpi);

        //EvenetBus注册
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void create_disconnect_dialog() {
        final Dialog dialog = new Dialog(getActivity(),R.style.noTitleDialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_tip, null);
        Button disconnect_btn_sure = v.findViewById(R.id.tip_btn_sure);
        Button disconnect_btn_cancel = v.findViewById(R.id.tip_btn_cancel);
        //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分
        //final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);//自定义布局应该在这里添加，要在dialog.show()的后面
        //dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置

        disconnect_btn_sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (BleManager.getInstance().isConnected(mbleDevice)) {
                    BleManager.getInstance().disconnect(mbleDevice);//断开蓝牙连接
                    EventManager.getInstance().setBleDevice(null);
                    EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_SOCKET,""));
                   /* EventBus.getDefault().postSticky(new DisConnectInstatntInfo());
                    EventBus.getDefault().postSticky(new DisConnectReceiveFormInfo());*/
                    //Fragment 控制跳转
                    ((ConnectControlFragment) getParentFragment()).triggler(0);//切换到
                    dialog.dismiss();
                }
            }
        });

        disconnect_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }
    /**
     * 发送指令，初始化
     */
    @Subscribe(sticky = true)
    public void initCommand(String isInit) {
        if (BleManager.getInstance().getAllConnectedDevice().size() != 0) {
            mbleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
            tvCurrentDeviceName.setText(mbleDevice.getName());
            tvDeviceMac.setText(mbleDevice.getMac());
            imState.setVisibility(View.VISIBLE);
            if (TextUtils.equals(isInit, "00001")) {
                EventManager.getInstance()
                        .initDeviceChara(mbleDevice, true);//开始加载发送，初始化指令
            }
        }
    }

    /**
     * 接收到断开连接信息消费
     */
    @Subscribe(sticky = true)
    public void disconnect(Disconnect disconnect){
        ((ConnectControlFragment) getParentFragment()).triggler(0);//切换到连接界面
    }


    private String alarmLimite;//报警限制

  /*  @Subscribe(sticky = true)
    public void getHistoryValue(ReceiveAlarmLimite receiveAlarmLimite) {
        alarmLimite = String.valueOf(receiveAlarmLimite.getValue()/100);
        //tvAlarmData.setText(alarmLimite);
        setAlarmLimite(alarmLimite);
        Log.d("Jerry.Xiao","alarmLimite = "+ alarmLimite);
    }*/

}
