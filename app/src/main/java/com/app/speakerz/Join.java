package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.app.speakerz.model.event.ViewEventHandler;
import com.example.speakerz.R;

public class Join extends AppCompatActivity{
    //REQUIRED_BEG MODEL
    DeviceModel model;
    CommonViewEventHandler viewEventHandler;
    public void setModel(DeviceModel m){
        model=m;
        viewEventHandler=new CommonViewEventHandler(this);
        model.addUpdateEventListener(viewEventHandler);
    }
    //REQUIRED_END MODEL



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MusicPlayer.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

        Button buttonDiscover = (Button) findViewById(R.id.discover);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });



    }
}
