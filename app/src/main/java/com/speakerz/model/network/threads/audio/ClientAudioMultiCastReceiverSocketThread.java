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
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.model.network.threads.util.ClientSocketStructWrapper;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientAudioMultiCastReceiverSocketThread extends Thread {



    private boolean running;
    private byte[] buf = new byte[1024];
    private Context context;
    //passes the file path to the mediaplayer
    public Event<EventArgs1<String>> SongDownloadedEvent;
    private ClientSocketStructWrapper wrapper=new ClientSocketStructWrapper();
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

    }
    int syncLagOffsetInPackages=0;

    Runnable playAudioRunnable=new Runnable() {
        @Override
        public void run() {
            D.log("starting playback at"+actualAudioPackage);
            at.play();
            Iterator itr= bufferQueue.iterator();
            while (itr.hasNext()&&!swapSong.get()) {
                AudioPacket packet=(AudioPacket)itr.next();
                if(packet.packageNumber>=actualAudioPackage+syncLagOffsetInPackages) {
                    at.write(packet.data, 0, packet.data.length);
                }
            }
            at.stop();
            swapSong.set(false);
            bufferQueue.clear();
            send(wrapper.senderInfoSocket,new ChannelObject(new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.EOF_RECEIVED)),TYPE.AUDIO_CONTROL_SERVER));
        }
    };
    AtomicBoolean swapSong=new AtomicBoolean(false);
    int actualAudioPackage=0;


    private void listen(SocketStruct struct) {
        while (!struct.socket.isClosed()) {
            try {
                ChannelObject inObject = (ChannelObject) struct.objectInputStream.readObject();
                D.log("got something");
                if (inObject.TYPE == TYPE.AUDIO_META) {
                    final AudioMetaBody body = (AudioMetaBody) inObject.body;
                    D.log("recieved meta packet");
                    //a sync csak a handle után jöhet
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                at = createAudioTrack(body.getContent());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            handleAudioPackets(at);
                        }
                    });
                    t.start();
                }else if(inObject.TYPE==TYPE.AUDIO_CONTROL_CLIENT) {
                    AudioControlBody body = (AudioControlBody) inObject.body;
                    D.log("recieved packet");
                    if(body.getContent().flag==AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE){
                        D.log("sync packet!!!");
                            //make sure, that she song will start.
                            actualAudioPackage=body.getContent().number;
                            D.log("actual audio pack set to"+actualAudioPackage);
                            Thread t=new Thread(playAudioRunnable);
                            t.start();
                            D.log("thread started");
                    }
                }else {
                    D.log("ClientAudioThread: received wrong package");
                }
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
                wrapper.receiverInfoSocket.socket=new Socket();
                wrapper.receiverInfoSocket.socket.setReuseAddress(true);
                wrapper.senderInfoSocket.socket=new Socket();
                wrapper.senderInfoSocket.socket.setReuseAddress(true);

                wrapper.dataSocket.socket=new Socket();
                wrapper.dataSocket.socket.setReuseAddress(true);

                D.log("datasocket connecting...");
                wrapper.senderInfoSocket.socket.connect(new InetSocketAddress(address, 9060));

                D.log("DataSocket connected");

                wrapper.senderInfoSocket.objectOutputStream=new ObjectOutputStream(wrapper.senderInfoSocket.socket.getOutputStream());
                D.log("output k");
                wrapper.senderInfoSocket.objectInputStream=new ObjectInputStream(wrapper.senderInfoSocket.socket.getInputStream());
                D.log("input k");

                D.log("infoSocket connecting...");
                wrapper.receiverInfoSocket.socket.connect(new InetSocketAddress(address, 9050));

                D.log("infoSocket connected");
                //OUTPUT FIRST!!!!!!!!!
                wrapper.receiverInfoSocket.objectOutputStream=new ObjectOutputStream(wrapper.receiverInfoSocket.socket.getOutputStream());
                D.log("output k");
                wrapper.receiverInfoSocket.objectInputStream=new ObjectInputStream(wrapper.receiverInfoSocket.socket.getInputStream());
                D.log("input k");

                D.log("datasocket connecting...");
                wrapper.dataSocket.socket.connect(new InetSocketAddress(address, 9070));

                D.log("DataSocket connected");

                wrapper.dataSocket.objectOutputStream=new ObjectOutputStream(wrapper.dataSocket.socket.getOutputStream());
                D.log("output k");
                wrapper.dataSocket.objectInputStream=new ObjectInputStream(wrapper.dataSocket.socket.getInputStream());
                D.log("input k");


                D.log("yeeeeeeeey");

            } catch (IOException e) {
                e.printStackTrace();
            }


        //first packet is always metadata
        buf=new byte[2048];
       // packet =new DatagramPacket(buf, buf.length);
        listen(wrapper.receiverInfoSocket);



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
    void send(SocketStruct struct,ChannelObject obj){
        try {
            struct.objectOutputStream.writeObject(obj);
            struct.objectOutputStream.flush();
            D.log("sync req sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Queue<AudioPacket> bufferQueue = new ConcurrentLinkedQueue<>();
    private void handleAudioPackets(final AudioTrack at) {

        D.log("receiving data packets:");
        ///buf = new byte[metaDto.packageSize];
        D.log("package size: "+metaDto.packageSize);
        int i=0;
        boolean playStarted=false;
        while (!wrapper.dataSocket.socket.isClosed()) {
            try {

                AudioPacket packet=(AudioPacket) wrapper.dataSocket.objectInputStream.readObject();
                if(packet.data.length==0){
                    swapSong.set(true);

                    break;
                }
                /////
                bufferQueue.add(packet);
               // D.log("audiopacket, size:"+audioPacket.size);
               // AudioPacket packet=(AudioPacket)e.getValue();
               // at.write(packet.data, 0,packet.size);
                D.log(""+packet.packageNumber+",i: "+i);
                //packet.packageNumber-metaDto.actualBufferedPackageNumber
                if(i>=100 &&!playStarted){
                    D.log("buffer size is over 100");
                    playStarted=true;
                    AudioControlDto dto =new AudioControlDto(AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE);
                    send(wrapper.senderInfoSocket,new ChannelObject(new AudioControlBody(dto),TYPE.AUDIO_CONTROL_SERVER));
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



        public void shutdown() {
        if(wrapper.receiverInfoSocket.socket!=null){
            try {
                wrapper.receiverInfoSocket.objectOutputStream.close();
                wrapper.receiverInfoSocket.objectInputStream.close();

                wrapper.receiverInfoSocket.socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(wrapper.dataSocket.socket!=null){
            try {
                wrapper.dataSocket.objectOutputStream.close();
                wrapper.dataSocket.objectInputStream.close();
                wrapper.dataSocket.socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }


}
