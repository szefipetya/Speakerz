package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.speakerz.R;
import com.speakerz.debug.D;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

import static androidx.core.content.ContextCompat.startActivity;

public class ServerAudioMultiCastSocketThread extends Thread {
    private InetAddress address;

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private Context context;

    public ServerAudioMultiCastSocketThread() {
        try {
            socket = new DatagramSocket(5040);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        running = true;
        try {
            streamAudio();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void streamAudio() throws IOException, InterruptedException {

        D.log("stream started");
        BufferedInputStream bis = null;
        InputStream fis=context.getResources().openRawResource(R.raw.passion_aac);
        try {

            DatagramPacket dp;
            int packetsize = 1024;
            double nosofpackets;

            bis = new BufferedInputStream( fis);
            byte[] mybytearray = new byte[packetsize];
            int i=0;
            while (   bis.read(mybytearray, 0, mybytearray.length)!=-1) {
                i++;
                System.out.println("Packet:" + (i + 1));
                D.log("Packet:" + (i + 1));
                dp = new DatagramPacket(mybytearray, mybytearray.length,address, 5040);
                socket.send(dp);
                mybytearray = new byte[packetsize];
            }
        }finally {
            if(bis!=null)
                bis.close();
            if(socket !=null)
                socket.close();
        }

    }


    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
  /* while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                D.log("recieved (server)");
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());

            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {

                socket.send(packet);
                D.log("sent (server)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();*/