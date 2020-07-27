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
        //a network jelez, hogy elkézült a ControllerSocket.
        network.ControllerSocketEstablishedEvent.addListener(new EventListener<EventArgs>() {
            @Override
            public void action(EventArgs args) {
                //itt a controllersocket MusicplayerActionevent eseményeire feliratkozom, hogy a Musicplayert tudjam vezérelni.
                //egyenlőre csak egy testPlayer van.
                D.log("Hostmodel: Controllersocket established ");
                network.getServerSocketWrapper().controllerSocket.MusicPlayerActionEvent.addListener(new EventListener<MusicPlayerActionEventArgs>() {
                    @Override
                    public void action(MusicPlayerActionEventArgs args) {

                        //ADD_SONG event történt, eszerint kasztoljuk az objectet.
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
            }
        });
    }



    @SuppressLint("MissingPermission")
    public void startAdvertising() {


        //stop();
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
        if(   network.getServerSocketWrapper().controllerSocket!=null)
        network.getServerSocketWrapper().controllerSocket.shutdown();
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


    }



    @Override
    public HostNetwork getNetwork() {
        return network;
    }
}
