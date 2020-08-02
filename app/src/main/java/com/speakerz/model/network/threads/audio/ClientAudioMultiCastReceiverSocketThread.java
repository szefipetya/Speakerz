package com.speakerz.model.network.threads.audio;

import android.media.AudioFormat;
import android.net.InetAddresses;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.SocketThread;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    private InetAddress address;



    public ClientAudioMultiCastReceiverSocketThread() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listen(){

    }


    @Override
    public void run() {
        D.log("testbegins");
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        while(true){
            //D.log("recieved from server: "+ sendEcho("from client"));
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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