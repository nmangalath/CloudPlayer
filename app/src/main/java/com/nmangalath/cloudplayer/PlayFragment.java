package com.nmangalath.cloudplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.DTDHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Narayanan on 12/08/2015.
 */
public class PlayFragment extends Fragment {

//    private ArrayAdapter<String> listAdapter;
    public PlayArrayAdapter playAdapter;
    ArrayList<String> playlist;
    DataStore ds;
    Context ctx;
    TextView artist_info_line;
    TextView album_info_line1;
    TextView album_info_line2;
    ImageButton play_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_play, container, false);

        final DataStore cds = ((MainActivity) getActivity()).ds;
        cds.plaFragmentLive = true;

        ListView lv = (ListView) view.findViewById(R.id.play_list);
        artist_info_line = (TextView) view.findViewById(R.id.play_artist_name);
        album_info_line1 = (TextView) view.findViewById(R.id.play_album_name1);
        album_info_line2 = (TextView) view.findViewById(R.id.play_album_name2);

        play_button = (ImageButton) view.findViewById(R.id.media_play);
        if (!cds.player_state.equals("paused") && !cds.player_state.equals("null")){
            play_button.setImageResource(R.drawable.pause);
        }

//        playlist = cds.playlist;
        ctx = ((MainActivity) getActivity());

        Log.i("CP:PlayFrag", "onCreateView Called");

        if (cds.play_list_type.equals("raga")) {
            Log.i("CP:PlayFrag", "Raga selected");
            cds.playlist.clear();
            for (String track_id : cds.raga_tracks.get(cds.raga_selected)) {
                String track_info = cds.track_id_info.get(track_id);
                String track_line = track_id.substring(0, 8) + ": " + track_info.substring(0, track_info.length() - 4);
                cds.playlist.add(track_line);
            }
            playAdapter = new PlayArrayAdapter(getActivity(), R.layout.play_list_item, R.id.play_list_item, cds.playlist);
            lv.setAdapter(playAdapter);
        }
        else if (cds.play_list_type.equals("song")) {
            Log.i("CP:PlayFrag", "Song selected");
            cds.playlist.clear();
            for (String track_id : cds.song_tracks.get(cds.song_selected)) {
                String track_info = cds.track_id_info.get(track_id);
                String track_line = track_id.substring(0, 8) + ": " + track_info.substring(0, track_info.length() - 4);
                cds.playlist.add(track_line);
            }
            playAdapter = new PlayArrayAdapter(getActivity(), R.layout.play_list_item, R.id.play_list_item, cds.playlist);
            lv.setAdapter(playAdapter);
        }
        else if (cds.play_list_type.equals("album")) {
            Log.i("CP:PlayFrag", "album selected");
            cds.playlist.clear();
            String album_id = cds.album_selected.split(" ")[0];
            for (String track : cds.album_id_tracks.get(album_id)) {
                String track_line = album_id + ": " + track.substring(0, track.length() - 4);
                cds.playlist.add(track_line);
            }
            playAdapter = new PlayArrayAdapter(getActivity(), R.layout.play_list_item, R.id.play_list_item, cds.playlist);
            lv.setAdapter(playAdapter);

        }else{
            Log.i("CP:PlayFrag", "nothing selected");
            retrieve_objects();
            show_objects();
            playAdapter = new PlayArrayAdapter(getActivity(), R.layout.play_list_item, R.id.play_list_item, cds.playlist);
            lv.setAdapter(playAdapter);
            display_checked();
        }
        Log.i("CP:PlayFrag:PL", cds.playlist.toString());
        cds.play_list_type = "none";



