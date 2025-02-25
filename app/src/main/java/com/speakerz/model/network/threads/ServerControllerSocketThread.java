package com.speakerz.model.network.threads;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.NetworkEventBody;
import com.speakerz.model.network.Serializable.body.controller.GetServerInfoBody;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.INITDeviceAddressBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.body.controller.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.NET_EVT;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.ThreadSafeEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerControllerSocketThread extends Thread implements SocketThread{

    public Event<EventArgs1<Exception>> ExceptionEvent;
    LinkedList<SocketStruct> socketList=new LinkedList<>();
    private InetAddress address;

    public ServerSocket getServerSocket() {
        return dataSocket;
    }

    ServerSocket dataSocket=null;
    //dependency injection
    public ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent =null;
    public Event<EventArgs1<Body>> MetaInfoEvent = null;
    public Event<EventArgs1<Body>> INITDeviceAddressEvent;
    public Event<EventArgs1<Body>> NameListInitEvent;
    public Event<EventArgs2<Body,TYPE>> NameChangeEvent=null;
    public Event<EventArgs1<Body>> DeleteSongEvent;
    public Event<EventArgs1<Body>> DeleteSongRequestEvent;

    volatile boolean externalShutdown=false;
    @Override
    public void run() {
        try{
            dataSocket = new ServerSocket();
            dataSocket.setReuseAddress(true);
            dataSocket.bind(new InetSocketAddress(8040));
            D.log("server address: "+dataSocket.getInetAddress().getHostAddress());
            D.log("localsocketaddress : "+dataSocket.getLocalSocketAddress());

            D.log("server running");
            this.INITDeviceAddressEvent.invoke(new EventArgs1<Body>(this,new INITDeviceAddressBody(address)));
            while(!externalShutdown) {
                final Socket socket = dataSocket.accept();
                if(socket == null){
                    D.log("nonblocking");
                    continue;
                }
                final SocketStruct struct = new SocketStruct();
                struct.socket = socket;
                struct.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                struct.objectInputStream = new ObjectInputStream(socket.getInputStream());
                recentStruct=struct;
                socketList.add(struct);
                writeWelcome(struct);


                D.log("client connected: "+socket.getInetAddress());


                //új szálon elindítjuk a socketet, hogy hallgassuk a bejövő adatokat.
                new Thread() {
                    @Override
                    public void run() {
                            // az új szál külön hallgatja az objektumot.
                            listen(struct);
                    }
                }.start();
            }

        }catch (IOException ex){
            ex.printStackTrace();
            if(ex.getMessage()!=null)
                D.log("Exception message: " + ex.getMessage());
            else
                D.log("null");

        }
    }

    private void writeWelcome(SocketStruct struct) throws IOException {
        struct.objectOutputStream.writeObject(new ChannelObject(new GetServerInfoBody(new ServerInfo("asd","bsd")),TYPE.META));
        struct.objectOutputStream.flush();

        MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this,new GetSongListBody(null)));

        D.log("welcome sent");

    }

    @Override
     public void listen(SocketStruct struct) {
        // read the list of messages from the socket
         while (dataSocket!=null&&!externalShutdown&&!struct.socket.isClosed()) {

                 recentStruct=struct;
                 D.log("listening...");

                 try {
                     handleIncomingObject((ChannelObject) struct.objectInputStream.readObject());
                 } catch (IOException e) {
                     e.printStackTrace();
                     closeClient(struct);
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                 }

           /*  if(struct.socket==null||struct.socket.isClosed()||!struct.socket.isConnected()){
                // D.log("client "+struct.socket.getInetAddress().getHostAddress()+" disconnected");
                 closeClient(struct);
                 break;
             }*/

         }
       //  closeClient(struct);

    }

    //be careful with that!
   volatile SocketStruct recentStruct=null;

    private SocketStruct getSocketStructByAddress(InetAddress address){
        for(SocketStruct s: socketList){
            if(s.socket.getInetAddress().equals(address)){
                return s;
            }
        }
        return null;
    }

    //be careful with null value!
    public void send(InetAddress address,ChannelObject channelObject) throws IOException{


        if(address==null&& recentStruct!=null){
            recentStruct.objectOutputStream.writeObject(channelObject);
            recentStruct.objectOutputStream.flush();
        }else{
            SocketStruct s=getSocketStructByAddress(address);
            if(s!=null) {
                s.objectOutputStream.writeObject(channelObject);
                s.objectOutputStream.flush();
            }else throw new IOException(this.toString()+" error: No socket found with address "+address);

        }
    }
    public void send(SocketStruct struct ,ChannelObject channelObject) throws IOException{
            recentStruct.objectOutputStream.writeObject(channelObject);
            recentStruct.objectOutputStream.flush();
    }

    public void sendAll(ChannelObject chObject) throws IOException {
        for(SocketStruct s : socketList){
            s.objectOutputStream.writeObject(chObject);
            s.objectOutputStream.flush();
        }

    }

    @Override
    public void handleIncomingObject(ChannelObject chObject) throws IOException {
        D.log("server: got an object: "+chObject.TYPE);

        if(chObject.TYPE== TYPE.MP){
            MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this,chObject.body));
            D.log(" server: MusicPlayerActionEvent Happened: ");

        }

        if(chObject.TYPE== TYPE.NAME){
            NameChangeEvent.invoke(new EventArgs2<Body,TYPE>(this,chObject.body,TYPE.NAME));
            D.log(" server: NameChange Happened: ");

        }

        if(chObject.TYPE== TYPE.DELETENAME) {
            NameChangeEvent.invoke(new EventArgs2<Body, TYPE>(this, chObject.body, TYPE.DELETENAME));
            D.log(" server: NameChange DELETE Happened: ");

        }

        if(chObject.TYPE == TYPE.INITNAMELIST) {
            NameListInitEvent.invoke(new EventArgs1<Body>(TYPE.INITNAMELIST,chObject.body));
            D.log(" Server:got NameListInit Request ");
        }

        if(chObject.TYPE== TYPE.NET){
            NetworkEventBody body= (NetworkEventBody) chObject.body;
            if(body.getContent()== NET_EVT.DISCONNECT){
                SocketStruct struct=getSocketStructByAddress(body.senderAddress);
                socketList.remove(struct);
                struct.objectOutputStream.close();
                struct.objectInputStream.close();
                struct.socket.close();
            }
            D.log(" server: NameChange Happened: ");

        }

        if(chObject.TYPE== TYPE.DELETE_SONG) {
            DeleteSongEvent.invoke(new EventArgs1<Body>(TYPE.DELETE_SONG,chObject.body));
            D.log(" Song " + (Integer)chObject.body.getContent()+ "deleted");
        }

        if(chObject.TYPE== TYPE.DELETE_SONG_REQUEST) {
            DeleteSongEvent.invoke(new EventArgs1<Body>(TYPE.DELETE_SONG,chObject.body));
        }
    }


    @Override
    public void shutdown(){
        D.log("Closing server socket");
        try {
            for(SocketStruct s: socketList){
                if(s.objectInputStream!=null)
                    s.objectInputStream.close();
                if(s.objectOutputStream!=null)
                    s.objectOutputStream.close();
                if(s.socket!=null)
                    s.socket.close();
            }
            if(dataSocket!=null){
                dataSocket.close();
            }

            externalShutdown=true;
        } catch (IOException e) {
            e.printStackTrace();
            D.log(e.getMessage());

        }
        D.log("Server socket closed");
    }
    void closeClient(SocketStruct struct){
        try {
            //TODO: DELETE NAME EVENT
            socketList.remove(struct);
if(struct.socket!=null){
            NameChangeEvent.invoke(new EventArgs2<Body, TYPE>(this, new PutNameChangeRequestBody(
                    new NameItem("torles","server",struct.socket.getInetAddress().toString())),TYPE.DELETENAME));
            D.log("lecsatlakozott egy ember");
            //struct.socket.getInetAddress();
            struct.objectInputStream.close();
            struct.objectOutputStream.close();
            struct.socket.close();
}

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setAddress(InetAddress address) {
        this.address=address;
    }

    public InetAddress getAddress(){
        return address;
    }
}
