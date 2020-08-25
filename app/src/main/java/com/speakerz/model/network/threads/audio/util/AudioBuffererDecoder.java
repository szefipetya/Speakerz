package com.speakerz.model.network.threads.audio.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class AudioBuffererDecoder {
    private static final int TIMEOUT_US = 1000;
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    private AtomicBoolean eosReceived=new AtomicBoolean(false);
    private int mSampleRate = 0;
    public Event<EventArgs1<AudioPacket>> AudioTrackBufferUpdateEvent;
    public Event<EventArgs1<AudioMetaDto>> MetaDtoReadyEvent;

    public final Queue<AudioPacket> bufferQueue=new ConcurrentLinkedQueue<>();

    AudioBuffererDecoder self=this;

    File currentFile=null;
    AudioMetaDto metaDto=new AudioMetaDto();
    public void startPlay(String path, AUDIO audioType, DECODER_MODE mode) throws IOException {
        currentFile=new File(path);
        startPlay(currentFile,audioType,mode);
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    AudioTrack audioTrack=null;
    AUDIO audioType=AUDIO.NONE;
    public void startPlay(File file, AUDIO audioType,DECODER_MODE mode) throws IOException {
        this.audioType=audioType;
        eosReceived.set(false);
        currentFile=file;


        if(audioType==AUDIO.MP3){
            playMP3(file,mode);
        }

    }

public AtomicInteger actualBufferedPackageNumber=new AtomicInteger(0);
public AtomicInteger maxPackageNumber=new AtomicInteger(0);

    private void playMP3(File file,DECODER_MODE mode) throws IOException {
        actualBufferedPackageNumber.set(0);
        maxPackageNumber.set(0);
        eosReceived.set(false);
        // Create a jlayer Decoder instance.
        D.log("PLAYING MP3 ");
        Decoder decoder = new Decoder();

        // Create a jlayer BitStream instance of a given mp3 source.

        AudioMetaInfo metaInfo=new AudioMetaInfo(file) ;
        metaDto.sampleRate=metaInfo.getAudioHeader().getSampleRate();
        metaDto.channels=(short)metaInfo.getAudioHeader().getChannelCount();
        metaDto.bitrate=(short)metaInfo.getAudioHeader().getBitRate();
        metaDto.bitsPerSample=(short)metaInfo.getAudioHeader().getBitsPerSample();
        InputStream mp3Source =new FileInputStream(file);
        Bitstream bitStream = new Bitstream(mp3Source);

        final int READ_THRESHOLD = 2147483647;

        Header frame = null;
        int framesReaded = 0;

        boolean l=false;
        while (!eosReceived.get()) {
            try {
                if (!(framesReaded++ <= READ_THRESHOLD && (frame = bitStream.readFrame()) != null)){
                    D.log("readed tha whole music");
                    D.log("max pack size: "+actualBufferedPackageNumber.get());
                 maxPackageNumber.set(actualBufferedPackageNumber.get());
                    synchronized (bufferQueue){
                        bufferQueue.notify();
                    }
                    break;
                }

            } catch (BitstreamException e) {
                e.printStackTrace();
            }
            SampleBuffer sampleBuffer = null;
            try {
                sampleBuffer = (SampleBuffer) decoder.decodeFrame(frame, bitStream);

            } catch (DecoderException e) {
                e.printStackTrace();
            }
            short[] pcmChunk = sampleBuffer.getBuffer();
            ByteBuffer buffer = ByteBuffer.allocate(pcmChunk.length * 2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.asShortBuffer().put(pcmChunk);

            byte[] bytes = buffer.array();

                if(!l){
                    l=true;
                    metaDto.packageSize=bytes.length;
                    MetaDtoReadyEvent.invoke(new EventArgs1<>(self,metaDto));
                    D.log("converted size: "+String.valueOf(metaDto.packageSize));
                    D.log("original package size: "+ String.valueOf(bytes.length));
                }

                AudioPacket pack=new AudioPacket(bytes.length,bytes);
                pack.packageNumber=actualBufferedPackageNumber.get();
                bufferQueue.add(pack);
                synchronized (bufferQueue){
                    bufferQueue.notify();
                }

            //  AudioTrackBufferUpdateEvent.invoke(new EventArgs1<>(self,pack));
             //   D.log("Event sent."+i);

            actualBufferedPackageNumber.addAndGet(1);

            bitStream.closeFrame();
        }
            if(eosReceived.get()){
                synchronized (playStoppedLocker) {
                    playStoppedLocker.notify();
                }
            }

    }

   final public Object playStoppedLocker=new Object();

    public void stop() throws InterruptedException {
        eosReceived.set(true);
        //if it's zero it means, that the file is not yet readed.
        if(maxPackageNumber.get()==0) {
            synchronized (playStoppedLocker) {
                D.log("waiting for buffererdecoder to stop");
                playStoppedLocker.wait(500);
                D.log(" buffererdecoder stopped");

            }
        }
        actualBufferedPackageNumber.set(0);
        maxPackageNumber.set(0);
        bufferQueue.clear();

    }
}
