package com.speakerz.view.recyclerview.main.player;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.model.Song;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<Song> mItemList;
    private OnItemClickListener mLisener;
    private int size;


    public Adapter(List<Song> exampleList) {
        mItemList = exampleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mLisener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        ViewHolder evh = new ViewHolder(v, mLisener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song currentItem = mItemList.get(position);
        if(currentItem.getSongCoverArt() == null){
            holder.mImageView.setImageResource(R.drawable.ic_song);
            System.out.println("nincs");
        }
        else{
            holder.mImageView.setImageBitmap(currentItem.getSongCoverArt());
        }
        holder.titleView.setText(currentItem.getTitle());
        holder.artistView.setText(currentItem.getArtist());
        holder.durationView.setText(currentItem.getDuration());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
    public int getSize() {
        return mItemList.size();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView titleView;
        public TextView artistView;
        public TextView durationView;
        public ImageView mDeleteImage;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.albumArt);
            titleView = itemView.findViewById(R.id.textView);
            artistView = itemView.findViewById(R.id.textView2);
            durationView = itemView.findViewById(R.id.text_song_time);
            mDeleteImage = itemView.findViewById(R.id.image_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            mDeleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}

