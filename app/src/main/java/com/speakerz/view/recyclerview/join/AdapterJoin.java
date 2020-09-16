package com.speakerz.view.recyclerview.join;

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
import com.speakerz.model.network.WifiP2pService;

import java.util.ArrayList;

public class AdapterJoin extends RecyclerView.Adapter<AdapterJoin.ViewHolderJoin> {
    private Context contextJoin;
    private ArrayList<WifiP2pService> listItems;

    public int selectedRowIndex = -1;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }


    @NonNull
    @Override
    public ViewHolderJoin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contextJoin).inflate(R.layout.item_join, parent, false);
        return new ViewHolderJoin(v);
    }

    public AdapterJoin(ArrayList<WifiP2pService> list, Context context) {
        listItems = list;
        contextJoin = context;
    }

    private static String textFromConnectionStatus(int num){

        if(num==WifiP2pService.SERVICE_STATUS_CONNECTED)
            return "Connected";
        if(num==WifiP2pService.SERVICE_STATUS_CONNECTING)
            return "Connecting...";
        if(num==WifiP2pService.SERVICE_STATUS_CONNECTION_FAILED)
            return "Connection failed";
        if(num==WifiP2pService.SERVICE_STATUS_CONNECTION_FAILED_WAIT)
            return "Try again in 5seconds. \nCleaning up after previous session...";
        if(num==WifiP2pService.SERVICE_STATUS_DISCONNECTED)
            return "Disconnected";
        return "-";
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderJoin holder, final int position) {
        WifiP2pService currentItem = listItems.get(position);

        String connectingText = textFromConnectionStatus(currentItem.connectionStatus);


        holder.deviceTextView.setText(currentItem.toString());
        holder.connectingTextView.setText(connectingText);

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRowIndex = position;
                notifyDataSetChanged();
            }
        });
        if(currentItem.connectionStatus==WifiP2pService.SERVICE_STATUS_CONNECTING){
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
            deviceTextView = itemView.findViewById(R.id.text_deviceName);
            connectingTextView = itemView.findViewById(R.id.text_status);
            mobileImageView = itemView.findViewById(R.id.imageDevice);
            mLayout = itemView.findViewById(R.id.layout_item);

        }



    }
}
