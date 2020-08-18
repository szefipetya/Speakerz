package android.example.com.examplerecyclerview.components;

import android.content.Intent;
import android.example.com.examplerecyclerview.ExtendedPlayerActivity;
import android.example.com.examplerecyclerview.R;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BottomMusicPlayer {
    AppCompatActivity activity;

    TextView titleSongTV;



    TextView detailsTV;

    //PILL ELMENTEM DINYÉÉRT
  public   BottomMusicPlayer(AppCompatActivity activity){
        this.activity = activity;
    }
    public void setButtons(){
        titleSongTV= (TextView)activity.findViewById(R.id.titleSong);
        detailsTV=(TextView)activity.findViewById(R.id.details);

    }

    View.OnClickListener openExtendedPlayer=(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent Act2 = new Intent(activity, ExtendedPlayerActivity.class);
           activity.startActivity(Act2);

        }
    });;
    public void addListeners(){
        titleSongTV.setOnClickListener(openExtendedPlayer);
        detailsTV.setOnClickListener(openExtendedPlayer);
    }


}
