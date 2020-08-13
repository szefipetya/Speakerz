package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.media.MediaMetadataRetriever;

import android.os.Build;
import android.os.Environment;

import com.google.common.collect.ImmutableSet;
import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.network.threads.audio.util.AUDIO;
import com.speakerz.model.network.threads.audio.util.AudioDecoderThread;
import com.speakerz.model.network.threads.audio.util.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.AudioMetaInfo;
import com.speakerz.model.network.threads.audio.util.YouTubeStreamAPI;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.io.BufferedInputStream;
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
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.lang3.SerializationUtils;



public class ServerAudioMultiCastSocketThread extends Thread {

    public void playAudioStreamFromLocalStorage(final File file){
        decoder.stop();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    decoder.startPlay(file, AUDIO.MP3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //  yt.play("TW9d8vYrVFQ");
            }
        });
        t.start();
    }

    public void stopAudioStream() {
        decoder.stop();
    }

    public void pauseAudioStream() {

        decoder.isPlaying=false;
    }

    public void resumeAudioStream() {
        decoder.isPlaying=true;
    }

    private class ClientDatagramStruct {
        public ClientDatagramStruct(DatagramSocket socket, InetAddress address, int port) {
            this.address = address;
            this.clientPort = port;

            this.socket = socket;

        }

        public DatagramSocket socket;
        public InetAddress address;
        public int clientPort;
    }

   // File currentMediaFile;
    private final List<ClientDatagramStruct> clients = Collections.synchronizedList(new LinkedList<ClientDatagramStruct>());
    private AudioTrack track;
    AudioMetaDto recentAudioMetaDto=null;
    private FileOutputStream os;
    AudioDecoderThread decoder=new AudioDecoderThread();
    YouTubeStreamAPI yt=new YouTubeStreamAPI();


    private void init(){
        subscribeDecoderEvents();
    }





    public void run() {
        // playWav();
       // currentMediaFile = getFileByResId(R.raw.tobu_wav, "target.wav");
        init();



        acceptClients();
    }

    public void sendAudioMeta(ClientDatagramStruct client) {

        recentAudioMetaDto.port = currentClientPort;
        byte[] dtoBytes = null;
        dtoBytes = SerializationUtils.serialize(recentAudioMetaDto);

        DatagramPacket dtoDp = new DatagramPacket(dtoBytes, dtoBytes.length, client.address,8050);
        try {
            recieverSocket.send(dtoDp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    ///
    private InetAddress address;

    private DatagramSocket recieverSocket;
    private boolean running;
    private byte[] buf = new byte[256];
    private Context context;


    public ServerAudioMultiCastSocketThread() {
        try {
            recieverSocket = new DatagramSocket(8050);
        } catch (SocketException e) {
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

    public void acceptClients() {

        while (true) {
            D.log("acccepting UDP clients...");
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                recieverSocket.receive(packet);
                D.log("recieved (server)"+packet.getAddress().getHostAddress()+ " "+packet.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ClientDatagramStruct newClient = null;
            try {
                //on the server side the client gets a designated port number.
                currentClientPort++;
                newClient = new ClientDatagramStruct(new DatagramSocket(currentClientPort), packet.getAddress(), currentClientPort);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            D.log("sening meta to " + newClient.address.getHostAddress());
            //the new clients recieve the meta info from the server
            //and their designated port number to listen on for the audio data

            //send audiodto if something is in play
            if(recentAudioMetaDto!=null){
                sendAudioMeta(newClient);
                waitForClientResponse(newClient);
            }
            synchronized (clients) {
                clients.add(newClient);
            }

        }

    }

    private void waitForClientResponse(ClientDatagramStruct newClient) {
        D.log("acccepting UDP clients...");
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length);
        try {
            newClient.socket.receive(packet);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            D.log("recieved" + address.getHostAddress() + " :" + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void subscribeDecoderEvents(){
        decoder.MetaDtoReadyEvent.addListener(new EventListener<EventArgs1<AudioMetaDto>>() {
            @Override
            public void action(EventArgs1<AudioMetaDto> args) {
                synchronized (clients){
                    recentAudioMetaDto = args.arg1();
                    //sending tha packet to all the clients
                    Iterator it = clients.iterator();
                    while (it.hasNext()) {
                        ClientDatagramStruct tmpClient = (ClientDatagramStruct) it.next();
                        sendAudioMeta(tmpClient);
                        waitForClientResponse(tmpClient);
                        //  D.log("Packet:" + i + " sent to" + tmpClient.address.getHostAddress() + ":" + tmpClient.clientPort);
                    }
                }

                }

        });
        decoder.AudioTrackBufferUpdateEvent.addListener(new EventListener<EventArgs2<byte[], Integer>>() {
            @Override
            public void action(EventArgs2<byte[], Integer> args) {
                synchronized (clients) {
                    //sending tha packet to all the clients
                    Iterator it = clients.iterator();
                    DatagramPacket dp=null;
                    while (it.hasNext()) {
                        ClientDatagramStruct tmpClient = (ClientDatagramStruct) it.next();
                        dp = new DatagramPacket(args.arg1(), args.arg1().length, tmpClient.address, tmpClient.clientPort);
                        try {
                            tmpClient.socket.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                      //  D.log("Packet:" + i + " sent to" + tmpClient.address.getHostAddress() + ":" + tmpClient.clientPort);
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