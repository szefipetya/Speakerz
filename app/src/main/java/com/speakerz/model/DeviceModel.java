package com.speakerz.model;

import android.content.Context;
import android.net.ConnectivityManager;

import com.speakerz.debug.D;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.channel.MusicPlayerActionEventArgs;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

public class DeviceModel extends BaseModel {
    DeviceNetwork network;


    @Override
    public void start() {
        network.start();
        network.getReciever().clearConnections();
        initNetworkListeners();
    }


        private void initNetworkListeners() {
            //a network jelez, hogy elkézült a ControllerSocket.
            network.ControllerSocketEstablishedEvent.addListener(new EventListener<EventArgs>() {
                @Override
                public void action(EventArgs args) {
                    //itt a controllersocket MusicplayerActionevent eseményeire feliratkozom, hogy a Musicplayert tudjam vezérelni.
                    //egyenlőre csak egy testPlayer van.
                   D.log("DeviceModel: controllerSOcket Is ready");
                }
            });
        }

    @Override
    public void stop() {
        if(network.getClientSocketWrapper().controllerSocket!=null) {
            network.getClientSocketWrapper().controllerSocket.shutdown();
            try {
                network.getClientSocketWrapper().controllerSocket.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            network.getReciever().clearConnections();
        }
    }

    public DeviceModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager){
        super(context, reciever);
        network=new DeviceNetwork(reciever);
        network.getReciever().setConnectivityManager(connectivityManager);
    }

    @Override
    public BaseNetwork getNetwork() {
        return network;
    }


    public void discoverPeers() {
        network.discoverPeers();
    }

}
