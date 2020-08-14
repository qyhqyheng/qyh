package com.justec.pillowalcohol.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.Log;
import android.view.View;

import com.justec.blemanager.utils.BleLog;
import com.justec.pillowalcohol.R;
import com.justec.pillowalcohol.event.Common;

import java.util.ArrayList;
import java.util.LinkedList;

public class LineChart extends View {
    public float XPoint = 60; // 原点的X坐标
    public float YPoint ;// 原点的Y坐标260
    public float XScale ; // X的刻度长度55
    public float YScale ; // Y的刻度长度40
    public float XLength ; // X轴的长度380
    public float YLength ; // Y轴的长度240
    public float XPerNumLen ; // X的刻度每一个数据点的长度

    public float YtoButtonSpace = 10; // X的刻度每一个数据点的长度


    public float XrealLength;//x轴有效长度
    //  private int scaleLength = 10;//刻度线的长度 TTTTTT
    private float top = 10;//上边缘距离
    private float left = 30;//左边缘距离
    private float right = 10;//右边缘距离
    private float bottom = 40;//下边缘距离

    private float unitTextHight = 50;

    private String[] YLabel;//y轴的刻度值
    private ArrayList XLabel;//X轴的刻度值
    public ArrayList<Float> DataStr; // 数据
    private long pointNumLimit;
    private int moveNum;
    //public String[] DataStr1; // 数据
    public Boolean isInitCanvas ;
    private static final int PER_DIVIDE_NUMBER = 5;
    private String limiteValue ="2";

    public static final int COLOR_LINE   = 0xFFDDDDDD;
    public static final int COLOR_TEST   = 0x000000;
    //  private Bitmap mBackGround;
    public LineChart(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//      mBackGround  = ((BitmapDrawable) this.getResources().getDrawable(R.drawable.viewbackground)).getBitmap(); //获取背景图片
    }
    //public void setDate(String[] YLabel,String[] XLabel,String[] DataStr,String[] DataStr1) {//如果只需要一条折线，最后这个参数给null就行了
    public void setDate(String[] YLabel, ArrayList XLabel, ArrayList DataStr,long pointNumLimit ,int moveNum) {//如果只需要一条折线，最后这个参数给null就行了

        this.YLabel = YLabel;
        this.XLabel = XLabel;
        this.DataStr = DataStr;
        this.pointNumLimit = pointNumLimit;
        this.moveNum = moveNum;
        invalidate();
    }
    public void setLimite(String limiteValue){
        this.limiteValue = limiteValue;
    }
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
/*
        Log.i("Main", "Width = " + getWidth());//1280
        Log.i("Main", "Height = " + getHeight());//670  测量整个view的高度不包括状态栏*/
/*
        Log.i("Main", "Width = " + getMeasuredWidth());//一个是测量整个view的高度  一个是测量view里内容的高度
        Log.i("Main", "Height = " + getMeasuredHeight());*/

        YLength = getHeight()-bottom-top-YtoButtonSpace;//整个Y轴的长度
        XLength = getWidth()-right-left;

        YPoint = getHeight()-bottom;
        XScale = (XLength/XLabel.size());//--x轴的刻度平均长度
        //XScale = (XLength/7);//--x轴的刻度平均长度
        YScale = ((YLength-unitTextHight)/10);//Y--Y轴的刻度平均长度 减去写单位长度的文字高度
        XPerNumLen =XScale/pointNumLimit*(XLabel.size()-1);   //每个数据的长度
        //Log.i("Main", "XScale = " + XScale);//1280

        BleLog.e("onDraw------XLength = "+XLength+"    YLength = "+YLength+"    XScale = "+XScale
        +"    YScale="+YScale+"    XPerNumLen = "+ XPerNumLen+"    XLabel = "+XLabel+"   YLabel = "+YLabel);

        Paint Ypaint = new Paint();
        Ypaint.setStrokeWidth(3);
        Ypaint.setColor(Color.BLACK);
        Ypaint.setTextSize(26);
        Ypaint.setAntiAlias(true);
        Ypaint.setTextAlign(Paint.Align.CENTER);

        Paint Xpaint = new Paint();
        Xpaint.setStrokeWidth(3);
        Xpaint.setColor(Color.BLACK);
        Xpaint.setTextSize(30);
        Xpaint.setAntiAlias(true);
        Xpaint.setTextAlign(Paint.Align.CENTER);

