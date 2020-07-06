package com.speakerz.model.network.event.channel;

import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.util.EventArgs;

public class MusicPlayerActionEventArgs extends EventArgs {
    private ChannelObject channelObject;

    public ChannelObject getChannelObject() {
        return channelObject;
    }

    public MusicPlayerActionEventArgs(Object _sender, ChannelObject channelObject) {
        super(_sender);
        this.channelObject = channelObject;
    }

    public MusicPlayerActionEventArgs(Object _sender) {
        super(_sender);
    }
}
