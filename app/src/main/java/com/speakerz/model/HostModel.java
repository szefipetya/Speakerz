package com.speakerz.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.*;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.GetSongListBody;
import com.speakerz.model.network.Serializable.body.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.content.SongItem;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.IOException;
import java.util.List;

public class HostModel extends BaseModel {
    HostNetwork network;


    public HostModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager) {
        super(context, reciever,true);
        network = new HostNetwork(reciever);
        network.PermissionCheckEvent=this.PermissionCheckEvent;
        network.getReciever().setConnectivityManager(connectivityManager);
        injectNetworkDependencies();

        subscribeMusicPlayerModelEvents();
        subscribeNetWorkEvents();
    }

    private void subscribeNetWorkEvents() {

    }

    private void subscribeMusicPlayerModelEvents() {


        musicPlayerModel.ModelCommunicationEvent.addListener(new EventListener<EventArgs3<MP_EVT, Object, Body>>() {
            @Override
            public void action(EventArgs3<MP_EVT, Object,Body> args) {
                if(args.arg1()==MP_EVT.SEND_LIST){
                    try {
                        network.getServerSocketWrapper().controllerSocket.send(args.arg3().senderAddress,new ChannelObject(new GetSongListBody((List<SongItem>)args.arg2()),TYPE.MP));
                       // SongQueueUpdatedEvent.invoke(null);
                    } catch (IOException e) {
                        D.log("could not send SongList. ");
                        e.printStackTrace();
                    }
                }
                if(args.arg1()==MP_EVT.SEND_SONG){
                    try {
                        network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new PutSongRequestBody((SongItem)args.arg2()),TYPE.MP));
                        SongQueueUpdatedEvent.invoke(null);
                        D.log("song sent to clients");
                    } catch (IOException e) {
                        D.log("could not send a single song to"+args.arg3().senderAddress+((SongItem)(args.arg3().getContent())).sender);
                    }

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
        network.getServerSocketWrapper().controllerSocket.MetaInfoEvent =MetaInfoReceivedEvent;
    }
}
