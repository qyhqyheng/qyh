package com.justec.pillowalcohol.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.justec.blemanager.blemanager.BleManager;
import com.justec.blemanager.scan.BleScanRuleConfig;
import com.justec.common.BottomNaviView.BottomBar;
import com.justec.common.BottomNaviView.BottomBarTab;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.fragment.connect.ConnectControlFragment;
import com.justec.pillowalcohol.fragment.history.HistoryControlFragment;
import com.justec.pillowalcohol.fragment.setting.SettingControlFragment;
import com.justec.pillowalcohol.fragment.test.TestControlFragment;
import com.justec.pillowalcohol.helper.TabSelectedEvent;

//import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;

public class MainFragment extends SupportFragment {
    private static final int REQ_MSG = 10;

    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;

    //    private SupportFragment[] mFragments = new SupportFragment[4];
    private BaseMainFragment[] mFragments = new BaseMainFragment[4];

    private BottomBar mBottomBar;

    public static MainFragment newInstance() {
//        CrashReport.testJavaCrash();
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        Log.e("Jerry.xiao","MainFragment");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        SupportFragment firstFragment = findChildFragment(ConnectControlFragment.class);
        BaseMainFragment firstFragment = findChildFragment(ConnectControlFragment.class);
        if (firstFragment == null) {
            mFragments[FIRST] = ConnectControlFragment.newInstance();
            mFragments[SECOND] = TestControlFragment.newInstance();
            mFragments[THIRD] = HistoryControlFragment.newInstance();
            mFragments[FOURTH] = SettingControlFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_tab_container, FIRST,
                    mFragments[FIRST], mFragments[SECOND],mFragments[THIRD],mFragments[FOURTH]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = TestControlFragment.newInstance();
            mFragments[THIRD] = HistoryControlFragment.newInstance();
            mFragments[FOURTH] = SettingControlFragment.newInstance();

        }
        initBleManger();//初始化BleManager
    }

    private void initBleManger() {
        //初始化BleManager的一些相关参数
        BleManager.getInstance().init(getActivity().getApplication());
        BleManager.getInstance()
                .enableLog(true)//打开log
                .setMaxConnectCount(1)//连接数目
                .setOperateTimeout(1500);//设置操作超时操作,1.5s
        //设置扫描规则
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(6000)              // 扫描超时时间，可选，默认5秒
                .build();
        //开始扫描
        BleManager.getInstance()
                .initScanRule(scanRuleConfig);
    }

    private void initView(View view) {
        mBottomBar = (BottomBar) view.findViewById(R.id.bottomBar);
        mBottomBar
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_connect_selected, getResources().getString(R.string.Scocket_Connect)))
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_test_selected, getResources().getString(R.string.Test_Name))).
                addItem(new BottomBarTab(_mActivity, R.drawable.ic_history_record_selected, getResources().getString(R.string.History)))
                .addItem(new BottomBarTab(_mActivity, R.drawable.ic_person_selected, getResources().getString(R.string.Test_About)));


        // 模拟未读消息
//        mBottomBar.getItem(FIRST).setUnreadCount(9);

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
                Log.e("Jerry.xiao","position = "+position);

//                if(position!=0&&Common.getInstance().getSwitchFragment()){
//                    new ConnectControlFragment().triggler(3);//切换到连接界面
//                }

                BottomBarTab tab = mBottomBar.getItem(FIRST);
                if (position == FIRST) {
//                    tab.setUnreadCount(0);
                } else {
//                    tab.setUnreadCount(tab.getUnreadCount() + 1);
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                // 在FirstPagerFragment,FirstHomeFragment中接收, 因为是嵌套的Fragment
                // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新

//                EventBusActivityScope.getDefault(_mActivity).post(new TabSelectedEvent(position));
            }
        });
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQ_MSG && resultCode == RESULT_OK) {

        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    /**
     * start other BrotherFragment
     */
    public void startBrotherFragment(SupportFragment targetFragment) {
        start(targetFragment, SINGLETASK);//类似于栈顶复用
    }

    /**
     * 切换到指定坐标的Fragment
     *
     * @param position
     */
    public void switchTargetFragment(int position) {
        mBottomBar.setCurrentItem(position);
    }

    /**
     * 获取到指定坐标的Fragment对象
     *
     * @param position
     */
    public BaseMainFragment targetPositionFragment(int position) {
        return mFragments[position];
    }
}
