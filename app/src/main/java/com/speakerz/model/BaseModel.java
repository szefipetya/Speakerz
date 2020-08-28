package com.speakerz.model;

import android.content.Context;

import com.speakerz.model.enums.MP_EVT;
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
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;
import com.speakerz.util.ThreadSafeEvent;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    MusicPlayerModel musicPlayerModel;
    private Context context;

    public final Event<EventArgs> SongQueueUpdatedEvent=new Event<>();
    public final ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent=new ThreadSafeEvent<>();
    public final Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public final Event<EventArgs1<String>> SongDownloadedEvent=new Event<>();
    public final Event<PermissionCheckEventArgs> PermissionCheckEvent;
    public final Event<EventArgs3<MP_EVT,Object,Body>> ModelCommunicationEvent=new Event<>();


    public BaseModel(Context context, WifiBroadcastReciever reciever, Boolean isHost, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        this.context = context;
        this.PermissionCheckEvent=PermissionCheckEvent;
        musicPlayerModel = new MusicPlayerModel(this, isHost);


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

    public Context getContext() { return context; }

}
