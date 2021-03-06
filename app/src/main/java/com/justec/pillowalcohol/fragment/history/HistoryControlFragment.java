package com.justec.pillowalcohol.fragment.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.fragment.setting.CalibrationFragment;
import com.justec.pillowalcohol.fragment.test.TestControlFragment;
import com.justec.pillowalcohol.fragment.test.TestFragment;

import me.yokeyword.fragmentation.SupportFragment;

public class HistoryControlFragment extends BaseMainFragment{
    private SupportFragment[] mFragments = new SupportFragment[2];

    public static HistoryControlFragment newInstance() {
        Bundle args = new Bundle();
        HistoryControlFragment fragment = new HistoryControlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_history_control;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //持久化持有Fragment 的引用，但是对应的Fragment刷新界面的操作生命周期就没有调用
        // 最好使用EventBus通知界面的改动
        SupportFragment firstFragment = findChildFragment(HistoryFragment.class);
        if (firstFragment == null) {
            mFragments[0] = HistoryFragment.newInstance();
            mFragments[1] = HistoryShowFragment.newInstance();
            //初始化连接切换状态的Fragment
            loadMultipleRootFragment(R.id.container_fg_history,
                    0,//默认显示的Fragment
                    mFragments[0],mFragments[1]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[0] = firstFragment;
            mFragments[1] = HistoryShowFragment.newInstance();
        }
    }

    //切换Fragment
    public void triggler(int triggleTag) {
        //放弃使用getAllConnectedDevice().size()来判断蓝牙连接状态，因为蓝牙应用变量变化太慢了
        // 直接使用标志位 0:
        if (triggleTag == 0)
            showHideFragment(mFragments[0], mFragments[1]);
        else if (triggleTag == 1)
            showHideFragment(mFragments[1], mFragments[0]);
    }

}
