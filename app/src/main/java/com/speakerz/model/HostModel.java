package com.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.network.*;

public class HostModel extends BaseModel {
    HostNetwork network;

    public HostModel(WifiBroadcastReciever reciever) {
        super(reciever);
        network = new HostNetwork(reciever);
    }

    @Override
    public void start() {
        network.start();
        network.startAdvertising();
        D.log("Model started");
    }

    @Override
    public void stop() {
        D.log("Model stopped");

    }

    @Override
    public BaseNetwork getNetwork() {
        return network;
    }
}
