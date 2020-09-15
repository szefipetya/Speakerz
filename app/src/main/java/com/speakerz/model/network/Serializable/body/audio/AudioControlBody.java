package com.speakerz.model.network.Serializable.body.audio;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.content.AudioControlDto;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;


public class AudioControlBody extends Body implements Serializable {
    public AudioControlBody(  AudioControlDto dto){
        this.dto=dto;
    }
    public AudioControlBody(InetAddress sender, AudioControlDto dto){
        this.senderAddress=sender;
        this.dto=dto;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.NONE;
    }

    @Override
    public void setContent(Object obj) {
        this.dto=(AudioControlDto) obj;
    }

    AudioControlDto dto;

    @Override
    public AudioControlDto getContent() {
        return dto;
    }


}