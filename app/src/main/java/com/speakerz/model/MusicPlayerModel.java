package com.speakerz.model;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.enums.VIEW_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.audio.MusicPlayerActionBody;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PlaybackControlRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
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
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Integer.parseInt;


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

    // Song lists + filter
    private List<Song> songQueue = new LinkedList<>(); // the Songs we want to play as Song files.
    private ArrayList<Song> audioList = new ArrayList<>();// all music in the phone
    private ArrayList<Song> audioListFiltered = new ArrayList<>();
    private String songFilter;
    Cursor audioReaderCursor;

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songAddedEvent = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songRemovedEvent = new Event<>();
    public final Event<EventArgs1<Song>> songChangedEvent = new Event<>();
    public final Event<EventArgs2<VIEW_EVT,String>> AdapterLibraryEvent=new Event<>();



    // Event handlers
    EventListener<EventArgs1<Body>> musicPlayerActionListener = new EventListener<EventArgs1<Body>>() {
        @Override
        public void action(EventArgs1<Body> args) {
            D.log("mp evt happened");
            Song _song = null;
            int songId = 0; int cp = 0;
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
                case MP_CONTROL_REQUEST:
                    PlaybackControlRequestBody pbr = (PlaybackControlRequestBody) body;
                    if(pbr.message == PlaybackControlRequestBody.PlaybackControlType.Pause)
                        pause();
                    if(pbr.message == PlaybackControlRequestBody.PlaybackControlType.Resume)
                        start();
                    if(pbr.message == PlaybackControlRequestBody.PlaybackControlType.SelectSong){
                        songId=((Integer)body.getContent()).intValue();
                        D.log("songId : "+songId);
                        _song = null;
                        cp = 0;
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
                            startONE(
                                    getContext(),
                                    Uri.parse(getSongQueue().get(cp).getData()),
                                    getSongQueue().get(cp).getId()
                            );
                        }
                    }
                    break;
                case MP_ACTION_EVT:
                    switch (((MusicPlayerActionBody)body).getEvt()){
                        case SONG_CHANGED:
                            songId=((Integer)body.getContent()).intValue();
                            D.log("songId : "+songId);
                            _song = null;
                            cp = 0;
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
                            Integer actTimeInSeconds = (Integer)body.getContent();
                            D.log("act time: "+actTimeInSeconds);
                            ;

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
        this.songFilter="";

        // Subscribe to model events
        model.MusicPlayerActionEvent.addListener(musicPlayerActionListener);
        AdapterLibraryEvent.addListener(new EventListener<EventArgs2<VIEW_EVT, String>>() {
            @Override
            public void action(EventArgs2<VIEW_EVT, String> args) {
                if(args.arg1()==VIEW_EVT.ADAPTER_SONG_FILTER){
                    if(args.arg2().equals("")){
                       D.log("ures mezo");
                       songFilter="";
                       ListFiltering(songFilter);
                    }
                    else{
                       songFilter=args.arg2();
                       D.log(songFilter);
                       ListFiltering(songFilter);
                    }
                }
            }
        });
    }

    // Getters
    public List<Song> getSongQueue(){ return Collections.unmodifiableList(songQueue); }
    public List<Song> getAudioList() { return Collections.unmodifiableList(audioList); }
    public List<Song> getAudioListFiltered() { return Collections.unmodifiableList(audioListFiltered); }
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
        setExtraDataForSong(song);
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
        externalShutdown=true;
    }

    boolean externalShutdown=false;
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
        invokeModelCommunication(MP_EVT.SONG_CHANGED, new SongChangedInfo(new File(uri.getPath()), songId), null);
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
    /*private void loadSomeAudio(final Cursor cursor){
                   while(!cursor.isLast()&&cursor.moveToNext()) {
                       loadNextAudio(cursor);
                   }
    }*/


    //TODO: nem rosszötlet depicit laggol mikor keres az ember Kéne egy szűrés a listára hogy lehessen keresni benne ahoz viszont az egésznek bekell töltve lennie
    private void loadNextAudio(Cursor cursor){
        if (cursor.isAfterLast()) return;
        if (cursor.isBeforeFirst()) return;
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

            //ez a rész most már a setExtraDataForSong-ban van.
            int albumID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            Long thisAlbumId = cursor.getLong(albumID);


            //TODO: replace alma to unique identifier
            Song s = new Song(audioReaderCursor.getPosition(), "", title, album, artist, "alma", thisAlbumId);
            //s.setSongCoverArt(songCoverArt);
            // s.setAlbumId(thisAlbumId);
            int durMili = parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            String duration;
            durMili = durMili / 1000;
            Integer durH = durMili / 3600;
            durMili = durMili % 3600;
            Integer durM = durMili / 60;
            durMili = durMili % 60;
            Integer durS = durMili;
            if (durH > 0) {
                s.setDuration(durH.toString() + ":" + durM.toString() + ":" + durS.toString());
            } else {
                if (durS < 10) {
                    s.setDuration(durM.toString() + ":0" + durS.toString());
                } else {
                    s.setDuration(durM.toString() + ":" + durS.toString());
                }

            }
            audioList.add(s);

    }


    void setExtraDataForSong(Song s){
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if(cursor.moveToPosition(s.getCursorIndex())){
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int albumID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            Long thisAlbumId = cursor.getLong(albumID);
            Bitmap songCoverArt = null;
            Uri uriSongCover = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), thisAlbumId);
            ContentResolver res = context.getContentResolver();


            InputStream in = null;
            try {
                in = res.openInputStream(uriSongCover);
                songCoverArt = BitmapFactory.decodeStream(in);
                in.close();

            } catch (FileNotFoundException e) {
                //e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            s.setData(data);
            s.setSongCoverArt(songCoverArt);
            //Print the title of the song that it found.
            // Save to audioList
        }
    }

    private void loadAudioWithPermission(){
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        audioReaderCursor = contentResolver.query(uri, null, selection, null, sortOrder);
        audioList = new ArrayList<>();

        if (audioReaderCursor != null && audioReaderCursor.getCount() > 0) {
            while (audioReaderCursor.moveToNext()) {
                loadNextAudio(audioReaderCursor);
            }
        }

    }


    public void loadAudio() {
        loadAudioWithPermission();
        for(Song s : audioList){
            audioListFiltered.add(s);
        }
    }


    private void ListFiltering(String songFilter){
        audioListFiltered.clear();
        if(songFilter.equals("")){
            for(Song s : audioList){
                audioListFiltered.add(s);
            }
        }
        else{
            for(Song s : audioList){
                if(s.getTitle().startsWith(songFilter)){
                    audioListFiltered.add(s);
                }
            }

        }
    }


}
