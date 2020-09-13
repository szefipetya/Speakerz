package com.speakerz.view.components;

import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.speakerz.R;
import com.speakerz.model.BaseModel;

public class TopMenu {

    AppCompatActivity activity;
    BaseModel model;

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
                //Toast.makeText(activity, "Network selected", Toast.LENGTH_SHORT).show();
                openNameListDialog();

                //Kicsit buggos xd
                /*activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_devices, new SongAddLibraryFragment())
                        .addToBackStack(null)
                        .commit();*/
                //vajon miért? :D -Peti

                return true;
            case R.id.profile:
                Toast.makeText(activity, "Profile selected", Toast.LENGTH_SHORT).show();
                openNameChangeDialog(activity);
                return true;
            case R.id.fav:
                Toast.makeText(activity, "Favorites selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.name_change:
                openNameChangeDialog(activity);
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
    public void openNameChangeDialog(AppCompatActivity activity) {
        NameChangeDialog nameChangeDialog = new NameChangeDialog();
        nameChangeDialog.show(activity.getSupportFragmentManager(), "example dialog");
    }

    public void openNameListDialog(){
        NameListDialog nameListDialog = new NameListDialog(model);
        nameListDialog.show(activity.getSupportFragmentManager(), "example dialog");


    }

    public BaseModel getModel() {
        return model;
    }

    public void setModel(BaseModel model) {
        this.model = model;
    }
}
