package com.justec.pillowalcohol.fragment.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import me.yokeyword.fragmentation.SupportFragment;

public class SettingControlFragment extends BaseMainFragment {

    private SupportFragment[] mFragments = new SupportFragment[6];

    public static SettingControlFragment newInstance() {
        Bundle args = new Bundle();
        SettingControlFragment fragment = new SettingControlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_setting_control;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //持久化持有Fragment 的引用，但是对应的Fragment刷新界面的操作生命周期就没有调用
        // 最好使用EventBus通知界面的改动
        SupportFragment firstFragment = findChildFragment(SettingFragment.class);
        if (firstFragment == null) {
            mFragments[0] = SettingFragment.newInstance();
            mFragments[1] = ParaSettintFragment.newInstance();
            mFragments[2] = BtSettingFragment.newInstance();
            mFragments[3] = CalibrationFragment.newInstance();
            mFragments[4] = PdSettingFragment.newInstance();
            mFragments[5] = AboutFragment.newInstance();

            //初始化连接切换状态的Fragment
            loadMultipleRootFragment(R.id.container_fl_setting,
                    0,//默认显示的Fragment
                    mFragments[0],mFragments[1],mFragments[2],mFragments[3],mFragments[4],mFragments[5]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[0] = firstFragment;
            mFragments[1] = ParaSettintFragment.newInstance();
            mFragments[2] = BtSettingFragment.newInstance();
            mFragments[3] = CalibrationFragment.newInstance();
            mFragments[4] = PdSettingFragment.newInstance();
            mFragments[5] = AboutFragment.newInstance();
        }
    }

    //切换Fragment
    public void triggler(int triggleTag) {
        //放弃使用getAllConnectedDevice().size()来判断蓝牙连接状态，因为蓝牙应用变量变化太慢了
        // 直接使用标志位 0: ConnectFragment_1  1:ConnectResultFragment_1
        if (triggleTag == 10)
            showHideFragment(mFragments[0], mFragments[1]);
        else if (triggleTag == 1)
            showHideFragment(mFragments[1], mFragments[0]);
        if (triggleTag == 20)
            showHideFragment(mFragments[0], mFragments[2]);
        else if (triggleTag == 2)
            showHideFragment(mFragments[2], mFragments[0]);
        if (triggleTag == 30)
            showHideFragment(mFragments[0], mFragments[3]);
        else if (triggleTag == 3)
            showHideFragment(mFragments[3], mFragments[0]);
        if (triggleTag == 40)
            showHideFragment(mFragments[0], mFragments[4]);
        else if (triggleTag == 4)
            showHideFragment(mFragments[4], mFragments[0]);
        if (triggleTag == 50)
            showHideFragment(mFragments[0], mFragments[5]);
        else if (triggleTag == 5)
            showHideFragment(mFragments[5], mFragments[0]);

    }

}
