package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioTrack;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.audio.AudioControlBody;
import com.speakerz.model.network.Serializable.body.audio.AudioMetaBody;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO_CONTROL;
import com.speakerz.model.network.Serializable.body.audio.content.AudioControlDto;
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

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.lang3.SerializationUtils;



public class ServerAudioMultiCastSocketThread extends Thread {

    public void playAudioStreamFromLocalStorage(final File file){

        synchronized(locker) {
            decoder.stop();
           // decoder.getAudioTrack().stop();
          //  decoder.getAudioTrack().release();

            D.log("SONG PLAYING");
            songpicked=true;
            currentFile=file;
            locker.notify();
        }
    }
    File currentFile=null;

    public void stopAudioStream() {
        decoder.stop();
    }

    public void pauseAudioStream() {

        decoder.isPlaying=false;
    }

    public void resumeAudioStream() {
        decoder.isPlaying=true;
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
        decoderBufferer =new AudioBuffererDecoder();
        AudioTrackBufferUpdateEvent=new Event<>();
        MetaDtoReadyEvent=new Event<>();
        decoderBufferer.AudioTrackBufferUpdateEvent=AudioTrackBufferUpdateEvent;
        decoderBufferer.MetaDtoReadyEvent=MetaDtoReadyEvent;

        subscribeDecoderEvents();
    }


    Boolean songpicked=false;
    final Object locker=new Object();
    public void run() {
        // playWav();
       // currentMediaFile = getFileByResId(R.raw.tobu_wav, "target.wav");
        init();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                        while (!receiverSocket.isClosed()) {
                            synchronized(locker) {
                            D.log("waiting for audio pick.");
                            while (!songpicked) {
                                locker.wait();
                            }
                            songpicked = false;

                                //getFileByResId(R.raw.tobu_wav,"target.wav")
                        }
                            Thread t=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        decoder.startPlay(currentFile, AUDIO.MP3, DECODER_MODE.PLAY);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            t.start();
                            Thread t2=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        decoderBufferer.startPlay(currentFile, AUDIO.MP3,DECODER_MODE.STREAM);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            t2.start();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //  yt.play("TW9d8vYrVFQ");
            }
        });

        t.start();
        acceptClients();
    }

    public void sendAudioMeta(ClientSocketStructWrapper client) {
        AudioMetaDto dto = decoder.getAudioMeta();
        dto.port = currentClientPort;
        byte[] dtoBytes = null;
        dtoBytes = SerializationUtils.serialize(dto);

        try {
            ObjectOutputStream out=new ObjectOutputStream(client.infoSocket.socket.getOutputStream());
          out.writeObject(
                    new ChannelObject(new AudioMetaBody(dto), TYPE.AUDIO_META)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    ///
    private InetAddress address;

    private ServerSocket receiverSocket;
    private ServerSocket dataSocket;

    private boolean running;
    private byte[] buf = new byte[256];
    private Context context;


    public ServerAudioMultiCastSocketThread() {

        try {
            receiverSocket = new ServerSocket();
            receiverSocket.setReuseAddress(true);
            receiverSocket.bind(new InetSocketAddress(address,9050));

            dataSocket = new ServerSocket();
            dataSocket.setReuseAddress(true);
            dataSocket.bind(new InetSocketAddress(address,9060));


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    int currentClientPort = 8100;
    private void listen(SocketStruct struct) {
        while (!struct.socket.isClosed()) {
            try {
                ChannelObject inObject = (ChannelObject) struct.objectInputStream.readObject();
               if(inObject.TYPE==TYPE.AUDIO_CONTROL) {
                    AudioControlBody body = (AudioControlBody) inObject.body;
                    if(body.getContent().flag==AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE) {
                        D.log("recieved packet");
                        if (body.getContent().flag == AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE) {
                            AudioControlBody body1 = new AudioControlBody(new AudioControlDto(AUDIO_CONTROL.SYNC_ACTUAL_PACKAGE));
                            body1.getContent().number=decoder.actualPackageNumber.get();
                            struct.objectOutputStream.writeObject(new ChannelObject(body1,TYPE.AUDIO_CONTROL));
                        }
                    }

                }
                D.log("ClientAudioThread: received wrong package");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    public void acceptClients() {



        while (!receiverSocket.isClosed()) {
            final ClientSocketStructWrapper newClient=new ClientSocketStructWrapper();
            try {

                D.log("accepting client 1");
                receiverSocket.setSoTimeout(1000000);
                newClient.infoSocket.socket = receiverSocket.accept();


                D.log("accepting client 2");
                dataSocket.setSoTimeout(1000000);
                newClient.dataSocket.socket = dataSocket.accept();
                clients.add(newClient);
                D.log("yeeeeey");
                newClient.dataSocket.objectOutputStream=new ObjectOutputStream(newClient.dataSocket.socket.getOutputStream());
                newClient.infoSocket.objectInputStream = new ObjectInputStream(newClient.infoSocket.socket.getInputStream());
                newClient.infoSocket.objectOutputStream = new ObjectOutputStream(newClient.infoSocket.socket.getOutputStream());


                D.log("client added!!!!");
            } catch (IOException e) {
                e.printStackTrace();
            }


            //the new clients recieve the meta info from the server
            //and their designated port number to listen on for the audio data
            /*try {
                newClient.socket.setSoTimeout(4000);
            } catch (SocketException e) {
                e.printStackTrace();
            }*/

            if(recentAudioMetaDto!=null)
            synchronized (clients) {
                D.log("client added");

                sendAudioMeta(newClient);
            }
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    listen(newClient.infoSocket);
                }
            });
            t.start();


        }

    }




    private void subscribeDecoderEvents(){

        decoderBufferer.MetaDtoReadyEvent.addListener(new EventListener<EventArgs1<AudioMetaDto>>() {
            @Override
            public void action(EventArgs1<AudioMetaDto> args) {
                    recentAudioMetaDto = args.arg1();
                    //sending tha packet to all the clients
                for (ClientSocketStructWrapper tmpClient : clients) {
                    sendAudioMeta(tmpClient);
                    //  D.log("Packet:" + i + " sent to" + tmpClient.address.getHostAddress() + ":" + tmpClient.clientPort);
                }


                }

        });
        decoderBufferer.AudioTrackBufferUpdateEvent.addListener(new EventListener<EventArgs1<AudioPacket>>() {
            @Override
            public void action(EventArgs1<AudioPacket> args) {

                   // D.log("event happened.");
                    //sending tha packet to all the clients
                for (ClientSocketStructWrapper tmpClient : clients) {
                //    D.log("client" + "event");

                    if (!tmpClient.dataSocket.socket.isClosed()) {
                        try {
                          //  D.log("" + "sent" + args.arg1().packageNumber);
                            tmpClient.dataSocket.objectOutputStream.writeObject(args.arg1());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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
        decoder.stop();
        if(receiverSocket !=null) {
            try {
                receiverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();e.printStackTrace();
            }
        }
        for(ClientSocketStructWrapper ds: clients){
            if(ds!=null){
                try {
                    ds.infoSocket.socket.close();
                    ds.dataSocket.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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