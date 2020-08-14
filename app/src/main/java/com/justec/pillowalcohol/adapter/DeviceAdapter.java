package com.justec.pillowalcohol.adapter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.justec.blemanager.blemanager.BleDevice;
import com.justec.common.recycler.RecyclerAdapter;
import com.justec.pillowalcohol.R;

import java.util.ArrayList;
import java.util.List;


/**
 * @author abvatous
 * @name BleOperator
 * @class name：app.justec.com.bleoperator.adapter
 * @class describe
 * @time 2018/5/29 0029 下午 4:02
 * @change
 * @chang time
 * @class describe 设备列表adapter
 */
public class DeviceAdapter extends RecyclerAdapter<BleDevice> {

    private Context context;
    private List<BleDevice> bleDeviceList;
    private OnDeviceClickListener mListener;

    public DeviceAdapter() {
        bleDeviceList = new ArrayList<>();
    }

    @Override
    protected int getItemViewType(int position, BleDevice image) {
        return R.layout.adapter_device;
    }

    //每次添加蓝牙设备，都要移除相同的旧的蓝牙设备
    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public List<BleDevice> addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
        return bleDeviceList;
    }

    @Override
    protected ViewHolder<BleDevice> onCreateViewHolder(View root, int viewType) {
        return new myViewHolder(root);
    }

    /**
     * Cell 对应的Holder
     */
    private class myViewHolder extends ViewHolder<BleDevice> {
        private TextView txtName;
        private TextView txtMac;
        private TextView txtRssi;
        private LinearLayout layoutIdle;

        public myViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            txtMac = (TextView) itemView.findViewById(R.id.txt_mac);
            //txtRssi = (TextView) itemView.findViewById(R.id.txt_rssi);
            layoutIdle = (LinearLayout) itemView.findViewById(R.id.layout_idle);
        }

        @Override
        protected void onBind(final BleDevice bleDevice) {
            //Jerry.Xiao0903
            String name = bleDevice.getName();
            if(name == null){
                name = "Unknow";
            }
            txtName.setText(name);
            txtMac.setText(bleDevice.getMac());
            //txtRssi.setText(String.valueOf(bleDevice.getRssi()));

            layoutIdle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onConnect(bleDevice);
                    }
                }
            });
        }
    }

    //设置对外的监听器
    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }

}
