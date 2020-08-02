package com.speakerz.model.network.threads;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.GetServerInfoBody;
import com.speakerz.model.network.Serializable.body.content.ServerInfo;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.channel.ConnectionUpdatedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientControllerSocketThread extends Thread implements SocketThread{
    SocketStruct struct;
    String hostAddress;

    //injection
   public Event<EventArgs1<Body>> MetaInfoReceivedEvent;
    public Event<EventArgs1<Body>> MusicPlayerActionEvent;

    public ClientControllerSocketThread(){

    }

    @Override
    public void run() {
        try {
            D.log("client running");
            struct=new SocketStruct();
            struct.socket=new Socket();
            struct.socket.setReuseAddress(true);
            struct.socket.connect(new InetSocketAddress(hostAddress,5050),1000);

            D.log("connection succesful to "+ hostAddress);
            //PrintWriter pr=new PrintWriter(socket.getOutputStream());
          //  pr.println("Hello, i am a client");
            //pr.flush();
            struct.objectOutputStream = new ObjectOutputStream(struct.socket.getOutputStream());
            struct.objectInputStream = new ObjectInputStream(struct.socket.getInputStream());
            listen(struct);
        } catch (IOException  | ClassNotFoundException e) {
            if(e.getMessage()!=null)
               D.log(e.getMessage());
            e.printStackTrace();


             shutdown();
        }
        //send and recieve
    }

    @Override
    public void listen(SocketStruct struct) throws IOException, ClassNotFoundException {
        while(struct.socket!=null) {
            D.log("listening...");
            // read the list of messages from the socket
            ChannelObject chObject = (ChannelObject) struct.objectInputStream.readObject();
            handleIncomingObject(chObject);
        }
    }

    @Override
    public void handleIncomingObject(ChannelObject chObject) {
        D.log("got a ChObj");
        if(chObject.TYPE== TYPE.META){
            MetaInfoReceivedEvent.invoke(new EventArgs1<Body>(this,chObject.body));
        }if(chObject.TYPE==TYPE.MP){
            MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this, chObject.body));
        }
    }


    //adds a new song to the party. returns true if the connection exists.
    public boolean addNewSong(ChannelObject chobj) throws Exception{
        if(struct.socket!=null)
        {
            chobj.body.senderAddress=struct.socket.getInetAddress().getHostAddress();
            struct.objectOutputStream.writeObject(chobj);
            struct.objectOutputStream.flush();
            return true;
        }else return false;
    }

    @Override
    public void shutdown(){
        try {
            if (struct != null) {
                if (struct.objectInputStream != null) {

                    struct.objectInputStream.close();
                }
                if (struct.objectOutputStream != null) {
                    struct.objectOutputStream.reset();
                    struct.objectOutputStream.flush();
                    struct.objectOutputStream.close();

                }
                if (struct.socket != null)
                    struct.socket.close();
                struct.socket = null;
                System.gc();

            }
            } catch(IOException e){
                e.printStackTrace();
                D.log(e.getMessage());
            }

    }

    public void setAddress(InetAddress address) {
        this.hostAddress=address.getHostAddress();
    }
}
