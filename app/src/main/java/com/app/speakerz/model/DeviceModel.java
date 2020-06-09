package com.app.speakerz.model;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.model.network.BaseNetwork;
import com.app.speakerz.model.network.DeviceNetwork;
import com.app.speakerz.model.network.HostNetwork;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;

public class DeviceModel extends BaseModel {
    @Override
    public void init(){
        STRATEGY=Strategy.P2P_STAR;
        network.setStrategy(STRATEGY);
        //lehet hogy P2P_POINT_TO_POINT
        network.init();
    }

    @Override
    public void start() {
        network.start();
    }

    public DeviceModel(){
        init();
    }
    public DeviceModel(AppCompatActivity a){
        network=new DeviceNetwork(a);

    }



}
