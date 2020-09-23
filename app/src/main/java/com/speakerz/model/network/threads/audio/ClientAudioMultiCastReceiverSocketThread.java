package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.AudioControlBody;
import com.speakerz.model.network.Serializable.body.audio.AudioMetaBody;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO_CONTROL;
import com.speakerz.model.network.Serializable.body.audio.content.AudioControlDto;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.model.network.threads.util.ClientSocketStructWrapper;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.ThreadSafeEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientAudioMultiCastReceiverSocketThread extends Thread {

    private class SyncTask{
        public long timeOnServerSinceConnected=0;

        public SyncTask(long timeOnServerSinceConnected) {
            this.timeOnServerSinceConnected = timeOnServerSinceConnected;
        }
    }

    public ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent;
    public Event<EventArgs1<Exception>> ExceptionEvent;
    private boolean running;
    private byte[] buf = new byte[1024];
    private Context context;
    //passes the file path to the mediaplayer
    public Event<EventArgs1<String>> SongDownloadedEvent;
    private ClientSocketStructWrapper wrapper=new ClientSocketStructWrapper();
    private long actualSyncTimeOnServer=0;

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

    ConcurrentLinkedQueue<SyncTask> syncTasks=new ConcurrentLinkedQueue<>();
    public ClientAudioMultiCastReceiverSocketThread() {

    }
    int syncLagOffsetInPackages=0;
    int packagesToSkipByDelta=0;
    private int lastPackageOffsetByDelta=0;

final AtomicBoolean playbackStarted=new AtomicBoolean(false);
    Runnable playAudioRunnable=new Runnable() {

        @Override
        public void run() {
            D.log("starting playback at"+actualAudioPackage);
            at.play();
            final Iterator<AudioPacket> itr=bufferQueue.iterator();
            boolean firstTimeFound=true;
            playbackStarted.set(true);
            while (itr.hasNext()&&!swapSong.get()) {

                AudioPacket packet=(AudioPacket)itr.next();

                if(packet.packageNumber>=actualAudioPackage) {

                    if(!syncTasks.isEmpty()){
                        SyncTask task=syncTasks.poll();
                        skipPackages(itr,task.timeOnServerSinceConnected);
                    }else {
                        at.write(packet.data, 0, packet.data.length);
                    }

                    if(isPaused.get()){
                        synchronized (isPausedLocker){
                            try {
                                //wait until ispausedLocker is notified to resume the song.
                                isPausedLocker.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
            synchronized (playbackStarted) {
                playbackStarted.set(false);
                playbackStarted.notify();
            }
            swapSong.set(false);
            synchronized (swapSong) {
                swapSong.notify();
            }

        }
    };

    private void skipPackages(final Iterator<AudioPacket> itr,long timeOnServerSinceConn){

            D.log("timeonServer: " + timeOnServerSinceConn);
            long timeSinceConnected = new Date().getTime() - timeWhenConnected;
            long deltaTime = timeSinceConnected - timeOnServerSinceConn;
            D.log("deltaTime in milliSec: " + deltaTime);
            long bytesPer1000ms = metaDto.sampleRate * metaDto.bitsPerSample / 8;//1000 ms alatt ennyi byte megy le
            D.log("bytesPer1000ms: " + bytesPer1000ms);

            long offsetInBytes = (long) ((float) (deltaTime / 1000 * bytesPer1000ms));

            D.log("offset in bytes:" + offsetInBytes);
            packagesToSkipByDelta = (int) offsetInBytes / metaDto.packageSize;
            lastPackageOffsetByDelta = (int) offsetInBytes % metaDto.packageSize;


            D.log("packagesToSkipByDelta: " + packagesToSkipByDelta);
            D.log("lastPackageOffsetByDelta: " + lastPackageOffsetByDelta);

            int i = 0;
            while (itr.hasNext() && i < packagesToSkipByDelta) {
                itr.next();
                i++;
                D.log("skip");
            }
            if (itr.hasNext()) {
                AudioPacket packet = (AudioPacket) itr.next();
                byte slicedBytes[] = Arrays.copyOfRange(packet.data, lastPackageOffsetByDelta, packet.data.length);
                D.log("sliced length:" + slicedBytes.length);
                at.write(slicedBytes, 0, slicedBytes.length);
            }


    }
    AtomicBoolean swapSong=new AtomicBoolean(false);
    int actualAudioPackage=0;
    AtomicBoolean isPaused=new AtomicBoolean(false);
    final Object isPausedLocker=new Object();
    final Object eofLocker=new Object();



    private void listen(SocketStruct struct) {
        while (!struct.socket.isClosed()) {
            try {
                ChannelObject inObject = (ChannelObject) wrapper.receiverInfoSocket.objectInputStream.readObject();
                D.log("got something");
                if (inObject.TYPE == TYPE.AUDIO_META) {
                    final AudioMetaBody body = (AudioMetaBody) inObject.body;
                    D.log("recieved meta packet");

                    at = createAudioTrack(body.getContent());
                    playStarted=false;
                    i=0;
                    bufferQueue.clear();
                    if(!handlerRunning){
                        Thread t=new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleAudioPackets();
                            }
                        });
                        t.start();
                    }
                    MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_CHANGED,body.getContent().songId)));
                    MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_MAX_TIME_SECONDS,body.getContent().maxTimeInSeconds)));

                }else if(inObject.TYPE==TYPE.AUDIO_CONTROL_CLIENT) {
                    AudioControlBody body = (AudioControlBody) inObject.body;
                    D.log("recieved packet");
                    if(body.getContent().flag==AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE){
                        D.log("sync packet!!!");
                            //make sure, that she song will start.
                            actualAudioPackage=body.getContent().number;
                            actualSyncTimeOnServer=body.getContent().timeInMilliSeconds;
                            syncTasks.offer(new SyncTask(actualSyncTimeOnServer));
                            D.log("actual audio pack set to"+actualAudioPackage);
                            swapSong.set(false);
                            if(playbackStarted.get())
                                synchronized (playbackStarted){
                                    try {
                                        playbackStarted.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            Thread t=new Thread(playAudioRunnable);
                            t.start();
                            D.log("thread started");
                    }else if(body.getContent().flag==AUDIO_CONTROL.RESUME_SONG){
                        isPaused.set(false);
                        synchronized (isPausedLocker) {
                            isPausedLocker.notify();
                        }
                        MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_RESUME,null)));
                    }else if(body.getContent().flag==AUDIO_CONTROL.PAUSE_SONG){
                        isPaused.set(true);
                        MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_PAUSE,null)));
                    }
                }else {
                    D.log("ClientAudioThread: received wrong package");
                }
            } catch (IOException e) {
                e.printStackTrace();
                shutdown();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    long timeWhenConnected;
    AudioTrack at;
    @Override
    public void run() {
        D.log("testbegins");


         /*   try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }*/

            try {

                wrapper.receiverInfoSocket.socket=new Socket();
                wrapper.receiverInfoSocket.socket.setReuseAddress(true);
                wrapper.senderInfoSocket.socket=new Socket();
                wrapper.senderInfoSocket.socket.setReuseAddress(true);

                wrapper.dataSocket.socket=new Socket();
                wrapper.dataSocket.socket.setReuseAddress(true);

                D.log("datasocket connecting...");
                wrapper.senderInfoSocket.socket.connect(new InetSocketAddress(address, 9060));

                timeWhenConnected=new Date().getTime();
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
                buf=new byte[2048];
                // packet =new DatagramPacket(buf, buf.length);
                listen(wrapper.receiverInfoSocket);
            } catch (IOException e) {
                e.printStackTrace();
                ExceptionEvent.invoke(new EventArgs1<Exception>(this,new ConnectException("AUIDO_CONN_REFUSED")));
            }


        //first packet is always metadata




    }

    AudioMetaDto metaDto=null;


    private AudioTrack createAudioTrack(AudioMetaDto metaDto) throws IOException {

    this.metaDto=metaDto;
        D.log("channels: "+metaDto.channels);
        D.log("bitsPerSample: (bitSize/sample) "+metaDto.bitsPerSample);
        D.log("samplerate (samples/second): "+metaDto.sampleRate);
        D.log("isBitRateVariable: "+metaDto.isBitRateVariable);
        D.log("bytes/1000mSec: "+metaDto.sampleRate*(metaDto.bitsPerSample/8));

        int minBufferSize = AudioTrack.getMinBufferSize(metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, metaDto.sampleRate,
                metaDto.channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                metaDto.bitsPerSample == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);

        return at;
    }
    void send(SocketStruct struct,ChannelObject obj){
        try {
            struct.objectOutputStream.writeObject(obj);
            struct.objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Queue<AudioPacket> bufferQueue = new ConcurrentLinkedQueue<>();
    int minBufferSizeToPlay=200;
    boolean handlerRunning=false;
    boolean playStarted=false;
    int i=0;
    private void handleAudioPackets() {

        D.log("receiving data packets:");
        ///buf = new byte[metaDto.packageSize];
        D.log("package size: "+metaDto.packageSize);
         i=0;
        playStarted=false;
handlerRunning=true;
        while (!wrapper.dataSocket.socket.isClosed()) {
            try {

                AudioPacket packet=(AudioPacket) wrapper.dataSocket.objectInputStream.readObject();
              //  D.log("pack"+i);
                if(packet.size==-1){
                    swapSong.set(true);
                    playbackStarted.set(false);
                    D.log("playback ended");
                    at.stop();

                    i=0;

                    bufferQueue.clear();
                    MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_EOF,null)));

                    send(wrapper.senderInfoSocket,new ChannelObject(new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.EOF_RECEIVED)),TYPE.AUDIO_CONTROL_SERVER));

                    continue;
                }
                /////
                bufferQueue.add(packet);

                if(i>=minBufferSizeToPlay &&!playStarted){
                    D.log("buffer size is over 100");
                    playStarted=true;
                    AudioControlDto dto =new AudioControlDto(AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE);
                    send(wrapper.senderInfoSocket,new ChannelObject(new AudioControlBody(dto),TYPE.AUDIO_CONTROL_SERVER));
                }
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


        public void stopPlayBack(){
            swapSong.set(true);
        }
        public void shutdown() {
        stopPlayBack();
        bufferQueue.clear();

        if(wrapper.receiverInfoSocket.socket!=null){
            try {
                if(wrapper.receiverInfoSocket.objectOutputStream!=null)
                wrapper.receiverInfoSocket.objectOutputStream.close();
                if(wrapper.receiverInfoSocket.objectInputStream!=null)
                wrapper.receiverInfoSocket.objectInputStream.close();
                if(wrapper.receiverInfoSocket.socket!=null)
                wrapper.receiverInfoSocket.socket.close();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
            if(wrapper.senderInfoSocket.socket!=null){
                try {
                    if( wrapper.senderInfoSocket.objectOutputStream!=null)
                    wrapper.senderInfoSocket.objectOutputStream.close();
                    if( wrapper.senderInfoSocket.objectInputStream!=null)
                    wrapper.senderInfoSocket.objectInputStream.close();
                    if( wrapper.senderInfoSocket.socket!=null)
                    wrapper.senderInfoSocket.socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        if(wrapper.dataSocket.socket!=null){
            try {if( wrapper.dataSocket.objectOutputStream!=null)
                wrapper.dataSocket.objectOutputStream.close();
                if( wrapper.dataSocket.objectInputStream!=null)
                wrapper.dataSocket.objectInputStream.close();
                if( wrapper.dataSocket.socket!=null)
                wrapper.dataSocket.socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }


}
