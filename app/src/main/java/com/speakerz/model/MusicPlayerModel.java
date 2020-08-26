package com.speakerz.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

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


    public Event<PermissionCheckEventArgs> PermissionCheckEvent=null;
    private Boolean isHost;
    private Boolean asd;



    private int currentPlayingIndex = 0;
    public MusicPlayerModel self = this;

    public List<String> getSongNameQueue() {
        return songNameQueue;
    }

    //TODO NEED A LITTLE BIT MORE SPECIFIC DATATYPE
    //I REQUEST SongItem...
    public List<String> songNameQueue = new LinkedList<>(); // the name of the Songs we want to play
    private List<Song> songQueue = new LinkedList<>(); // the Songs we want to play as Song files.
    public ArrayList<Song> audioList = new ArrayList<Song>(); // all music in the phone
    public ArrayList<String> audioNameList = new ArrayList<String>(); // names of all the songs for the view
    public Context context;
    private Song activeSong;
    public String playedSongName;
    private String mediaFile;

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();
    public final Event<EventArgs2<Song, Integer>> songAddedEvent = new Event<>();
    public Event<EventArgs1<Body>> MusicPlayerActionEvent;
    public Event<EventArgs3<MP_EVT,Object,Body>> ModelCommunicationEvent=new Event<>();
    public Event<EventArgs1<String>> SongDownloadedEvent;

    public List<Song> getSongQueue(){
        return Collections.unmodifiableList(songQueue);
    }

    public void addSong(Song song){
        songQueue.add(song);
        songAddedEvent.invoke(new EventArgs2<Song, Integer>(this, song, songQueue.size()));
    }


    public void subscribeEventsFromModel(){
        MusicPlayerActionEvent.addListener(new EventListener<EventArgs1<Body>>() {
            @Override
            public void action(EventArgs1<Body> args) {
                if(args.arg1().SUBTYPE()== SUBTYPE.MP_PUT_SONG){
                    D.log("recieved a song.");
                    SongItem item=((PutSongRequestBody)args.arg1()).getContent();
                    songNameQueue.add(item.title+ "\n"+item.sender);
                    ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object,Body>(self,MP_EVT.SEND_SONG,item,args.arg1()));
                }

                if(args.arg1().SUBTYPE()==SUBTYPE.MP_GET_LIST) {

                    if (isHost) {
                        List<SongItem> tmp = new LinkedList<SongItem>();
                        for (String str : songNameQueue) {
                            //TODO: after a few rounds this becomes chaotic...
                            tmp.add(new SongItem(str, " ", "link"));
                        }
                        //((GetSongListBody)args.arg1()).getContent();
                        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self, MP_EVT.SEND_LIST, tmp, args.arg1()));
                    }else{
                        //kliensként csak kiolvassuk
                        songNameQueue.clear();
                        for (SongItem item : ((List<SongItem>)((GetSongListBody)args.arg1()).getContent())) {
                            songNameQueue.add(item.title+"\n"+item.sender);
                        }

                        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object,Body>(self,MP_EVT.SEND_LIST,null,null));

                    }
                }
            }
        });
    }


    public MusicPlayerModel(Context context,Event<PermissionCheckEventArgs> permEvt) {
        this.context = context;
        this.PermissionCheckEvent=permEvt;

        // Event handler to start next song automatically


      //  playFromAudioStream();
      //  InputStream is =context.getResources().openRawResource(R.raw.passion_aac);
      //  playFromAudioStreamDirectly(is);


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
        if (currentPlayingIndex>= songNameQueue.size()-1){
            currentPlayingIndex =0;
            start(currentPlayingIndex);
        }
        else{
            start(currentPlayingIndex + 1);
        }
    }


    // starting song by Uri
    public void startONE(Context context, Uri uri){
        D.log("---START FROM UI");
        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self,MP_EVT.SONG_PLAY,new File(uri.getPath()),null));
    }

    public void start(int songIndex){
        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self,MP_EVT.SONG_RESUME,null,null));
        if(songNameQueue.size() > 0 && songIndex < songNameQueue.size()) {
            currentPlayingIndex = songIndex;

            startONE(context,Uri.parse(this.songQueue.get(songIndex).getData()));
            playedSongName = songNameQueue.get(songIndex);
        }
        else{
            System.out.println("nincstöbb zene a listában");
        }
    }

    // Start paused playing
    boolean isPlaying=true;
    public void start(){
        playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        isPlaying=true;
        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self,MP_EVT.SONG_RESUME,null,null));
    }

    // returns true is media player exists and playing media
    public boolean isPlaying(){
        return false; // TODO
    }

    // pauses media player if exists
    public void pause(){
        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self, MP_EVT.SONG_PAUSE, null, null));
    }

    // Toggles pause and start state of player
    public void togglePause(){
        if(isPlaying)
            pause();
        else
            start();
    }

    //Getter && Setter
    public Boolean getHost() {
        return isHost;
    }

    public void setHost(Boolean host) {
        isHost = host;
    }



    //Load All Audio from the device To AudioList ( you will be bale to choose from these to add to the SongQueue
    public void loadAudioWithPermission(){
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
                audioNameList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
        }
        cursor.close();


    }

    public void loadAudio() {
        loadAudioWithPermission();
    }
}
