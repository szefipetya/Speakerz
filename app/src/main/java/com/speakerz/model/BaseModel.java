package com.speakerz.model;

import android.content.Context;

import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.util.ThreadSafeEvent;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    MusicPlayerModel musicPlayerModel;
    public Event<PermissionCheckEventArgs> PermissionCheckEvent;

    public volatile Event<EventArgs> SongQueueUpdatedEvent=new Event<>();
    public volatile ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent=new ThreadSafeEvent<>();
    public volatile Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public Event<EventArgs1<String>> SongDownloadedEvent=new Event<>();



    public BaseModel(Context context, WifiBroadcastReciever reciever,Boolean isHost, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        this.PermissionCheckEvent=PermissionCheckEvent;
        musicPlayerModel = new MusicPlayerModel(context,this.PermissionCheckEvent);
        musicPlayerModel.setHost(isHost);


        //inject Events to MusicPLayerModel

        musicPlayerModel.SongDownloadedEvent=SongDownloadedEvent;
        musicPlayerModel.MusicPlayerActionEvent=this.MusicPlayerActionEvent;

        musicPlayerModel.subscribeEventsFromModel();


        reciever.WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
            }
        });

    }


    public abstract BaseNetwork getNetwork();

    public MusicPlayerModel getMusicPlayerModel(){return musicPlayerModel;}

    private Boolean AreUiEventsSubscribed=false;

    public Boolean getAreUiEventsSubscribed() {
        return AreUiEventsSubscribed;
    }

    public void setAreUiEventsSubscribed(Boolean areUiEventsSubscribed) {
        AreUiEventsSubscribed = areUiEventsSubscribed;
    }
    protected abstract void injectNetworkDependencies();


}
