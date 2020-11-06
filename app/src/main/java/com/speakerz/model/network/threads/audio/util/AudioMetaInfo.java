package com.speakerz.model.network.threads.audio.util;

import android.media.AudioTrack;

import com.google.common.collect.ImmutableSet;
import com.speakerz.debug.D;

import java.io.File;
import java.io.IOException;

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


public class AudioMetaInfo {


    private AudioFile audioFile = null;
    private AudioHeader audioHeader = null;
    private Tag audioTag = null;

    public AudioMetaInfo(File inputFile) throws CannotReadException {
        try {
            audioFile = AudioFileIO.read(inputFile);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        if(audioFile!=null)
            init();
    }

    private void init(){
        TagOptionSingleton.getInstance().setAndroid(true);  // Android - put in Application.onCreate()
        audioHeader = audioFile.getAudioHeader();
        final int channels = audioHeader.getChannelCount();

        D.log("channels: " + channels);
        D.log("data length"+audioHeader.getAudioDataLength());
        final int bitRate = audioHeader.getBitsPerSample();
        D.log("bitRate: " + bitRate);
        D.log("bits per sample: " + audioHeader.getBitsPerSample());
        D.log("sample rate: " + audioHeader.getSampleRate());
        final String encodingType = audioHeader.getEncodingType();
        D.log("encodingType: " + encodingType);
        D.log("isvariablebitrate: " + audioHeader.isVariableBitRate());
    }

    //GETTER and SETTER
    public AudioFile getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(AudioFile audioFile) {
        this.audioFile = audioFile;
    }

    public AudioHeader getAudioHeader() {
        return audioHeader;
    }

    public Tag getAudioTag() {
        return audioTag;
    }


}
