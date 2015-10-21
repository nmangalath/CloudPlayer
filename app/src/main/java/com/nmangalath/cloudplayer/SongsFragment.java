package com.nmangalath.cloudplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Narayanan on 26/07/2015.
 */
public class SongsFragment extends Fragment {

    private ArrayAdapter<String> listAdapter ;
    DataStore ds;
    ViewGroup view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final DataStore cds = ((MainActivity)getActivity()).ds;
        Log.i("SongsFragment", cds.songs.toString());

        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_songs, container, false);
        ListView songsView = (ListView) view.findViewById(R.id.id_songs);
        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.songs_list_item, R.id.songs_item, cds.songs);
        songsView.setAdapter(listAdapter);

        EditText song_search = (EditText) view.findViewById(R.id.song_search);
        song_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                listAdapter.getFilter().filter(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        songsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!cds.player_state.equals("stopped") && !cds.player_state.equals("null")){
                    Toast.makeText(getActivity(), "Please stop music first before changing the playlist", Toast.LENGTH_LONG).show();
                    return;
                }

                DataStore cds = ((MainActivity)getActivity()).ds;
                cds.play_list_type = "song";
                cds.song_selected = cds.songs.get(position);
                Log.i("Raga - Raga:", cds.raga_selected);
                Log.i("Raga - Type:", cds.play_list_type);
                getActivity().getActionBar().setSelectedNavigationItem(0);
                ((MainActivity)getActivity()).mAdapter.notifyDataSetChanged();

            }
        });

        return view;
    }

//    EditText raga_search = (EditText) view.findViewById(R.id.raga_search);


}

