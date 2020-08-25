/**TO DO:**
 * Színeket rendberakni
 *
 * Szebb törlés gomb
 * Sötétíteni a hátteret

 */


package com.speakerz.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import com.speakerz.R;
import com.speakerz.view.components.BottomMusicPlayer;
import com.speakerz.view.components.TopMenu;
import com.speakerz.view.recyclerview.RecyclerView_FAB;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView_FAB recyclerViewFab;
    TopMenu menu;
    BottomMusicPlayer bottomPlayer;
    AppCompatActivity self=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerViewFab=new RecyclerView_FAB(self);

        recyclerViewFab.createItemList();
        recyclerViewFab.buildRecyclerView();
        recyclerViewFab.setButtons();

       menu=new TopMenu(self);
        bottomPlayer =new BottomMusicPlayer(self);
        bottomPlayer.setButtons();
        bottomPlayer.addListeners();

    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
       return this.menu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menu.onOptionsItemSelected(item);
    }


}