package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.InetAddresses;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.SocketThread;
import com.speakerz.model.network.threads.audio.util.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.AudioMetaInfo;
import com.speakerz.model.network.threads.audio.util.StreamUtil;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;

import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private byte[] buf = new byte[1024];
    private Context context;
    //passes the file path to the mediaplayer
    public Event<EventArgs1<String>> SongDownloadedEvent;

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

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        File tmpFile = new File(context.getFilesDir(), "audio.mp3");
        try {
            fos = new FileOutputStream(tmpFile);
            this.fos = new FileOutputStream(tmpFile);
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
        init();

      //send a packet to the host to know about this client

            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, address, 5040);
            try {

                socket.send(packet);
                D.log("sent");
            } catch (IOException e) {
                e.printStackTrace();
            }


        //first packet is always metadata
        buf=new byte[1024];
        packet =new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            AudioTrack at=createAudioTrack(packet.getData());
            handleAudioPackets(at);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    AudioMetaDto metaDto=null;

    int bufferSize = 1024;
    private AudioTrack createAudioTrack(byte[] data) {

            metaDto=(AudioMetaDto) SerializationUtils.deserialize(data);

        int minBufferSize = AudioTrack.getMinBufferSize(metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);
        return at;
    }

    private void handleAudioPackets(AudioTrack at) {


        buf = new byte[1024];
        at.play();
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                D.log("recv");
                /////
                int i = 0;

                        at.write(buf, 0, packet.getLength());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    File tmpFile;
    OutputStream fos;




   /* private void handlePacket(byte[] buf,int len){

        try {


             D.log("rec");
             if(buf.equals(bufEnd)){
                SongDownloadedEvent.invoke(new EventArgs1<String>(this,tmpFile.getPath()));
                D.log("song ready");
             }else{
                 fos.write(buf);
             }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // handle exception here
        } catch (IOException e) {
            e.printStackTrace();
            // handle exception here
        }

      //  D.log("FileOutputStream", "Saved");
    }*/


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