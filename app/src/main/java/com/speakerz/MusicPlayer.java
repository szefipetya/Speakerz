package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.lang.reflect.Field;
import java.util.List;

public class MusicPlayer extends Activity {
// TODO: 1 bug az első zene után ha a user huzza végiga seekbart valamiért a 3. indul el nema 2. eztleszámítva error nincsen amennyire én látom,kommetelés és kód rendezés
    Button play;
    //Button stop;
    int playedSongnum=0;

    SpeakerzService _service;
    boolean _isBounded=false;
    MusicPlayer selfActivity=this;

    SeekBar seekBar;
    ListView playListView;
    ArrayAdapter songLA;
    int totalTime;
    boolean playSong=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }

        });

        //start or stop
        final Button buttonPlay = (Button) findViewById(R.id.play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!(_service.getModel().getMusicPlayerModel()).mediaPlayer.isPlaying()) {
                    (_service.getModel().getMusicPlayerModel()).mediaPlayer.start();
                    buttonPlay.setText("Stop");
                    playSong=true;

                } else {
                    (_service.getModel().getMusicPlayerModel()).mediaPlayer.pause();
                    buttonPlay.setText("Start");
                    playSong=false;
                }

            }

        });

        final Button buttonNext = (Button) findViewById(R.id.next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                NextSong(playedSongnum,(_service.getModel().getMusicPlayerModel()).songQueue);
                System.out.println(playedSongnum);
            }

        });


    }

    public void initAndStart(){

        // playlist, this adds the songs from the memory to the list
        playListView = (ListView) findViewById(R.id.playlist);

        Field[] fields = R.raw.class.getFields();
        for( int i = 0 ; i < fields.length ; i++){
            (_service.getModel().getMusicPlayerModel()).songQueue.add(fields[i].getName());
        }

        //front, This adapts the song queue into list view
        songLA = new ArrayAdapter<String>(this, R.layout.list_item, (_service.getModel().getMusicPlayerModel()).songQueue);
        playListView.setAdapter(songLA);


        // if you click a song that will be played.
        playListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if((_service.getModel().getMusicPlayerModel()).mediaPlayer !=null){
                    (_service.getModel().getMusicPlayerModel()).mediaPlayer.reset();
                }
                int resID = getResources().getIdentifier((_service.getModel().getMusicPlayerModel()).songQueue.get(i),"raw",getPackageName());
                (_service.getModel().getMusicPlayerModel()).mediaPlayer = (_service.getModel().getMusicPlayerModel()).mediaPlayer.create(MusicPlayer.this,resID);
                totalTime = (_service.getModel().getMusicPlayerModel()).mediaPlayer.getDuration();
                seekBar.setMax(totalTime);
                playedSongnum=i;
                (_service.getModel().getMusicPlayerModel()).mediaPlayer.start();

            }
        });

        //others
        // This Part make the seekbar work
        (_service.getModel().getMusicPlayerModel()).mediaPlayer = (_service.getModel().getMusicPlayerModel()).mediaPlayer.create(this, R.raw.rock);
        totalTime = (_service.getModel().getMusicPlayerModel()).mediaPlayer.getDuration();
        seekBar = (SeekBar) findViewById(R.id.elapsedtime);
        seekBar.setMax(totalTime);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            (_service.getModel().getMusicPlayerModel()).mediaPlayer.seekTo(progress);
                            MusicPlayer.this.seekBar.setProgress(progress);
                            if(progress>=totalTime){
                                System.out.println("vege user");
                                NextSong(playedSongnum,(_service.getModel().getMusicPlayerModel()).songQueue);

                            }
                        }
                        else{
                            if(progress>=totalTime){
                                System.out.println("vege");
                                NextSong(playedSongnum,(_service.getModel().getMusicPlayerModel()).songQueue);

                            }

                        }

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        new Thread(new Runnable() {
            @Override
            public void run() {
                while ((_service.getModel().getMusicPlayerModel()).mediaPlayer != null) {
                    try {
                        Message msg = new Message();
                        msg.what = (_service.getModel().getMusicPlayerModel()).mediaPlayer.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("interuppted");
                    }
                }
            }
        }).start();

    }
    // Seekbar handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            seekBar.setProgress(currentPosition);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service = localBinder.getService();
            _isBounded = true;

            selfActivity.initAndStart();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBounded = false;
        }
    };



    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        _isBounded = false;
    }

    public void NextSong(int lastplayedsongnum, List<String> songQueue){
        if ((_service.getModel().getMusicPlayerModel()).mediaPlayer.isPlaying()) {
            (_service.getModel().getMusicPlayerModel()).mediaPlayer.stop();
        }
         (_service.getModel().getMusicPlayerModel()).mediaPlayer.release();
        int i;
        if(lastplayedsongnum >= songQueue.size() -1 || lastplayedsongnum < 0){
           i=0;
        }
        else{
            i=lastplayedsongnum+1;
        }
        int resID = getResources().getIdentifier(songQueue.get(i),"raw",getPackageName());
        (_service.getModel().getMusicPlayerModel()).mediaPlayer = MediaPlayer.create(MusicPlayer.this,resID);
        totalTime = (_service.getModel().getMusicPlayerModel()).mediaPlayer.getDuration();
        seekBar.setMax(totalTime);
        playedSongnum=i;
        if(playSong){
            (_service.getModel().getMusicPlayerModel()).mediaPlayer.start();
        }


    }

// using relative layout as view but its to complicated for now so i decided that i go for functionality first;
    /*public class ListAdapter extends ArrayAdapter<String>{
        private int layout;
        private ListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder mainViewholder = null;
            if (convertView==null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.text= (TextView) convertView.findViewById(R.id.list_item_text);
                convertView.setTag(viewHolder);
                return convertView;

            }
            else{
                mainViewholder = (ViewHolder) convertView.getTag();
                mainViewholder.text.setText(getItem(position));

            }
            return convertView;
        }
    }

    public class ViewHolder {
        TextView text;

    }*/


}



