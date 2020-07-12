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

import com.speakerz.debug.D;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.util.EventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MusicPlayer extends Activity {
// TODO: Szövegek beállítása,gomvnyomásra zeneváltás, Modul részbe integrállás,kimmetelés és kód rendezés
    Button play;
    Button stop;
    MediaPlayer mp;
    SeekBar elapsedtime;
    ListView playListView;
    ArrayList<String> data = new ArrayList<String>();
    ArrayAdapter LA;
    int totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);


        // playlist
        playListView = (ListView) findViewById(R.id.playlist);
        Field[] fields = R.raw.class.getFields();
        for( int i = 0 ; i < fields.length ; i++){
            data.add(fields[i].getName());

        }


        //front
        LA = new ArrayAdapter<String>(this, R.layout.list_item, data);

        playListView.setAdapter(LA);



        playListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mp!=null){
                    mp.release();
                }
                int resID = getResources().getIdentifier(data.get(i),"raw",getPackageName());
                mp= MediaPlayer.create(MusicPlayer.this,resID);
                totalTime = mp.getDuration();
                elapsedtime.setMax(totalTime);
                mp.start();



            }
        });

        //front_end

    //others
        play=(Button) findViewById(R.id.play);
        mp = MediaPlayer.create(this, R.raw.rock);
        totalTime = mp.getDuration();
        elapsedtime = (SeekBar) findViewById(R.id.elapsedtime);
        elapsedtime.setMax(totalTime);
        elapsedtime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            elapsedtime.setProgress(progress);
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


        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
             //   Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
              //  Act2.putExtra("Hello","Hello World");
             //   startActivity(Act2);
                finish();
            }

        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();


    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            elapsedtime.setProgress(currentPosition);
        }
    };

    public void playClick(View view){
        if(!mp.isPlaying()){
            mp.start();

        }
        else{
            mp.pause();

        }

    }


    SpeakerzService _service;
    boolean _isBounded=false;
    MusicPlayer selfActivity=this;

    public void initAndStart(){

    }

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