        Paint paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        PathEffect effects = new DashPathEffect(new float[]{30,10,20,10},10);//设置虚线的间隔和点的长度
//      canvas.drawBitmap(mBackGround, 0, 0, paint); //画背景图片
        //画Y轴
        canvas.drawLine(XPoint,YPoint, XPoint,top, Ypaint);
        canvas.drawText("(mg/100ml)", XPoint+15,unitTextHight/2, Ypaint);
        canvas.drawText(getResources().getString(R.string.Alcohol_Concentration), 2*XLength/5,2*unitTextHight/3, paint);
        canvas.drawText(Common.getInstance().getTotalTestTime(), 4*XLength/5,2*unitTextHight/3, paint);
        canvas.drawText("0", XPoint - 30,YPoint-5, Ypaint);
        Ypaint.setColor(Color.RED);//警戒线
        Ypaint.setPathEffect(effects);
        //警戒线横线画图 警戒值2
        canvas.drawLine(XPoint, YPoint -  YScale*10/Integer.valueOf(YLabel[YLabel.length-1])*Integer.valueOf(limiteValue)-YtoButtonSpace, XLength,
                YPoint -  YScale*10/Integer.valueOf(YLabel[YLabel.length-1])*Integer.valueOf(limiteValue)-YtoButtonSpace, Ypaint);
        for (int i = 1; i * YScale <= YLength-unitTextHight; i++) {   //画横刻度
            Ypaint.setColor(COLOR_LINE);
            if(YLabel[i-1].equals(limiteValue)){
                Ypaint.setColor(Color.RED);//警戒线
            }
            canvas.drawLine(XPoint, YPoint - i * YScale-YtoButtonSpace, XLength, YPoint - i* YScale-YtoButtonSpace, Ypaint);
            Ypaint.setColor(Color.BLACK);
            canvas.drawText(YLabel[i-1], XPoint - 30,YPoint - i * YScale -YtoButtonSpace+7, Ypaint);
        }

        //画X轴
        canvas.drawLine(XPoint, YPoint, XLength, YPoint, Xpaint);

        //for (int i = 1; i* XScale  <= (XLength-XPoint); i++) {//画竖刻度
        for (int i = 1; i* XScale  <= (XLength-XPoint); i++) {//画竖刻度
            Xpaint.setColor(COLOR_LINE);
            Xpaint.setPathEffect(effects);
            if(moveNum>0){
                canvas.drawLine(XPoint + i * XScale-moveNum*XPerNumLen, YPoint,
                        XPoint + i * XScale-moveNum*XPerNumLen,unitTextHight+top, Xpaint);
                if(i<=XLabel.size()){
                    Xpaint.setColor(Color.BLACK);
                    canvas.drawText(String.valueOf(XLabel.get(i-1)), XPoint + i* XScale - 3-moveNum*XPerNumLen,YPoint+30, Xpaint);
                }
            }else{
                canvas.drawLine(XPoint + i * XScale, YPoint, XPoint + i * XScale,unitTextHight+top, Xpaint);
                Xpaint.setColor(Color.BLACK);
                //x,y轴起点时间值
                canvas.drawText(String.valueOf(XLabel.get(0)), XPoint - 3,YPoint+30, Xpaint);
                if(i<=XLabel.size()){
                    canvas.drawText(String.valueOf(XLabel.get(i)), XPoint + (i) * XScale - 3,YPoint+30, Xpaint);
                }
            }
        }
        //画数据图
        Xpaint.setPathEffect(null);
        Xpaint.setColor(Color.BLUE);
        Xpaint.setStrokeWidth(1);
        for (int i = 0; i<DataStr.size(); i++) {
            //canvas.drawCircle(XPoint+(i)*XPerNumLen,calcuLations(DataStr.get(i)),3, Xpaint);
            if (i+1<DataStr.size()){
                canvas.drawLine(XPoint+(i)*XPerNumLen,calcuLations(DataStr.get(i))
                        ,XPoint+(i+1)*XPerNumLen,calcuLations(DataStr.get(i + 1)), Xpaint);
            }
        }
    }

  private float calcuLations(float y) //计算y轴坐标
  {
      float Yvalue = 0;
      //return (int)(YPoint-YPoint*(y/Double.parseDouble(YLabel[YLabel.length-1])));
      float Ylen = YLength-unitTextHight;
      Yvalue = (YPoint-y*(Ylen/Float.parseFloat(YLabel[YLabel.length-1]))-YtoButtonSpace);
      return Yvalue;
  }
}
