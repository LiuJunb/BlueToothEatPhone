package com.xmg.bluetoothearphone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xmg.bluetoothearphone.bean.BlueDevice;
import com.xmg.bluetoothearphone.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/8/11 0011
 */
public class BlueAdapter extends BaseAdapter {

    private  Context context;
    private List<BlueDevice> listDevices=new ArrayList<>();

    public BlueAdapter(Context context, Set<BlueDevice> setDevices) {
        this.context=context;
        if(setDevices!=null)
        for (BlueDevice blueDevice: setDevices){
            listDevices.add(blueDevice);
        }
    }

    public Set<BlueDevice> getSetDevices() {
        Set<BlueDevice> setDevices=new HashSet<>();
        if(listDevices!=null)
        for (BlueDevice blueDevice: listDevices){
            setDevices.add(blueDevice);
        }
        return setDevices;
    }

    public List<BlueDevice> getListDevices() {
        return listDevices;
    }

    public void setSetDevices(Set<BlueDevice> setDevices) {
        listDevices.clear();
        for (BlueDevice blueDevice: setDevices){
            listDevices.add(blueDevice);
        }
    }

    @Override
    public int getCount() {
        return listDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder=null;
        if (convertView == null) {
            viewHolder=new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_main, viewGroup, false);
            viewHolder.blueName= (TextView) convertView.findViewById(R.id.tv_blueName);
            viewHolder.blueState= (TextView) convertView.findViewById(R.id.tv_status);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BlueDevice blueDevice = listDevices.get(i);
        if(blueDevice!=null) {
            viewHolder.blueName.setText("蓝牙名："+blueDevice.getName());
            viewHolder.blueState.setText(blueDevice.getStatus());
        }
        return convertView;
    }


    static  class  ViewHolder{
        TextView blueName;
        TextView blueState;
    }
}
