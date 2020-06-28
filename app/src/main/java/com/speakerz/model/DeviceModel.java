package com.speakerz.model;

import android.net.ConnectivityManager;

import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;

public class DeviceModel extends BaseModel {
    DeviceNetwork network;


    @Override
    public void start() {
        network.start();
    }

    @Override
    public void stop() {

    }

    public DeviceModel(WifiBroadcastReciever reciever, ConnectivityManager connectivityManager){
        super(reciever);
        network=new DeviceNetwork(reciever);
        network.getReciever().setConnectivityManager(connectivityManager);
    }

    @Override
    public BaseNetwork getNetwork() {
        return network;
    }


    public void discoverPeers() {
        network.discoverPeers();
    }

}
