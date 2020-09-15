package com.speakerz.view.recyclerview.join;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.debug.D;

import com.speakerz.view.recyclerview.join.itemJoin;

import java.util.ArrayList;

public class AdapterJoin extends RecyclerView.Adapter<AdapterJoin.ViewHolderJoin> {
    private Context contextJoin;
    private ArrayList<itemJoin> listItems;
    private OnItemClickListener mListener;
    private int row_index = -1;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolderJoin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contextJoin).inflate(R.layout.item_join, parent, false);
        return new ViewHolderJoin(v);
    }

    public AdapterJoin(ArrayList<itemJoin> list, Context context) {
        listItems = list;
        contextJoin = context;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolderJoin holder, final int position) {
        itemJoin currentItem = listItems.get(position);
        ContentResolver res = contextJoin.getContentResolver();


        String deviceName = currentItem.getDeviceName();
        String connectingText = currentItem.getConnectingText();


        holder.deviceTextView.setText(deviceName);
        holder.connectingTextView.setText(connectingText);

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index= position;
                notifyDataSetChanged();
            }
        });
        if(row_index == position){
            holder.deviceTextView.setTextColor(Color.parseColor("#ffffff"));
            holder.connectingTextView.setTextColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mobileImageView.setImageResource(R.drawable.ic_phone_android_white);
            }
            holder.mLayout.setBackgroundColor(Color.parseColor("#1f4068"));
        }
        else
        {
            holder.deviceTextView.setTextColor(Color.parseColor("#1f4068"));
            holder.connectingTextView.setTextColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mobileImageView.setImageResource(R.drawable.ic_phone_android_blue);
            }
            holder.mLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        D.log("clicked:"+position);
        //ez azért kell, hogyha vissza görget, akkor azokat már ne adja hozzá.
        /*if(!tabooPositions.contains(position)){
        AdapterLibraryEvent.invoke(new EventArgs2<VIEW_EVT, Integer>(this,VIEW_EVT.ADAPTER_SONG_SCROLL,position));
        tabooPositions.add(position);
        }*/
        //Nem tudom hogy ez mennyire kell ide, de inkább itt hagyom

    }

    /*List<Integer> tabooPositions=new ArrayList<>();
    Event<EventArgs2<VIEW_EVT,Integer>> AdapterLibraryEvent;*/

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    public static class ViewHolderJoin extends RecyclerView.ViewHolder{
        public TextView deviceTextView;
        public TextView connectingTextView;
        public ImageView mobileImageView;
        public ConstraintLayout mLayout;

        public ViewHolderJoin(@NonNull View itemView) {
            super(itemView);
            deviceTextView = itemView.findViewById(R.id.songArtistTextView);
            connectingTextView = itemView.findViewById(R.id.songLengthTimeTextView);
            mobileImageView = itemView.findViewById(R.id.imageDevice);
            mLayout = itemView.findViewById(R.id.layout_item);

        }



    }
}
