package com.speakerz.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;


public class MusicPlayerModel{
    // Context variables
    private final MusicPlayerModel self = this;
    private final Context context;

    public BaseModel getModel() {
        return model;
    }

    private final BaseModel model;

    // Playback managing variables
    public Integer currentSongId=1;
    private int currentPlayingIndex = 0;
    private Boolean isHost;
    boolean isPlaying=false;

    // Song lists
    private List<Song> songQueue = new LinkedList<>(); // the Songs we want to play as Song files.
    private ArrayList<Song> audioList = new ArrayList<>(); // all music in the phone

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songAddedEvent = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songRemovedEvent = new Event<>();
    public final Event<EventArgs1<Song>> songChangedEvent = new Event<>();

    // Event handlers
    EventListener<EventArgs1<Body>> musicPlayerActionListener = new EventListener<EventArgs1<Body>>() {
        @Override
        public void action(EventArgs1<Body> args) {
            D.log("mp evt happened");
            Body body = args.arg1();
            switch (args.arg1().SUBTYPE()){
                case MP_PUT_SONG:
                    Song song=((PutSongRequestBody)body).getContent();

                    if(isHost){
                        song.setId(currentSongId++);
                    }
                    songQueue.add(song);

                    D.log("recieved a song.");
                    // kliens kapott egy zenét. be kéne tenni a listába.
                    invokeModelCommunication(MP_EVT.SEND_SONG,song,body);
                    songAddedEvent.invoke(new EventArgs2<>(this, song, songQueue.size()));
                    break;
                case MP_GET_LIST:
                    if(isHost) invokeModelCommunication(MP_EVT.SEND_LIST, songQueue, body);

                    else {
                        GetSongListBody body1=(GetSongListBody)body;
                        List<Song> recvQueue= body1.getContent();
                        songQueue.clear();
                        for(Song e:recvQueue){
                            songQueue.add(e);
                            songAddedEvent.invoke(new EventArgs2<>(this, e, songQueue.size()));
                        }

                        invokeModelCommunication(MP_EVT.SEND_LIST, null, null);
                    }
                    break;
                case MP_ACTION_EVT:
                    switch (((MusicPlayerActionBody)body).getEvt()){
                        case SONG_CHANGED:
                            Integer songId=(Integer)body.getContent();
                            D.log("songId : "+songId);
                            Song _song = null;
                            int cp = 0;
                            for (Song s: songQueue) {
                                if(s.getId() == songId){
                                    _song = s;
                                    break;
                                }
                                cp++;
                            }
                            isPlaying = true;
                            playbackStateChanged.invoke(new EventArgs1<>(this, true));
                            if(_song != null) {
                                currentPlayingIndex = cp;
                                songChangedEvent.invoke(new EventArgs1<Song>(self, _song));
                            }
                            break;
                        case SONG_MAX_TIME_SECONDS:
                            Long timeInSeconds = (Long)body.getContent();
                            D.log("max time: "+timeInSeconds);
                            break;
                        case SONG_ACT_TIME_SECONDS:
                            //TODO, not implemented
                            break;
                        case SONG_RESUME:
                            D.log("resume");
                            isPlaying = true;
                            playbackStateChanged.invoke(new EventArgs1<>(this, true));
                            break;
                        case SONG_PAUSE:
                        case SONG_EOF:
                            D.log("pause/eof");
                            isPlaying = false;
                            playbackStateChanged.invoke(new EventArgs1<>(this, false));
                            break;
                        case SONG_NEXT:
                            D.log("song_next event happened");

                            Thread t=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startNext();
                                }
                            });
                            t.start();
                            playbackStateChanged.invoke(new EventArgs1<>(this, true));
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
    public boolean isPlaying() {return isPlaying; }
    public Song getCurrentSong() {
        if (currentPlayingIndex < 0 || currentPlayingIndex >= songQueue.size())
            return null;
        return songQueue.get(currentPlayingIndex);
    }

    // Song managing functions
    public void addSong(Song song){
        if (isHost){
            song.setId(currentSongId++);
            songQueue.add(song);
            invokeModelCommunication(MP_EVT.SEND_SONG,song,null);
        }else{
            invokeModelCommunication(MP_EVT.ADD_SONG_CLIENT,song,null);
        }
        D.log("addSong");

        songAddedEvent.invoke(new EventArgs2<>(this, song, songQueue.size()));
    }

    public void removeSong(Song song) {
        int i = songQueue.indexOf(song);
        if(i >= 0) {
            songQueue.remove(i);
            songRemovedEvent.invoke(new EventArgs2<>(this, song, i));
        }
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
        if (currentPlayingIndex>= songQueue.size()-1)
            start(0);
        else
            start(currentPlayingIndex + 1);
    }
    public void startPrev(){
        if (currentPlayingIndex == 0)
            start(songQueue.size()-1);
        else
            start(currentPlayingIndex - 1);
    }


    // starting song by Uri
    public void startONE(Context context, Uri uri,Integer songId){
        D.log("---START FROM UI");
        if(uri.getPath()!=null) {
            invokeModelCommunication(MP_EVT.SONG_CHANGED, new SongChangedInfo(new File(uri.getPath()), songId), null);
        }else{
            Toast.makeText(context,"Not yet implemented",Toast.LENGTH_SHORT).show();
        }
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
    public void start(){
        isPlaying=true;
        invokeModelCommunication(MP_EVT.SONG_RESUME, null, null);
    }


    // pauses media player if exists
    public void pause(){
        isPlaying=false;
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
            int i =0;
            while (cursor.moveToNext() && i <20) {
                //TODO: fail at to big number (250 mar fail)
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Bitmap songCoverArt = null;
                int albumID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                Long thisalbumId = cursor.getLong(albumID);
                Uri uriSongCover = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), thisalbumId);
                ContentResolver res = context.getContentResolver();
                i++;
                D.log("Cover : " +thisalbumId +" " + title);
                //File f = new File(uriSongCover.getPath());
                //TODO: handle corrupted files
                if(!title.equals("I Hope You Rot") && !title.equals("Shadow Boxing") ){
                    InputStream in = null;
                    try {
                        in = res.openInputStream(uriSongCover);
                        songCoverArt = BitmapFactory.decodeStream(in);
                        D.log("van CoverArt" + title);
                        in.close();

                    } catch (FileNotFoundException e) {
                        //e.printStackTrace();
                        D.log("nincs Coverart" + title);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    D.log(uriSongCover.toString());
                }
                /*try {
                    InputStream in = res.openInputStream(uriSongCover);
                    songCoverArt = BitmapFactory.decodeStream(in);
                    D.log("van CoverArt");
                }catch (FileNotFoundException e){
                    D.log("nincs Coverart");
                }*/



                //Print the title of the song that it found.

                // Save to audioList
                //TODO: replace alma to unique identifier
                audioList.add(new Song(data, title, album, artist,"alma",thisalbumId,songCoverArt));
            }
        }
        cursor.close();


    }



    public void loadAudio() {
        loadAudioWithPermission();
    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

}
