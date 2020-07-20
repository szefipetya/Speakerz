package com.speakerz.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.speakerz.MusicPlayer;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.util.ArrayList;

public class MusicPlayerModel{
    private int currentPlayingIndex = -1;

    public MusicPlayerModel self = this;
    public ArrayList<String> songQueue = new ArrayList<String>();
    public MediaPlayer mediaPlayer;

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();


    Thread durationUpdateThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try{
                int _current = -1, _total = -1;
                while(true){
                    if(mediaPlayer != null) {
                        int current = mediaPlayer.getCurrentPosition();
                        int total = mediaPlayer.getDuration();

                        if(_current != current || _total != total){
                            _current = current;
                            _total = total;
                            playbackDurationChanged.invoke(new EventArgs2<Integer, Integer>(self, _current, _total));
                        }
                    }
                    Thread.sleep(250);
                }
            }
            catch (InterruptedException e) { }
        }
    });

    public MusicPlayerModel(Context context) {
        mediaPlayer = new MediaPlayer();
        durationUpdateThread.start();
        playbackDurationChanged.addListener(new EventListener<EventArgs2<Integer, Integer>>() {
            @Override
            public void action(EventArgs2<Integer, Integer> args) {
                int current = args.arg1();
                int total = args.arg2();

                if(current >= total){

                }
            }
        });
    }

    public void close(){
        stop();
        durationUpdateThread.interrupt();
    }


    public void stop(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
        }
    }

    public void start(){
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
    }
    public void start(Context context, Uri uri){
        try {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
            if(mediaPlayer != null) mediaPlayer.reset();
            mediaPlayer = null;

            mediaPlayer = MediaPlayer.create(context, uri);
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
        catch (Exception e){}
    }
    public void start(Context context, int resId){
        try {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
            if(mediaPlayer != null) mediaPlayer.reset();
            mediaPlayer = null;

            mediaPlayer = MediaPlayer.create(context, resId);
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
        catch (Exception e){}
    }

    public void start(int songIndex){

    }

    public boolean isPlaying(){
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pause(){
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
        }
    }

    public void togglePause(){
        if(isPlaying())
            pause();
        else
            start();
    }

}
