package com.speakerz.model;

import android.net.ConnectivityManager;

import com.speakerz.debug.D;
import com.speakerz.model.network.*;
import com.speakerz.model.network.WifiBroadcastReciever;

public class HostModel extends BaseModel {
    HostNetwork network;

    public HostModel(WifiBroadcastReciever reciever, ConnectivityManager connectivityManager) {
        super(reciever);
        network = new HostNetwork(reciever);
        network.getReciever().setConnectivityManager(connectivityManager);
    }

    @Override
    public void start() {
        network.start();
        network.startAdvertising();
        //D.log("Model started");
    }

    @Override
    public void stop() {
        //D.log("Model stopped");

    }

    @Override
    public HostNetwork getNetwork() {
        return network;
    }
}
