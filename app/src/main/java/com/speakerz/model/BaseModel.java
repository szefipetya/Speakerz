package com.speakerz.model;

import com.speakerz.model.event.SongItemEventArgs;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    public Event<SongItemEventArgs> SongListChangedEvent=new Event<>();


    public BaseModel(WifiBroadcastReciever reciever){
        reciever.WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
            }
        });
    }

    public abstract BaseNetwork getNetwork();

    public List<String> getSongList() {
        return songList;
    }

    protected List<String> songList=new ArrayList<>();




}
