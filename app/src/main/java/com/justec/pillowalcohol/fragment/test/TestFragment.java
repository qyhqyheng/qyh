package com.justec.pillowalcohol.fragment.test;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.justec.blemanager.messageEvent.AnalyzeData;
import com.justec.blemanager.messageEvent.Disconnect;
import com.justec.blemanager.messageEvent.ResultCommand;
import com.justec.blemanager.messageEvent.SettingUploadSpace;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.DateUtil;
import com.justec.blemanager.utils.HexUtil;
import com.justec.common.CommonLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.activity.MainActivity;
import com.justec.pillowalcohol.dataBase.DBManager;
import com.justec.pillowalcohol.dataBase.ItemTime;
import com.justec.pillowalcohol.dataBase.Person;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.event.EventManager;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.fragment.MainFragment;
import com.justec.pillowalcohol.helper.DashboardView;
import com.justec.pillowalcohol.helper.HighlightCR;
import com.justec.pillowalcohol.helper.LineChart;
import com.justec.pillowalcohol.media.MediaManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;

public class TestFragment extends BaseMainFragment{
    @BindView(R.id.DataBoardView)
    DashboardView dataBoardView;
    @BindView(R.id.tv_show_time)
    TextView tvShowTime;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R.id.rl_toolbar)
    RelativeLayout rlToolBar;
    @BindView(R.id.tv_alarm_data)
    TextView tvAlarmData;

    private  long dataSizeLimit = 0;//x轴内显示有效点数
    private static final int HANDLER_UPLOAD_DATA_MSG = 0X00;
    private static final int HANDLER_UPLOAD_TIME_MSG = 0X01;
    private static final long XLABEL_TIME_SUM = 10*60*1000;
    private static final int XLABEL_NUMBER = 5;//X轴分为5大刻度
    private static final long XLABEL_SPACE_TIME = XLABEL_TIME_SUM/XLABEL_NUMBER;//X轴每一大格的时间间隔
    private int moveNum = 0;//x轴动态移动点数
    private boolean flag = false;//x轴是否动态移动
//    private boolean isTesting = false;//判断是否在测试

    private float uploadValue ;//设备上传到app的测试值
    private float uploadValue1 ;//设备上传到app的测试值
    private int uploadSpace ;//上传时间间隔值
    private long secondCount = 0;
    private int dataStr_index = 0;//测试次数计数
    private int DataCount ;

    private String alarmLimite = "20";//报警限制
    MainActivity mActivity;
    private SharedPreferences preferences;
    private boolean isVibrate;
    private boolean isAlarm;
    private boolean vibrateFlag = false;
    private boolean MusicFlag = true;
    private boolean LoadDateFlag = false;

    ArrayList<Float> dataStr = new ArrayList<Float>();
    ArrayList<String> db_all_data = new ArrayList<String>();       //全部的测试数据
    ArrayList<String> db_item_time = new ArrayList<String>();     //每一条记录的时间列表
    ArrayList<String> db_item_show_information = new ArrayList<String>();     //历史记录的每列显示列表
    private int data_count = 0;
