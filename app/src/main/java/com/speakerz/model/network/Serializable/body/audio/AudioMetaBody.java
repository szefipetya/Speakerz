package com.speakerz.model.network.Serializable.body.audio;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.content.AudioMetaDto;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;


public class AudioMetaBody extends Body implements Serializable {
    public AudioMetaBody(  AudioMetaDto dto){
        this.dto=dto;
    }
    public AudioMetaBody(InetAddress sender, AudioMetaDto dto){
        this.senderAddress=sender;
        this.dto=dto;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.AUDIO_SEND_META;
    }

    @Override
    public void setContent(Object obj) {
        this.dto=(AudioMetaDto) obj;
    }

    AudioMetaDto dto;

    @Override
    public AudioMetaDto getContent() {
        return dto;
    }


}
