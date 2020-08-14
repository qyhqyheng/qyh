package com.justec.pillowalcohol.fragment.setting;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingFragment extends BaseMainFragment {

    @BindView(R.id.iv_alignment_into)
    ImageView alignment_back;

    private final String TAG = "SettingFragment";
    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_setting;
    }

    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @OnClick({R.id.ll_parameter_setting,R.id.ll_btName_setting,R.id.ll_alignment_setting,R.id.ll_product_setting,R.id.ll_about_setting})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_parameter_setting:
                ((SettingControlFragment) getParentFragment()).triggler(1);//切换到参数设置
                break;
            case R.id.ll_btName_setting:
                ((SettingControlFragment) getParentFragment()).triggler(2);//切换到蓝牙名
                break;
            case R.id.ll_alignment_setting:
                ((SettingControlFragment) getParentFragment()).triggler(3);//切换到校准
                break;
            case R.id.ll_product_setting:
                ((SettingControlFragment) getParentFragment()).triggler(4);//切换到产品
                break;
            case R.id.ll_about_setting:
                ((SettingControlFragment) getParentFragment()).triggler(5);//切换到产品
                break;
        }
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        if (BleManager.getInstance().getAllConnectedDevice().size() == 0) {
            Toast.makeText(getActivity(), R.string.connected_device_unknown, Toast.LENGTH_SHORT).show();
        }else{
            String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.READ_PRODUCT_NAME);
            EventManager.getInstance().sendComand(commandCode);
        }
    }
}