//    Vibrator vibrator;
    long[] patter = {1000, 1000};

    String[] YLabel = new String[10];
    ArrayList XLabel = new ArrayList();
    LineChart view;
    LinearLayout layout;
    private DBManager dm;
    //List<Person> persons = new ArrayList<>();
    private uploadToServerTask muploadToServerTask = new uploadToServerTask();
    List<HighlightCR> highlight = new ArrayList<>();

    int Dispatch_DbCount = 0;    //每十个数据保存到数据库
    public static final int Dispatch_SubTime = 10;
    public static final int Test_MaxTime = 24*60*60;                        //最大测试时长，超过则重新生成Item
    String mDleDataItem="";   //删除的数据是否是当前正在生成的
    int mTemCount = 0;
    List<Person> TemPersons = new ArrayList<>();

    float[] testdata_arr;
    int TestCount = 0;
    int MaxTestCount;

    Boolean LimiteFlag = false;
    Boolean StopPlayFlag = false;

    private int TimeSpaceCount = 0;              /** 时间间隔的计数 * */
    private int UpdateBoardViewTimeCount = 0;   /** 指示盘刷新计数* */

    private int TimeCalMax = 25;
    private int TimeCalCount = 0;


    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_test;
    }

    public static TestFragment newInstance() {
        Bundle args = new Bundle();
        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public static TestFragment newInstance(Bundle bundle) {
        TestFragment fragment = new TestFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MainActivity ) getActivity();
        dm = new DBManager(getActivity());
        //默认设置限值为2        //圆弧为200度，默认最大显示值为20，所以*10
        highlight.add(new HighlightCR(170, 20, Color.parseColor("#58ce41")));
        highlight.add(new HighlightCR(190, 180, Color.parseColor("#c72424")));
//        vibrator = (Vibrator)getActivity().getSystemService(mActivity.VIBRATOR_SERVICE);
        MediaManager.getInstance().mediaInterface.getContext(MainActivity.context);
        //保存参数设置界面的数据
//        preferences = mActivity.getSharedPreferences("Preferences", mActivity.MODE_PRIVATE);
        Log.e("onCreateView","初始化界面");
        isVibrate = MediaManager.getInstance().bValibrate;
        isAlarm = MediaManager.getInstance().bAlarm;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        dm.createTable();//ToDo 有待优化
        dataBoardView.setStripeHighlightColorAndRange(highlight);
        dataBoardView.setRealTimeValue(uploadValue);
        tvShowTime.setText(getStringTime(secondCount));
        rlToolBar.setBackgroundColor(Color.parseColor("#4b5cc4"));
        Common.getInstance().setTotalTestTime("");
    }

    private void init() {
        dataSizeLimit = XLABEL_TIME_SUM/1000;   //采样频率固定为1
        layout = getActivity().findViewById(R.id.lv_linechart);
        view = new LineChart(getActivity());
        YLabel = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        XLabel.clear();
        setXlabeldata(DateUtil.getTimeToHHmm());
        view.setLimite(alarmLimite);
        view.setDate(YLabel,XLabel,dataStr,dataSizeLimit,0);
//      view.setBackgroundDrawable(getResources().getDrawable(R.drawable.viewbackground));//给整个View加背景
        layout.addView(view);
    }
    private void setXlabeldata(String startTime){
        XLabel.add(startTime);
        for(int i=1;i<=XLABEL_NUMBER;i++){
            XLabel.add(DateUtil.getXlabelTime(XLABEL_SPACE_TIME*i));
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_UPLOAD_DATA_MSG:
                    UpdateBoardView();    //刷新指示盘
                    if(dataStr_index<= dataSizeLimit){
                        AddValue(false);
                        layout.removeAllViews();
                        view.setDate(YLabel,XLabel,dataStr,dataSizeLimit,0);
                        layout.addView(view);
                    }else {
                        AddValue(true);
                        if(!flag){
                            XLabel.remove(0);
                            XLabel.add(DateUtil.getXlabelTime(XLABEL_SPACE_TIME));
                            flag = true;
                        }
                        if((dataStr_index-dataSizeLimit)%(dataSizeLimit/XLABEL_NUMBER)==0){
                            flag = false;
                            moveNum = 0;
                        }
                        moveNum++;
                        layout.removeAllViews();
                        view.setDate(YLabel,XLabel,dataStr,dataSizeLimit,moveNum);
                        layout.addView(view);
                    }
//                    float[] testdata_arr = new float[5];
//                    int TestCount = 0;
//                    int MaxTestCount = 5;
//                    if(TestCount == MaxTestCount){
//                        TestCount = 0;
//                        db_all_data.add(String.valueOf(MeanFloat(testdata_arr)));
//                        db_item_time.add(DateUtil.getLastTime(0));
//                        db_item_show_information.add(DateUtil.getLastTime(0));
//                    }
//                    testdata_arr[TestCount] = uploadValue;
//                    ++TestCount;
                    BleLog.d("db_data.size = "+db_all_data.size()+" db_item_date.size = "+ db_item_time.size());
                    break;
                case HANDLER_UPLOAD_TIME_MSG:
                    secondCount += DataCount;
                    tvShowTime.setText(getStringTime(secondCount));
                    TimeCalCount++;
                    if(TimeCalCount==TimeCalMax){
                        TimeCalCount = 0;
                        String commandCode = EventManager.getInstance().ReadCommandCode(ResultCommand.WRITE_TIME_CAL);
                        EventManager.getInstance().sendComand(commandCode);
                        BleLog.e("Testing000------getUploadData---TimeCal-=--------sendComand---commandCode="+commandCode);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void UpdateBoardView(){
        try {
            float TmpValue = 0;
            if(uploadValue>100)uploadValue = 100;  //折线图设置上限值为100
            if(uploadValue>=Integer.valueOf(alarmLimite)){
                TmpValue = uploadValue;
//                UpdateBoardViewTimeCount = 0;
            }else {
                if(UpdateBoardViewTimeCount>=DataCount){
                    TmpValue = testdata_arr[DataCount-1];
                }else {
                    TmpValue = testdata_arr[UpdateBoardViewTimeCount];
                }
                UpdateBoardViewTimeCount++;
            }
            BleLog.e("Testing------UpdateBoardView------TmpValue = "+TmpValue+"    UpdateBoardViewTimeCount = "+UpdateBoardViewTimeCount);
            if (dataBoardView != null) {
                //dataBoardView.setRealDataValue(uploadValue);
                dataBoardView.setRealTimeValue(TmpValue);
                highlight.clear();
                if(TmpValue>20){
                    //圆弧为200度，最大显示值为100，所以*2
                    highlight.add(new HighlightCR(170, Integer.valueOf(alarmLimite)*2, Color.parseColor("#58ce41")));
                    highlight.add(new HighlightCR(170+Integer.valueOf(alarmLimite)*2, 200-Integer.valueOf(alarmLimite)*2, Color.parseColor("#c72424")));
                    dataBoardView.setMaxValue(100);
                }
                else{
                    //圆弧为200度，默认最大显示值为20，所以*10
                    highlight.add(new HighlightCR(170, Integer.valueOf(alarmLimite)*10, Color.parseColor("#58ce41")));
                    highlight.add(new HighlightCR(170+Integer.valueOf(alarmLimite)*10, 200-Integer.valueOf(alarmLimite)*10, Color.parseColor("#c72424")));
                    dataBoardView.setMaxValue(20);
                }
            }
        }catch (Exception e){
            CommonLog.e("UpdateBoardView------"+e.toString());
        }
    }

    private void AddValue(boolean move){
        try {
            if(uploadValue<Integer.valueOf(alarmLimite)){
                if(TimeSpaceCount%DataCount!=0){    //MaxTestCount
                    TimeSpaceCount++;
                    return;    //每隔设置的时间间隔添加数据
                }else{
                    TimeSpaceCount = 0;
                    TimeSpaceCount++;
                }
                for(int m=0;m<DataCount;m++){
                    if(move)
                        dataStr.remove(0);
                    db_item_time.add(DateUtil.getLastTime(DataCount-1-m));
                    db_item_show_information.add(DateUtil.getLastTime(DataCount-1-m));
                    db_all_data.add(String.valueOf(testdata_arr[m]));
                    updateTestTime();
                    dataStr.add(testdata_arr[m]);    //添加缓存的数据
                    if(m==DataCount-1){
                        calculateYLabel(testdata_arr[m]);
                    }
                }
            }
            else {
                if(move)
                   dataStr.remove(0);
                dataStr.add(uploadValue);
                db_item_time.add(DateUtil.getLastTime(0));
                db_item_show_information.add(DateUtil.getLastTime(0));
                db_all_data.add(String.valueOf(uploadValue));
                updateTestTime();
                calculateYLabel(uploadValue);
            }

            if(Dispatch_DbCount>=Dispatch_SubTime){       //每隔10s，把数据加到数据库
                if(Dispatch_DbCount<Test_MaxTime){
                    add(db_all_data, db_item_time,true);
                    addItem(db_item_show_information,true);
                }else {
                    add(db_all_data, db_item_time,false);
                    addItem(db_item_show_information,false);
                }
            }
            Dispatch_DbCount++;
//            BleLog.e("TestFragment---AddValue------db_all_data="+db_all_data);
        }
        catch (Exception e){
            CommonLog.e("AddValue-----"+e.toString());
        }
    }

    /*
    * 根据数据计算Y轴的高度实时更新
    * */
    private void calculateYLabel(float uploadValue){
        float max = 0;
        for(int i=0;i<dataStr.size();i++){
            if(dataStr.get(i) > max){
                max = dataStr.get(i);
            }
        }
        int scal = (int) (max/10 + 1);
        int j;
        //动态设置Y轴上限最大值，依次增加20,最大100
        for(j=1;j<=10;j++){
            if(j>=scal){
                scal = j;
                break;
            }
        }
        if(scal>1){
            if(scal>=10)scal = 10;
            for(int i=0;i<=(YLabel.length-1);i++){
                YLabel[i] = String.valueOf(scal*(i+1));
            }
        }else{
            YLabel = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        }
    }
    private Runnable mUpdateDataThread = new Runnable() {
        public void run() {
            dataStr_index++;
            updateStatus();
        }
    };
    private void updateStatus(){
        Message message =Message.obtain();
        message.what=HANDLER_UPLOAD_DATA_MSG;
        handler.sendMessage(message);
        handler.postDelayed(mUpdateDataThread, 1000); //1s是报警后默认上传时间
    }

    private Runnable mUpdateTimeThread = new Runnable(){
        public void run() {
            updateTestTime();
        }
    };
    private void updateTestTime() {
        Message message =Message.obtain();
        message.what=HANDLER_UPLOAD_TIME_MSG;
        handler.sendMessage(message);
//        handler.postDelayed(mUpdateTimeThread, 1000);
    }
    /**
     * 获取设备上传值
     */
    @SuppressLint("ResourceAsColor")
    @Subscribe(sticky = true)
    public void getUploadData(AnalyzeData analyzeData){
        String data = analyzeData.getData();
        DataCount = analyzeData.getCount();
        if(!LoadDateFlag){
            LoadDateFlag = true;
            updateStatus();
//            updateTestTime();
        }
        BleLog.e("Testing000------getUploadData------data="+data+"    DataCount ="+DataCount);
        if(DataCount>MaxTestCount){
            testdata_arr = new float[DataCount];
//            UpdateBoardViewTimeCount = DataCount;
        }
        UpdateBoardViewTimeCount = 0;
        if(DataCount==0)
            return;
//        if(data.length()%2==0)
        ChangeDataArr(data);
//        BleLog.e("getUploadData------getData="+analyzeData.getData());
//        BigDecimal bg = new BigDecimal(0.0);
//        uploadValue = bg.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
//        if(data.length()<=2){
//            uploadValue = testdata_arr[0];
//        }else {
//            uploadValue = testdata_arr[data.length()/2-1];
//        }
//        uploadValue = 0.0;
//        DataCount = analyzeData.getCount();

        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_TEXT,uploadValue+""));
        BleLog.d(alarmLimite+"= alarmLimite===uploadValue="+uploadValue+"---alarmLimite="+alarmLimite + "---isAlarm="+isAlarm +"---isVibrate="
                +isVibrate+"---MusicFlag="+MusicFlag+"---vibrateFlag="+vibrateFlag);
        //导航栏警报变色
//        if(uploadValue<2)
//            uploadValue = 0;//警戒值以下设为0.
        if(uploadValue>=Integer.valueOf(alarmLimite)){
            rlToolBar.setBackgroundColor(0xFFB00000);
        }
        else {
            rlToolBar.setBackgroundColor(Color.parseColor("#4b5cc4"));
        }
        MediaManager.getInstance().MediaAction(uploadValue);
    }

    private void ChangeDataArr(String data){
        try {
              if(testdata_arr==null)
                  return;
              String value = data;
//              int len = data.length()/2;
              for(int m=0;m<DataCount;m++){
//                  if(m<MaxTestCount){
                      float tmp = HexUtil.formatHexStringTo10Int(value.substring(2*m,2*(m+1)));
                      BigDecimal bg = new BigDecimal(tmp);
                      testdata_arr[m] = bg.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
                      BleLog.e("ChangeDataArr----testdata_arr["+m+"]="+testdata_arr[m]);
//                  }
              }
//              if(DataCount<MaxTestCount){
//                  uploadValue = testdata_arr[0];
//              }else {
                  uploadValue = testdata_arr[DataCount-1];
//              }
              CommonLog.e(uploadValue+"=uploadValue---UpdateBoardView---ChangeDataArr---DataCount="+DataCount+"");
        }
        catch (Exception e){
            CommonLog.e("ChangeDataArr------"+e.toString());
        }
    }

    /**
     * 获取设备上传时间间隔消费
     */
    @Subscribe(sticky = true)
    public void getUploadSpace(SettingUploadSpace settingUploadSpace){
        uploadSpace =  Math.round(settingUploadSpace.getSpaceTime()*1000);
        MaxTestCount = Math.round(settingUploadSpace.getSpaceTime());
        testdata_arr = new float[MaxTestCount];
        EventManager.getInstance().setSpaceTime(MaxTestCount);
        BleLog.e("TestFragment---getUploadSpace-----uploadSpace = "+uploadSpace+"   MaxTestCount="+MaxTestCount);
    //if(uploadSpace!=200)
    //dataSizeLimit = XLABEL_TIME_SUM/uploadSpace;
    //dataSizeLimit = XLABEL_TIME_SUM/200;
    }
    /**
     * 接收到断开连接信息消费
     */
    @Subscribe(sticky = true)
    public void clearInfo(Disconnect disconnect){
        BleLog.e("clearInfo-------断开连接！");
        handler.removeCallbacks(mUpdateDataThread);
        handler.removeCallbacks(mUpdateTimeThread);
        TimeCalCount = 0;
        secondCount = 0;
        uploadValue = 0;
//        DataCount = 0;
        Dispatch_DbCount=0;
        //重新初始化折线图
        dataStr_index = 0;
        dataStr.clear();
        MediaManager.getInstance().mediaInterface.TestingStatus(false);
//        isTesting =false;
        LoadDateFlag = false;
        //db_end_date.add(DateUtil.getCurrentTime());
        add(db_all_data, db_item_time,false);
        addItem(db_item_show_information,false);

        MediaManager.getInstance().StopMedia();

        if(layout!=null)
        layout.removeAllViews();
//        vibrator.cancel();
        tvDeviceName.setText("");
        //dataBoardView.setRealDataValue(uploadValue);
        ((MainFragment) getParentFragment().getParentFragment()).switchTargetFragment(0);//直接切换至ConnectFragment_1//切换搜索页
    }
 /*   @Subscribe(sticky = true)
    public void getHistoryValue(ReceiveAlarmLimite receiveAlarmLimite) {
        alarmLimite = String.valueOf(receiveAlarmLimite.getValue()/100);
        tvAlarmData.setText(alarmLimite);
    }*/

    @Subscribe(sticky = true)
    public void UiEventMsg(UiMessage uiMessage){
        switch (uiMessage.getType()){
            case UiEvent.MSG_ALARMDATA_LIMITE:
                AlarmDateHandle(uiMessage.getMsg());
                break;
            case UiEvent.MSG_DATA_DELETE:
                mDleDataItem = uiMessage.getMsg();
                DeleteItem(db_item_time);
                break;
        }
    }

    private void  AlarmDateHandle(String data){      //警报限值数据接收
        try {
            BleLog.e("TestingUpdataView-----AlarmDateHandle===data="+data);
//            MediaAction.getInstance().setAlamValue(Integer.valueOf(data));
            MediaManager.getInstance().mediaInterface.AlarmValue(Integer.valueOf(data));
            alarmLimite = data;
            tvAlarmData.setText(alarmLimite+"mg/100ml");
            //圆弧为200度，默认最大显示值为20，所以*10
            highlight.add(new HighlightCR(170, Integer.valueOf(alarmLimite)*10, Color.parseColor("#58ce41")));
            highlight.add(new HighlightCR(170+Integer.valueOf(alarmLimite)*10, 200-Integer.valueOf(alarmLimite)*10, Color.parseColor("#c72424")));
            if(!MediaManager.getInstance().TestStatus){
                init();
                SystemClock.sleep(100);
                if(LoadDateFlag){
//                    updateStatus();
//                    updateTestTime();
                }
                //startTask();
                MediaManager.getInstance().mediaInterface.TestingStatus(true);
//                isTesting = true;
//                MediaAction.getInstance().setTesting();
                tvDeviceName.setText(EventManager.getInstance().getBleDevice().getName());
            }
            if(EventManager.getInstance().getBleDevice()==null){
                tvDeviceName.setText(getResources().getString(R.string.Equipment_UnConnect));
            }
            //tvDataCount.setText(String.valueOf(DataCount));
            //刷新折线图报警限值
            view.setLimite(alarmLimite);
            view.setDate(YLabel,XLabel,dataStr,dataSizeLimit,0);
        }catch (Exception e){
            BleLog.d("AlarmDateHandle==="+e.toString());
        }
    }

    private String getStringTime(long cnt) {
        long hour = cnt/3600;
        long min = cnt % 3600 / 60;
        long second = cnt % 60;
        //BleLog.d("second = "+second+" min = "+min);
        return String.format(Locale.CHINA,"%02d:%02d:%02d",hour,min,second);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LoadDateFlag = false;
//        vibrator.cancel();
        if(MediaManager.getInstance().TestStatus){
            add(db_all_data, db_item_time,false);
            addItem(db_item_show_information,false);
        }
        EventBus.getDefault().unregister(this);
        dm.closeDB();
        handler.removeCallbacks(mUpdateDataThread);
        handler.removeCallbacks(mUpdateTimeThread);
        BleLog.d("onDestroyView -=-=-");
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaManager.getInstance().StopMedia();
        BleLog.d("onStop -=-=-");
    }

    public void add(ArrayList<String> dataValue, ArrayList<String> date,Boolean Flag){
        //Person[] dataInfo = Person.getStudentInstance(dataValue.size(),dataValue);
        data_count = data_count+dataValue.size();    //记录存储的数据个数
        List<Person> persons = new ArrayList<>();
        Person[] person = new Person[dataValue.size()-mTemCount];

        for(int i = mTemCount;i<dataValue.size();i++){          //分段截取，防止重复添加导致查找错乱
            person[i-mTemCount] = new Person(date.get(i),dataValue.get(i));
            persons.add(person[i-mTemCount]);
        }
        mTemCount = mTemCount+persons.size();
        dm.add(persons);//添加对象到数据库
        if(!Flag){
            mTemCount=0;
            db_all_data.clear();
            db_item_time.clear();
        }
    }

    public void addItem(ArrayList<String> date,Boolean Flag){
        if(date.size()<=0)
            return;
        List<ItemTime> items = new ArrayList<>();
        ItemTime[] item =new ItemTime[1];
        //参数注释：第一个为item显示时间，第二个为测试时长，第三个为数据个数
//        item[0] = new ItemTime(date.get(0),String.valueOf(DateUtil.getTimeSpace(date.get(date.size()-1),
//                date.get(0))),date.size(),alarmLimite);
        item[0] = new ItemTime(date.get(0),String.valueOf(date.size()*1000),date.size(),alarmLimite);
        items.add(item[0]);
        BleLog.d("mDleDataItem="+mDleDataItem+"---date.get(0)="+date.get(0)+"---item = "+date.get(date.size()-1)+" time = "+date.get(0)+" size = "+date.size());
        dm.deleteItem(date.get(0));
        dm.addItem(items);//添加对象到数据库
        if(!Flag){
            Dispatch_DbCount=0;
            db_item_show_information.clear();
        }
    }

    public void DeleteItem(ArrayList<String> date){
        if(date.size()<=0)
            return;
        dm.deleteItem(mDleDataItem);
        if(mDleDataItem.equals(date.get(0))){
            db_item_show_information.clear();
            db_all_data.clear();
            db_item_time.clear();
            mDleDataItem="";
            Dispatch_DbCount=0;
            mTemCount=0;
        }
    }

    public void update(View view){
        Person p = new Person();
        p.setName("jhon");
        //p.setAge(40);
        dm.updateAge(p);
    }

    public class uploadToServerTask extends TimerTask {
        public void run() {
            add(db_all_data, db_item_time,false);
            BleLog.d("add===uploadToServerTask");
        }
    }
    /**
     * start Timer
     */
    public synchronized void startTask() {
        stopTask();
        if (muploadToServerTask == null) {
            muploadToServerTask = new uploadToServerTask();
            Timer m_task = new Timer();
            m_task.schedule(muploadToServerTask, 0, 60*1000);
        }
    }
    /**
     * stop Timer
     */
    public synchronized void stopTask() {
        try {
            if (muploadToServerTask != null) {
                muploadToServerTask.cancel();
                muploadToServerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float MeanFloat(float b[])
    {
        int i;
        int n = b.length;
        float sum = 0,aver;
        for(i = 0;i < n;i++)
            sum += b[i];
        aver = sum / n;
        return aver;
    }
}
