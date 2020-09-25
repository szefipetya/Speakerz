package com.speakerz.view.recyclerview.songadd.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;

import java.util.ArrayList;

public class AdapterDevice extends RecyclerView.Adapter<AdapterDevice.ViewHolderDevice> {
    Context contextDevice;
    ArrayList<deviceItem> listDevice;

    public AdapterDevice(Context mContext, ArrayList<deviceItem> mList){
        contextDevice = mContext;
        listDevice = mList;
    }

    @NonNull
    @Override
    public ViewHolderDevice onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contextDevice).inflate(R.layout.item_devices, parent, false);
        return new ViewHolderDevice(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDevice holder, int position) {
        deviceItem currentItem = listDevice.get(position);

        String deviceName = currentItem.getDeviceName();
        String deviceNickName = currentItem.getDeviceNickName();

        holder.deviceNameTextView.setText(deviceName);
        holder.deviceNickNameTextView.setText(deviceNickName);

    }

    @Override
    public int getItemCount() {
        return listDevice.size();
    }

    public class ViewHolderDevice extends RecyclerView.ViewHolder{

        public TextView deviceNameTextView;
        public TextView deviceNickNameTextView;


        public ViewHolderDevice(@NonNull View itemView) {
            super(itemView);
            deviceNickNameTextView = itemView.findViewById(R.id.songNameTextView);
        }
    }
}
