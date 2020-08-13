package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.speakerz.debug.D;
import com.speakerz.model.network.threads.audio.util.AudioMetaDto;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientAudioMultiCastReceiverSocketThread extends Thread {


    private DatagramSocket infoSocket;
    private DatagramSocket dataSocket;
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
            //create the infoSocket to send request for a port and the audio meta
            infoSocket = new DatagramSocket(8050);

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


        // write to file until complete

    }



    @Override
    public void run() {
        D.log("testbegins");
        init();

      //send a packet to the host to know about this client

            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, address, 8050);
            try {

                infoSocket.send(packet);
                D.log("sent");
            } catch (IOException e) {
                e.printStackTrace();
            }


        //first packet is always metadata
        buf=new byte[1024];
        packet =new DatagramPacket(buf, buf.length);
        try {
            infoSocket.receive(packet);
            D.log("recieved packet");
            AudioTrack at=createAudioTrack(packet);

            handleAudioPackets(at);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    AudioMetaDto metaDto=null;


    private AudioTrack createAudioTrack(DatagramPacket p_packet) throws SocketException {

            metaDto=(AudioMetaDto) SerializationUtils.deserialize(p_packet.getData());
            dataSocket=new DatagramSocket(metaDto.port);
         DatagramPacket packet
                = new DatagramPacket(buf, buf.length,address,metaDto.port);
        try {
            //send a packet to the server that we crated the client socket
            dataSocket.send(packet);
            D.log("sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
        D.log("channels: "+metaDto.channels);
        D.log("bitsPerSample: "+metaDto.bitsPerSample);
        D.log("bitrate: "+metaDto.bitrate);
        D.log("samplerate: "+metaDto.sampleRate);

        /*int minBufferSize = AudioTrack.getMinBufferSize(metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);*/
        int minBufferSize = AudioTrack.getMinBufferSize(metaDto.sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC,
                metaDto.sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        return at;


    }

    private void handleAudioPackets(AudioTrack at) {

        D.log("receiving data packets:");
        buf = new byte[metaDto.packageSize];
        D.log("package size: "+metaDto.packageSize);
        at.play();
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                dataSocket.receive(packet);

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
        dataSocket.close();
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
                = new DatagramPacket(buf, buf.length, address, ****);
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