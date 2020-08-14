package com.justec.pillowalcohol.fragment.setting;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.justec.blemanager.blemanager.BleDevice;
import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;

public class BtSettingFragment extends BaseMainFragment{
    @BindView(R.id.tv_bt_software)
    TextView tvBtSoftware;
    @BindView(R.id.et_BtName)
    EditText etBtName;
    @BindView(R.id.btname_setting)
    Button BtName_Setting;

    private static BleDevice mbleDevice;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_bt_setting;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public static BtSettingFragment newInstance() {
        Bundle args = new Bundle();
        BtSettingFragment fragment = new BtSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        etBtName.setText("");
        EventBus.getDefault().register(this);
        if (BleManager.getInstance().getAllConnectedDevice().size() > 0) {
            mbleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
            etBtName.setText(mbleDevice.getName());
            etBtName.setSelection(mbleDevice.getName().length());
        }else{
            Toast.makeText(getActivity(), R.string.connected_device_unknown, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.iv_bt_back,R.id.et_BtName,R.id.btname_setting})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_bt_back:
                ((SettingControlFragment) getParentFragment()).triggler(20);//切换到setting页
                break;
            case R.id.et_BtName:
                /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.setName();*/
                break;
            case R.id.btname_setting:
                SendInputCode();
                break;
        }
    }

    private void SendInputCode(){
        try {
            //EditText输入系数值的控制
            String input = etBtName.getText().toString();
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            if(!p.matcher(input).find()&&input.length()<=14){
                etBtName.setText(input);
                String commandCode = EventManager.getInstance().WriteAscallCommandCode(input, ResultCommand.WRITE_BLUTH_NAME);
                EventManager.getInstance().sendComand(commandCode);
            }else {
                Toast.makeText(getContext(),getResources().getString(R.string.Para_Data_WrongFormat),Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            BleLog.d("SendInputCode==="+e.toString());
        }
    }

    @Subscribe(sticky = true)
    public void DispatchInitial(UiMessage uiMessage){
        switch (uiMessage.getType()){
            case UiEvent.MSG_WRITE_BLUTH_NAME:
                BleLog.e("WRITE_BLUTH_NAME----uiMessage.getMsg()="+uiMessage.getMsg());
                if(uiMessage.getMsg().equals("01")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Para_Setting_Success), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Para_Setting_Fail), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}