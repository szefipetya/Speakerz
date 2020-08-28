package com.speakerz.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;
import com.speakerz.util.ThreadSafeEvent;

import java.io.File;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

//TODO: Az első zene elindításakor elég bugosak a dolgok, ennek a kijavítása kell BUGOK: Seekbar nemindul, startgomb nemjól van,seekbar nemműködik
// Ha ráléptetjük egy zenére valamilyen módon és megnyomjuka start gombot onnantól jó megy

public class MusicPlayerModel{
    // Context variables
    private final MusicPlayerModel self = this;
    private final Context context;
    private final BaseModel model;

    // Playback managing variables
    public Integer currentSongId=1;
    private int currentPlayingIndex = 0;
    private Boolean isHost;

    // Song lists
    private List<Song> songQueue = new LinkedList<>(); // the Songs we want to play as Song files.
    private ArrayList<Song> audioList = new ArrayList<>(); // all music in the phone

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songAddedEvent = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songRemovedEvent = new Event<>();

    // These events provided by Model
    /*private Event<EventArgs1<String>> SongDownloadedEvent;
    private ThreadSafeEvent<EventArgs1<Body>> MusicPlayerActionEvent;
    private Event<PermissionCheckEventArgs> PermissionCheckEvent;*/

    // Event handlers
    EventListener<EventArgs1<Body>> musicPlayerActionListener = new EventListener<EventArgs1<Body>>() {
        @Override
        public void action(EventArgs1<Body> args) {
            Body body = args.arg1();
            switch (args.arg1().SUBTYPE()){
                case MP_PUT_SONG:
                    D.log("recieved a song.");
                    Song song=((PutSongRequestBody)body).getContent();
                    // kliens kapott egy zenét. be kéne tenni a listába.
                    invokeModelCommunication(MP_EVT.SEND_SONG,song,body);
                    break;
                case MP_GET_LIST:
                    if(isHost) invokeModelCommunication(MP_EVT.SEND_LIST, songQueue, body);
                    else invokeModelCommunication(MP_EVT.SEND_LIST, null, null);
                    break;
                case MP_ACTION_EVT:
                    Long timeInSeconds=(Long)body.getContent();
                    switch (((MusicPlayerActionBody)body).getEvt()){
                        case SONG_CHANGED:
                            Integer songId=(Integer)body.getContent();
                            D.log("songId : "+songId);
                            break;
                        case SONG_MAX_TIME_SECONDS:
                            D.log("max time: "+timeInSeconds);
                            break;
                        case SONG_ACT_TIME_SECONDS:
                            //TODO, not implemented
                            break;
                        case SONG_RESUME:
                            D.log("resume evt");
                            break;
                        case SONG_PAUSE:
                            D.log("pause evt");
                            break;
                        case SONG_EOF:
                            D.log("eof evt");
                            break;
                    }
            }
        }
    };

    public MusicPlayerModel(BaseModel model, boolean isHost) {
        this.model = model;
        this.isHost = isHost;
        this.context = model.getContext();

        // Subscribe to model events
        model.MusicPlayerActionEvent.addListener(musicPlayerActionListener);
    }

    // Getters
    public List<Song> getSongQueue(){ return Collections.unmodifiableList(songQueue); }
    public List<Song> getAudioList() { return Collections.unmodifiableList(audioList); }
    public Context getContext() {return context; }
    public Boolean isHost() { return isHost; }
    public boolean isPlaying() {return false; }
    public Song getCurrentSong() {
        if (currentPlayingIndex < 0 || currentPlayingIndex >= songQueue.size())
            return null;
        return songQueue.get(currentPlayingIndex);
    }

    // Song managing functions
    public void addSong(Song song){
        song.setId(currentSongId++);
        songQueue.add(song);

        if (isHost) invokeModelCommunication(MP_EVT.SEND_SONG,song,null);
        else invokeModelCommunication(MP_EVT.SEND_SONG,song,null);

        songAddedEvent.invoke(new EventArgs2<>(this, song, songQueue.size()));
    }

    public void removeSong(Song song) {
        int i = songQueue.indexOf(song);
        if(i >= 0) {
            songQueue.remove(i);
            songRemovedEvent.invoke(new EventArgs2<>(this, song, i));
        }
    }



    public void subscribeEventsFromModel(){
    }





    // Close music player services
    public void close(){
        stop();
    }

    // Stop playing
    public void stop(){
        // TODO stop music
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
    public void startONE(Context context, Uri uri,Integer songId){
        D.log("---START FROM UI");
        invokeModelCommunication(MP_EVT.SONG_CHANGED,new SongChangedInfo(new File(uri.getPath()),songId),null);
    }

    public void start(int songIndex){
        invokeModelCommunication(MP_EVT.SONG_RESUME, null, null);
        if(songQueue.size() > 0 && songIndex < songQueue.size()) {
            currentPlayingIndex = songIndex;
            startONE(context,Uri.parse(this.songQueue.get(songIndex).getData()),this.songQueue.get(songIndex).getId());
        }
        else{
            System.out.println("nincstöbb zene a listában");
        }
    }

    // Start paused playing
    boolean isPlaying=true;
    public void start(){
        invokeModelCommunication(MP_EVT.SONG_RESUME, null, null);
    }


    // pauses media player if exists
    public void pause(){
        invokeModelCommunication(MP_EVT.SONG_PAUSE, null, null);
    }

    // Toggles pause and start state of player
    public void togglePause(){
        if(isPlaying)
            pause();
        else
            start();
    }

    private void invokeModelCommunication(MP_EVT arg1, Object arg2, Body arg3){
        model.ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self, arg1, arg2, arg3));
    }

    //Load All Audio from the device To AudioList ( you will be bale to choose from these to add to the SongQueue
    private void loadAudioWithPermission(){
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                //Print the title of the song that it found.

                // Save to audioList
                audioList.add(new Song(data, title, album, artist));
            }
        }
        cursor.close();


    }

    public void loadAudio() {
        loadAudioWithPermission();
    }

}
