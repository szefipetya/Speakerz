package com.speakerz.model;

import android.content.Context;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.TYPE;
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

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;

import ealvatag.audio.exceptions.CannotReadException;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    public String NickName = "placeholder";
    public String deviceID= "";
    public HashMap<String,String> NickNames;

    MusicPlayerModel musicPlayerModel;
    protected final Context context;
    public final Event<EventArgs1<Exception>> ExceptionEvent=new Event<>();
    public final Event<EventArgs> SongQueueUpdatedEvent=new Event<>();
    public final ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent=new ThreadSafeEvent<>();
    public final Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public final Event<EventArgs1<String>> SongDownloadedEvent=new Event<>();
    public final Event<EventArgs1<Body>> NameListInitEvent =new Event<>();;
    public final Event<PermissionCheckEventArgs> PermissionCheckEvent;
    public final Event<EventArgs3<MP_EVT,Object,Body>> ModelCommunicationEvent=new Event<>();
    public final Event<EventArgs2<Body, TYPE>> NameChangeEvent=new Event<>();
    public final Event<EventArgs1<Body>> INITDeviceAddressEvent = new Event<>();



    public BaseModel(final Context context, WifiBroadcastReciever reciever, Boolean isHost, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        this.context = context;
        this.PermissionCheckEvent=PermissionCheckEvent;
        musicPlayerModel = new MusicPlayerModel(this, isHost);
        
        NickName = "placeholder";
        deviceID = "test";
        NickNames= new HashMap<>();


        subscribeToNameChange();


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
    public void subscribeToNameChange(){




    }


}
