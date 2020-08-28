package com.speakerz.view.recyclerview;

import android.net.Uri;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.speakerz.R;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.util.ArrayList;

public class RecyclerView_FAB  {
    AppCompatActivity activity;

    private ArrayList<Item> itemList;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FloatingActionButton mMainFab, mLibraryFab, mYoutubeFab; //Floating Action Button
    private TextView mLibraryText, mYoutubeText;
    private boolean isFabOpen;
    private MusicPlayerModel model;

    // private Animation mFabOpenAnim, mFabCloseAnim; //Jelenleg nem működik

    public RecyclerView_FAB(AppCompatActivity activity){
        this.activity = activity;
        itemList = new ArrayList<>();
        buildRecyclerView();
        initButtons();
    }

    final EventListener<EventArgs2<Song, Integer>> songAddedListener = new EventListener<EventArgs2<Song, Integer>>() {
        @Override
        public void action(EventArgs2<Song, Integer> args) {
            if(mAdapter == null) return;
            mAdapter.notifyItemInserted(args.arg2());
        }
    };    final EventListener<EventArgs2<Song, Integer>> songRemovedListener = new EventListener<EventArgs2<Song, Integer>>() {
        @Override
        public void action(EventArgs2<Song, Integer> args) {
            if(mAdapter == null) return;
            mAdapter.notifyItemRemoved(args.arg2());
        }
    };

    public void insertItem(int position, String from, int pic) {
        itemList.add(new Item(pic, from + ": New Item at position: " + (position), "Artist"));
        mAdapter.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        itemList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    public void changeItem(int position, String text) {
        itemList.get(position).changeText1(text);
        mAdapter.notifyItemChanged(position);
    }

    public void buildRecyclerView() {
        mRecyclerView = activity.findViewById(R.id.recyclerView);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    private void initButtons() {
        isFabOpen = false;
        // mFabOpenAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_open);
        // mFabCloseAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_close);

        mMainFab = activity.findViewById(R.id.fab_basic);
        mMainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFabOpen){
                    mLibraryFab.setVisibility(View.INVISIBLE);
                    mYoutubeFab.setVisibility(View.INVISIBLE);
                    mLibraryText.setVisibility(View.INVISIBLE);
                    mYoutubeText.setVisibility(View.INVISIBLE);
                    //mLibraryFab.setAnimation(mFabCloseAnim);
                    //mYoutubeFab.setAnimation(mFabCloseAnim);

                    isFabOpen = false;
                }else {
                    mLibraryFab.setVisibility(View.VISIBLE);
                    mYoutubeFab.setVisibility(View.VISIBLE);
                    mLibraryText.setVisibility(View.VISIBLE);
                    mYoutubeText.setVisibility(View.VISIBLE);
                    //mLibraryFab.setAnimation(mFabOpenAnim);
                    //mYoutubeFab.setAnimation(mFabOpenAnim);

                    isFabOpen = true;
                }
            }
        });

        mLibraryFab = activity.findViewById(R.id.fab_library);
        mLibraryText = activity.findViewById(R.id.library_text);
        mLibraryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = model.getSongQueue().size();

                model.addSong(model.getAudioList().get(position));
                isFabOpen = false;

                mLibraryFab.setVisibility(View.INVISIBLE);
                mYoutubeFab.setVisibility(View.INVISIBLE);
                mLibraryText.setVisibility(View.INVISIBLE);
                mYoutubeText.setVisibility(View.INVISIBLE);
            }
        });

        mYoutubeFab = activity.findViewById(R.id.fab_youtube);
        mYoutubeText = activity.findViewById(R.id.youtube_text);
        mYoutubeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemList.size();
                insertItem(position, "Youtube", R.drawable.ic_youtube_bassline);
                isFabOpen = false;

                mLibraryFab.setVisibility(View.INVISIBLE);
                mYoutubeFab.setVisibility(View.INVISIBLE);
                mLibraryText.setVisibility(View.INVISIBLE);
                mYoutubeText.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void initModel(final MusicPlayerModel model) {
        mAdapter = new Adapter(model.getSongQueue());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {


                model.startONE(model.context, Uri.parse(model.getSongQueue().get(position).getData()),model.getSongQueue().get(position).getId());

            }

            @Override
            public void onDeleteClick(int position) {
                model.removeSong(model.getSongQueue().get(position));
            }
        });
        model.songAddedEvent.addListener(songAddedListener);
        model.songRemovedEvent.addListener(songRemovedListener);
        this.model = model;
    }

    public void releaseModel() {
        mRecyclerView.setAdapter(null);
        mAdapter.setOnItemClickListener(null);
        model.songAddedEvent.removeListener(songAddedListener);
        model.songRemovedEvent.removeListener(songRemovedListener);

        mAdapter = null;
        model = null;
    }
}
