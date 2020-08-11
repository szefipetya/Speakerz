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
import com.speakerz.model.network.threads.audio.util.AudioDecoderThread;
import com.speakerz.model.network.threads.audio.util.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.AudioMetaInfo;

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


import android.media.AudioFormat;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.SerializationUtils;

import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.AudioHeader;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.NullTag;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.TagOptionSingleton;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;


public class ServerAudioMultiCastSocketThread extends Thread {
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


    private final List<ClientDatagramStruct> clients = Collections.synchronizedList(new LinkedList<ClientDatagramStruct>());
    private AudioTrack track;
    private FileOutputStream os;
    AudioDecoderThread decoder=new AudioDecoderThread();

    public void run() {
        // playWav();
        currentMediaFile = getFileByResId(R.raw.tobu_wav, "target.wav");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    streamAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        decoder.startPlay(getFileByResId(R.raw.videoplayback,"passion.aac").getAbsolutePath());

        //   acceptClients();
    }


    public AudioMetaDto getAudioMetaDtoFromFile(File file) {
        AudioMetaInfo info = new AudioMetaInfo(file);
        AudioMetaDto dto = new AudioMetaDto();
        dto.bitsPerSample = (short) info.getAudioHeader().getBitsPerSample();
        dto.channels = (short) info.getAudioHeader().getChannelCount();
        dto.bitrate = (short) info.getAudioHeader().getBitRate();
        dto.sampleRate = info.getAudioHeader().getSampleRate();
        return dto;
    }

    public void sendAudioMeta(ClientDatagramStruct client) {
        AudioMetaDto dto = getAudioMetaDtoFromFile(currentMediaFile);
        dto.port = currentClientPort;
        byte[] dtoBytes = null;
        dtoBytes = SerializationUtils.serialize(dto);

        DatagramPacket dtoDp = new DatagramPacket(dtoBytes, dtoBytes.length, client.address,8050);
        try {
            recieverSocket.send(dtoDp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void streamAudio() throws IOException, InterruptedException {
        D.log("stream started");
        BufferedInputStream bis = null;
        AudioTrack at = createAudioTrack(currentMediaFile);
        try {

            DatagramPacket dp;
            int bufferSize = 1024;

            byte[] buffer = new byte[bufferSize];

            int i = 0;


            FileInputStream fin = new FileInputStream(currentMediaFile);
            DataInputStream dis = new DataInputStream(fin);

            at.play();
            while ((i = dis.read(buffer, 0, bufferSize)) > -1) {
                at.write(buffer, 0, i);
                synchronized (clients) {
                    //sending tha packet to all the clients
                    Iterator it = clients.iterator();
                    while (it.hasNext()) {
                        ClientDatagramStruct tmpClient = (ClientDatagramStruct) it.next();
                        dp = new DatagramPacket(buffer, buffer.length, tmpClient.address, tmpClient.clientPort);
                        tmpClient.socket.send(dp);
                        D.log("Packet:" + i + " sent to" + tmpClient.address.getHostAddress() + ":" + tmpClient.clientPort);
                    }
                }
                // D.log("pack sent");
            }
            i++;
            //  System.out.println("Packet:" + (i + 1));
            D.log("Packet:" + (i + 1));

            //data end
        } finally {
            if (bis != null)
                bis.close();
            if (recieverSocket != null)
                recieverSocket.close();
        }
    }

    private AudioTrack createAudioTrack(File file) {
        AudioMetaInfo metaInfo = new AudioMetaInfo(file);
        //
        ///play wav

        int minBufferSize = AudioTrack.getMinBufferSize(metaInfo.getAudioHeader().getSampleRate(),
                metaInfo.getAudioHeader().getChannelCount() == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaInfo.getAudioHeader().getBitsPerSample() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);
        int bufferSize = 512;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, metaInfo.getAudioHeader().getSampleRate(),
                metaInfo.getAudioHeader().getChannelCount() == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaInfo.getAudioHeader().getBitsPerSample() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);

        return at;
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

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            ClientDatagramStruct newClient = null;
            try {
                //on the server side the client gets a designated port number.
                currentClientPort++;
                newClient = new ClientDatagramStruct(new DatagramSocket(currentClientPort), address, currentClientPort);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            D.log("sening meta to " + newClient.address.getHostAddress());
            //the new clients recieve the meta info from the server
            //and their designated port number to listen on for the audio data
            sendAudioMeta(newClient);
            waitForClientResponse(newClient);
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