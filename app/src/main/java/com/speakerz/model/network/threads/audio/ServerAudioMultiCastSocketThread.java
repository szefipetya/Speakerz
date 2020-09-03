package com.speakerz.model.network.threads.audio;

import android.content.Context;

import com.speakerz.debug.D;
import com.speakerz.model.SongChangedInfo;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.NetworkEventBody;
import com.speakerz.model.network.Serializable.body.audio.AudioControlBody;
import com.speakerz.model.network.Serializable.body.audio.AudioMetaBody;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO_CONTROL;
import com.speakerz.model.network.Serializable.body.audio.content.AudioControlDto;
import com.speakerz.model.network.Serializable.enums.NET_EVT;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.audio.util.AudioBuffererDecoder;
import com.speakerz.model.network.threads.audio.util.AudioDecoderThread;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.DECODER_MODE;
import com.speakerz.model.network.threads.audio.util.YouTubeStreamAPI;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.model.network.threads.util.ClientSocketStructWrapper;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;
import com.speakerz.util.ThreadSafeEvent;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ealvatag.audio.exceptions.CannotReadException;


public class ServerAudioMultiCastSocketThread extends Thread {

    public ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent;
    public Event<EventArgs1<Exception>> ExceptionEvent;
    private Integer currentSongId;

    ///
    private InetAddress address;

    private ServerSocket receiverServerSocket;
    private ServerSocket senderServerSocket;
    private ServerSocket rdataServerSocket;

    private Context context;
    private boolean externalShutdown=false;


    public ServerAudioMultiCastSocketThread() {
        init();
        mainLoop();
    }


    public void playAudioStreamFromLocalStorage(final SongChangedInfo info){

        synchronized(locker) {
            swapSong.set(true);

            currentFile=info.file;
            currentSongId=info.songId;
            locker.notify();
            resumeAudioStream();


        }
    }
    File currentFile=null;

    public void stopAudioStream() {
        try {
            decoder.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void resumeAudioStream(){
        D.log("resume");
        sendAll(new ChannelObject(new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.RESUME_SONG)),TYPE.AUDIO_CONTROL_CLIENT));
        MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_RESUME,null)));
        synchronized (decoder.isPaused){
            decoder.isPaused.set(false);
            decoder.isPaused.notify();
        }
    }

    public void pauseAudioStream() {
        sendAll(new ChannelObject(new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.PAUSE_SONG)),TYPE.AUDIO_CONTROL_CLIENT));
        MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_PAUSE,null)));
        D.log("pause");
        decoder.isPaused.set(true);
    }





   // File currentMediaFile;
    private final List<ClientSocketStructWrapper> clients = Collections.synchronizedList(new LinkedList<ClientSocketStructWrapper>());
    AudioMetaDto recentAudioMetaDto=null;
    AudioDecoderThread decoder;
    AudioBuffererDecoder decoderBufferer;
    YouTubeStreamAPI yt=new YouTubeStreamAPI();
    public Event<EventArgs1<AudioPacket>> AudioTrackBufferUpdateEvent;
    public Event<EventArgs1<AudioMetaDto>> MetaDtoReadyEvent;

    private void init(){
        decoder=new AudioDecoderThread();
        decoder.MusicPlayerActionEvent=MusicPlayerActionEvent;
        decoderBufferer =new AudioBuffererDecoder();
        AudioTrackBufferUpdateEvent=new Event<>();
        MetaDtoReadyEvent=new Event<>();
        decoderBufferer.AudioTrackBufferUpdateEvent=AudioTrackBufferUpdateEvent;
        decoderBufferer.MetaDtoReadyEvent=MetaDtoReadyEvent;
        decoderBufferer.clients=clients;
        subscribeDecoderEvents();
    }
