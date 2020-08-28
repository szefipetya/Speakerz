package com.speakerz.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;
import java.lang.reflect.Method;

public class DeviceModel extends BaseModel {
    DeviceNetwork network;
    public Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();



    @Override
    public void start() {
        network.start();
        network.getReciever().clearConnections();
    }

    protected void injectNetworkDependencies() {
        network.getClientSocketWrapper().audioSocket.MusicPlayerActionEvent=this.MusicPlayerActionEvent;
        network.getClientSocketWrapper().controllerSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getClientSocketWrapper().controllerSocket.MetaInfoReceivedEvent=MetaInfoReceivedEvent;
        network.getClientSocketWrapper().controllerSocket.NameChangeEvent = NameChangeEvent;
    }


    private void subscribeNetworkEvents() {


        NameChangeEvent.addListener(new EventListener<EventArgs1<Body>>() {

            @Override
            public void action(EventArgs1<Body> args) {
                D.log("name:"+NickName);
                D.log("NAME CHANGE HAPPEND.");
                NickNames.put(((PutNameChangeRequestBody)args.arg1()).getContent().id,((PutNameChangeRequestBody)args.arg1()).getContent().name);
                D.log("name:"+NickNames.get(((PutNameChangeRequestBody)args.arg1()).getContent().id));

            }
        });
        //a network jelzi, hogy songObjectet kapott.


        //a network jelez, hogy elkézült a ControllerSocket.
    }

    @Override
    public void stop() {
        //network.getReciever().getWifiP2pManager().cancelConnect( network.getReciever().getChannel(),null);

        if(network.getClientSocketWrapper().controllerSocket!=null) {
            network.getClientSocketWrapper().controllerSocket.shutdown();
            network.getClientSocketWrapper().audioSocket.shutdown();
            try {
                network.getClientSocketWrapper().controllerSocket.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            network.getReciever().clearConnections();
        }
        network.PermissionCheckEvent.removeAllListeners();
        network.PermissionCheckEvent=null;
        network.ListChanged.removeAllListeners();
        network.ListChanged=null;
        network.TextChanged.removeAllListeners();
        network.TextChanged=null;
        deletePersistentGroups();

    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(network.getReciever().getWifiP2pManager(), network.getReciever().getChannel(), netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public DeviceModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        super(context, reciever,false,PermissionCheckEvent);
        network=new DeviceNetwork(reciever);
        network.PermissionCheckEvent=this.PermissionCheckEvent;
        injectNetworkDependencies();

        subscribeNetworkEvents();

        network.getReciever().setConnectivityManager(connectivityManager);
        subscribeMusicPlayerModelEvents();
        network.getClientSocketWrapper().audioSocket.setContext(context);





    }
    private void subscribeMusicPlayerModelEvents() {


        ModelCommunicationEvent.addListener(new EventListener<EventArgs3<MP_EVT, Object, Body>>() {
            @Override
            public void action(EventArgs3<MP_EVT, Object,Body> args) {
                if(args.arg1()==MP_EVT.SEND_LIST){

                        SongQueueUpdatedEvent.invoke(null);

                }
                if(args.arg1()==MP_EVT.SEND_SONG){
                    SongQueueUpdatedEvent.invoke(null);

                }
            }
        });
    }

    @Override
    public DeviceNetwork getNetwork() {
        return network;

    }


    public void discoverPeers() {
        network.discoverPeers();
    }

}
