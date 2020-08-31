package com.speakerz.view.recyclerview;

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

import java.util.ArrayList;
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
        /*TODO: sikerült csak elfailel ha nemtudja megnyitni -->minden songnak van album artja csak van akinek nulla ettől függetlenül van helyefoglalva de az értéke null így nem lehet megnyitni ->exception de akinek van arra működik
         */
        if(currentItem.getAlbumId() == null){
            holder.mImageView.setImageResource(R.drawable.ic_song);
            System.out.println("nincs");
        }
        else{
            try{
                holder.mImageView.setImageURI(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), currentItem.getAlbumId()));
            }
            catch(Exception e){
                holder.mImageView.setImageResource(R.drawable.ic_song);
            }
            //holder.mImageView.setImageURI(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), currentItem.getAlbumId()));
            //System.out.println(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), currentItem.getAlbumId()));
            System.out.println("van");
        }
        holder.mTextView1.setText(currentItem.getTitle());
        holder.mTextView2.setText(currentItem.getArtist());
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
        public TextView mTextView1;
        public TextView mTextView2;
        public ImageView mDeleteImage;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.albumArt);
            mTextView1 = itemView.findViewById(R.id.textView);
            mTextView2 = itemView.findViewById(R.id.textView2);
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

