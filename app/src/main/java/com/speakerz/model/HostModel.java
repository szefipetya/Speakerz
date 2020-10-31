package com.speakerz.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;


import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.*;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameListInitRequestBody;
import com.speakerz.model.network.Serializable.body.controller.DeleteSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.body.controller.content.NameList;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;


import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;


public class HostModel extends BaseModel {
    HostNetwork network;


    public HostModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager, Event<PermissionCheckEventArgs> PermissionCheckEvent) {

        super(context, reciever,true,PermissionCheckEvent);
        network = new HostNetwork(reciever);
        network.PermissionCheckEvent=this.PermissionCheckEvent;
        network.ExceptionEvent=this.ExceptionEvent;
        network.TextChanged=this.TextChanged;
        network.getReciever().setConnectivityManager(connectivityManager);
        network.setAppRunning(isAppRunning);

        injectNetworkDependencies();

        subscribeMusicPlayerModelEvents();
        subscribeNetWorkEvents();
        network.getServerSocketWrapper().audioSocket.setContext(context);
        network.setNickName(NickName);
    }

    private void subscribeNetWorkEvents() {
        NameChangeEvent.addListener(new EventListener<EventArgs2<Body,TYPE>>(){
            @Override
            public void action(EventArgs2<Body,TYPE> args) {
                if(args.arg2() == TYPE.DELETENAME){
                    D.log("NAME DELETE HAPPEND.");
                    try {
                        NameItem delname = (NameItem) args.arg1().getContent();


                        D.log("id=="+delname.id);
                        D.log("name=="+NickNames.get(delname.id));
                        TextChanged.invoke(new TextChangedEventArgs(self, EVT.toast,( NickNames.get(delname.id)+" left the party")));
                        deleteFromNicknamesByAddress(delname.id);
                        DeviceListChangedEvent.invoke(null);

                            network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new PutNameChangeRequestBody((NameItem) args.arg1().getContent()), TYPE.DELETENAME));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(args.arg2() == TYPE.NAME){
                    D.log("name:"+NickNames.get(((PutNameChangeRequestBody)args.arg1()).getContent().id));
                    if(NickNames.get(((NameItem) args.arg1().getContent()).id) ==null || !NickNames.get(((NameItem) args.arg1().getContent()).id).equals(((NameItem) args.arg1().getContent()).name)) {
                        D.log("NAME CHANGE HAPPEND.");
                        TextChanged.invoke(new TextChangedEventArgs(self, EVT.toast,((NameItem) args.arg1().getContent()).name+" joined the party"));

                        NickNames.put(((PutNameChangeRequestBody)args.arg1()).getContent().id,((PutNameChangeRequestBody)args.arg1()).getContent().name);
                        DeviceListChangedEvent.invoke(null);

                        try {
                            network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new PutNameChangeRequestBody((NameItem) args.arg1().getContent()), TYPE.NAME));
                            D.log("NameChange sent");
                        } catch (IOException e) {
                        }
                    }
                   }

            }

            });

        NameListInitEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                D.log("NAMELIST INIT REQUEST HAPPEND. SERVER");
                try {
                    PutNameListInitRequestBody body = (PutNameListInitRequestBody) args.arg1();
                    network.getServerSocketWrapper().controllerSocket.send(body.senderAddress,new ChannelObject(new PutNameListInitRequestBody(new NameList(NickNames)),TYPE.INITNAMELIST));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        INITDeviceAddressEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                deviceID=args.arg1().getContent().toString();
                deviceAddress= (InetAddress) args.arg1().getContent();
                //a server hozzáadja magát a listához.
                NickNames.put(self.deviceID,self.NickName);
                DeviceListChangedEvent.invoke(null);
            }
        });

        DeleteSongEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                musicPlayerModel.removeSongByIndex(musicPlayerModel.getSongQueue().get((Integer) args.arg1().getContent()),(Integer) args.arg1().getContent());
                try {
                    network.getServerSocketWrapper().controllerSocket.sendAll(new ChannelObject(new DeleteSongRequestBody((Integer) args.arg1().getContent()), TYPE.DELETE_SONG));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                D.log("delete Song Sended" + (Integer) args.arg1().getContent());
            }
        });

        DeleteSongRequestEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                DeleteSongEvent.invoke(new EventArgs1<Body>(TYPE.DELETE_SONG,new DeleteSongRequestBody((Integer) args.arg1().getContent())));
            }
        });
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
                      //  SongQueueUpdatedEvent.invoke(null);
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
        deletePersistentGroups();
        startAdvertising();


        //D.log("Model started");
    }

    HostModel self=this;




    @SuppressLint("MissingPermission")
    public void startAdvertising() {

        network.getReciever().getWifiP2pManager().discoverPeers(network.getReciever().getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                D.log("advertising...");
                network.removeGroupIfExists();
                network.startRegistration();

            }

            @Override
            public void onFailure(int i) {
                D.log("advertising init failed");

            }
        });

    }
    @Override
    public void stop() {
        isAppRunning=false;
        D.log("Model stopped");
        if(   network.getServerSocketWrapper().controllerSocket!=null) {
            network.getServerSocketWrapper().controllerSocket.shutdown();
        }
        if(   network.getServerSocketWrapper().audioSocket!=null)
            network.getServerSocketWrapper().audioSocket.shutdown();
        musicPlayerModel.close();
        network.getReciever().clearConnections();
        deletePersistentGroups();
        network.stop();

    }



    @Override
    public HostNetwork getNetwork() {
        return network;
    }

    @Override
    protected void injectNetworkDependencies() {
        network.getServerSocketWrapper().controllerSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getServerSocketWrapper().audioSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getServerSocketWrapper().audioSocket.ExceptionEvent=ExceptionEvent;
        network.getServerSocketWrapper().controllerSocket.MetaInfoEvent =MetaInfoReceivedEvent;
        network.getServerSocketWrapper().controllerSocket.ExceptionEvent =ExceptionEvent;
        network.getServerSocketWrapper().controllerSocket.NameChangeEvent =NameChangeEvent;
        network.getServerSocketWrapper().controllerSocket.NameListInitEvent= NameListInitEvent;
        network.getServerSocketWrapper().controllerSocket.INITDeviceAddressEvent= INITDeviceAddressEvent;
        network.getServerSocketWrapper().controllerSocket.DeleteSongEvent = DeleteSongEvent;
        network.getServerSocketWrapper().controllerSocket.DeleteSongRequestEvent = DeleteSongRequestEvent;

        network.getServerSocketWrapper().audioSocket.init();

    }
}
