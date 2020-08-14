package com.justec.pillowalcohol.fragment.setting;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;

import com.justec.blemanager.messageEvent.Disconnect;
import com.justec.blemanager.messageEvent.NotificationDialog;
import com.justec.blemanager.messageEvent.ReadCalibrationData;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.DateUtil;
import com.justec.blemanager.utils.HexUtil;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.ServiceManager;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.fragment.MainFragment;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;


public class CalibrationFragment extends BaseMainFragment {

    private static BleDevice mbleDevice;

    @BindView(R.id.tv_bt_name)
    TextView bt_name;
    @BindView(R.id.tv_connected_show)
    TextView connected_statu;
    @BindView(R.id.tv_zero_day)
    TextView tv_zero_day;
    @BindView(R.id.tv_zero_second)
    TextView tv_zero_second;
    @BindView(R.id.tv_day)
    TextView tv_day;
    @BindView(R.id.tv_second)
    TextView tv_second;
    @BindView(R.id.tv_calibration_factor)
    TextView calibration_factor;
    @BindView(R.id.tv_default_factor)
    TextView default_factor;
    @BindView(R.id.bt_calibration)
    Button bt_calibration;

    private final String TAG = "CalibrationFragment";

    //全局定义
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;  // 快速点击间隔
    private static int MSG_DISMISS_DIALOG = -1;
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    private Dialog dialog;
    private controlDialogTask mcontrolDialogTask = new controlDialogTask();
    //第一次获取设备校准系数
    private Boolean isInitCalibrationFactor = false;

    String LastDay="";
    String LastSecond="";
    int CountTime = 0;
    String dif;    //校准命令差异
    private boolean ShowFirst ;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_calibration_time;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        sp = activity.getSharedPreferences("Setting_Time", activity.MODE_PRIVATE);
        editor = sp.edit();
        dialog=new Dialog(getActivity());
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        CountTime = 0;
        ShowFirst = false;
//        BleLog.e("Calibrate---onSupportVisible");
        isInitCalibrationFactor = true;
        if (BleManager.getInstance().getAllConnectedDevice().size() == 0) {
            connected_statu.setText(getResources().getString(R.string.UnConnect));
            bt_name.setText("");
            Toast.makeText(getActivity(), R.string.connected_device_unknown, Toast.LENGTH_SHORT).show();
        }else{
            connected_statu.setText(getResources().getString(R.string.Connectted));
            bt_name.setText(EventManager.getInstance().getBleDevice().getName());
            if(isInitCalibrationFactor) {
                //获取初始校准系数
                String commandCode1 = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_CALIBRATION_ZERO);
                EventManager.getInstance().sendComand(commandCode1);
                SystemClock.sleep(200);
                String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_CALIBRATION_FACTOR);
                EventManager.getInstance().sendComand(commandCode);
                Log.d("isInitCalibrationFactor","isInitCalibrationFactor");
            }
        }
        //获取上次记录数据
       /* lastdate_day.setText(sp.getString("time_to_day",""));
        lastdate_second.setText(sp.getString("time_to_second",""));
        calibration_factor.setText(sp.getString("setting_coefficient",""));*/
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public static CalibrationFragment newInstance() {
        Bundle args = new Bundle();
        CalibrationFragment fragment = new CalibrationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

    }

    //拦截返回键事件
    @Override
    public boolean onBackPressedSupport() {
        ((SettingControlFragment) getParentFragment()).triggler(0);//切换到setting页
        return true;
    }

    @OnClick({R.id.bt_calibration,R.id.tv_default_factor,R.id.iv_setting_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_default_factor:
                create_input_dialog();
                break;
            case R.id.bt_calibration:
                if(BleManager.getInstance().getAllConnectedDevice().size()>0){
                    show_progressbar_dialog(10);
                    startTask();
                    int tempvalue = (Integer.valueOf(String.valueOf(default_factor.getText())));
                    //int tempvalue =Math.round(temp);//保留两位小数
                    String TimeData = HexUtil.getFixedCurrentTime().substring(2,HexUtil.getFixedCurrentTime().length());
                    String date = HexUtil.format10StringToHex(String.valueOf(default_factor.getText()))+HexUtil.StrAsciiToHex(TimeData);
                    String commandCode = EventManager.getInstance().WriteFixedCommandCode(date, ResultCommand.WRITE_CALIBRATION_VALUE);
                    //String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_CALIBRATION_FACTOR);
                    EventManager.getInstance().sendComand(commandCode);
                    BleLog.e(TimeData+"= TimeData---Calibrate------sendComand = "+commandCode);
                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Equipment_UnConnect), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_setting_back:
                ((SettingControlFragment) getParentFragment()).triggler(30);//切换到setting页
                break;
        }
    }

    private void create_input_dialog() {
        dialog=new Dialog(getContext(),R.style.noTitleDialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_setting_factor, null);
        final EditText tv_content = v.findViewById(R.id.dialog_content);
        Button btn_sure = v.findViewById(R.id.dialog_btn_sure);
        Button btn_cancel = v.findViewById(R.id.dialog_btn_cancel);
        //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分
        //final Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getWindow().setContentView(v);//自定义布局应该在这里添加，要在dialog.show()的后面
        //dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                //更新缺省系数
                String temp_value = String.valueOf(tv_content.getText().toString());
                if(temp_value.equals("")){
                    Toast.makeText(getActivity(), getResources().getString(R.string.Para_Carabration_UnEmpty), Toast.LENGTH_SHORT).show();
                }else{
                    if((Float.valueOf(temp_value)!=0)&&(Float.valueOf(temp_value)>50||Float.valueOf(temp_value)<20)){
                        Toast.makeText(getActivity(), getResources().getString(R.string.Para_Carabration_Error), Toast.LENGTH_SHORT).show();
                    }else{
                        default_factor.setText(tv_content.getText());
                        dialog.dismiss();
                    }
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        //EditText输入系数值的控制
        tv_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        tv_content.setText(s);
                        tv_content.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    tv_content.setText(s);
                    tv_content.setSelection(2);
                }
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        tv_content.setText(s.subSequence(0, 1));
                        tv_content.setSelection(1);
                        return;
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    public void show_progressbar_dialog(int count){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_loading, null);
        TextView tv_countTime = v.findViewById(R.id.tv_countTime);
        tv_countTime.setText(String.valueOf(count));
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(v);//自定义布局应该在这里添加，要在dialog.show()的后面
    }
    public void dialog_success_statu(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_setting_success, null);
        dialog.show();
        dialog.getWindow().setContentView(v);
        myHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 2000);
        /*thisdate_day.setText(DateUtil.getTimeToDay());
        thisdate_second.setText(DateUtil.getTimeToSecond());*/
        Log.e("Jerry.Xiao","dialog_success_statu");