private void mainLoop(){
    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                while (/*!receiverServerSocket.isClosed()*/!externalShutdown) {
                    synchronized(locker) {
                        D.log("waiting for audio pick.");
                        while (!swapSong.get()) {
                            locker.wait();
                            D.log("waiting ui notify clicked");
                        }
                        swapSong.set(false);
                    }



                    //megvárjuk, ameddig az összeskliensnek elküld egy 0 byte-os csomagot, ezzel jelezve a stream végét.
                    if(isSongInPlay.get()) {
                        decoderBufferer.stop();
                        decoder.stop();
                        for (ClientSocketStructWrapper cli : clients) {
                            cli.eofSongReached = true;
                            if (cli.isBuffering) {
                                synchronized (cli.eofSongReachedLocker) {
                                    cli.eofSongReachedLocker.notify();
                                }
                            } else {
                                try {
                                    sendObjectOrDelete(cli, cli.dataSocket, new AudioPacket(-1, new byte[0]));
                                } catch (IOException ex) {
                                    closeClient(cli);
                                    ex.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        for (ClientSocketStructWrapper cli : clients) {
                            synchronized (cli.eofReceivedFromClientLocker) {
                                cli.eofReceivedFromClientLocker.wait(3000);
                            }
                        }


                    }
                    D.log("starting threads");
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                decoder.startPlay(currentFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (CannotReadException e) {
                                decoder.isPlaying.set(false);
                                isSongInPlay.set(false);

                                D.log("playerDecoder also Can not read file");
                            } catch (Exception e){
                                decoder.isPlaying.set(false);
                                isSongInPlay.set(false);
                            }
                        }
                    });

                    t.start();
                    Thread t2=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                try {
                                    decoderBufferer.startPlay(currentFile,currentSongId);
                                } catch (CannotReadException e) {
                                    isSongInPlay.set(false);
                                    decoderBufferer.eosReceived.set(true);
                                    ExceptionEvent.invoke(new EventArgs1<Exception>(self,e));
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }catch (Exception e){
                                isSongInPlay.set(false);
                                decoderBufferer.eosReceived.set(true);
                                e.printStackTrace();
                                ExceptionEvent.invoke(new EventArgs1<>(self,e));
                            }
                        }
                    });

                    t2.start();

                    isSongInPlay.set(true);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  yt.play("TW9d8vYrVFQ");
        }
    });

    t.start();
}

    AtomicBoolean swapSong=new AtomicBoolean(false);
    AtomicBoolean isSongInPlay=new AtomicBoolean(false);
    final Object locker=new Object();
    void closeClient(ClientSocketStructWrapper wrapper){
        try {
            clients.remove(wrapper);
            wrapper.dataSocket.socket.close();
            wrapper.dataSocket.objectOutputStream.close();
            wrapper.dataSocket.objectInputStream.close();

            wrapper.senderInfoSocket.socket.close();
            wrapper.senderInfoSocket.objectOutputStream.close();
            wrapper.senderInfoSocket.objectInputStream.close();

            wrapper.receiverInfoSocket.socket.close();
            wrapper.receiverInfoSocket.objectOutputStream.close();
            wrapper.receiverInfoSocket.objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initSockets(){
        try {

            if(senderServerSocket!=null){
                senderServerSocket.close();
            }
            if(receiverServerSocket!=null){
                receiverServerSocket.close();
            }
            if(rdataServerSocket!=null){
                rdataServerSocket.close();
            }
        } catch (IOException e) {
        e.printStackTrace();
    }
        try {

            senderServerSocket = new ServerSocket();
            senderServerSocket.setReuseAddress(true);
            senderServerSocket.bind(new InetSocketAddress(9050));

            receiverServerSocket = new ServerSocket();
            receiverServerSocket.setReuseAddress(true);
            receiverServerSocket.bind(new InetSocketAddress(9060));

            rdataServerSocket = new ServerSocket();
            rdataServerSocket.setReuseAddress(true);
            rdataServerSocket.bind(new InetSocketAddress(9070));


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        initSockets();
        acceptClients();
    }
ServerAudioMultiCastSocketThread self=this;
    public void sendAudioMeta(ClientSocketStructWrapper client) {
        AudioMetaDto dto =recentAudioMetaDto;
        dto.actualBufferedPackageNumber=decoder.actualPackageNumber.get();
        dto.port = currentClientPort;
        try {
        if(client.senderInfoSocket.objectInputStream!=null) {
            client.senderInfoSocket.objectOutputStream.writeObject(new ChannelObject(new AudioMetaBody(dto), TYPE.AUDIO_META));
            client.senderInfoSocket.objectOutputStream.flush();
        }
        } catch (IOException e) {
            closeClient(client);
            e.printStackTrace();
        }

    }





    int currentClientPort = 8100;
    private void listen(ClientSocketStructWrapper struct) {
        while (struct.senderInfoSocket.socket!=null&&!struct.receiverInfoSocket.socket.isClosed()) {
            try {
                ChannelObject inObject = (ChannelObject) struct.receiverInfoSocket.objectInputStream.readObject();
                D.log("got something");
               if(inObject.TYPE==TYPE.AUDIO_CONTROL_SERVER) {
                    AudioControlBody body = (AudioControlBody) inObject.body;
                    if(body.getContent().flag==AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE) {
                        D.log("recieved sync on server");

                            AudioControlBody body1 = new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE));
                        synchronized (decoder.actualPackageNumber) {
                            decoder.actualPackageNumber.wait();

                            body1.getContent().number = decoder.actualPackageNumber.get();
                        }

                            body1.getContent().timeInMilliSeconds=new Date().getTime()- struct.timeWhenConnected;
                            struct.senderInfoSocket.objectOutputStream.writeObject(new ChannelObject(body1,TYPE.AUDIO_CONTROL_CLIENT));
                            struct.senderInfoSocket.objectOutputStream.flush();
                            D.log("sync info  sent back");
                    }else if(body.getContent().flag==AUDIO_CONTROL.EOF_RECEIVED){
                        //client says, that he is received the eof package
                        D.log("eof package received.");
                        struct.eofSongReached=false;
                        synchronized (struct.eofReceivedFromClientLocker) {
                            struct.eofReceivedFromClientLocker.notify();
                        }
                        struct.isClientInStream=false;
                    }

                }else if(inObject.TYPE==TYPE.NET) {
                   NetworkEventBody body = (NetworkEventBody) inObject.body;
                   if(body.getContent()== NET_EVT.DISCONNECT){
                       clients.remove(struct);
                       struct.dataSocket.objectOutputStream.close();
                       struct.dataSocket.objectInputStream.close();
                       struct.dataSocket.socket.close();

                       struct.senderInfoSocket.objectOutputStream.close();
                       struct.senderInfoSocket.objectInputStream.close();
                       struct.senderInfoSocket.socket.close();

                       struct.receiverInfoSocket.objectOutputStream.close();
                       struct.receiverInfoSocket.objectInputStream.close();
                       struct.receiverInfoSocket.socket.close();
                       D.log("client "+body.senderAddress+" disconnected.");
                       break;
                   }
               }else
                {
                   D.log("ClientAudioThread: received wrong package");
               }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void acceptClients() {
        while (!receiverServerSocket.isClosed()) {
          //  initSockets();
            final ClientSocketStructWrapper newClient;
            newClient=new ClientSocketStructWrapper();
            try {

                D.log("accepting client 1");
                newClient.receiverInfoSocket.socket = receiverServerSocket.accept();

                newClient.receiverInfoSocket.objectOutputStream = new ObjectOutputStream(newClient.receiverInfoSocket.socket.getOutputStream());
                D.log("output k");
                newClient.receiverInfoSocket.objectInputStream = new ObjectInputStream(newClient.receiverInfoSocket.socket.getInputStream());
                D.log("input k");
                newClient.timeWhenConnected=new Date().getTime();


                D.log("accepting client 2");
                newClient.senderInfoSocket.socket = senderServerSocket.accept();

                newClient.senderInfoSocket.objectOutputStream = new ObjectOutputStream(newClient.senderInfoSocket.socket.getOutputStream());
                D.log("output k");
                newClient.senderInfoSocket.objectInputStream = new ObjectInputStream(newClient.senderInfoSocket.socket.getInputStream());
                D.log("input k");

                D.log("accepting client 3");
                newClient.dataSocket.socket = rdataServerSocket.accept();
                D.log("socket k");

                D.log("socket k");
                newClient.dataSocket.objectOutputStream=new ObjectOutputStream(newClient.dataSocket.socket.getOutputStream());
                D.log("output k");
                newClient.dataSocket.objectInputStream=new ObjectInputStream(newClient.dataSocket.socket.getInputStream());
                D.log("input k");
                //OUTPUTSTREAM FIRST
                clients.add(newClient);
                if(recentAudioMetaDto!=null) {

                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendAudioMeta(newClient);
                            sendAudioFromBuffer(newClient);
                        }
                    });
                    t.start();
                }

                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listen(newClient);
                    }
                });
                t.start();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                D.log("client added, NO ERRORS");

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }
    void send(SocketStruct struct,ChannelObject obj){
        try {
            struct.objectOutputStream.writeObject(obj);
            struct.objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendAll(ChannelObject obj){
        for(ClientSocketStructWrapper wrapper:clients){
            send(wrapper.senderInfoSocket,obj);
        }
    }
    void sendObjectOrDelete(ClientSocketStructWrapper wrapper,SocketStruct struct,Object obj) throws IOException {
        if(struct.socket!=null&&!struct.socket.isClosed()){
                struct.objectOutputStream.writeObject(obj);
                struct.objectOutputStream.flush();

        }else{
            clients.remove(wrapper);
        }
    }


    void sendAudioFromBuffer(ClientSocketStructWrapper struct) {
        struct.bufferItr = decoderBufferer.bufferQueue.iterator();

        // hasNext() returns true if the queue has more elements
        int liveplayPackageNumber = decoder.actualPackageNumber.get();
        D.log("LIVE PLAY PACK NUMBER." + liveplayPackageNumber);
        struct.bufferHeadPosition = 0;
        struct.isClientInStream=true;
        struct.eofSongReached=false;
        struct.isBuffering=true;
            while ((struct.bufferHeadPosition <= decoderBufferer.maxPackageNumber.get() || decoderBufferer.maxPackageNumber.get() == 0)) {
                if(struct.eofSongReached){
                    try {
                        struct.dataSocket.objectOutputStream.writeObject(new AudioPacket(-1,new byte[0]));
                        struct.dataSocket.objectOutputStream.flush();
                    } catch (IOException e) {

                        e.printStackTrace();
                        closeClient(struct);
                        break;
                    }

                    D.log("break");
                    break;
                }
                  //the buffered decoder is not finished yet
                if(decoderBufferer.maxPackageNumber.get()==0) {
                    synchronized (decoderBufferer.bufferQueue) {
                            try {
                             //   D.log("waiting for notify");
                                //decoderbufferer will notify us, when a new package is added to the queue
                                decoderBufferer.bufferQueue.wait(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                    }
                }
               // D.log("----");
                if(struct.bufferItr.hasNext()) {
                    AudioPacket packet = struct.bufferItr.next();
                    try {
                        struct.bufferHeadPosition = packet.packageNumber;
                        if(struct.bufferHeadPosition>=liveplayPackageNumber) {
                            sendObjectOrDelete(struct,struct.dataSocket,packet);
                            D.log("packet" + packet.packageNumber + " sent");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeClient(struct);
                        break;
                    }
                }else{
                    D.log("no next :(");
                }

            }
            struct.isBuffering=false;
       // }
    }



    private void subscribeDecoderEvents(){

        decoderBufferer.MetaDtoReadyEvent.addListener(new EventListener<EventArgs1<AudioMetaDto>>() {
            @Override
            public void action(EventArgs1<AudioMetaDto> args) {
                    recentAudioMetaDto = args.arg1();
                 D.log("sending meta to clients.");
                MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_CHANGED,args.arg1().songId)));
                MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_MAX_TIME_SECONDS,args.arg1().maxTimeInSeconds)));
                for (final ClientSocketStructWrapper tmpClient : clients) {
                    sendAudioMeta(tmpClient);
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            D.log("sending buffered audio to "+tmpClient.dataSocket.socket.getInetAddress().getHostAddress());
                            sendAudioFromBuffer(tmpClient);
                        }
                    });
                    t.start();
                }
            }
        });

    }

//GETTER & SETTER
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

    public void shutdown() {
        externalShutdown=true;
        try {
            decoder.stop();
            decoderBufferer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(receiverServerSocket !=null) {
            try {
                receiverServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();e.printStackTrace();
            }
        }
        for(ClientSocketStructWrapper ds: clients){
            if(ds!=null){
                try {
                    ds.receiverInfoSocket.objectOutputStream.close();
                    ds.receiverInfoSocket.objectInputStream.close();

                    ds.receiverInfoSocket.socket.close();

                    ds.dataSocket.objectOutputStream.close();
                    ds.dataSocket.objectInputStream.close();
                    ds.dataSocket.socket.close();

                    ds.senderInfoSocket.objectOutputStream.close();
                    ds.senderInfoSocket.objectInputStream.close();
                    ds.senderInfoSocket.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    File getFileByResId(int id, String targetFileName) {
        // D.log(path);
        InputStream fis = context.getResources().openRawResource(id);

        File file = new File(context.getFilesDir(), targetFileName);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            copy(fis, outputStream);
            D.log("file readed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // handle exception here
        } catch (IOException e) {
            e.printStackTrace();
            // handle exception here
        }
        return file;
    }

    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
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
