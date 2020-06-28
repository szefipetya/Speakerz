package com.speakerz.model;

import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventListener;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    public BaseModel(WifiBroadcastReciever reciever){
        reciever.WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
            }
        });
    }


    public abstract BaseNetwork getNetwork();
}
