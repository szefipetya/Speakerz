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
import com.speakerz.model.network.threads.audio.util.StreamUtil;

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
import java.util.stream.Stream;


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


    private AudioTrack track;
    private FileOutputStream os;
    AudioDecoderThread decoder=new AudioDecoderThread();

    public void run() {
       // playWav();

       // decoder.startPlay(getFileByResId(R.raw.passion_aac,"passion.aac").getAbsolutePath());

        //   acceptClients();
    }


    public AudioMetaDto getAudioMetaDtoFromFile(File file){
        AudioMetaInfo info=new AudioMetaInfo(file);
        AudioMetaDto dto=new AudioMetaDto();
        dto.bitsPerSample= (short) info.getAudioHeader().getBitsPerSample();
        dto.channels= (short) info.getAudioHeader().getChannelCount();
        dto.bitrate= (short) info.getAudioHeader().getBitRate();
        dto.sampleRate=info.getAudioHeader().getSampleRate();
        return dto;
    }





    public void streamAudio(InetAddress clientAdress,Integer clientPort) throws IOException, InterruptedException {
        File file=getFileByResId(R.raw.passion_aac,"target");
        //first, send the metadata from the audio
        {
          //  AudioMetaDto dto = getAudioMetaDtoFromFile(file);
            //byte[] dtoBytes=null;


             //  dtoBytes = StreamUtil.encode(dto);
             //   dtoBytes=SerializationUtils.serialize(dto);

           // DatagramPacket dtoDp = new DatagramPacket(dtoBytes, dtoBytes.length, clientAdress, clientPort);
            //socket.send(dtoDp);
        }



        D.log("stream started");
        BufferedInputStream bis = null;
        AudioTrack at= createAudioTrack(file);



        try {

            DatagramPacket dp;
            int bufferSize = 1024;

            byte[] buffer = new byte[bufferSize];

            int i=0;
                FileInputStream fin = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fin);

                at.play();
                while((i = dis.read(buffer, 0, bufferSize)) > -1){
                    at.write(buffer, 0, i);
                 //   dp = new DatagramPacket(buffer, buffer.length,clientAdress, clientPort);
                 //   socket.send(dp);
                   // D.log("pack sent");
                }

                i++;
                //  System.out.println("Packet:" + (i + 1));
                D.log("Packet:" + (i + 1));


                D.log("-");

            //data end
        }finally {
            if(bis!=null)
                bis.close();
            if(socket !=null)
                socket.close();
        }
    }


    private AudioTrack createAudioTrack(File file){
     //   AudioMetaInfo metaInfo=new AudioMetaInfo(file);
        //
        ///play wav




        int minBufferSize = AudioTrack.getMinBufferSize(44100,
               AudioFormat.CHANNEL_CONFIGURATION_STEREO,AudioFormat.ENCODING_AAC_LC);
        int bufferSize = 512;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_CONFIGURATION_STEREO,AudioFormat.ENCODING_AAC_LC, minBufferSize, AudioTrack.MODE_STREAM);

        return at;
    }



    ///
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

    File getFileByResId(int id,String targetFileName){
        // D.log(path);
        InputStream fis=context.getResources().openRawResource(id);

        File file = new File(context.getFilesDir(),targetFileName);
        try(OutputStream outputStream = new FileOutputStream(file)){
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

    public void acceptClients(){

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
            D.log("client exited: "+address.getHostAddress());
        }else{
            try {
                streamAudio(address,port);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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