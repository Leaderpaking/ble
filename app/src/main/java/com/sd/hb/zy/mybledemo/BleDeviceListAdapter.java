package com.sd.hb.zy.mybledemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zhangyu on 2018-05-31.
 * Email rainzhangm@gmail.com
 */

public class BleDeviceListAdapter extends BaseAdapter {

    private final MainActivity activity;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private Context context;

    public BleDeviceListAdapter(ArrayList<BluetoothDevice> deviceArrayList, Context context) {
        this.deviceArrayList = deviceArrayList;
        this.context = context;
        activity = (MainActivity) context;
    }

    public void clear(){
        if (deviceArrayList!=null) {
            deviceArrayList.clear();
            notifyDataSetChanged();
        }
    }
    @Override
    public int getCount() {
        return deviceArrayList.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return deviceArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final BluetoothDevice bluetoothDevice = deviceArrayList.get(i);
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_de, null);
            holder.tvName = view.findViewById(R.id.tv_name);
            holder.tvMac = view.findViewById(R.id.tv_mac);
            holder.tvState = view.findViewById(R.id.tv_state);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvName.setText(bluetoothDevice.getName());
        holder.tvMac.setText(bluetoothDevice.getAddress() + "");
        if (bluetoothDevice == activity.getmBluetoothDevice() && activity.getCharacteristic() != null) {
            holder.tvState.setText("已连接");
        } else {
            holder.tvState.setText("连接");
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothDevice == activity.getmBluetoothDevice() && activity.getCharacteristic() != null) {
                    activity.bleDisConnectDevice(bluetoothDevice);
                } else {
                    activity.connectBle(bluetoothDevice);
                }
            }
        });
        return view;
    }

    class ViewHolder {
        TextView tvName, tvMac, tvState;

    }
}
