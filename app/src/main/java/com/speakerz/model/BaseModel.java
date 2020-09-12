package com.speakerz.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;

import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;
import com.speakerz.util.ThreadSafeEvent;


import java.net.InetAddress;
import java.util.HashMap;


import ealvatag.audio.exceptions.CannotReadException;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    public SharedPreferences sharedpreferences;
    public SharedPreferences.Editor editor;
    public static final String mypreference = "mypref";
    public static final String myName= "Name";

    public Boolean isAppRunning=true;

    public String NickName;
    public String deviceID= "";
    public InetAddress deviceAddress;
    public HashMap<String,String> NickNames;

    MusicPlayerModel musicPlayerModel;
    protected final Context context;
    public final Event<EventArgs1<Exception>> ExceptionEvent=new Event<>();
    public final Event<EventArgs> SongQueueUpdatedEvent=new Event<>();
    public final ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent=new ThreadSafeEvent<>();
    public final Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public final Event<EventArgs1<String>> SongDownloadedEvent=new Event<>();
    public final Event<EventArgs1<Body>> NameListInitEvent =new Event<>();
    public final Event<EventArgs1<Body>> DeviceListChangedEvent =new Event<>();
    public final Event<PermissionCheckEventArgs> PermissionCheckEvent;
    public final Event<EventArgs3<MP_EVT,Object,Body>> ModelCommunicationEvent=new Event<>();
    public final Event<EventArgs2<Body, TYPE>> NameChangeEvent=new Event<>();
    public final Event<EventArgs1<Body>> INITDeviceAddressEvent = new Event<>();
    public Event<TextChangedEventArgs> TextChanged=new Event<>();
    public Event<EventArgs1<Body>> DeleteSongEvent=new Event<>();
    public Event<EventArgs1<Body>> DeleteSongRequestEvent=new Event<>();

    public boolean deleteFromNicknamesByAddress(String address){
        D.log("in the map----");
        for(HashMap.Entry ent:NickNames.entrySet()){
            D.log((String) ent.getKey());
            D.log((String) ent.getValue());
        }
        boolean l=false;
        if(NickNames.get(address)!=null){
            NickNames.remove(address);
            l=true;
        }
        D.log("after-----");
        for(HashMap.Entry ent:NickNames.entrySet()){
            D.log("in the map----");
            D.log((String) ent.getKey());
            D.log((String) ent.getValue());
        }
        return l;

    }


    public BaseModel(final Context context, WifiBroadcastReciever reciever, final Boolean isHost, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        this.context = context;
        this.PermissionCheckEvent=PermissionCheckEvent;
        musicPlayerModel = new MusicPlayerModel(this, isHost);


        //Name peristing in memory init
        sharedpreferences = context.getSharedPreferences(mypreference,Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        NickName = sharedpreferences.getString(myName, "placeholder");
        deviceID = "ip::null";
        NickNames= new HashMap<>();


        subscribeToNameChange();



        TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(final TextChangedEventArgs args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(args.event()== EVT.toast){
                            Toast.makeText(context,args.text(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        ExceptionEvent.addListener(new EventListener<EventArgs1<Exception>>() {
            @Override
            public void action(final EventArgs1<Exception> args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(args.arg1() instanceof CannotReadException)
                            Toast.makeText(context,"Not supported format",Toast.LENGTH_SHORT).show();
                        else{
                            Toast.makeText(context,args.arg1().getMessage(),Toast.LENGTH_LONG).show();
                            D.log(args.arg1().getMessage());
                        }

                    }
                });
            }
        });
        reciever.ConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if(args.getValue()){
                    isConnected=true;
                }
                if(!args.getValue()&&isConnected) {
                    isConnected=false;
                    stop();
                }
            }
        });
    }
    boolean isConnected=false;


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
