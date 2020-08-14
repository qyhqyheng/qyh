package com.justec.pillowalcohol.fragment.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.scan.BleScanRuleConfig;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.fragment.BaseMainFragment;

import me.yokeyword.fragmentation.SupportFragment;

public class ConnectControlFragment extends BaseMainFragment {
    private SupportFragment[] mFragments = new SupportFragment[3];

    public static ConnectControlFragment newInstance() {
        Bundle args = new Bundle();
        ConnectControlFragment fragment = new ConnectControlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_connect_control;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //持久化持有Fragment 的引用，但是对应的Fragment刷新界面的操作生命周期就没有调用
        // 最好使用EventBus通知界面的改动
        SupportFragment firstFragment = findChildFragment(ConnectFragment.class);
        if (firstFragment == null) {
            mFragments[0] = ConnectFragment.newInstance();
            mFragments[1] = ConnectResultFragment.newInstance();
            mFragments[2] = SocketFragment.newInstance();
            //初始化连接切换状态的Fragment
            loadMultipleRootFragment(R.id.container_conenect_Triggle,
                    0,//默认显示的Fragment
                    mFragments[0],mFragments[1],mFragments[2]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[0] = firstFragment;
            mFragments[1] = ConnectResultFragment.newInstance();
        }

        //初始化BleManager的一些相关参数
        BleManager.getInstance().init(getActivity().getApplication());
        BleManager.getInstance()
                .enableLog(true)//打开log
                .setMaxConnectCount(1)//连接数目
                .setOperateTimeout(1500);//设置操作超时操作,1.5s
        //设置扫描规则
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                // 连接时的autoConnect参数，可选，默认false
                //.setAutoConnect(isAutoConnect)
                .setScanTimeOut(6000)// 扫描超时时间，可选，默认5秒//6000
                .build();
        //开始扫描
        BleManager.getInstance()
                .initScanRule(scanRuleConfig);
    }

    //切换Fragment
    public void triggler(int triggleTag) {
        //放弃使用getAllConnectedDevice().size()来判断蓝牙连接状态，因为蓝牙应用变量变化太慢了
        // 直接使用标志位 0: ConnectFragment_1  1:ConnectResultFragment_1
        if (triggleTag == 0)
            showHideFragment(mFragments[0], mFragments[1]);
        else if (triggleTag == 1)
            showHideFragment(mFragments[1], mFragments[0]);
        else if (triggleTag == 2)
            showHideFragment(mFragments[2], mFragments[0]);
        else if (triggleTag == 3)
            showHideFragment(mFragments[0], mFragments[2]);
    }
}