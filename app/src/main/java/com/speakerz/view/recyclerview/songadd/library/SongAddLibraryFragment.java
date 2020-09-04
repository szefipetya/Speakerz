package com.speakerz.view.recyclerview.songadd.library;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.speakerz.view.recyclerview.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SongAddLibraryFragment extends Fragment {

    private RecyclerView recyclerViewLibrary;

    private ArrayList<libraryItem> listLibrary = new ArrayList<>();
    private AdapterLibrary adapterLibrary;

    public void setModel(MusicPlayerModel model) {
        this.model = model;
        synchronized (modelIsNullLocker) {
            modelIsNullLocker.notify();
        }
        fillAudioList(model.getAudioList(), listLibrary);
        //  listLibrary = new ArrayList<>();
        // listLibrary.add(new libraryItem("Egy két há", "Belga", "mindegy", "2:45"));
        // listLibrary.add(new libraryItem("Daylight", "JOJI", "mindegy", "2:43"));


    }

    private void fillAudioList(List<Song> input, List<libraryItem> output) {
        if (output.isEmpty())
            for (Song s : input) {
                libraryItem e = new libraryItem(s.getTitle(), s.getArtist(), "kép_path", "idő");
                output.add(e);
            }
    }

    Object modelIsNullLocker=new Object();
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
                self.getActivity().onBackPressed();
            }
        });


       /* if (model == null) {
            synchronized (modelIsNullLocker) {
                try {
                    modelIsNullLocker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/
        adapterLibrary = new AdapterLibrary(mView.getContext(), listLibrary);

        recyclerViewLibrary.setAdapter(adapterLibrary);
        recyclerViewLibrary.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), recyclerViewLibrary, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        model.addSong(model.getAudioList().get(position));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );


        return mView;
    }


}
