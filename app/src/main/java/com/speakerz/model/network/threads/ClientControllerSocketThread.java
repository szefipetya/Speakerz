package com.speakerz.model.network.threads;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.SongRequestObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientControllerSocketThread extends Thread{
    Socket socket;
    String hostAddress;
    ObjectOutputStream objectOutputStream=null;

    public ClientControllerSocketThread(InetAddress hostAddress){
        this.hostAddress=hostAddress.getHostAddress();
        socket=new Socket();
    }

    @Override
    public void run() {
        try {
            D.log("client running");
            socket.connect(new InetSocketAddress(hostAddress,5052),1000);

            D.log("connection succesful to "+ hostAddress);
            PrintWriter pr=new PrintWriter(socket.getOutputStream());
            pr.println("Hello, i am a client");
            pr.flush();
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            D.log(e.getMessage());
            e.printStackTrace();
        }
        //send and recieve
    }

    private void sendObjects() throws IOException {
        // create an object output stream from the output stream so we can send an object through it
        D.log("client: msg sent");
    }
    //adds a new song to the party. returns true if the connection exists.
    public boolean addNewSong(SongRequestObject obj) throws Exception{
        if(socket!=null)
        {
            objectOutputStream= new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();

            return true;
        }else return false;
    }
    public void shutdown(){
        try {
            objectOutputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            D.log(e.getMessage());
        }
    }

}
