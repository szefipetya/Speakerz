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
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;
import com.speakerz.view.recyclerview.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SongAddLibraryFragment extends Fragment {

    private RecyclerView recyclerViewLibrary;

    private ArrayList<libraryItem> listLibrary = new ArrayList<>();
    private AdapterLibrary adapterLibrary;
    public final Event<EventArgs> CloseEvent=new Event<>();
    public void setModel(MusicPlayerModel model) {
        this.model = model;

        fillAudioList(model.getAudioList(), listLibrary);
        AudioListUpdate=model.AudioListUpdate;
        AudioListUpdate.addListener(new EventListener<EventArgs1<Song>>() {
            @Override
            public void action(EventArgs1<Song> args) {
                Song s=args.arg1();
                libraryItem e = new libraryItem(s.getTitle(), s.getArtist(), s.getSongCoverArt(), s.getDuration());
                listLibrary.add(e);
                if(adapterLibrary!=null){
                    if(getActivity()!=null)
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 recyclerViewLibrary.post(new Runnable()
                                 {
                                     @Override
                                     public void run() {
                                         adapterLibrary.notifyDataSetChanged();
                                     }
                                 });
                             }
                         });
                }
            }
        });
        //  listLibrary = new ArrayList<>();
        // listLibrary.add(new libraryItem("Egy két há", "Belga", "mindegy", "2:45"));
        // listLibrary.add(new libraryItem("Daylight", "JOJI", "mindegy", "2:43"));


    }

    private void fillAudioList(List<Song> input, ArrayList<libraryItem> output) {
        List<Song> copy = new ArrayList<Song>(input);

        if (output.isEmpty())
            for (Song s : copy) {
                libraryItem e = new libraryItem(s.getTitle(), s.getArtist(), s.getSongCoverArt(), s.getDuration());
                output.add(e);
            }
    }

    public Event<EventArgs1<Song>>AudioListUpdate;

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



        adapterLibrary = new AdapterLibrary(mView.getContext(), listLibrary,model);

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
