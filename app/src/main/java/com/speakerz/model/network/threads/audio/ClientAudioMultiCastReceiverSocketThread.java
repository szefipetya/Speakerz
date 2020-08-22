package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.audio.AudioControlBody;
import com.speakerz.model.network.Serializable.body.audio.AudioMetaBody;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO_CONTROL;
import com.speakerz.model.network.Serializable.body.audio.content.AudioControlDto;
import com.speakerz.model.network.Serializable.body.controller.GetServerInfoBody;
import com.speakerz.model.network.Serializable.body.controller.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientAudioMultiCastReceiverSocketThread extends Thread {


    private Socket infoSocket;
    private Socket dataSocket;
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
       /* try {
            infoSocket.bind(new InetSocketAddress(address,8050));
        } catch (SocketException e) {
            e.printStackTrace();
        }*/
    }

    private InetAddress address;



    public ClientAudioMultiCastReceiverSocketThread() {

    }

    Runnable playAudioRunnable=new Runnable() {
        @Override
        public void run() {
                bufferMap.get(actualAudioPackage);
            for (Map.Entry entry : bufferMap.entrySet()) {
                if((Integer)entry.getKey()==actualAudioPackage) {
                    AudioPacket packet=(AudioPacket)entry.getValue();
                    at.write(packet.data, 0, packet.size);
                }
            }
        }
    };
    int actualAudioPackage=0;


    private void listen(Socket socket) {
        while (!socket.isClosed()) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(infoSocket.getInputStream());
                ChannelObject inObject = (ChannelObject) in.readObject();
                D.log("listening for meta or sync.");
                if (inObject.TYPE == TYPE.AUDIO_META) {
                    AudioMetaBody body = (AudioMetaBody) inObject.body;
                    D.log("recieved packet");
                    at = createAudioTrack(body.getContent());
                    handleAudioPackets(at);
                }if(inObject.TYPE==TYPE.AUDIO_CONTROL) {
                    AudioControlBody body = (AudioControlBody) inObject.body;
                    D.log("recieved packet");
                    if(body.getContent().flag==AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE){
                            actualAudioPackage=body.getContent().number;
                            Thread t=new Thread(playAudioRunnable);
                            t.start();
                    }
                    handleAudioPackets(at);
                }
                    D.log("ClientAudioThread: received wrong package");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    AudioTrack at;
    @Override
    public void run() {
        D.log("testbegins");


      //send a packet to the host to know about this client
            try {
                infoSocket = new Socket();
                infoSocket.setReuseAddress(true);

                dataSocket = new Socket();
                dataSocket.setReuseAddress(true);

                while(!infoSocket.isConnected()) {
                    D.log("infoSocket connecting...");
                    infoSocket.connect(new InetSocketAddress(address, 9050), 10000);
                }

                D.log("infoSocket connected");
                //infoSocket.getOutputStream().write(buf);
                while(!dataSocket.isConnected()) {
                    D.log("datasocket connecting...");
                    dataSocket.connect(new InetSocketAddress(address, 9060), 10000);
                }
                D.log("DataSocket connected");

            } catch (IOException e) {
                e.printStackTrace();
            }


        //first packet is always metadata
        buf=new byte[2048];
       // packet =new DatagramPacket(buf, buf.length);
        try {
            ObjectInputStream in =new ObjectInputStream(infoSocket.getInputStream());

            ChannelObject inObject=(ChannelObject)in.readObject();
            if(inObject.TYPE==TYPE.AUDIO_META) {
                AudioMetaBody body = (AudioMetaBody) inObject.body;
                D.log("recieved packet");
               at = createAudioTrack(body.getContent());
                handleAudioPackets(at);
            }else
                D.log("ClientAudioThread: received wrong package");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                listen(infoSocket);
            }
        });
        t.start();

    }

    AudioMetaDto metaDto=null;


    private AudioTrack createAudioTrack(AudioMetaDto metaDto) throws IOException {

    this.metaDto=metaDto;
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
    void send(Socket socket,ChannelObject obj){
        try {
            ObjectOutputStream out=new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(obj);
            D.log("sync req sent");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<Integer,AudioPacket> bufferMap= new ConcurrentHashMap<>();
    private void handleAudioPackets(final AudioTrack at) {

        D.log("receiving data packets:");
        ///buf = new byte[metaDto.packageSize];
        D.log("package size: "+metaDto.packageSize);
        ObjectInputStream in= null;
        try {
            in = new ObjectInputStream(dataSocket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        at.play();
        int i=0;
        boolean playStarted=false;
        while (!dataSocket.isClosed()) {
            try {

                AudioPacket packet=(AudioPacket) in.readObject();
                /////
                bufferMap.put(packet.packageNumber,packet);
               // D.log("audiopacket, size:"+audioPacket.size);
               // AudioPacket packet=(AudioPacket)e.getValue();
               // at.write(packet.data, 0,packet.size);
                D.log(""+packet.packageNumber+",i: "+i);
                if(packet.packageNumber==400||i==400){
                    AudioControlDto dto =new AudioControlDto(AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE);
                    send(infoSocket,new ChannelObject(new AudioControlBody(dto),TYPE.AUDIO_CONTROL));
                }
                i++;
                //buf=new byte[metaDto.packageSize];
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
        try {
            dataSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public void listen(DatagramPacket packet) throws IOException, ClassNotFoundException {

        }

        public void handleIncomingObject(Object chObject) throws IOException {

        }

        public void shutdown() {
        if(infoSocket!=null){
            try {
                infoSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(dataSocket!=null){
            try {
                dataSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }


}
