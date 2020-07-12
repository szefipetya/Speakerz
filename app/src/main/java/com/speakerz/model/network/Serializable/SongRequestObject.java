package com.speakerz.model.network.Serializable;

import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;

import java.io.Serializable;

public class SongRequestObject implements ChannelObject, Serializable {
    String link="test_link";
    String Title="Test_title";
    String sender="Test_sender";

    public SongRequestObject(String link, String title, String sender) {
        this.link = link;
        Title = title;
        this.sender = sender;
    }

    public String getTitle() {
        return Title;
    }

    public String getSender() {
        return sender;
    }

    public String getLink() {
        return link;
    }

    @Override
    public SongRequestObject getObj() {
        return this;
    }

    @Override
    public TYPE getType() {
        return TYPE.MP;
    }

    @Override
    public SUBTYPE getSubType() {
        return SUBTYPE.MP_ADD_SONG;
    }

    @Override
    public String toString() {
        return "SongRequestObject{" +
                "link='" + link + '\'' +
                ", Title='" + Title + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
