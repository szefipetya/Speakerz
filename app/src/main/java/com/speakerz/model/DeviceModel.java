package com.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ListView;

import android.app.Activity;

import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;

public class DeviceModel extends BaseModel {
    DeviceNetwork network;

@Override
    public void init(){
        network=new DeviceNetwork();
        network.init();
        network.addUpdateEventListener(this);

    }

    @Override
    public void start() {

        network.start();
    }

    public DeviceModel(){
        super();
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

    public void discoverPeers(Activity activity, ListView lvPeersList) {
        network.discoverPeers(activity,lvPeersList);
    }

    public String[] getDeviceNames() {
        return network.getDeviceNames();
    }
}
