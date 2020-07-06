package com.speakerz.model.network.threads;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.channel.MusicPlayerActionEventArgs;
import com.speakerz.util.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerControllerSocketThread extends Thread{
    Socket socket;
    ServerSocket serverSocket;
    public Event<MusicPlayerActionEventArgs> MusicPlayerActionEvent =new Event<>();


    InputStream inputStream;
    ObjectInputStream objectInputStream;
    @Override
    public void run() {
        try{
            D.log("server running");
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(5052));
            //waiting for someone
            socket=serverSocket.accept();
            D.log("client connected: "+socket.getLocalAddress());

            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            BufferedReader bf=new BufferedReader(in);

            String str=bf.readLine();
            D.log("client says: "+ str);

            inputStream= socket.getInputStream();
            // create a DataInputStream so we can read data from it.
             objectInputStream = new ObjectInputStream(inputStream);


            listen();
        }catch (IOException | ClassNotFoundException ex){
            ex.printStackTrace();
            D.log(ex.getMessage());
        }
    }

    private void listen() throws IOException, ClassNotFoundException {

        // read the list of messages from the socket
        ChannelObject chObject = (ChannelObject) objectInputStream.readObject();
        handleIncomingObject(chObject);
        listen();
    }

    private void handleIncomingObject(ChannelObject chObject) {
        D.log("server: got an object: "+chObject.getType());
        if(chObject.getType()== TYPE.MP){
            MusicPlayerActionEvent.invoke(new MusicPlayerActionEventArgs(this,chObject));
            D.log(" server: MusicPlayerActionEvent Happened: ");
            D.log(((SongRequestObject)chObject).toString());
        }
    }
    public void shutdown(){
        try {
            if(inputStream!=null)
                inputStream.close();
            if(objectInputStream!=null)
            objectInputStream.close();
            if(serverSocket!=null){

                serverSocket.close();

            }
            if(socket!=null)
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            D.log(e.getMessage());
        }
    }

}
