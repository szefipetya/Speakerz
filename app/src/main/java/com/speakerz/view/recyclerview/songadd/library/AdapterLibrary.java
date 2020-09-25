package com.speakerz.view.recyclerview.songadd.library;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.VIEW_EVT;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AdapterLibrary extends RecyclerView.Adapter<AdapterLibrary.ViewHolderLibrary> {
    private final MusicPlayerModel model;
    Context contextLibrary;
    ArrayList<libraryItem> listItems;


    public AdapterLibrary(Context mContext, ArrayList<libraryItem> mList, MusicPlayerModel model){
        contextLibrary = mContext;
        listItems = mList;
        this.model=model;
        this.AdapterLibraryEvent=model.AdapterLibraryEvent;
    }


    @NonNull
    @Override
    public ViewHolderLibrary onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contextLibrary).inflate(R.layout.item_song_import, parent, false);
        return new ViewHolderLibrary(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolderLibrary holder, int position) {

        //ez azért kell, hogyha vissza görget, akkor azokat már ne adja hozzá.
        if(!(position<model.getAudioListFiltered().size())) {
            D.log("wat " +model.getAudioListFiltered().size() +" "+position);}
        else{
            Song s=model.getAudioListFiltered().get(position);
            String songName = s.getTitle();
            String artist = s.getArtist();
            String songLengthTime = s.getDuration();

            holder.songNameTextView.setText(songName);
            holder.songArtistTextView.setText(artist);
            holder.songLengthTimeTextView.setText(songLengthTime);

            D.log("clicked:"+position);

        }
    }


    Event<EventArgs2<VIEW_EVT,String>> AdapterLibraryEvent;

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    public class ViewHolderLibrary extends RecyclerView.ViewHolder{
        public TextView songNameTextView;
        public TextView songArtistTextView;
        public TextView songLengthTimeTextView;

        public ViewHolderLibrary(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.songNameTextView);
            songArtistTextView = itemView.findViewById(R.id.songArtistTextView);
            songLengthTimeTextView = itemView.findViewById(R.id.songLengthTimeTextView);
        }



    }
}
