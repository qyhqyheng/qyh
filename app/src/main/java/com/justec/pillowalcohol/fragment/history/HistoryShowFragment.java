package com.justec.pillowalcohol.fragment.history;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.justec.blemanager.messageEvent.CommonValue;
import com.justec.blemanager.utils.BleLog;
import com.justec.blemanager.utils.DateUtil;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.fragment.BaseMainFragment;
import com.justec.pillowalcohol.helper.LineChart;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.OnClick;

public class HistoryShowFragment extends BaseMainFragment{
    @BindView(R.id.show_history_data)
    RelativeLayout showHistoryData;
    @BindView(R.id.iv_history_back)
    ImageView iv_back;
    @BindView(R.id.tv_history_back)
    TextView tv_back;

    private static final int XLABEL_NUMBER= 5;//X轴分为6大刻度
    private static int XLABEL_SPACE_TIME = 0;
    String[] YLabel = new String[10];
    ArrayList XLabel = new ArrayList();
    LineChart view;
    RelativeLayout layout;
    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_history_show;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static HistoryShowFragment newInstance() {
        Bundle args = new Bundle();
        HistoryShowFragment fragment = new HistoryShowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /*
    * 画历史记录每条数据的折线图
    * */
    private void showLineChart(ArrayList<Float> date, String itemStartTime,String timeSum,String itemLimite) {
        Log.d("childPosition", "======---====== ");
        layout = getActivity().findViewById(R.id.show_history_data);
        view = new LineChart(getActivity());
        //String[] DataStr = new String[]{"123","110","136","145","123","110","136","145"};  //数据
        XLabel.clear();
        XLABEL_SPACE_TIME = (Integer.valueOf(timeSum)/XLABEL_NUMBER);
        setXlabeldata(itemStartTime);
        calculateYLabel(date);//计算Y轴刻度最大高度
        view.setLimite(itemLimite);
        view.setDate(YLabel,XLabel,date,date.size(),0);
//      view.setBackgroundDrawable(getResources().getDrawable(R.drawable.viewbackground));//给整个View加背景
        layout.addView(view);
        Log.d("childPosition", "=========++======== ");
    }
    private void setXlabeldata(String startTime){
        String itemStartTime = startTime.substring(startTime.length()-9,startTime.length()-3);//取后8位 hh:mm:ss
        XLabel.add(itemStartTime);
        for(int i=1;i<=XLABEL_NUMBER;i++){
            XLabel.add(DateUtil.getHistoryXlabel(XLABEL_SPACE_TIME*i,startTime));
        }
    }
    @Subscribe(sticky = true)
    public void getHistoryValue(CommonValue commonValue) {
        String timeSumValue = commonValue.getStrValue();//测试总时间ms单位
        String itemStartTimeTemp = commonValue.getStartTime();//开始时间
        //String itemStartTime = itemStartTimeTemp.substring(itemStartTimeTemp.length()-9,itemStartTimeTemp.length()-1);//取后八位 hh:mm:ss
        ArrayList<Float> dataBean = commonValue.getListData();//测试数据
        String itemLimite = commonValue.getLimiteValue();//测试限值
        BleLog.d("dataBean-=-=-= = "+dataBean+" itemStartTimeTemp = "+itemStartTimeTemp);
        if(layout!=null)
        layout.removeAllViews();
        showLineChart(dataBean,itemStartTimeTemp,timeSumValue,itemLimite);
    }


    @Override
    public boolean onBackPressedSupport() {
        ((HistoryControlFragment) getParentFragment()).triggler(0);//切换到结果页
        return true;
    }
    /*
     * 根据数据计算Y轴的高度实时更新
     * */
    private void calculateYLabel(ArrayList<Float> dataStr){
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
    @OnClick({R.id.iv_history_back,R.id.tv_history_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_history_back:
                ((HistoryControlFragment) getParentFragment()).triggler(0);//切换到setting页
                break;
            case R.id.tv_history_back:
                ((HistoryControlFragment) getParentFragment()).triggler(0);//切换到setting页
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private class MyTask extends AsyncTask<String, Integer, String> {

        private int groupPosition = 0;
        private int childPosition = 0;
       /* public MyTask(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }*/

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
        protected String doInBackground(String... params) {

           return "";
        }
        // 方法3：onProgressUpdate（）
        // 作用：在主线程 显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }
        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(String result) {
            // 执行完毕后，则更新UI
            if(result.equals("success")){

            }
        }
    }

}
