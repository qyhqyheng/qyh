package com.justec.pillowalcohol.fragment.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.justec.blemanager.utils.BleLog;
import com.justec.common.Interface.dataCallbackInterface;
import com.justec.common.event.ToastUtil;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.socketcontrol.SocketInterface;
import com.justec.socketcontrol.SocketManager;
import com.justec.socketcontrol.SocketStatus;
import com.justec.socketcontrol.WebClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URI;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.OnClick;

public class SocketFragment extends BaseMainFragment implements SocketInterface<SocketStatus> {

    private String ProductName="";
    private String SerizableName="";
    private String TextData="";
    private String AlarmDevice="";
    private String AlarmEnable="";
    private String DataMsg="";
    private int PackageCount = 0;
    private static final int PackageMaxCount=99;
    private static final String Default_URL = "ws://121.15.182.147:9000";
    private static final String FixedType = "\r\n";
    private static final int MaxCount = 10;
    private WebClient mClient;
    int Count=0;
    private static final String Suffix = FixedType+FixedType+"$";
    private static final String PreTestDataSuffix="Android"+FixedType+"User"+FixedType+"ZT"+FixedType;

    @BindView(R.id.et_url)
    EditText et_url;
    @BindView(R.id.bt_socketconnect)
    Button bt_socketconnect;

    @Override
    protected int getContentLayoutId() {
        return R.layout.socket_connect;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        EventBus.getDefault().register(this);
        et_url.setText(Default_URL);
        et_url.setSelection(et_url.getText().toString().length());
        SocketManager.getInstance().initCallback(this);
    }

    @OnClick({R.id.iv_back,R.id.bt_socketconnect})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                ((ConnectControlFragment) getParentFragment()).triggler(3);//切换到连接界面
//                Common.getInstance().setSwitchFragment(false);
                break;
            case R.id.bt_socketconnect:
                handleConnect();
                break;

        }
    }

    /**
     * 处理连接事件
     */
    private void handleConnect() {
        if (mClient == null) {//未连接
            SocketConnect();
        } else if (mClient.getStatus() == SocketStatus.CONNECTED) {//已连接,则断开
            SocketDisConnect();
        } else if (mClient.getStatus() == SocketStatus.CONNECTING) {//连接中,不做处理
        } else {//其他状态,连接服务端
            SocketConnect();
        }
    }

    @Subscribe(sticky = true)
    public void DispatchInitial(UiMessage uiMessage){
        switch (uiMessage.getType()){
            case UiEvent.MSG_DATA_SERIZABLE:
                SerizableName="";
                SerizableName = uiMessage.getMsg()+FixedType;
                break;

            case UiEvent.MSG_DATA_PRUNAME:
                ProductName="";
                ProductName = uiMessage.getMsg()+FixedType;
                break;

            case UiEvent.MSG_DATA_TEXT:
                DataHandle_UpLoad(uiMessage.getMsg());
                break;

            case UiEvent.MSG_DATA_AlarmEnAble:
                AlarmDevice="";
                AlarmDevice = uiMessage.getMsg()+FixedType;
                break;

            case UiEvent.MSG_DATA_SOCKET:
                SendSocketData();
                break;
        }
    }

    private void DataHandle_UpLoad(String data){
        try {
            if(Count< MaxCount){
                TextData = TextData+data+",";
                Count++;
            }else {
                SendSocketData();
            }
        }catch (Exception e){
            BleLog.d("DataHandle_UpLoad==="+e.toString());
        }
    }

    private void SendSocketData(){
        try {
            AlarmEnable = DealWithAlarmEnable(Common.getInstance().getAlamDateEnable());
            TextData = TextData.substring(0,TextData.length()-1)+FixedType;
            if(PackageCount>=PackageMaxCount){
                PackageCount = 0;
            }

            BleLog.d("ProductName="+ProductName+"---SerizableName="+SerizableName+"---TextData="+TextData+"---AlarmEnable="+AlarmEnable
                    +"---AlarmDevice="+AlarmDevice+"---PackageCount="+String.valueOf(PackageCount+FixedType)+"---Suffix="+Suffix);

            DataMsg = PreTestDataSuffix+ProductName+SerizableName+TextData+AlarmEnable+AlarmDevice+String.valueOf(PackageCount+FixedType)+Suffix;
            if(mClient!=null){
//                BleLog.d("SendDataMsg="+DataMsg);
                mClient.send(DataMsg);
            }
            TextData="";
            Count=0;
            PackageCount++;
        }catch (Exception e){
            BleLog.d("SendSocketData==="+e.toString());
        }
    }

    private String DealWithAlarmEnable(Boolean Enable){
        String result;
        if(Enable){
            result = "1";
        }else {
            result = "0";
        }
        return result+FixedType;
    }

    private void SocketConnect(){
        try {
            mClient = WebClient.getSingleTon(new URI(et_url.getText().toString().trim()));
            Log.e("connect","URL="+et_url.getText().toString().trim());
            mClient.connect();
        }catch (URISyntaxException e){
            BleLog.d("SocketConnect==="+e.toString());
        }
    }

    private void SocketDisConnect(){
        if (mClient != null){
            mClient.close();
        }
    }

    public static SocketFragment newInstance() {
        Bundle args = new Bundle();
        SocketFragment fragment = new SocketFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onDestroy() {
        BleLog.d("SocketFragment---onDestroy");
        super.onDestroy();
    }

    @Override
    public void SocketStatusCallback(SocketStatus data) {
        getActivity().runOnUiThread(updateThread);
    }

    Runnable updateThread = new Runnable(){
        @Override
        public void run()
        {
            switch (mClient.getStatus()) {
                case INIT:
                    bt_socketconnect.setText(getResources().getString(R.string.Scocket_Connect));
                    break;
                case CONNECTING:
                    bt_socketconnect.setText(getResources().getString(R.string.Connecting));
                    break;
                case CONNECTED:
                    ToastUtil.showToast(getContext(),getResources().getString(R.string.Connect_Sucess));
                    bt_socketconnect.setText(getResources().getString(R.string.Break));
                    break;
                case DISCONNECTED:
                    ToastUtil.showToast(getContext(),getResources().getString(R.string.BreakLink));
                    bt_socketconnect.setText(getResources().getString(R.string.Scocket_Connect));
                    break;
                case ERROR:
                    bt_socketconnect.setText(getResources().getString(R.string.Scocket_Connect));
                    break;
                default:
                    break;
            }
        }
    };
}
