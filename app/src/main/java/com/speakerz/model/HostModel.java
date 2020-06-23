package com.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.model.network.*;

public class HostModel extends BaseModel {
    HostNetwork network;

    public HostModel(WifiBroadcastReciever reciever) {
        super(reciever);
        network = new HostNetwork(reciever);
    }

    @Override
    public void setWifiManager(WifiManager manager) {
        network.setWifiManager(manager);
        network.initWifiManager();
    }


    @Override
    public void setIntentFilter(IntentFilter intentFilter) {
        network.setIntentFilter(intentFilter);
    }

    @Override
    public void start() {
        network.start();
    }

    @Override
    public BaseNetwork getNetwork() {
        return network;
    }

    public void startAdvertising() {
        network.startAdvertising();
    }
}
