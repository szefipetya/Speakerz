package com.app.speakerz.model;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.enums.EVT;
import com.app.speakerz.model.event.EventHandler;
import com.app.speakerz.model.event.Model_ViewEventHandler;
import com.app.speakerz.model.event.Model_ViewUpdateEventManager;
import com.app.speakerz.model.network.BaseNetwork;
import com.app.speakerz.model.network.WifiBroadcastReciever;
import com.app.speakerz.viewModel.TextValueStorage;
import com.example.speakerz.R;


public abstract class BaseModel implements EventHandler {


    private BaseNetwork network;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private Model_ViewUpdateEventManager viewUpdateEventManager;

    public abstract void start();
    public abstract void init();
    public BaseModel(){
        initSelf();

        }

    /** onUpdate with 1 Object parameters*/
    @Override
    public void onUpdate(EVT evt, Object o) {

    }
    /** onUpdate with 2 Object parameters*/
    @Override
    public void onUpdate(EVT evt, Object o, Object o2) {

        if(evt==EVT.updateText ){
            viewUpdateEventManager.setText((Integer)o,(String)o2);
        }
    }
    /** onUpdate with 3 Object parameters*/
    @Override
    public void onUpdate(EVT evt, Object o,Object o2,Object o3) {

    }
    /** onUpdate with 4 Object parameters*/
    @Override
    public void onUpdate(EVT evt, Object o,Object o2,Object o3,Object o4) {

    }
    private void initSelf(){
        viewUpdateEventManager =new Model_ViewUpdateEventManager();

    }
    public void addUpdateEventListener(Model_ViewEventHandler event){
        viewUpdateEventManager.addListener(event);
    }

    abstract public  void setWifiManager(WifiManager manager);
    abstract public void setWifiP2pManager(WifiP2pManager manager);


    public void setTextValueStorageForViewUpdateEventManager(TextValueStorage storage){
        viewUpdateEventManager.setValueStorage(storage);
    }

   abstract public void setWifiP2pChannel(WifiP2pManager.Channel wifiP2pChannel) ;

    public abstract void setIntentFilter(IntentFilter intentFilter);



    public abstract void setWifiBroadcastReciever(WifiBroadcastReciever reciever);
}
