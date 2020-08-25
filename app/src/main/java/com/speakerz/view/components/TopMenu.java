package com.speakerz.view.components;

import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.speakerz.R;

public class TopMenu {

    AppCompatActivity activity;

    public TopMenu(AppCompatActivity activity) {
        this.activity = activity;
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.network:
                Toast.makeText(activity, "Network selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.profile:
                Toast.makeText(activity, "Profile selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fav:
                Toast.makeText(activity, "Favorites selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.name_change:
                Toast.makeText(activity, "Name Change selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.feedback:
                Toast.makeText(activity, "Feedback selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                Log.d("TopMenu"," default item selecedted");
                return false;
               //return activity.onOptionsItemSelected(item);
        }
    }
}
