package com.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.EventHandler;
import com.speakerz.model.event.Model_ViewEventHandler;
import com.speakerz.model.event.Model_ViewUpdateEventManager;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventListener;
import com.speakerz.viewModel.TextValueStorage;
import com.speakerz.R;


public abstract class BaseModel {
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private Model_ViewUpdateEventManager viewUpdateEventManager;

    public abstract void start();
    public abstract void stop();

    public BaseModel(WifiBroadcastReciever reciever){
        reciever.WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
            }
        });
    }

    abstract public  void setWifiManager(WifiManager manager);

    public void setTextValueStorageForViewUpdateEventManager(TextValueStorage storage){
        //viewUpdateEventManager.setValueStorage(storage);
    }

    public abstract void setIntentFilter(IntentFilter intentFilter);

    public abstract BaseNetwork getNetwork();
}
