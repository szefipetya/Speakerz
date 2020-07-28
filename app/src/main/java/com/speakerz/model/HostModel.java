package com.speakerz.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.event.SongItemEventArgs;
import com.speakerz.model.network.*;
import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.channel.MusicPlayerActionEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;

import java.util.ArrayList;

public class HostModel extends BaseModel {
    HostNetwork network;


    public HostModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager) {
        super(context, reciever);
        network = new HostNetwork(reciever);
        network.getReciever().setConnectivityManager(connectivityManager);
        initNetworkListeners();
    }

    @Override
    public void start() {
        network.start();
        network.getReciever().clearConnections();
        startAdvertising();


        //D.log("Model started");
    }

    HostModel self=this;
    private void initNetworkListeners() {


        //a network jelzi, hogy songObjectet kapott.
        network.MusicPlayerActionEvent.addListener(new EventListener<MusicPlayerActionEventArgs>() {
            @Override
            public void action(MusicPlayerActionEventArgs args) {
                if(args.getChannelObject().getSubType()== SUBTYPE.MP_ADD_SONG){
                    D.log("Hostmodel: MusicplayerActionEvent Happened, got a song object.");
                    //megkapom az objectet
                    final SongRequestObject songReq=(SongRequestObject)(args.getChannelObject().getObj());
                    //beteszem a listába
                    // songList.add(songReq.getTitle()+" "+songReq.getSender());
                    //UPDATE: majd a nézet teszi be, mert nem mingid tudja lekövetni.
                    //értesítem a nézetet, aki fel van iratkozva.
                    SongListChangedEvent.invoke(new SongItemEventArgs(self,songReq));
                }
            }
        });
        //a network jelez, hogy elkézült a ControllerSocket.
    }



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
        network.ConnectionUpdatedEvent.removeAllListeners();
        network.ConnectionUpdatedEvent=null;
        network.PackageRecievedEvent.removeAllListeners();
        network.PackageRecievedEvent=null;
        network.ListChanged.removeAllListeners();
        network.ListChanged=null;
        network.TextChanged.removeAllListeners();
        network.TextChanged=null;


    }



    @Override
    public HostNetwork getNetwork() {
        return network;
    }
}
