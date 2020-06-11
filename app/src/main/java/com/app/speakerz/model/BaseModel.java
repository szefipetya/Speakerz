package com.app.speakerz.model;

import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.event.EventHandler;
import com.app.speakerz.model.event.UpdateEventManager;
import com.app.speakerz.model.event.ViewEventHandler;
import com.app.speakerz.model.event.ViewUpdateEventManager;
import com.app.speakerz.model.network.BaseNetwork;
import com.example.speakerz.R;


public abstract class BaseModel implements EventHandler {


    BaseNetwork network=null;
    WifiManager wifiManager;
    ViewUpdateEventManager viewUpdateEventManager;
    EventHandler eventHandlerFromNetwork ;
    public abstract void start();
    public abstract void init();
    public BaseModel(){
        initSelf();

        }
    @Override
    public void onInvoke(Object o) {
        D.log("from basemodel: "+ o.toString());
       // viewUpdateEventManager.invokeAll(o);
    }
    private void initSelf(){
        viewUpdateEventManager =new ViewUpdateEventManager();

    }
    public void addUpdateEventListener(ViewEventHandler event){
        viewUpdateEventManager.addListener(event);
    }

   public void setWifiManager(WifiManager manager){
        wifiManager=manager;
        initWifiManager();
    }

    private void initWifiManager() {
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            viewUpdateEventManager.toast("Turning on your Wifi...");
            viewUpdateEventManager.setText(R.id.wifi_status,"Wifi is on");
        }
    }


}
