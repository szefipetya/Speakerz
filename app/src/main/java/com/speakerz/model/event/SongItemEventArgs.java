package com.speakerz.model.event;

import com.speakerz.model.network.Serializable.SongRequestObject;
import com.speakerz.util.EventArgs;

public class SongItemEventArgs extends EventArgs {
    SongRequestObject songRequestObject;

    public SongRequestObject getSongRequestObject() {
        return songRequestObject;
    }

    public SongItemEventArgs(Object _sender, SongRequestObject songRequestObject) {
        super(_sender);
        this.songRequestObject = songRequestObject;
    }

    public SongItemEventArgs(Object _sender) {
        super(_sender);
    }
}
