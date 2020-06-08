package com.example.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

        Button buttonOptions = (Button) findViewById(R.id.options);
        buttonOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });







    }





}
