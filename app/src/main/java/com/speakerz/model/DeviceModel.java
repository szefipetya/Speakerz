package com.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ListView;

import android.app.Activity;

import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;

public class DeviceModel extends BaseModel {
    DeviceNetwork network;


    @Override
    public void start() {
        network.start();
    }

    public DeviceModel(WifiBroadcastReciever reciever){
        super(reciever);
        network=new DeviceNetwork(reciever);
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
    public BaseNetwork getNetwork() {
        return network;
    }


    public void discoverPeers(Activity activity, ListView lvPeersList) {
        network.discoverPeers(activity,lvPeersList);
    }

    public String[] getDeviceNames() {
        return network.getDeviceNames();
    }
}
