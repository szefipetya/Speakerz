package com.speakerz.model;

import android.net.ConnectivityManager;

import com.speakerz.debug.D;
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


    public HostModel(WifiBroadcastReciever reciever, ConnectivityManager connectivityManager) {
        super(reciever);
        network = new HostNetwork(reciever);
        network.getReciever().setConnectivityManager(connectivityManager);
        initNetworkListeners();
    }

    @Override
    public void start() {
        network.start();
        network.getReciever().clearConnections();
        network.startAdvertising();


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
                            SongRequestObject songReq=(SongRequestObject)(args.getChannelObject().getObj());
                            //beteszem a listába
                            songList.add(songReq.getTitle()+" "+songReq.getSender());
                            //értesítem a nézetet, aki fel van iratkozva.
                            SongListChangedEvent.invoke(new EventArgs(self));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void stop() {
        //D.log("Model stopped");
        if(   network.getServerSocketWrapper().controllerSocket!=null)
        network.getServerSocketWrapper().controllerSocket.shutdown();
        network.getReciever().getWifiP2pManager().removeGroup(network.getReciever().getChannel(),null);


    }



    @Override
    public HostNetwork getNetwork() {
        return network;
    }
}
