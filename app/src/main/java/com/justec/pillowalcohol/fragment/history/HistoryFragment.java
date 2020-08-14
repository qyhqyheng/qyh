package com.justec.pillowalcohol.fragment.history;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import android.widget.ImageView;
import android.widget.TextView;

import com.justec.blemanager.messageEvent.CommonValue;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.DateUtil;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.dataBase.DBManager;
import com.justec.pillowalcohol.dataBase.ItemTime;
import com.justec.pillowalcohol.dataBase.Person;
import com.justec.pillowalcohol.event.Common;
import com.justec.pillowalcohol.event.UiEvent;
import com.justec.pillowalcohol.event.UiMessage;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.listview.ChildBean;
import com.justec.pillowalcohol.listview.GroupBean;
import com.justec.pillowalcohol.listview.MyAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class HistoryFragment extends BaseMainFragment implements View.OnClickListener, ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener {
    //@BindView(R.id.tv_year)
    TextView tvYearTime;
    //@BindView(R.id.tv_month)
    TextView tvMonthTime;
    //@BindView(R.id.tv_day)
    TextView tvDayTime;
    ImageView ivDeleteItem;
    ImageView iv_delete_history;

    private ExpandableListView mListView;
    private MyAdapter adapter;
    private List<GroupBean> listGroupBean;

    private ArrayList<String> testDateStr = new ArrayList<String>();
    //listChildItem为每一段测试数据显示item名称
    ArrayList<String> listChildItem = new ArrayList<String>();
    //listChildItem为每一段测试数据最后一个数据点的测试时间
    ArrayList<String> listChildIndex = new ArrayList<String>();
    //listChildData为item中所有数据的一个数据点
    ArrayList<String> listChildData= new ArrayList<String>();
    //perChildData为每一段item的测试数据
    ArrayList<List<String>> perChildData = new ArrayList<List<String>>();

    //itemTotalTime为一段测试数据的测试时间
    ArrayList<String> itemTotalTime = new ArrayList<String>();
    //itemCount为一段测试数据个数
    ArrayList<Integer> itemCount = new ArrayList<Integer>();
    //itemLimite为一段测试数据测试时的报警限值
    ArrayList<String> itemLimite = new ArrayList<String>();
    ArrayList<String> listTestTime = new ArrayList<String>();
    private View rootView;
    private DBManager dm;
    private boolean isLoadFinish = false;
    private ProgressDialog progressDialog;
    private int flagClick = 2;
    private int flag = 0;
    private final static int DELETE_ALL = 1;
    private final static int DELETE_ITEM = 2;

    protected HandlerThread deleteThread;
    protected Handler deleteHandler;

    private int TmpgroupPos;
    private int TmpPos;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_history;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, null);
        dm = new DBManager(getActivity());
        //initData();
        tvYearTime = rootView.findViewById(R.id.tv_year);
        tvMonthTime = rootView.findViewById(R.id.tv_month);
        tvDayTime = rootView.findViewById(R.id.tv_day);
        iv_delete_history = rootView.findViewById(R.id.iv_delete_history);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvYearTime.setOnClickListener(this);
        tvMonthTime.setOnClickListener(this);
        tvDayTime.setOnClickListener(this);

        mListView = rootView.findViewById(R.id.my_listview);
        mListView.setOnChildClickListener(this);

        tvYearTime.setSelected(false);
        tvDayTime.setSelected(false);
        tvMonthTime.setSelected(true);
        mListView = rootView.findViewById(R.id.my_listview);

        /*ivDeleteItem =rootView.findViewById(R.id.iv_delete_item);
        ivDeleteItem.setOnClickListener(this);
*/
        iv_delete_history.setOnClickListener(this);
        if(listGroupBean!=null){
            loadAdapter();
        }
       /* query();
        if(listGroupBean!=null){
            adapter = new MyAdapter(listGroupBean, getActivity());
            mListView.setAdapter(adapter);
            mListView.setGroupIndicator(null);
        }*/
    }
    public static HistoryFragment newInstance() {
        Bundle args = new Bundle();
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();

        showDialog(getResources().getString(R.string.Data_Loading));
        MyTask myTask = new MyTask(1);
        myTask.execute();

        deleteThread = new HandlerThread("deleteThread");
        deleteThread.start();
        deleteHandler = new Handler(deleteThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case DELETE_ALL:
                        dm.deletTable();
                        listGroupBean.clear();
                        loadAdapter();
                        break;
                    case DELETE_ITEM:
                        String startTimeItem =listGroupBean.get(TmpgroupPos).getChildren().get(TmpPos).getdate();
                        listGroupBean.get(TmpgroupPos).getChildren().remove(TmpPos);
                        dm.deleteItem(startTimeItem);//删除数据库中对应item
                        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_DELETE,startTimeItem));
                        if(flagClick ==1)
                            showItemList(0,4,startTimeItem);
                        if(flagClick ==2)
                            showItemList(0,7,startTimeItem);
                        if(flagClick ==3)
                            showItemList(0,10,startTimeItem);

                        if(listGroupBean!=null){
                            loadAdapter();
                        }
                        break;
                }
                return true;
            }
        });

       /* if(!isLoadFinish){
            showDialog("加载数据");
        }*/
    }

   /* public boolean queryListData(){
        List<ItemTime> items = dm.queryItem();
        for (int i = 0;i<items.size();i++){
            ItemTime item = items.get(i);
            Log.d("Jerry.Xiao","item = "+item.getItemInfo()+" time = "+item.getTimeTotal());
        }


        List<Person>persons = dm.findAllPerson();
        ArrayList<Map<String,String>> list = new ArrayList<>();
        listGroupBean = new ArrayList<GroupBean>();
        int index = 0;

        //避免数据重复叠加
        listChildItem.clear();
        listChildIndex.clear();
        listChildData.clear();
        //for (Person p:persons){
        for (int i = 0;i<persons.size();i++){
            Person p =persons.get(i);
            listChildData.add(p.getInfo());
            if(i>0) {
                    //测试中断一次为一个数据,默认设置1500ms无数据为中断一次
                    if (DateUtil.getTimeSpace(persons.get(i).getName(), persons.get(i - 1).getName()) > 1500) {
                        //记录每段数据起始时间作为item
                        listChildItem.add(p.getName());
                        //记录每段数据最后一个测试时间，减去这段数据起始时间则为测试时长
                        listChildIndex.add(persons.get(i - 1).getName());

                        perChildData.add(new ArrayList<String>(listChildData.subList(index,listChildData.size()-1)));

                        index = i;

                    }
            }else{
                listChildItem.add(p.getName());
                //listChildData.add(p.getInfo());
            }
        }
        if(persons.size()>0){
            listChildIndex.add(persons.get(persons.size()-1).getName());//最后一段数据的测试时间
            perChildData.add((new ArrayList<String>(listChildData.subList(index,listChildData.size()))));
        }

        showItemList(0,4);//默认显示年分类
        return true;
    }
*/
    public boolean queryListData(){
       List<ItemTime> items = dm.queryItem();
       if(items==null)
           return false;
       listGroupBean = new ArrayList<GroupBean>();

       //避免数据重复叠加
       listChildItem.clear();
       itemTotalTime.clear();
       itemCount.clear();
       itemLimite.clear();
       //for (Person p:persons){
       for (int i = 0;i<items.size();i++) {
           ItemTime item = items.get(i);
           listChildItem.add(item.getItemInfo());
           itemTotalTime.add(item.getTimeTotal());
           itemCount.add(item.getItemCount());
           itemLimite.add(item.getAlarmLimite());
           BleLog.d("itemTotalTime="+itemTotalTime+"---itemCount="+itemCount+"---itemLimite="+itemLimite);
       }
       if(flagClick ==1)
           showItemList(0,4);
       if(flagClick ==2)
           showItemList(0,7);
       if(flagClick ==3)
           showItemList(0,10);
       return true;
   }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_year:
                if (!tvYearTime.isSelected()) {
                    tvYearTime.setSelected(true);
                    tvDayTime.setSelected(false);
                    tvMonthTime.setSelected(false);
                }
                flagClick = 1;
                //0-4显示年份2018
                showItemList(0,4);
                if(listGroupBean!=null){
                    loadAdapter();
                }
                break;
            case R.id.tv_month:
                if (!tvMonthTime.isSelected()) {
                    tvMonthTime.setSelected(true);
                    tvDayTime.setSelected(false);
                    tvYearTime.setSelected(false);
                }
                flagClick = 2;
                //0-7显示月份2018-11
                showItemList(0,7);
                if(listGroupBean!=null){
                    loadAdapter();
                }
                break;
            case R.id.tv_day:
                if (!tvDayTime.isSelected()) {
                    tvDayTime.setSelected(true);
                    tvMonthTime.setSelected(false);
                    tvYearTime.setSelected(false);
                }
                flagClick =3;
                //0-10显示日期2018-11-16
                showItemList(0,10);
                if(listGroupBean!=null){
                    loadAdapter();
                }
                break;
            case R.id.iv_delete_history:
                create_tip_dialog(DELETE_ALL,0,0);
                break;
        }
    }
    /*
    * 此方法显示年月日的item目录及内容
    * */
    private void showItemList(int startIndex,int endIndex){
        ChildBean[] childBean =new ChildBean[listChildItem.size()];
        //GroupBean[] groupBean =new GroupBean[listChildItem.size()];
        List<ChildBean> itemlist = new ArrayList<>();
        listGroupBean = new ArrayList<GroupBean>();

        //List<ChildBean>[] tempList = new List[listChildItem.size()];
        int itemIndex = 0;

        GroupBean groupBean;
        //清除上次保存内容,否则出现数据重叠现象
        listGroupBean.clear();
        itemlist.clear();

        for(int i = 0; i< listChildItem.size(); i++) {
            childBean[i] = new ChildBean(listChildItem.get(i),getTestTime(Long.valueOf(itemTotalTime.get(i))),
                    itemCount.get(i),itemTotalTime.get(i),itemLimite.get(i));
            itemlist.add(childBean[i]);
        }
        for(int i = 0; i< listChildItem.size(); i++) {
            //两个相邻数比较是否时间相同,如下方法最后一组分组不能显示
            if (i + 1 < listChildItem.size()) {
                if (!listChildItem.get(i).substring(startIndex, endIndex).equals(listChildItem.get(i + 1).substring(startIndex, endIndex))) {
                    groupBean = new GroupBean(listChildItem.get(i).substring(startIndex, endIndex), itemlist.subList(itemIndex,i+1));
                    BleLog.d("groupBean===="+groupBean);
                    listGroupBean.add(groupBean);
                    itemIndex = i+1;
                }
            }
        }
        if(listChildItem.size()>0){
            //listGroupBean.size()==0为所有数据目标时间对比相同情况，否则是显示如上未显示的最后一组数据
            if(listGroupBean.size()==0){
                groupBean = new GroupBean(listChildItem.get(listChildItem.size()-1).substring(startIndex,endIndex), itemlist);
                listGroupBean.add(groupBean);
            }else {
                groupBean = new GroupBean(listChildItem.get(listChildItem.size()-1).substring(startIndex, endIndex), itemlist.subList(itemIndex, listChildItem.size()));
                listGroupBean.add(groupBean);
            }
        }
    }
    /*
     * 此方法显示删除后的年月日的item目录及内容
     * */
    private void showItemList(int startIndex,int endIndex,String deleteItem){
        ChildBean[] childBean = new ChildBean[listChildItem.size()];
        List<ChildBean> itemlist = new ArrayList<>();
        listGroupBean = new ArrayList<GroupBean>();
        int itemIndex = 0;
        GroupBean groupBean;
        //清除上次保存内容,否则出现数据重叠现象
        listGroupBean.clear();
        itemlist.clear();
        Log.d("listChildItem", "listChildItem = " + listChildItem.size());
            for(int i = 0; i< listChildItem.size(); i++) {
                if(deleteItem.equals(listChildItem.get(i))){
                    listChildItem.remove(i);
                    itemTotalTime.remove(i);
                    itemCount.remove(i);
                    itemLimite.remove(i);
                }
            }
            for(int i = 0; i< listChildItem.size(); i++) {
                childBean[i] = new ChildBean(listChildItem.get(i),getTestTime(Long.valueOf(itemTotalTime.get(i))),
                        itemCount.get(i),itemTotalTime.get(i),itemLimite.get(i));
                itemlist.add(childBean[i]);
                if(deleteItem.equals(listChildItem.get(i))){
                    listChildItem.remove(i);
                    itemlist.remove(i);
                }
        }
        for(int i = 0; i< listChildItem.size(); i++) {
            //两个相邻数比较是否时间相同,如下方法最后一组分组不能显示
            if (i + 1 < listChildItem.size()) {
                if (!listChildItem.get(i).substring(startIndex, endIndex).equals(listChildItem.get(i + 1).substring(startIndex, endIndex))) {
                    groupBean = new GroupBean(listChildItem.get(i).substring(startIndex, endIndex), itemlist.subList(itemIndex,i+1));
                    listGroupBean.add(groupBean);
                    itemIndex = i+1;
                }
            }
        }
        if(listChildItem.size()>0){
            //listGroupBean.size()==0为所有数据目标时间对比相同情况，否则是显示如上未显示的最后一组数据
            if(listGroupBean.size()==0){
                groupBean = new GroupBean(listChildItem.get(listChildItem.size()-1).substring(startIndex,endIndex), itemlist);
                listGroupBean.add(groupBean);
            }else {
                Log.d("listChildItem", "listChildItem = " + listChildItem.size()+" itemlist = " + itemlist.size());
                groupBean = new GroupBean(listChildItem.get(listChildItem.size()-1).substring(startIndex, endIndex), itemlist.subList(itemIndex, listChildItem.size()));
                listGroupBean.add(groupBean);
            }
        }
    }

    private void loadAdapter(){
        try {
            adapter = new MyAdapter(listGroupBean, getActivity());
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.setAdapter(adapter);
                    mListView.setGroupIndicator(null);
                }
            });
            adapter.setClickInview(new MyAdapter.ClickInview() {
                @Override
                public void click_delete(int groupPosition,int position) {
                    create_tip_dialog(DELETE_ITEM,groupPosition,position);
                }
            });
        }catch (Exception e){
            BleLog.e("loadAdapter---------"+e.toString());
        }
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ArrayList<Float> dataBean = new ArrayList<>();
        String itemTemp = listGroupBean.get(groupPosition).getChildren().get(childPosition).getdate();
        String testTimeTemp = listGroupBean.get(groupPosition).getChildren().get(childPosition).gettime();
        int testValueCount = listGroupBean.get(groupPosition).getChildren().get(childPosition).getCount();
        String itemLimite = listGroupBean.get(groupPosition).getChildren().get(childPosition).getLimiteValue();

        if(!testTimeTemp.equals(""))
            Common.getInstance().setTotalTestTime(getTestTime(Integer.parseInt(testValueCount*1000+"")));
