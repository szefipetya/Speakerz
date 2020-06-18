package com.app.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.app.speakerz.model.network.*;

public class HostModel extends BaseModel {
    @Override
    public void init(){
        network=new HostNetwork();
        network.init(this);
        network.addUpdateEventListener(this);
    }
    @Override
    public void setWifiManager(WifiManager manager) {
        network.setWifiManager(manager);
        network.initWifiManager();
    }

    @Override
    public void setWifiP2pManager(WifiP2pManager manager) {
        network.setWifiP2pManager(manager);
        network.initP2pWifiManager();
    }

    @Override
    public void setWifiP2pChannel(WifiP2pManager.Channel wifiP2pChannel) {
        network.setWifiP2pChannel(wifiP2pChannel);
    }

    @Override
    public void setIntentFilter(IntentFilter intentFilter) {
        network.setIntentFilter(intentFilter);
    }

    @Override
    public void setWifiBroadcastReciever(WifiBroadcastReciever reciever) {
        network.setWifiBroadcastReciever(reciever);
    }
    @Override
    public void start() {
    network.start();
    }

    HostNetwork network;
    public HostModel(){
        super();
    }


    public void startAdvertising() {
        network.startAdvertising();
    }
}
