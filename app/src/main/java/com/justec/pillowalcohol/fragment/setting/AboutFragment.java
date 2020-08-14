package com.justec.pillowalcohol.fragment.setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutFragment extends BaseMainFragment{
    @BindView(R.id.soft_version)
    TextView soft_version;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_about_setting;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public static AboutFragment newInstance() {
        Bundle args = new Bundle();
        AboutFragment fragment = new AboutFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }
    @OnClick({R.id.iv_about_back})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_about_back:
                ((SettingControlFragment) getParentFragment()).triggler(50);//切换到setting页
                break;
        }
    }

    @Override
    public void onSupportVisible() {
        soft_version.setText(Common.getInstance().getSoftVersion());
        super.onSupportVisible();
    }
}