//        Common.getInstance().setTotalTestTime(DateUtil.getTestTime(Integer.parseInt(testTimeTemp)));
        Log.e("childPosition", "itemTemp = " + itemTemp+" testTimeTemp = "
                + getTestTime(Integer.parseInt(testTimeTemp))+" testValueCount = " + testValueCount+" itemLimite = " + itemLimite);

        //List<String> testValueList = listGroupBean.get(groupPosition).getChildren().get(childPosition).getTestValue();

        /*for(int i=0;i<testValueList.size();i++){
            dataBean.add(Float.valueOf(testValueList.get(i)));
        }*/
        showDialog(getResources().getString(R.string.Data_Loading));
        MyTask myTask = new MyTask(itemTemp,testTimeTemp,testValueCount,itemLimite);
        myTask.execute();
       /* int itemid =dm.query(itemTemp);
        List<Person>persons = dm.findFromId(itemid,itemid+testValueCount);
        //ToDo
        for (int i=0;i<testValueCount;i++){
            Log.d("p.getInfo()", "i = " + i);
            Person p =persons.get(i);
            dataBean.add(Float.valueOf(p.getInfo()));
            Log.d("p.getInfo()", "p.getInfo() = " + persons.size()+" itemid = " + itemid+" testValueCount = " + testValueCount);
        }
        EventBus.getDefault().postSticky(new CommonValue(dataBean,itemTemp,testTimeTemp));
        ((HistoryControlFragment) getParentFragment()).triggler(1);//切换到结果页
        Log.d("childPosition", "---------------- ");*/

        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        listGroupBean.remove(groupPosition);
        return false;
    }

    private class MyTask extends AsyncTask<String, Integer, ArrayList<Float>> {
        private String itemTemp ;
        private String testTimeTemp ;
        private String limiteValue ;
        private int testValueCount =0;
        private int flag =0;
        ArrayList<Float> dataBean = new ArrayList<>();
        public MyTask(String itemTemp,String testTimeTemp, int testValueCount,String limiteValue) {
            this.itemTemp = itemTemp;
            this.testValueCount = testValueCount;
            this.testTimeTemp = testTimeTemp;
            this.limiteValue = limiteValue;
        }
        public MyTask(int flag) {
            this.flag = flag;
        }
        // 方法1：onPreExecute（）
        // 作用：执行 线程任务前的操作
        @Override
        protected void onPreExecute() {
            // 执行前显示提示
        }
        // 方法2：doInBackground（）
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        // 此处通过计算从而模拟“加载进度”的情况
        @Override
        protected ArrayList<Float> doInBackground(String... params) {
           /* if(queryListData()){
                return "success";
            }else
            return "fail";*/
           if(flag==1){
               queryListData();
           }else{
                int itemid =dm.query(itemTemp);
                List<Person>persons = dm.findFromId(itemid,itemid+testValueCount);
                BleLog.d("persons.size="+persons.size()+"---testValueCount="+testValueCount);
                //ToDo
                for (int i=0;i<testValueCount;i++){
                    Person p =persons.get(i);
                    dataBean.add(Float.valueOf(p.getInfo()));
                    BleLog.d("p.getInfo()="+p.getInfo()+"---testValueCount="+testValueCount);
                }
               Log.e("===jerry.xiao", "doInBackground for_end  flag="
                       +flag+"   ---testValueCount="+testValueCount);
           }
            return dataBean;
        }
        // 方法3：onProgressUpdate（）
        // 作用：在主线程 显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... progresses) {
            BleLog.e("onProgressUpdate-----progresses="+progresses);
        }
        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(ArrayList<Float> result) {
            // 执行完毕后，则更新UI
               /* if(progressDialog!=null)
                    progressDialog.dismiss();
                isLoadFinish = true;
                loadAdapter();*/
            Log.e("===jerry.xiao", "onPostExecute flag="+flag);
            if(flag!=1){
                ((HistoryControlFragment) getParentFragment()).triggler(1);//切换到结果页
                Log.e("===jerry.xiao", "onPostExecute_switch flag="+flag);
                if(progressDialog!=null){
                    EventBus.getDefault().postSticky(new CommonValue(result,itemTemp,testTimeTemp,limiteValue));
                    progressDialog.dismiss();
                }

            }else {
                if(listGroupBean!=null){
                    if(progressDialog!=null)
                        progressDialog.dismiss();
                    loadAdapter();
                }
            }
        }
    }

    //展示对话框
    private void showDialog(String msg) {
        if ( progressDialog == null ) {
            //创建ProgressDialog对象
            progressDialog = new ProgressDialog(getActivity());
            //设置进度条风格，风格为圆形，旋转的
            progressDialog.setProgressStyle(
                    ProgressDialog.STYLE_SPINNER);
            //设置ProgressDialog 标题图标
            progressDialog.setIcon(android.R.drawable.btn_star);
            //设置ProgressDialog 的进度条是否不明确
            progressDialog.setIndeterminate(false);
            //设置ProgressDialog 是否可以按退回按键取消
            progressDialog.setCancelable(false);
        }
        //设置ProgressDialog 提示信息
        progressDialog.setMessage(getResources().getString(R.string.ing) + msg + "...");
        // 让ProgressDialog显示
        progressDialog.show();
    }

    private void create_tip_dialog(final int delete_flag, final int groupPos, final int pos) {
        final Dialog dialog = new Dialog(getActivity(),R.style.noTitleDialog);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_tip, null);
        TextView content = v.findViewById(R.id.dialog_content);
        Button disconnect_btn_sure = v.findViewById(R.id.tip_btn_sure);
        Button disconnect_btn_cancel = v.findViewById(R.id.tip_btn_cancel);
        //builer.setView(v);//这里如果使用builer.setView(v)，自定义布局只会覆盖title和button之间的那部分
        //final Dialog dialog = builder.create();
        content.setText(getResources().getString(R.string.Para_Data_Deleting));
        dialog.show();
        dialog.getWindow().setContentView(v);//自定义布局应该在这里添加，要在dialog.show()的后面
        //dialog.getWindow().setGravity(Gravity.CENTER);//可以设置显示的位置

        disconnect_btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (delete_flag){
                    case DELETE_ALL:
                        if (deleteHandler != null) {
                            deleteHandler.sendEmptyMessage(DELETE_ALL);
                        }
//                        dm.deletTable();
//                        listGroupBean.clear();
//                        loadAdapter();
                        break;
                    case DELETE_ITEM:
                        TmpgroupPos = groupPos;
                        TmpPos = pos;
                        if (deleteHandler != null) {
                            deleteHandler.sendEmptyMessage(DELETE_ITEM);
                        }
//                        String startTimeItem =listGroupBean.get(groupPos).getChildren().get(pos).getdate();
//                        listGroupBean.get(groupPos).getChildren().remove(pos);
//                        dm.deleteItem(startTimeItem);//删除数据库中对应item
//                        EventBus.getDefault().postSticky(new UiMessage(UiEvent.MSG_DATA_DELETE,startTimeItem));
//                        if(flagClick ==1)
//                            showItemList(0,4,startTimeItem);
//                        if(flagClick ==2)
//                            showItemList(0,7,startTimeItem);
//                        if(flagClick ==3)
//                            showItemList(0,10,startTimeItem);
//
//                        if(listGroupBean!=null){
//                            loadAdapter();
//                        }
                        break;
                }
                dialog.dismiss();
            }
        });
        disconnect_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        if (deleteHandler != null) {
            deleteHandler.removeMessages(0);
            deleteHandler = null;
        }
        if (deleteThread != null) {
            deleteThread.quit();
            deleteThread = null;
        }
    }

    public String getTestTime(long time) {
        //long time = getTimeSpace(startTime,endTime);
        long hour = time/(1000*60*60);
        long minute = (time%(1000*60*60))/(1000*60);
        long second = (time%(1000*60*60)%(1000*60))/1000;
        String showFormat = getString(R.string.Test_MyTime_Total)+" "+hour+" "+getString(R.string.Test_MyTime_Hour)+" "+minute
                +" "+getString(R.string.Test_MyTime_Minute)+" "+second+" "+getString(R.string.Test_MyTime_Second);
        return  showFormat;
    }
}
