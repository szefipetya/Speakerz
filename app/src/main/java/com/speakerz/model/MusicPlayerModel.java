package com.speakerz.model;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.GetSongListBody;
import com.speakerz.model.network.Serializable.body.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

//TODO: Az első zene elindításakor elég bugosak a dolgok, ennek a kijavítása kell BUGOK: Seekbar nemindul, startgomb nemjól van,seekbar nemműködik
// Ha ráléptetjük egy zenére valamilyen módon és megnyomjuka start gombot onnantól jó megy

public class MusicPlayerModel{


    private Boolean isHost;



    private int currentPlayingIndex = 0;
    public MusicPlayerModel self = this;

    public List<String> getSongQueue() {
        return songQueue;
    }

    //TODO NEED A LITTLE BIT MORE SPECIFIC DATATYPE
    //I REQUEST SongItem...
    public List<String> songQueue = new LinkedList<>();
    public MediaPlayer mediaPlayer;
    public Context context;
    public String SongPlayed;

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
                    songQueue.add(item.title+ "\n"+item.sender);
                    ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object,Body>(self,MP_EVT.SEND_SONG,item,args.arg1()));
                }

                if(args.arg1().SUBTYPE()==SUBTYPE.MP_GET_LIST) {

                    if (isHost) {
                        List<SongItem> tmp = new LinkedList<SongItem>();
                        for (String str : songQueue) {
                            //TODO: after a few rounds this becomes chaotic...
                            tmp.add(new SongItem(str, " ", "link"));
                        }
                        //((GetSongListBody)args.arg1()).getContent();
                        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object, Body>(self, MP_EVT.SEND_LIST, tmp, args.arg1()));
                    }else{
                        //kliensként csak kiolvassuk
                        songQueue.clear();
                        for (SongItem item : ((List<SongItem>)((GetSongListBody)args.arg1()).getContent())) {
                            songQueue.add(item.title+"\n"+item.sender);
                        }

                        ModelCommunicationEvent.invoke(new EventArgs3<MP_EVT, Object,Body>(self,MP_EVT.SEND_LIST,null,null));

                    }
                }
            }
        });
        SongDownloadedEvent.addListener(new EventListener<EventArgs1<String>>() {
            @Override
            public void action(EventArgs1<String> args) {
                playFromFile(args.arg1());
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
           /* try{
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
            catch (InterruptedException e) { }*/
        }
    });

    public MusicPlayerModel(Context context) {
        this.context = context;

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
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if(songQueue.size() > 0 && songIndex < songQueue.size()) {
            currentPlayingIndex = songIndex;
            int resId = context.getResources().getIdentifier(songQueue.get(songIndex), "raw", context.getPackageName());
            start(context, resId);
            SongPlayed = songQueue.get(songIndex);
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
    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }
    public void playFromFile(String path) {
//        String path= context.getClassLoader().getResource("tobu_good_times.mp3").getPath();

        // D.log(path);
     //   InputStream fis = context.getResources().openRawResource(R.raw.tobu_good_times);

      //  File file = new File(context.getFilesDir(), "audio.mp3");
      //  try (OutputStream outputStream = new FileOutputStream(file)) {
       //     copy(fis, outputStream);
        try {
            D.log("played from file-------------------------");
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
      /*      D.log("file readed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // handle exception here
        } catch (IOException e) {
            e.printStackTrace();
            // handle exception here
        }*/
    }





   private void playFromAudioStreamDirectly(InputStream is){


        final int duration = 10; // duration of sound
        final int sampleRate = 22050; // Hz (maximum frequency is 7902.13Hz (B8))
        final int numSamples = duration * sampleRate;
        final double samples[] = new double[numSamples];
        final short buffer[] = new short[numSamples];


        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffer.length,
                AudioTrack.MODE_STATIC);

     //  audioTrack.write(buffer, 0, buffer.length);
      // audioTrack.play();
       while(true){
           try {
               byte[] buffer2 = new byte[1024];

               if (!(is.read(buffer2)==-1)) break;
               audioTrack.write(buffer2, 0, buffer2.length);
                audioTrack.play();
           } catch (IOException e) {
               e.printStackTrace();
           }

       }

    }

    File tmpMediaFile;
    public void playFromAudioStream(InputStream p_is){
            try {


                InputStream is =context.getResources().openRawResource(R.raw.passion_aac);

                // create file to store audio
                tmpMediaFile = new File(this.context.getCacheDir(),"mediafile");
                FileOutputStream fos = new FileOutputStream(tmpMediaFile);
                byte buf[] = new byte[16 * 1024];
               D.log("FileOutputStream", "Download");

                // write to file until complete
                do {
                    int numread = is.read(buf);
                    if (numread <= 0)
                        break;
                    fos.write(buf, 0, numread);
                } while (true);
                fos.flush();
                fos.close();
                D.log("FileOutputStream", "Saved");
                MediaPlayer mp = new MediaPlayer();

                // create listener to tidy up after playback complete
                MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        // free up media player
                        mp.release();
                        D.log("MediaPlayer.OnCompletionListener", "MediaPlayer Released");
                    }
                };
                mp.setOnCompletionListener(listener);

                FileInputStream fis = new FileInputStream(tmpMediaFile);
                // set mediaplayer data source to file descriptor of input stream
                mp.setDataSource(fis.getFD());
                mp.prepare();
                D.log("MediaPlayer", "Start Player");
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



//Getter && Setter
    public Boolean getHost() {
        return isHost;
    }

    public void setHost(Boolean host) {
        isHost = host;
    }
}
