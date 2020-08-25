package com.speakerz.view.recyclerview;

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
    private Animation mFabOpenAnim, mFabCloseAnim; //Jelenleg nem működik

    public RecyclerView_FAB(AppCompatActivity activity){
        this.activity = activity;
    }

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

    public void createItemList() {
        itemList = new ArrayList<>();
    }
    public void buildRecyclerView() {
        mRecyclerView = activity.findViewById(R.id.recyclerView);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(activity);
        mAdapter = new Adapter(itemList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                changeItem(position, "Clicked");
            }

            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }
        });
    }
    public void setButtons() {
        setFabButtons();
        mLibraryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemList.size();
                insertItem(position, "Library", R.drawable.ic_song);

                mLibraryFab.setVisibility(View.INVISIBLE);
                mYoutubeFab.setVisibility(View.INVISIBLE);
                mLibraryText.setVisibility(View.INVISIBLE);
                mYoutubeText.setVisibility(View.INVISIBLE);
            }
        });

        mYoutubeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = itemList.size();
                insertItem(position, "Youtube", R.drawable.ic_youtube_bassline);

                mLibraryFab.setVisibility(View.INVISIBLE);
                mYoutubeFab.setVisibility(View.INVISIBLE);
                mLibraryText.setVisibility(View.INVISIBLE);
                mYoutubeText.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void setFabButtons() {
        mMainFab = activity.findViewById(R.id.fab_basic);
        mLibraryFab = activity.findViewById(R.id.fab_library);
        mYoutubeFab = activity.findViewById(R.id.fab_youtube);
        mLibraryText = activity.findViewById(R.id.library_text);
        mYoutubeText = activity.findViewById(R.id.youtube_text);

        mFabOpenAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_open);
        mFabCloseAnim = AnimationUtils.loadAnimation(activity, R.anim.fab_close);

        isFabOpen = false;

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
    }

    public void initModel(MusicPlayerModel model) {

    }

    public void releaseModel() {

    }
}
