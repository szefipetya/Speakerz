package com.speakerz.model;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.GetSongListBody;
import com.speakerz.model.network.Serializable.body.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public List<Song> songQueue = new LinkedList<>(); // the Songs we want to play as Song files.
    public ArrayList<Song> audioList = new ArrayList<Song>(); // all music in the phone
    public ArrayList<String> audioNameList = new ArrayList<String>(); // names of all the songs for the view
    public MediaPlayer mediaPlayer;
    public Context context;
    private Song activeSong;
    public String playedSongName;
    private String mediaFile;

    // Events
    public final Event<EventArgs1<Boolean>> playbackStateChanged = new Event<>();
    public final Event<EventArgs2<Integer, Integer>> playbackDurationChanged = new Event<>();

    //From Model
    //comes as external dependency from model
    public Event<EventArgs1<Body>> MusicPlayerActionEvent;
    //common communation channel with model.
    public Event<EventArgs3<MP_EVT,Object,Body>> ModelCommunicationEvent=new Event<>();

    public Event<EventArgs1<String>> SongDownloadedEvent;


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
            try{
                int _current = -1, _total = -1;
                while(true){
                    if(mediaPlayer != null && mediaPlayer.isPlaying()) {
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

    public MusicPlayerModel(Context context,Event<PermissionCheckEventArgs> permEvt) {
        this.context = context;
        this.PermissionCheckEvent=permEvt;
        mediaPlayer = new MediaPlayer();
        durationUpdateThread.start();

        // Event handler to start next song automatically

        mediaPlayer.setOnCompletionListener(completionListener);

      //  playFromAudioStream();
      //  InputStream is =context.getResources().openRawResource(R.raw.passion_aac);
      //  playFromAudioStreamDirectly(is);


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
        if (currentPlayingIndex>= songNameQueue.size()-1){
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

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(context,uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.start();

            playbackStateChanged.invoke(new EventArgs1<Boolean>(this, true));
        }
        catch (Exception e){}
    }

    // Starting song by resId
   /* public void start(Context context, int resId){
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
    }*/

    // Starting song from songQueue by index
    public void start(int songIndex){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if(songNameQueue.size() > 0 && songIndex < songNameQueue.size()) {
            currentPlayingIndex = songIndex;
            //int resId = context.getResources().getIdentifier(songNameQueue.get(songIndex), "raw", context.getPackageName());
            //start(context, resId);
            start(context,Uri.parse(this.songQueue.get(songIndex).getData()));
            playedSongName = songNameQueue.get(songIndex);
        }
        else{
            System.out.println("nincstöbb zene a listában");

        }
    }

    // Start paused playing
    public void start(){
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
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
