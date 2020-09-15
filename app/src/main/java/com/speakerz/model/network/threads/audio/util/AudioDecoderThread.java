package com.speakerz.model.network.threads.audio.util;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * https://github.com/taehwandev/MediaCodecExample/blob/master/src/net/thdev/mediacodecexample/decoder/AudioDecoderThread.java
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.audio.content.AUDIO;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.ThreadSafeEvent;

import ealvatag.audio.exceptions.CannotReadException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import static android.os.FileUtils.copy;


/**
 *
 * @author taehwan
 *
 */
public class AudioDecoderThread {
    private static final int TIMEOUT_US = 1000;
    public final AtomicBoolean isPaused=new AtomicBoolean(false);
    public ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent;
    public AtomicBoolean getSyncPackage=new AtomicBoolean(false);
    public final Object getSyncPackageLocker=new Object();
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    private volatile boolean eosReceived;
    private int mSampleRate = 0;
   // public Event<EventArgs1<AudioPacket>> AudioTrackBufferUpdateEvent=  new Event<>();
    //public Event<EventArgs1<AudioMetaDto>> MetaDtoReadyEvent =new Event<>();


    AudioMetaDto metaDto=new AudioMetaDto();
    public void startPlay(String path) throws IOException, CannotReadException,NullPointerException {
        currentFile=new File(path);
       startPlay(currentFile);
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    AudioTrack audioTrack=null;
    AUDIO audioType=AUDIO.NONE;
    public void startPlay(File file) throws IOException, CannotReadException,NullPointerException {
        this.audioType=audioType;
        eosReceived = false;
        currentFile=file;
        eosReceived=false;
        actualPackageNumber.set(0);
        AudioMetaInfo metaInfo=new AudioMetaInfo(file) ;
        if(metaInfo.getAudioHeader()==null) throw new NullPointerException("Playback from client resource is not yet implemented");
        if(metaInfo.getAudioHeader().getEncodingType()=="mp3") {
            playMP3(file);
        }
    }


   public final AtomicInteger actualPackageNumber=new AtomicInteger(0);
    public AtomicBoolean isPlaying=new AtomicBoolean(false);



    private void playMP3(File file) throws IOException, CannotReadException {
       // Create a jlayer Decoder instance.
        D.log("PLAYING MP3 ");
        Decoder decoder = new Decoder();

       // Create a jlayer BitStream instance of a given mp3 source.
        AudioMetaInfo metaInfo=new AudioMetaInfo(file) ;
        metaDto.sampleRate=metaInfo.getAudioHeader().getSampleRate();
        metaDto.channels=(short)metaInfo.getAudioHeader().getChannelCount();
        metaDto.bitsPerSample=(short)metaInfo.getAudioHeader().getBitsPerSample();
        InputStream mp3Source =new FileInputStream(file);
        Bitstream bitStream = new Bitstream(mp3Source);

        isPlaying.set(true);
       // Create an AudioTrack instance.
        final int minBufferSize = AudioTrack.getMinBufferSize( metaDto.sampleRate,
               TransformAF.channel(metaDto.channels),
                AudioFormat.ENCODING_PCM_16BIT);


         audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                metaDto.sampleRate,
                TransformAF.channel(metaDto.channels),
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);

      //  Decode the mp3 BitStream data by Decoder and feed the outcoming PCM chunks to AudioTrack.
        audioTrack.play();
        byte[] bytes;
        short[] pcmChunk;
        final int READ_THRESHOLD = 2147483647;
        Header frame = null;
        int framesReaded = 0;
        while (!eosReceived) {
            if(isPaused.get()){
                synchronized (isPaused) {
                    try {
                        isPaused.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
                     try {
                if (!(framesReaded++ <= READ_THRESHOLD && (frame = bitStream.readFrame()) != null)){
                    //TODO: Ez nem érkezik meg a musicplayermodelbe
                    MusicPlayerActionEvent.invoke(new EventArgs1<Body>(self,new MusicPlayerActionBody(MP_EVT.SONG_NEXT,null)));
                    D.log("played tha whole music");
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
            pcmChunk = sampleBuffer.getBuffer();
            ByteBuffer buffer = ByteBuffer.allocate(pcmChunk.length * 2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.asShortBuffer().put(pcmChunk);
            bytes = buffer.array();
            audioTrack.write(bytes, 0, bytes.length);
                synchronized (actualPackageNumber){
                  actualPackageNumber.notify();
                }
            actualPackageNumber.addAndGet(1);


            Method cleanerMethod = null;
            buffer.clear();
            bitStream.closeFrame();
        }

        isPlaying.set(false);

        synchronized (playStoppedLocker) {
            playStoppedLocker.notify();
        }
        //TODO: Ez nem érkezik meg a musicplayermodelbe
        MusicPlayerActionEvent.invoke(new EventArgs1<Body>("",new MusicPlayerActionBody(MP_EVT.SONG_EOF,null)));

    }

    public void stop() throws InterruptedException {
        eosReceived = true;
        if(isPlaying.get()) {

            synchronized (playStoppedLocker) {
                D.log("waiting for player to stop");
                playStoppedLocker.wait(100);
                D.log(" player stopped");

            }
            audioTrack.stop();
            audioTrack.release();
        }

    }

final public Object playStoppedLocker=new Object();

    File currentFile=null;
    public AudioMetaDto getAudioMeta(){
          return metaDto;
    }

    private void playWAV(File file) throws CannotReadException {
        D.log("stream started");
        BufferedInputStream bis = null;
         audioTrack = createWavAudioTrack(file);
        try {

            DatagramPacket dp;
            int bufferSize = 1024;

            byte[] buffer = new byte[bufferSize];

            int i = 0;


            FileInputStream fin = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fin);

            audioTrack.play();
          //  MetaDtoReadyEvent.invoke(new EventArgs1<AudioMetaDto>(self,metaDto));
            while ((i = dis.read(buffer, 0, bufferSize)) > -1) {
                audioTrack.write(buffer, 0, i);
                AudioPacket pack=new AudioPacket(i,buffer);

              //  AudioTrackBufferUpdateEvent.invoke(new EventArgs1<AudioPacket>( self,pack));
                // D.log("pack sent");
            }
            i++;
            //  System.out.println("Packet:" + (i + 1));
            D.log("Packet:" + (i + 1));

            //data end
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public AudioMetaDto getAudioMetaDtoFromWavFile(File file) throws CannotReadException {
        AudioMetaInfo info = new AudioMetaInfo(file);
        AudioMetaDto dto = new AudioMetaDto();
        dto.bitsPerSample = (short) info.getAudioHeader().getBitsPerSample();
        dto.channels = (short) info.getAudioHeader().getChannelCount();
        dto.sampleRate = info.getAudioHeader().getSampleRate();
        dto.packageSize=1024;
        return dto;
    }

    private AudioTrack createWavAudioTrack(File file) throws CannotReadException {
        AudioMetaInfo metaInfo = new AudioMetaInfo(file);
        //
        ///play wav

        metaDto=getAudioMetaDtoFromWavFile(file);

        int minBufferSize = AudioTrack.getMinBufferSize(metaInfo.getAudioHeader().getSampleRate(),
                metaInfo.getAudioHeader().getChannelCount() == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaInfo.getAudioHeader().getBitsPerSample() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);
        int bufferSize = 512;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, metaInfo.getAudioHeader().getSampleRate(),
                metaInfo.getAudioHeader().getChannelCount() == 2 ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO,
                metaInfo.getAudioHeader().getBitsPerSample() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);

        return at;
    }

    /////
    private void playM4A_AAC(String path){


        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int channel = 0;
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mExtractor.selectTrack(i);
                Log.d("TAG", "format : " + format);
                ByteBuffer csd = format.getByteBuffer("csd-0");

                for (int k = 0; k < csd.capacity(); ++k) {
                    Log.e("TAG", "csd : " + csd.array()[k]);
                }
                mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                metaDto.sampleRate=mSampleRate;
                channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                metaDto.channels=(short)channel;
                break;
            }
        }
        MediaFormat format = makeAACCodecSpecificData(MediaCodecInfo.CodecProfileLevel.AACObjectLC, mSampleRate, channel);
        if (format == null)
            return;

        try {
            mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDecoder.configure(format, null, null, 0);

        if (mDecoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        mDecoder.start();

        new Thread(AACDecoderAndPlayRunnable).start();
    }
    /**
     * The code profile, Sample rate, channel Count is used to
     * produce the AAC Codec SpecificData.
     * Android 4.4.2/frameworks/av/media/libstagefright/avc_utils.cpp refer
     * to the portion of the code written.
     *
     * MPEG-4 Audio refer : http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio#Audio_Specific_Config
     *
     * @param audioProfile is MPEG-4 Audio Object Types
     * @param sampleRate
     * @param channelConfig
     * @return MediaFormat
     */
    private MediaFormat makeAACCodecSpecificData(int audioProfile, int sampleRate, int channelConfig) {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelConfig);

        int samplingFreq[] = {
                96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000
        };

        // Search the Sampling Frequencies
        int sampleIndex = -1;
        for (int i = 0; i < samplingFreq.length; ++i) {
            if (samplingFreq[i] == sampleRate) {
                Log.d("TAG", "kSamplingFreq " + samplingFreq[i] + " i : " + i);
                sampleIndex = i;
            }
        }

        if (sampleIndex == -1) {
            return null;
        }

        ByteBuffer csd = ByteBuffer.allocate(2);
        csd.put((byte) ((audioProfile << 3) | (sampleIndex >> 1)));

        csd.position(1);
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelConfig << 3)));
        csd.flip();
        format.setByteBuffer("csd-0", csd); // add csd-0

        for (int k = 0; k < csd.capacity(); ++k) {
            Log.e("TAG", "csd : " + csd.array()[k]);
        }

        return format;
    }

    Runnable AACDecoderAndPlayRunnable = new Runnable() {

        @Override
        public void run() {
            AACDecoderAndPlay();
        }
    };

    /**
     * After decoding AAC, Play using Audio Track.
     */
    AudioDecoderThread self=this;
    public void AACDecoderAndPlay() {
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();

        BufferInfo info = new BufferInfo();

        int buffsize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        // create an audiotrack object
        D.log("buffsize"+buffsize);
        metaDto.bitsPerSample=16;
        metaDto.sampleRate=mSampleRate;
        metaDto.packageSize=info.size ==0?4096:info.size;
      //  MetaDtoReadyEvent.invoke(new EventArgs1<AudioMetaDto>(self,metaDto));

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);
        audioTrack.play();

        while (!eosReceived) {

            int inIndex = mDecoder.dequeueInputBuffer(TIMEOUT_US);
            if (inIndex >= 0) {
                ByteBuffer buffer = inputBuffers[inIndex];
                int sampleSize = mExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    // We shouldn't stop the playback at this point, just pass the EOS
                    // flag to mDecoder, we will get it again from the
                    // dequeueOutputBuffer
                    Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                    mDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                } else {
                    mDecoder.queueInputBuffer(inIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
                    mExtractor.advance();
                }

                int outIndex = mDecoder.dequeueOutputBuffer(info, TIMEOUT_US);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = mDecoder.getOutputBuffers();
                        break;

                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        MediaFormat format = mDecoder.getOutputFormat();
                        Log.d("DecodeActivity", "New format " + format);
                        audioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                        break;

                    default:
                        ByteBuffer outBuffer = outputBuffers[outIndex];
                      //  Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + outBuffer);

                        final byte[] chunk = new byte[info.size];
                        outBuffer.get(chunk); // Read the buffer all at once
                        outBuffer.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

                      //  D.log("offset"+info.offset);
                       // D.log("size"+info.offset + info.size);
                        audioTrack.write(chunk, info.offset, info.offset + info.size); // AudioTrack write data
                        AudioPacket pack=new AudioPacket(info.offset + info.size,chunk);

                      //  AudioTrackBufferUpdateEvent.invoke(new EventArgs1<AudioPacket>( self,pack));
                        mDecoder.releaseOutputBuffer(outIndex, false);
                        break;
                }

                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }
        }


        mDecoder.stop();
        mDecoder.release();
        mDecoder = null;

        mExtractor.release();
        mExtractor = null;


    }





}