//        Boolean same_list = true;
//        if(cds.playlist.size() != cds.prev_list.size()) {
//            same_list = false;
//        }
//        else{
//            for(int i=0; i<cds.playlist.size(); i++){
//                if (!cds.playlist.get(i).equals(cds.prev_list.get(i))){
//                    Log.i("CP:PlayFrag", "Different list displayed");
//                    same_list = false;
//                    break;
//                }
//            }
//            Log.i("CP:PlayFrag", "Same list displayed");
//        }
//        if(same_list){
//            for(int i = 0; i < cds.playlist.size(); i++ ){
//                if(cds.prev_checked.contains(i)){
//                    cds.manual_click = false;
//                    playAdapter.toggleChecked(i);
//                }
//            }
//        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!cds.player_state.equals("stopped") && !cds.player_state.equals("null")){
                    Toast.makeText(getActivity(), "Please stop music first before changing the playlist", Toast.LENGTH_LONG).show();
                    return;
                }
                cds.manual_click = true;
                playAdapter.toggleChecked(position);
                cds.checked_positions = playAdapter.getCheckedItemPositions();
                Log.i("CP:PlayFrag:", cds.checked_positions.toString());
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("CP:PlayFrag:", "onResume Called");
//        retrieve_objects();
//        display_checked();
    }
    public void display_checked(){
        DataStore cds = ((MainActivity) getActivity()).ds;
        ArrayList checked_postions = cds.checked_positions;
        cds.playlist_selected.clear();
        Log.i("CP:PlayFrag:dispchk:", checked_postions.toString());
        for(int i=0; i<checked_postions.size(); i++){
            cds.manual_click = false;
            playAdapter.toggleChecked(cds.checked_positions.get(i));
        }
    }

    public void show_objects(){
        DataStore cds = ((MainActivity) getActivity()).ds;
        Log.i("CP:PlayFrag:PL", cds.playlist.toString());
        Log.i("CP:PlayFrag:PLSel", cds.playlist_selected.toString());
        Log.i("CP:PlayFrag:Checked", cds.checked_positions.toString());
        Log.i("CP:PlayFrag:Checked", cds.myChecked.toString());
        Log.i("CP:PlayFrag:state", cds.player_state);
        Log.i("CP:PlayFrag:index", cds.song_index + " ");
    }

    public void save_objects() {
        // // http://www.javaworld.com/article/2076943/core-java/object-persistence-and-java.html
        // How do I refactor this?

        String files_dir = ctx.getFilesDir().toString();
        String out_file;
        DataStore cds = ((MainActivity) getActivity()).ds;

        out_file = files_dir + '/' + "playlist";
        Log.i("CP:PlayFrag", "saving objects");
//        Log.i("CP:PlayFrag", cds.playlist.toString());
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.playlist);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "playlist_selected";
//        Log.i("CP:PlayFrag", out_file);
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.playlist_selected);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "checked_positions";
//        Log.i("CP:PlayFrag", out_file);
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.checked_positions);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "mychecked";
//        Log.i("CP:PlayFrag", out_file);
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.myChecked);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "player_state";
//        Log.i("CP:PlayFrag", out_file);
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.player_state);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "song_index";
//        Log.i("CP:PlayFrag", out_file);
        try {
            FileOutputStream out = new FileOutputStream(out_file);
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(cds.song_index);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void retrieve_objects(){
        String files_dir = ctx.getFilesDir().toString();
        String out_file;
        ObjectInputStream ins;
        DataStore cds = ((MainActivity) getActivity()).ds;

        out_file = files_dir + '/' + "playlist";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
           cds.playlist = (ArrayList) ins.readObject();
//            cds.playlist = playlist;
//            Log.i("CP:PlayFrag:retPL", cds.playlist.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "playlist_selected";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
        cds.playlist_selected = (ArrayList) ins.readObject();
//            cds.playlist_selected = playlist_selected;
//            Log.i("CP:PlayFrag:retPLSel", cds.playlist_selected.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "checked_positions";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
         cds.checked_positions = (ArrayList) ins.readObject();
//            cds.checked_positions = checked_positions;
//            Log.i("CP:PlayFrag:retChecked", cds.checked_positions.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "mychecked";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
            cds.myChecked = (HashMap<Integer,Boolean>) ins.readObject();
//            cds.checked_positions = checked_positions;
//            Log.i("CP:PlayFrag:retChecked", cds.checked_positions.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "player_state";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
            cds.player_state = (String) ins.readObject();
//            ds.player_state = player_state;
//            Log.i("CP:PlayFrag:state", cds.player_state);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        out_file = files_dir + '/' + "song_index";
        try {
            FileInputStream in = new FileInputStream(out_file);
            ins = new ObjectInputStream(in);
            cds.song_index = (int) ins.readObject();
//            ds.player_state = player_state;
//            Log.i("CP:PlayFrag:retindex", cds.song_index + " ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//        ds.song_index=0;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("CP:PlayFrag:", "onPause Called");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("CP:PlayFrag:", "onStart Called");
        DataStore cds = ((MainActivity) getActivity()).ds;
        cds.plaFragmentLive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("CP:PlayFrag:", "onStop Called");
        save_objects();
        DataStore cds = ((MainActivity) getActivity()).ds;
        cds.plaFragmentLive = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("CP:PlayFrag:", "onDestroy Called");
//        show_objects();
//        save_objects();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("CP:PlayFrag:", "onDetach Called");
    }
}
