package com.speakerz.view.recyclerview.songadd.library;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.model.enums.VIEW_EVT;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;
import com.speakerz.view.recyclerview.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SongAddLibraryFragment extends Fragment {

    private RecyclerView recyclerViewLibrary;

    private List<Song> listLibrary = new ArrayList<>();
    private AdapterLibrary adapterLibrary;
    public final Event<EventArgs> CloseEvent=new Event<>();
    public void setModel(MusicPlayerModel model) {
        this.model = model;

        listLibrary = model.getAudioListFiltered();

    }

    SongAddLibraryFragment self = this;
    private MusicPlayerModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //FIXME ANDROID 4.4 -> crash
        final View mView = inflater.inflate(R.layout.fragment_song_adding_from_library, container, false);
        //
        recyclerViewLibrary = mView.findViewById(R.id.recyclerview_song_add_library);
        recyclerViewLibrary.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewLibrary.setLayoutManager(mLinearLayoutManager);

        ImageButton backButton = (ImageButton) mView.findViewById(R.id.button_back_to_main);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                D.log("back");
                // self.getActivity().getFragmentManager().popBackStack();
                ((PlayerRecyclerActivity)self.getActivity()).onBackPressed(true);
                CloseEvent.invoke(null);
            }
        });

        EditText songNameFilter = (EditText) mView.findViewById(R.id.name_filter);
        songNameFilter.setText(this.model.getSongFilter());
        songNameFilter.addTextChangedListener(new TextWatcher(){


            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                model.AdapterLibraryEvent.invoke(new EventArgs2<VIEW_EVT, String>(this,VIEW_EVT.ADAPTER_SONG_FILTER, editable.toString()));
                adapterLibrary.notifyDataSetChanged();
            }
        });



        adapterLibrary = new AdapterLibrary(mView.getContext(), listLibrary,model);

        recyclerViewLibrary.setAdapter(adapterLibrary);
        recyclerViewLibrary.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), recyclerViewLibrary, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(position>=0){
                            model.addSong(model.getAudioListFiltered().get(position));
                        }

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        adapterLibrary.notifyDataSetChanged();

        return mView;
    }


}
