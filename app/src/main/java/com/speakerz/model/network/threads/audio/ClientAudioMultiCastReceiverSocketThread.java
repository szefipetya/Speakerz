package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.net.InetAddresses;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.SocketThread;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

public class ClientAudioMultiCastReceiverSocketThread extends Thread {


    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    private InetAddress address;



    public ClientAudioMultiCastReceiverSocketThread() {
        try {
            socket = new DatagramSocket(5040,address);
            init();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        tmpFile = new File(this.context.getCacheDir(),"mediafile");
        try {
            fos = new FileOutputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        D.log("FileOutputStream", "Download");

        // write to file until complete

    }

    public void listen(){

    }


    @Override
    public void run() {
        D.log("testbegins");

        while(socket.isConnected()){
            //D.log("recieved from server: "+ sendEcho("from client"));
            byte[] buffer=new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                D.log("recieved");
                socket.receive(packet);
               handlePacket(packet.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    File tmpFile;
    InputStream audioInputStream;
    FileOutputStream fos;

    private void handlePacket(byte[] buffer){
       //audioInputStrea

        fos.write(buf, 0, numread);
        fos.flush();

        D.log("FileOutputStream", "Saved");
    }
    public void close() {
        socket.close();
    }

        public void listen(DatagramPacket packet) throws IOException, ClassNotFoundException {

        }

        public void handleIncomingObject(Object chObject) throws IOException {

        }

        public void shutdown() {

        }


}
 /*public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 5040);
        try {
            D.log("sent");
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        packet = new DatagramPacket(buf, buf.length);
        try {
            D.log("recieved");
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String received = new String(
                packet.getData(), 0, packet.getLength());
        return received;
    }*/