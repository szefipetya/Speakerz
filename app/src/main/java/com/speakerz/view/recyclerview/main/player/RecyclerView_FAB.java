package com.speakerz.view.recyclerview.main.player;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.speakerz.R;
import com.speakerz.model.BaseModel;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;
import com.speakerz.view.recyclerview.songadd.library.SongAddLibraryFragment;


import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.DeleteSongRequestBody;

import java.util.ArrayList;

public class RecyclerView_FAB  {
    PlayerRecyclerActivity activity;

    private ArrayList<Item> itemList;

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FloatingActionButton mMainFab, mLibraryFab, mYoutubeFab; //Floating Action Button
    private TextView mLibraryText, mYoutubeText;
    private boolean isFabOpen;
    private MusicPlayerModel model;
   private Boolean isSongPickerOpen=false;
    private BaseModel controllModel;
    // private Animation mFabOpenAnim, mFabCloseAnim; //Jelenleg nem működik

    public RecyclerView_FAB(PlayerRecyclerActivity activity){
        this.activity = activity;
        itemList = new ArrayList<>();
        buildRecyclerView();
        //initButtons();
    }

    final EventListener<EventArgs2<Song, Integer>> songAddedListener = new EventListener<EventArgs2<Song, Integer>>() {
        @Override
        public void action(final EventArgs2<Song, Integer> args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mAdapter == null) return;
                    mAdapter.notifyItemInserted(args.arg2());
                }
            });

        }
    };

    final EventListener<EventArgs2<Song, Integer>> songRemovedListener = new EventListener<EventArgs2<Song, Integer>>() {
        @Override
        public void action(final EventArgs2<Song, Integer> args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mAdapter == null) return;
                    mAdapter.notifyItemRemoved(args.arg2());
                }
            });

        }
    };

    public void insertItem(final int position,final String from,final int pic) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemList.add(new Item(pic, from + ": New Item at position: " + (position), "Artist"));
                mAdapter.notifyItemInserted(position);
            }
        });

    }

    public void removeItem(final int position) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemList.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        });

    }

    public void changeItem(final int position,final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemList.get(position).changeText1(text);
                mAdapter.notifyItemChanged(position);
            }
        });

    }

    public void buildRecyclerView() {
        mRecyclerView = activity.findViewById(R.id.recyclerView);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initButtons() {
        isFabOpen = false;
        // mFabOpenAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_open);
        // mFabCloseAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_close);
        //TODO: cliend music adding CLIENT BLOCK
        mMainFab = activity.findViewById(R.id.fab_basic);
        //TODO elavult picit ez a modszer de csak ideiglenes ugyis
        if(!model.isHost()){
            mMainFab.setImageDrawable(activity.getDrawable(R.drawable.ic_baseline_lock_24));
        }
        mMainFab = activity.findViewById(R.id.fab_basic);
        mMainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CLIENT BLOCK MUSICADDING
                if(model.isHost()) {
                    if (isFabOpen) {
                        mLibraryFab.setVisibility(View.INVISIBLE);
                        mYoutubeFab.setVisibility(View.INVISIBLE);
                        mLibraryText.setVisibility(View.INVISIBLE);
                        mYoutubeText.setVisibility(View.INVISIBLE);
                        //mLibraryFab.setAnimation(mFabCloseAnim);
                        //mYoutubeFab.setAnimation(mFabCloseAnim);

                        isFabOpen = false;
                        activity.lightOverlay();
                    } else {
                        mLibraryFab.setVisibility(View.VISIBLE);
                        mYoutubeFab.setVisibility(View.VISIBLE);
                        mLibraryText.setVisibility(View.VISIBLE);
                        mYoutubeText.setVisibility(View.VISIBLE);
                        //mLibraryFab.setAnimation(mFabOpenAnim);
                        //mYoutubeFab.setAnimation(mFabOpenAnim);

                        isFabOpen = true;
                        activity.darkOverlay();
                    }
                }
                else{
                    Toast.makeText(activity, "This function is not available yet", Toast.LENGTH_SHORT).show();
                }
                //Darker background + Toolbar -> Ezt kell visszaállítani az eredeti színekkel a listában lévő plusz gomb lenyomása után
                //Sötétítés


                //Visszavilágosítás
                /*
                * ConstraintLayout mConstraintLayout = activity.findViewById(R.id.layout_darker);
                mConstraintLayout.setBackgroundResource(R.color.transparent);
                activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(31, 64, 104)));
                * */
            }
        });


        mLibraryFab = activity.findViewById(R.id.fab_library);
        mLibraryText = activity.findViewById(R.id.library_text);
        mLibraryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //int position = model.getSongQueue().size();

                //model.addSong(model.getAudioList().get(position));
                isFabOpen = false;

                mLibraryFab.setVisibility(View.INVISIBLE);
                mYoutubeFab.setVisibility(View.INVISIBLE);
                mLibraryText.setVisibility(View.INVISIBLE);
                mYoutubeText.setVisibility(View.INVISIBLE);
                SongAddLibraryFragment fragment=new SongAddLibraryFragment();
                fragment.setModel(model);

                //Song add from library fragment open
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_song_import,fragment)
                        .addToBackStack(null)
                        .commit();
                isSongPickerOpen=true;
                fragment.CloseEvent.addListener(new EventListener<EventArgs>() {
                    @Override
                    public void action(EventArgs args) {
                        //set the darkness back when the dialog is closed
                       activity.lightOverlay();
                       isSongPickerOpen=false;
                    }
                });
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
                activity.lightOverlay();
            }
        });
    }



    public void initModel(final MusicPlayerModel model) {
        mAdapter = new Adapter(model.getSongQueue());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                model.startONE(model.getContext(), Uri.parse(model.getSongQueue().get(position).getData()),model.getSongQueue().get(position).getId());

            }

            @Override
            public void onDeleteClick(int position) {
                //TODO: DELETE SONG
                model.getModel().DeleteSongRequestEvent.invoke(new EventArgs1<Body>(TYPE.DELETE_SONG_REQUEST,new DeleteSongRequestBody(position)));
            }
        });
        model.songAddedEvent.addListener(songAddedListener);
        model.songRemovedEvent.addListener(songRemovedListener);
        this.model = model;
        // api level öreg picit de ezvan
        initButtons();
    }

    public void releaseModel() {
        mRecyclerView.setAdapter(null);
        mAdapter.setOnItemClickListener(null);
        model.songAddedEvent.removeListener(songAddedListener);
        model.songRemovedEvent.removeListener(songRemovedListener);

        mAdapter = null;
        model = null;
    }
    public Boolean getSongPickerOpen() {
        return isSongPickerOpen;
    }
}
