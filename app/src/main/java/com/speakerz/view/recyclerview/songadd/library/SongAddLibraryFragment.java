package com.speakerz.view.recyclerview.songadd.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;

import java.util.ArrayList;

public class SongAddLibraryFragment extends Fragment {

    private RecyclerView recyclerViewLibrary;

    private ArrayList<libraryItem> listLibrary;
    private AdapterLibrary adapterLibrary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_song_adding_from_library, container, false);

        recyclerViewLibrary =mView.findViewById(R.id.recyclerview_song_add_library);
        recyclerViewLibrary.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewLibrary.setLayoutManager(mLinearLayoutManager);


        listLibrary = new ArrayList<>();
        listLibrary.add(new libraryItem("Egy két há", "Belga", "mindegy", "2:45"));
        listLibrary.add(new libraryItem("Daylight", "JOJI", "mindegy", "2:43"));

        adapterLibrary = new AdapterLibrary(mView.getContext(), listLibrary);

        recyclerViewLibrary.setAdapter(adapterLibrary);
        return mView;
    }
}
