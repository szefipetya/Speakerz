package com.speakerz.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.enums.TYPE;
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
        deletePersistentGroups();
    }

    protected void injectNetworkDependencies() {
        network.getClientSocketWrapper().audioSocket.ExceptionEvent=this.ExceptionEvent;
        network.getClientSocketWrapper().audioSocket.MusicPlayerActionEvent=this.MusicPlayerActionEvent;
        network.getClientSocketWrapper().controllerSocket.ExceptionEvent=ExceptionEvent;
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
                NickName = ((PutNameChangeRequestBody)args.arg1()).getContent().name;
                NickNames.put("Host",NickName);
                //Toast.makeText(context, "New name: "+NickNames.get("Host"), Toast.LENGTH_SHORT).show();
                D.log("name:"+NickNames.get("Host"));

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
        network.ExceptionEvent=this.ExceptionEvent;
        injectNetworkDependencies();

        subscribeNetworkEvents();

        network.getReciever().setConnectivityManager(connectivityManager);
        subscribeMusicPlayerModelEvents();
        network.getClientSocketWrapper().audioSocket.setContext(context);
        network.setNickName(NickName);




    }
    private void subscribeMusicPlayerModelEvents() {


        ModelCommunicationEvent.addListener(new EventListener<EventArgs3<MP_EVT, Object, Body>>() {
            @Override
            public void action(EventArgs3<MP_EVT, Object,Body> args) {
                if(args.arg1()==MP_EVT.SEND_LIST){

                      //  SongQueueUpdatedEvent.invoke(null);

                }
                if(args.arg1()==MP_EVT.SEND_SONG){

                  //  SongQueueUpdatedEvent.invoke(null);

                }if(args.arg1()==MP_EVT.ADD_SONG_CLIENT){
                    try {
                        network.getClientSocketWrapper().controllerSocket.send(

                                new ChannelObject(new PutSongRequestBody((Song)args.arg2()), TYPE.MP)
                        );
                    } catch (Exception e) {
                        D.log("error:problem with sending song to server");

                        e.printStackTrace();
                    }
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
