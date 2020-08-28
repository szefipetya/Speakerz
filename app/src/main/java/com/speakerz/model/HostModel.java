package com.speakerz.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.*;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HostModel extends BaseModel {
    HostNetwork network;


    public HostModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager, Event<PermissionCheckEventArgs> PermissionCheckEvent) {

        super(context, reciever,true,PermissionCheckEvent);
        network = new HostNetwork(reciever);
        network.PermissionCheckEvent=this.PermissionCheckEvent;
        network.getReciever().setConnectivityManager(connectivityManager);
        injectNetworkDependencies();

        subscribeMusicPlayerModelEvents();
        subscribeNetWorkEvents();
        network.getServerSocketWrapper().audioSocket.setContext(context);
        //NickNames.put("Host",this.NickName);
    }

    private void subscribeNetWorkEvents() {
        NameChangeEvent.addListener(new EventListener<EventArgs1<Body>>() {

            @Override
            public void action(EventArgs1<Body> args) {
                D.log("NAME CHANGE HAPPEND.");
                NickNames.put(((PutNameChangeRequestBody)args.arg1()).getContent().id,((PutNameChangeRequestBody)args.arg1()).getContent().name);
                D.log("name:"+NickNames.get(((PutNameChangeRequestBody)args.arg1()).getContent().id));
                try {
                    network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new PutNameChangeRequestBody( (NameItem) args.arg1().getContent()),TYPE.NAME));
                    //SongQueueUpdatedEvent.invoke(null);
                    D.log("NameChange sent");
                } catch (IOException e) { }

            }
        });
    }

    private void subscribeMusicPlayerModelEvents() {


        ModelCommunicationEvent.addListener(new EventListener<EventArgs3<MP_EVT, Object, Body>>() {
            @Override
            public void action(EventArgs3<MP_EVT, Object,Body> args) {
                if(args.arg1()==MP_EVT.SEND_LIST){
                    try {
                        network.getServerSocketWrapper().controllerSocket.send(args.arg3().senderAddress,new ChannelObject(new GetSongListBody((List<Song>)args.arg2()),TYPE.MP));
                    } catch (IOException e) {
                        D.log("could not send SongQueue. ");
                        e.printStackTrace();
                    }
                }
                if(args.arg1()==MP_EVT.SEND_SONG){
                    try {
                        network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new PutSongRequestBody((Song)args.arg2()),TYPE.MP));
                        SongQueueUpdatedEvent.invoke(null);
                        D.log("song sent to clients");
                    } catch (IOException e) {
                        D.log("could not send a single song");
                    }

                }
                if(args.arg1()==MP_EVT.SONG_CHANGED){
                    network.getServerSocketWrapper().audioSocket.playAudioStreamFromLocalStorage((SongChangedInfo)args.arg2());
                }
                if(args.arg1()==MP_EVT.SONG_PAUSE){
                    network.getServerSocketWrapper().audioSocket.pauseAudioStream();
                }
                if(args.arg1()==MP_EVT.SONG_RESUME){
                    network.getServerSocketWrapper().audioSocket.resumeAudioStream();
                }


            }
        });
    }

    @Override
    public void start() {
        network.start();
        network.getReciever().clearConnections();
        startAdvertising();


        //D.log("Model started");
    }

    HostModel self=this;




    @SuppressLint("MissingPermission")
    public void startAdvertising() {


        //stop();st
       // network.getReciever().getWifiP2pManager().d
        network.getReciever().getWifiP2pManager().discoverPeers(network.getReciever().getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                D.log("advertising...");
                network.removeGroupIfExists();

            }

            @Override
            public void onFailure(int i) {
                D.log("advertising init failed");

            }
        });

    }
    @Override
    public void stop() {
        D.log("Model stopped");
        if(   network.getServerSocketWrapper().controllerSocket!=null) {
            network.getServerSocketWrapper().controllerSocket.shutdown();
            network.getServerSocketWrapper().audioSocket.shutdown();
            try {
                network.getServerSocketWrapper().controllerSocket.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        musicPlayerModel.close();
        network.getReciever().clearConnections();
        network.getReciever().getWifiP2pManager().removeGroup(network.getReciever().getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                D.log("group removed by stop()");

            }

            @Override
            public void onFailure(int i) {
                D.log("fail: group can't be removed by stop() errcode: "+i);

            }
        });
      //  getNetwork().getReciever().abortBroadcast();
        network.PermissionCheckEvent.removeAllListeners();
        network.PermissionCheckEvent=null;
        network.ListChanged.removeAllListeners();
        network.ListChanged=null;
        network.TextChanged.removeAllListeners();
        network.TextChanged=null;


    }



    @Override
    public HostNetwork getNetwork() {
        return network;
    }

    @Override
    protected void injectNetworkDependencies() {
        network.getServerSocketWrapper().controllerSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getServerSocketWrapper().audioSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getServerSocketWrapper().controllerSocket.MetaInfoEvent =MetaInfoReceivedEvent;
        network.getServerSocketWrapper().controllerSocket.NameChangeEvent =NameChangeEvent;
    }
}
