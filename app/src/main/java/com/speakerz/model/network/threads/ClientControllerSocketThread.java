package com.speakerz.model.network.threads;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.model.network.Serializable.WelcomeChObject;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.channel.ConnectionUpdatedEventArgs;
import com.speakerz.util.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientControllerSocketThread extends Thread implements SocketThread{
    SocketStruct struct;
    String hostAddress;

   public Event<ConnectionUpdatedEventArgs> ConnectionUpdatedEvent=new Event<>();
    public ClientControllerSocketThread(InetAddress hostAddress){
        this.hostAddress=hostAddress.getHostAddress();

    }

    @Override
    public void run() {
        try {
            D.log("client running");
            struct=new SocketStruct();
            struct.socket=new Socket();
            struct.socket.setReuseAddress(true);
            struct.socket.connect(new InetSocketAddress(hostAddress,5048),1000);

            D.log("connection succesful to "+ hostAddress);
            //PrintWriter pr=new PrintWriter(socket.getOutputStream());
          //  pr.println("Hello, i am a client");
            //pr.flush();
            struct.objectOutputStream = new ObjectOutputStream(struct.socket.getOutputStream());
            struct.objectInputStream = new ObjectInputStream(struct.socket.getInputStream());
            listen(struct);
        } catch (IOException  | ClassNotFoundException e) {
            D.log(e.getMessage());
            e.printStackTrace();


             shutdown();



        }
        //send and recieve
    }

    @Override
    public void listen(SocketStruct struct) throws IOException, ClassNotFoundException {
        while(true) {
            D.log("listening...");
            // read the list of messages from the socket
            ChannelObject chObject = (ChannelObject) struct.objectInputStream.readObject();
            handleIncomingObject(chObject);
        }
    }

    @Override
    public void handleIncomingObject(ChannelObject chObject) {
        D.log("got a ChObj");
        if(chObject.getType()== TYPE.WELCOME){
            WelcomeChObject welcomeObj=(WelcomeChObject)chObject.getObj();
            ConnectionUpdatedEvent.invoke(new ConnectionUpdatedEventArgs(
                    this
                    ,welcomeObj.getHostNickName()
                    ,welcomeObj.getWelcomeMessage()
                    ,welcomeObj.getHostAddress())
            );
        }
    }


    //adds a new song to the party. returns true if the connection exists.
    public boolean addNewSong(SongRequestObject obj) throws Exception{
        if(struct.socket!=null)
        {
            struct.objectOutputStream.writeObject(obj);
            struct.objectOutputStream.flush();
            return true;
        }else return false;
    }

    @Override
    public void shutdown(){
        try {


            if(struct.objectInputStream!=null){

                struct.objectInputStream.close();
            }
            if(struct.objectOutputStream!=null){
                struct.objectOutputStream.reset();
                struct.objectOutputStream.flush();
                struct.objectOutputStream.close();
            }
            if(struct.socket!=null)
                struct. socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            D.log(e.getMessage());
        }
    }

}
