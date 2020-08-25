/**TO DO:**
 * Színeket rendberakni
 *
 * Szebb törlés gomb
 * Sötétíteni a hátteret

 */


package android.example.com.examplerecyclerview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.example.com.examplerecyclerview.components.BottomMusicPlayer;
import android.example.com.examplerecyclerview.components.TopMenu;
import android.example.com.examplerecyclerview.recyclerview.Adapter;
import android.example.com.examplerecyclerview.recyclerview.Item;
import android.example.com.examplerecyclerview.recyclerview.RecyclerView_FAB;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    /*private Button insertButton;
    private Button removeButton;
    private EditText editTextInsert;
    private EditText editTextRemove;*/
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