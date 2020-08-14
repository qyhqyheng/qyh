package com.justec.pillowalcohol.activity;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.SystemClock;
import android.util.Log;

import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.fragment.connect.ConnectFragment;
import com.justec.pillowalcohol.fragment.connect.SocketFragment;
import com.justec.pillowalcohol.media.MediaInterface;
import com.justec.pillowalcohol.media.MediaManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class PillowApplication extends Application{
    private static final String TAG = "PillowApplication";
    private static volatile PillowApplication singletonInstance;
    private FragmentManager mFm;
    ConnectFragment mConnectFragment;
    SocketFragment mSocketFragment;
    private ArrayList<BaseMainFragment> mFragmentList = new ArrayList<BaseMainFragment>();

    public static PillowApplication getInstance() {
        if(singletonInstance==null){
            singletonInstance=new PillowApplication();
        }
        return singletonInstance;
    }

    @Override
    public void onCreate() {
        Initial();
        super.onCreate();
    }

    private void Initial(){
        EventBus.getDefault().register(this);
        mConnectFragment = new ConnectFragment();
        mSocketFragment = new SocketFragment();
        MediaManager.getInstance().initCallback();
    }


    private void SwithFragment(Fragment mCurrentFragmen,Fragment fragment){
        try {
            if(mCurrentFragmen != fragment){
                FragmentTransaction transaction = mFm.beginTransaction();
                if(!fragment.isAdded()){
                    // 没有添加过:
                    // 隐藏当前的，添加新的，显示新的
                    transaction.hide(mCurrentFragmen).add(R.id.fl_content, fragment).show(fragment);
                }else{
                    // 隐藏当前的，显示新的
                    transaction.hide(mCurrentFragmen).show(fragment);
                }
                mCurrentFragmen = fragment;
                transaction.commitAllowingStateLoss();
            }
        }catch (Exception e){
            BleLog.d("SwithFragment---"+e.toString());
        }
    }


    @Subscribe(sticky = true)
    public void SwitchMsg(UiMessage uiMessage){
        switch (uiMessage.getType()){
            case UiEvent.MSG_SWITCH_SOCKET:
//                SwithFragment(mConnectFragment,mSocketFragment);
                break;
            case UiEvent.MSG_SWITCH_TEST:

                break;
        }
    }
}