//        value_memory();
    }
    public void dialog_fail_statu(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_setting_fail, null);
        dialog.show();
        dialog.getWindow().setContentView(v);
        myHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 2000);
    }
    public void dialogClose(){
        if(dialog != null){
            dialog.dismiss();
            dialog.cancel();
            myHandler.removeMessages(MSG_DISMISS_DIALOG);
        }
    }
    public void value_memory(){
        editor.clear();
        editor.putString("time_to_day",LastDay);
        editor.putString("time_to_second",LastSecond);
        editor.putString("setting_coefficient", String.valueOf(calibration_factor.getText()));
        editor.commit();
    }

    /**
     * 关闭校准界面消息消费
     */
    @Subscribe(sticky = true)
    public void event_Dialog_statu(NotificationDialog notificationTag) {
        if(TextUtils.equals(notificationTag.getNotificationTag(),"2")){
            dialog_success_statu();
        }
        if(TextUtils.equals(notificationTag.getNotificationTag(),"0")) {
            dialog_fail_statu();
        }
    }
    /**
     * 获取更新后的校准系数
     */
    @Subscribe(sticky = true)
    public void read_calibration_factor(ReadCalibrationData readCalibrationData) throws ParseException {
        BleLog.e(dif+" = dif---Calibrate---ReadCalibrationData = "+readCalibrationData.getDataValue());
        LastDay = DateUtil.DateFormatToDay(String.valueOf(readCalibrationData.getDataValue().substring(0,8)));
        LastSecond = DateUtil.DateFormatToSecond(String.valueOf(readCalibrationData.getDataValue().substring(8,readCalibrationData.getDataValue().length())));
        dif = readCalibrationData.getDifCode();
        if(dif.equals(ResultCommand.READ_CALIBRATION_FACTOR)){
            tv_day.setText(LastDay);
            tv_second.setText(LastSecond);
            calibration_factor.setText(String.valueOf(readCalibrationData.getCalibrationValue()));
        }
        else if(dif.equals(ResultCommand.READ_CALIBRATION_ZERO)){
            tv_zero_day.setText(LastDay);
            tv_zero_second.setText(LastSecond);
            if(ShowFirst){
                calibration_factor.setText(String.valueOf(readCalibrationData.getCalibrationValue()));
            }
            ShowFirst = true;
        }

        //如下为第一次获取设备的校准系数使用
        /*if(isInitCalibrationFactor){
            editor.clear();
            editor.putString("setting_coefficient", String.valueOf(calibration_factor.getText()));
            editor.commit();
            isInitCalibrationFactor = false;
        }else {*/
            if(!isInitCalibrationFactor){
//                thisdate_day.setText(DateUtil.getTimeToDay());
//                thisdate_second.setText(DateUtil.getTimeToSecond());
            }
            isInitCalibrationFactor = false;
            //value_memory();
        //}
    }

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        // 2.重写消息处理函数
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MSG_DISMISS_DIALOG==msg.what){
                dialogClose();
                stopTask();
            }
            if(msg.what>0){
                show_progressbar_dialog(msg.what);
            }
        }

    };

    public class controlDialogTask extends TimerTask {
        int countTime = 10;
        public void run() {
            if (countTime > 0) {
                countTime--;
            }
//            BleLog.e("Calibrate---controlDialogTask---countTime = "+countTime);
            if(countTime==0){
                myHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 1000);
//                dialog_fail_statu();
            }else {
                Message msg = new Message();
                msg.what = countTime;
                myHandler.sendMessage(msg);
            }
        }
    }
    /**
     * start Timer
     */
    public synchronized void startTask() {
        stopTask();
        if (mcontrolDialogTask == null) {
            mcontrolDialogTask = new controlDialogTask();
            Timer m_task = new Timer();
            m_task.schedule(mcontrolDialogTask, 1000, 1000);
        }
    }

    /**
     * stop Timer
     */
    public synchronized void stopTask() {
        try {
            if (mcontrolDialogTask != null) {
                mcontrolDialogTask.cancel();
                mcontrolDialogTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收到断开连接信息消费
     */
    @Subscribe(sticky = true)
    public void clearInfo(Disconnect disconnect){
        stopTask();
        dialogClose();
        //dialog_fail_statu();
        connected_statu.setText(getResources().getString(R.string.UnConnect));
        bt_name.setText("");
    }
}