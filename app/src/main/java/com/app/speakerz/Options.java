package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.HostModel;
import com.example.speakerz.R;

public class Options extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
               // Act2.putExtra("Hello","Hello World");
              //  startActivity(Act2);
                finish();

            }

        });
    }
}
