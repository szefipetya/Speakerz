package com.speakerz.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.DeleteSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PlaybackControlRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
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

public class DeviceModel extends BaseModel {
    DeviceNetwork network;
    public Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();

    public DeviceModel(Context context, WifiBroadcastReciever reciever, ConnectivityManager connectivityManager, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        super(context, reciever,false,PermissionCheckEvent);
        network=new DeviceNetwork(reciever);
        network.PermissionCheckEvent=this.PermissionCheckEvent;
        network.ExceptionEvent=this.ExceptionEvent;
        network.TextChanged=this.TextChanged;
    network.setAppRunning(isAppRunning);
        injectNetworkDependencies();

        subscribeNetworkEvents();

        network.getReciever().setConnectivityManager(connectivityManager);
        subscribeMusicPlayerModelEvents();
        network.getClientSocketWrapper().audioSocket.setContext(context);
        network.setNickName(NickName);



    }


    @Override
    public void start() {
        deletePersistentGroups();
        network.start();
        network.getReciever().clearConnections();


    }

    protected void injectNetworkDependencies() {
        network.getClientSocketWrapper().audioSocket.ExceptionEvent=this.ExceptionEvent;
        network.getClientSocketWrapper().audioSocket.MusicPlayerActionEvent=this.MusicPlayerActionEvent;
        network.getClientSocketWrapper().controllerSocket.ExceptionEvent=ExceptionEvent;
        network.getClientSocketWrapper().controllerSocket.MusicPlayerActionEvent=MusicPlayerActionEvent;
        network.getClientSocketWrapper().controllerSocket.MetaInfoReceivedEvent=MetaInfoReceivedEvent;
        network.getClientSocketWrapper().controllerSocket.NameChangeEvent = NameChangeEvent;
        network.getClientSocketWrapper().controllerSocket.NameListInitEvent= NameListInitEvent;
        network.getClientSocketWrapper().controllerSocket.INITDeviceAddressEvent= INITDeviceAddressEvent;
        network.getClientSocketWrapper().controllerSocket.DeleteSongEvent = DeleteSongEvent;
        network.getClientSocketWrapper().controllerSocket.DeleteSongRequestEvent = DeleteSongRequestEvent;


    }

DeviceModel self=this;
    private void subscribeNetworkEvents() {


        NameChangeEvent.addListener(new EventListener<EventArgs2<Body,TYPE>>() {

            @Override
            public void action(EventArgs2<Body,TYPE> args) {
                //TODO: EZT azért biztos lehet szebben is de így működik
                if(args.arg2() == TYPE.DELETENAME){
                    D.log("NAME DELETE HAPPEND.");
                    try {
                        NameItem delname = (NameItem) args.arg1().getContent();
                        if(NickNames.get(delname.id) != null){

                            TextChanged.invoke(new TextChangedEventArgs(self, EVT.toast,( NickNames.get(delname.id)+" left the party")));

                            network.getClientSocketWrapper().controllerSocket.send(new ChannelObject(new PutNameChangeRequestBody( (NameItem) args.arg1().getContent()),TYPE.DELETENAME));
                            deleteFromNicknamesByAddress(delname.id);

                            DeviceListChangedEvent.invoke(null);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //TODO: EZT azért biztos lehet szebben is de így működik
                if(args.arg2() == TYPE.NAME){
                    if(NickNames.get(((NameItem) args.arg1().getContent()).id) ==null || !NickNames.get(((NameItem) args.arg1().getContent()).id).equals(((NameItem) args.arg1().getContent()).name)){
                        D.log("NAME CHANGE HAPPEND.");
                        NickNames.put(((PutNameChangeRequestBody)args.arg1()).getContent().id,((PutNameChangeRequestBody)args.arg1()).getContent().name);
                        TextChanged.invoke(new TextChangedEventArgs(self, EVT.toast,((NameItem) args.arg1().getContent()).name+" joined the party"));
                        DeviceListChangedEvent.invoke(null);

                        D.log("name:"+NickNames.get(((PutNameChangeRequestBody)args.arg1()).getContent().id));
                        try {
                            network.getClientSocketWrapper().controllerSocket.send(new ChannelObject(new PutNameChangeRequestBody( (NameItem) args.arg1().getContent()),TYPE.NAME));
                            D.log("NameChange sent");
                        } catch (Exception e) {e.printStackTrace(); }

                    }
                }

            }
        });

        NameListInitEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                D.log("NAMELIST INIT REQUEST HAPPEND. SERVER");
                NickNames = ((NameList) args.arg1().getContent()).namelist;
                NameItem nameitem = new NameItem(NickName,"",deviceID);
                NameChangeEvent.invoke(new EventArgs2<Body,TYPE>(this,new PutNameChangeRequestBody(nameitem),TYPE.NAME));
            }
        });

        INITDeviceAddressEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
               deviceID=args.arg1().getContent().toString();
               deviceAddress= (InetAddress) args.arg1().getContent();
            }
        });

        DeleteSongEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                D.log("deleteSong Client");
                musicPlayerModel.removeSongByIndex(musicPlayerModel.getSongQueue().get((Integer) args.arg1().getContent()),(Integer) args.arg1().getContent());
            }
        });

        DeleteSongRequestEvent.addListener(new EventListener<EventArgs1<Body>>(){
            @Override
            public void action(EventArgs1<Body> args) {
                D.log("deleteSong Request Sended");
                try {
                    network.getClientSocketWrapper().controllerSocket.send(new ChannelObject(new DeleteSongRequestBody( (Integer) args.arg1().getContent()),TYPE.DELETE_SONG_REQUEST));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        //a network jelzi, hogy songObjectet kapott.


        //a network jelez, hogy elkézült a ControllerSocket.
    }

    @Override
    public void stop() {
isAppRunning=false;

        if(network.getClientSocketWrapper().controllerSocket!=null) {
            network.getClientSocketWrapper().controllerSocket.shutdown();
        }
        if( network.getClientSocketWrapper().audioSocket!=null) {
            network.getClientSocketWrapper().audioSocket.shutdown();
        }
        network.getReciever().clearConnections();

        deletePersistentGroups();
        musicPlayerModel.close();
        network.stop();
        network.createServiceRequest();
        network.discoverPeers();
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

                        SongQueueUpdatedEvent.invoke(null);

                }
                if(args.arg1()==MP_EVT.SEND_SONG){
                    SongQueueUpdatedEvent.invoke(null);

                }
                if(args.arg1()==MP_EVT.ADD_SONG_CLIENT){
                    try {
                        getNetwork().getClientSocketWrapper().controllerSocket.send(
                                        new ChannelObject(new PutSongRequestBody(self.deviceAddress,(Song)args.arg2()), TYPE.MP)
                                );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if(args.arg1() == MP_EVT.SONG_CHANGED){
                        getNetwork().getClientSocketWrapper().controllerSocket.send(
                                new ChannelObject(new PlaybackControlRequestBody(
                                        self.deviceAddress,
                                        PlaybackControlRequestBody.PlaybackControlType.SelectSong,
                                        ((SongChangedInfo)args.arg2()).songId
                                ), TYPE.MP)
                        );
                    }
                    if(args.arg1()==MP_EVT.SONG_PAUSE){
                        getNetwork().getClientSocketWrapper().controllerSocket.send(
                                new ChannelObject(new PlaybackControlRequestBody(
                                        self.deviceAddress,
                                        PlaybackControlRequestBody.PlaybackControlType.Pause
                                ), TYPE.MP)
                        );
                    }
                    if(args.arg1()==MP_EVT.SONG_RESUME){
                        getNetwork().getClientSocketWrapper().controllerSocket.send(
                                new ChannelObject(new PlaybackControlRequestBody(
                                        self.deviceAddress,
                                        PlaybackControlRequestBody.PlaybackControlType.Resume
                                ), TYPE.MP)
                        );
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
