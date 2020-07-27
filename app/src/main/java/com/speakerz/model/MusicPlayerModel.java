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

//TODO: Az első zene elindításakor elég bugosak a dolgok, ennek a kijavítása kell BUGOK: Seekbar nemindul, startgomb nemjól van,seekbar nemműködik
// Ha ráléptetjük egy zenére valamilyen módon és megnyomjuka start gombot onnantól jó megy

public class MusicPlayerModel{
    private int currentPlayingIndex = 0;

    public MusicPlayerModel self = this;
    public ArrayList<String> songQueue = new ArrayList<String>();
    public MediaPlayer mediaPlayer;
    public Context context;
    boolean playSong=false;

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();

    // Listeners
    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            startNext();
        }
    };

    // thread to sync playback durations
    Thread durationUpdateThread = new Thread(new Runnable() {
        @Override
        public void run() {
           /* try{
                int _current = -1, _total = -1;
                while(true){
                    if(mediaPlayer != null && playSong) {
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
            catch (InterruptedException e) { }*/
        }
    });

    public MusicPlayerModel(Context context) {
        this.context = context;

        mediaPlayer = new MediaPlayer();
        durationUpdateThread.start();

        // Event handler to start next song automatically

        mediaPlayer.setOnCompletionListener(completionListener);
    }

    // Close music player services
    public void close(){
        stop();
        durationUpdateThread.interrupt();
    }

    // Stop playing
    public void stop(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
        }
    }

    public void startNext(){
        if (currentPlayingIndex>= songQueue.size()-1){
            currentPlayingIndex =0;
            start(currentPlayingIndex);
        }
        else{
            start(currentPlayingIndex + 1);
        }
    }

    // starting song by Uri
    public void start(Context context, Uri uri){
        try {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
            if(mediaPlayer != null) mediaPlayer.reset();
            mediaPlayer = null;

            mediaPlayer = MediaPlayer.create(context, uri);
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
        catch (Exception e){}
    }

    // Starting song by resId
    public void start(Context context, int resId){
        try {
            mediaPlayer.stop();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
            if(mediaPlayer != null) mediaPlayer.reset();
            mediaPlayer = null;

            mediaPlayer = MediaPlayer.create(context, resId);
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
        catch (Exception e){}
    }

    // Starting song from songQueue by index
    public void start(int songIndex){
        if(songQueue.size() > 0 && songIndex < songQueue.size()) {
            currentPlayingIndex = songIndex;
            int resId = context.getResources().getIdentifier(songQueue.get(songIndex), "raw", context.getPackageName());
            start(context, resId);
        }
    }

    // Start paused playing
    public void start(){
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            playSong=true;
            mediaPlayer.start();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
    }

    // returns true is media player exists and playing media
    public boolean isPlaying(){
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    // pauses media player if exists
    public void pause(){
        if(mediaPlayer != null) {
            playSong=false;
            mediaPlayer.pause();
            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, false));
        }
    }

    // Toggles pause and start state of player
    public void togglePause(){
        if(isPlaying())
            pause();
        else
            start();
    }

}
