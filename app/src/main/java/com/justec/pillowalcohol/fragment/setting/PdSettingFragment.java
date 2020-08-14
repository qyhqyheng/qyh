package com.justec.pillowalcohol.fragment.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.messageEvent.Disconnect;
import com.justec.blemanager.messageEvent.ProductInfoEvent;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class PdSettingFragment extends BaseMainFragment{
    @BindView(R.id.tv_software_version)
    TextView tv_software_version;
    @BindView(R.id.tv_product_name)
    TextView tv_product_name;
    @BindView(R.id.tv_serial_number)
    TextView tv_serial_number;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_product_setting;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public static PdSettingFragment newInstance() {
        Bundle args = new Bundle();
        PdSettingFragment fragment = new PdSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @OnClick({R.id.iv_product_back,R.id.tv_software_version,R.id.tv_product_name,R.id.tv_serial_number})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_product_back:
                ((SettingControlFragment) getParentFragment()).triggler(40);//切换到setting页
                break;
        }
    }

    @Subscribe(sticky = true)
    public void recieveProductInfo(ProductInfoEvent productInfoEvent){
        ArrayList<String> info = productInfoEvent.getInfo();
        if(info.size() == 3){
            tv_product_name.setText(info.get(0));
            tv_serial_number.setText(info.get(1));
            tv_software_version.setText(info.get(2));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
