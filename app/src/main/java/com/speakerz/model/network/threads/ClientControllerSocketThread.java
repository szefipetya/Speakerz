package com.speakerz.model.network.threads;

import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.NetworkEventBody;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.enums.NET_EVT;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.ThreadSafeEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientControllerSocketThread extends Thread implements SocketThread{
    public Event<EventArgs1<Exception>> ExceptionEvent;
    SocketStruct struct;
    String hostAddress;

    //injection
   public Event<EventArgs1<Body>> MetaInfoReceivedEvent;
    public ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent;
    public Event<EventArgs1<Body>> NameChangeEvent;
    public Event<EventArgs1<Body>> DisconectedNameErase;
    volatile boolean externalShutdown=false;
    public ClientControllerSocketThread(){

    }

    @Override
    public void run() {
        try {
            D.log("client running");
            externalShutdown=false;
            struct=new SocketStruct();

            while(true) {
                try {
                    //Thread.sleep(2000);
                    struct.socket=new Socket();
                    struct.socket.setReuseAddress(true);
                    struct.socket.connect(new InetSocketAddress(hostAddress, 8040), 20000);
                    break;
                }catch (IOException e){
                    e.printStackTrace();
                } /*catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }


            D.log("connection succesful to "+ hostAddress);
            //PrintWriter pr=new PrintWriter(socket.getOutputStream());
          //  pr.println("Hello, i am a client");
            //pr.flush();
            struct.objectOutputStream = new ObjectOutputStream(struct.socket.getOutputStream());
            struct.objectInputStream = new ObjectInputStream(struct.socket.getInputStream());
            listen(struct);
        } catch (IOException  e) {
            if(e.getMessage()!=null)
               D.log(e.getMessage());
            e.printStackTrace();


             shutdown();

        }
        //send and recieve
    }

    @Override
    public void listen(SocketStruct struct){
        while(!externalShutdown&&!struct.socket.isClosed()) {
            D.log("listening...");
            // read the list of messages from the socket
            ChannelObject chObject = null;
            try {
                chObject = (ChannelObject) struct.objectInputStream.readObject();
                handleIncomingObject(chObject);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                shutdown();
                break;
            }

        }
    }

    @Override
    public void handleIncomingObject(ChannelObject chObject) {
        D.log("got a ChObj");
        if(chObject.TYPE== TYPE.META){
            MetaInfoReceivedEvent.invoke(new EventArgs1<Body>(this,chObject.body));
        }if(chObject.TYPE==TYPE.MP){
            MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this,chObject.body));
        }
        if(chObject.TYPE== TYPE.NAME){
            NameChangeEvent.invoke(new EventArgs1<Body>(this,chObject.body));
            D.log(" server: NameChange Happened: ");

        }
    }


    //adds a new song to the party. returns true if the connection exists.
    public boolean send(ChannelObject chobj) throws Exception{
        if(struct.socket!=null&&!struct.socket.isClosed())
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
            externalShutdown=true;

          //  send(new ChannelObject(new NetworkEventBody(struct.socket.getInetAddress().getHostAddress(), NET_EVT.DISCONNECT),TYPE.NET));
            if (struct != null) {
                if (struct.objectInputStream != null) {
                    struct.objectInputStream.close();
                }
                if (struct.objectOutputStream != null) {
                    struct.objectOutputStream.close();
                }
                if (struct.socket != null)
                    struct.socket.close();

            }
            } catch(IOException e){
                e.printStackTrace();
                D.log(e.getMessage());
            } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setAddress(InetAddress address) {
        this.hostAddress=address.getHostAddress();
    }
}
