package com.justec.pillowalcohol.fragment.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.messageEvent.NotificationDialog;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.media.MediaManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

public class ParaSettintFragment extends BaseMainFragment{
    @BindView(R.id.tv_alarm_value)
    TextView tvAlarmValue;
    @BindView(R.id.iv_para_back)
    ImageView ivParaBack;
    @BindView(R.id.rl_alarm_value)
    RelativeLayout rlAlarmValue;
    @BindView(R.id.tb_alarm)
    ToggleButton tb_alarm;
    @BindView(R.id.tb_vibration)
    ToggleButton tb_vibration;
    @BindView(R.id.rl_alarm_bell)
    RelativeLayout rlAlarmBell;
    @BindView(R.id.tv_alarm_bell)
    TextView tvAlarmBell;

    private boolean isVibrate;
    private boolean isAlarm;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Dialog dialog;

    //全局定义
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;  // 快速点击间隔

    String[] name_items;
    Uri[] url_items;
    int ItemIndex;
//    int AlarmValue;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_para_setting;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static ParaSettintFragment newInstance() {
        Bundle args = new Bundle();
        ParaSettintFragment fragment = new ParaSettintFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        preferences = activity.getSharedPreferences("ParaSetting", activity.MODE_PRIVATE);
        editor = preferences.edit();
        dialog=new Dialog(getActivity());
    }

    private void create_input_dialog() {
        dialog=new Dialog(getContext(),R.style.noTitleDialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_setting_factor, null);
        final EditText tv_content = v.findViewById(R.id.dialog_content);
        TextView tv_title = v.findViewById(R.id.dialog_title);
        Button btn_sure = v.findViewById(R.id.dialog_btn_sure);
        Button btn_cancel = v.findViewById(R.id.dialog_btn_cancel);
        //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分
        //final Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getWindow().setContentView(v);//自定义布局应该在这里添加，要在dialog.show()的后面
        //dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置
        tv_title.setText(getResources().getString(R.string.Para_AlarmData));

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
                    Toast.makeText(getActivity(), getResources().getString(R.string.Para_AlarmData_UnEmpty), Toast.LENGTH_SHORT).show();
                }else{
                    if(Float.valueOf(temp_value)>100||Float.valueOf(temp_value)<0){
                        Toast.makeText(getActivity(), getResources().getString(R.string.Para_AlarmData_Error), Toast.LENGTH_SHORT).show();
                    }else{
                        tvAlarmValue.setText(temp_value);
                        String commandCode = EventManager.getInstance().WriteCommandCode(temp_value, ResultCommand.WRITE_ALARM_LIMITE);
                        EventManager.getInstance().sendComand(commandCode);
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

    @OnClick({R.id.iv_para_back,R.id.rl_alarm_value,R.id.tb_vibration,R.id.tb_alarm,R.id.rl_alarm_bell})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_para_back:
            ((SettingControlFragment) getParentFragment()).triggler(10);//切换到setting页
            break;
            case R.id.rl_alarm_value:
                if (BleManager.getInstance().getAllConnectedDevice().size() > 0) {
                    create_input_dialog();
                }else{
                    Toast.makeText(getActivity(), R.string.connected_device_unknown, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tb_vibration:
                isVibrate = !isVibrate;
                MediaManager.getInstance().mediaInterface.ValibrateChecked(isVibrate);
//                MediaAction.getInstance().setValibrate(isVibrate);
                EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_VIBRATE_CHECKCHANGE,""));
                break;
            case R.id.tb_alarm:
                isAlarm = !isAlarm;
                MediaManager.getInstance().mediaInterface.AlarmChecked(isAlarm);
//                MediaAction.getInstance().setAlarm(isAlarm);
                EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_VIBRATE_CHECKCHANGE,""));
                break;
            case R.id.rl_alarm_bell:
                showListAlertDialog();
                break;
        }
    }
    // 信息列表提示框
    private AlertDialog alertDialog1;
    public void showListAlertDialog(){
//        name_items = MediaAction.getInstance().getName_items();
        name_items = MediaManager.getInstance().getName_items();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(getResources().getString(R.string.Para_AlarmBela));
        alertBuilder.setItems(name_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                alertDialog1.dismiss();
                ItemIndex = index + 1;
                BleLog.e("MediaAction---ItemIndex=========="+ItemIndex);
//                MediaAction.getInstance().Prepare(ItemIndex);
                MediaManager.getInstance().Prepare(ItemIndex);
                tvAlarmBell.setText(name_items[index]);
                SystemClock.sleep(200);
//                MediaAction.getInstance().StartSelection();
//                MediaAction.getInstance().setItemIndex(ItemIndex);
                MediaManager.getInstance().Start(true);
                MediaManager.getInstance().mediaInterface.SonesIndexChecked(ItemIndex);
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();

        ItemIndex = MediaManager.getInstance().SonesIndex;
        BleLog.e("ParaSettintFragment------onSupportVisible---ItemIndex="+ItemIndex);

        tvAlarmValue.setText(String.valueOf(MediaManager.getInstance().AlarmValue));
        if(ItemIndex>=1)
          tvAlarmBell.setText(MediaManager.getInstance().SonesList.get(ItemIndex-1).getName());
        isVibrate = MediaManager.getInstance().bValibrate;
        isAlarm = MediaManager.getInstance().bAlarm;

        if(!isVibrate)
            tb_vibration.setChecked(false);
        if(!isAlarm)
            tb_alarm.setChecked(false);
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        editor.putBoolean("isVibrate", isVibrate);
        editor.putBoolean("isAlarm",isAlarm);
        editor.putInt("alarmValue", Integer.parseInt(tvAlarmValue.getText().toString()));
        editor.putInt("ItemIndex",ItemIndex);
        editor.commit();
    }

    @Subscribe(sticky = true)
    public void getSettingLimite(NotificationDialog notificationDialog){
        if(notificationDialog.getParaSetting().equals("10001")){
            boolean isSuccess = notificationDialog.getSuccess();
            if(isSuccess){
                Toast.makeText(getActivity(), getResources().getString(R.string.Para_Setting_Success), Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(getActivity(), getResources().getString(R.string.Para_Setting_Fail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
