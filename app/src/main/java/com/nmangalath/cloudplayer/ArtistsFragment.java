package com.nmangalath.cloudplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Narayanan on 26/07/2015.
 */

public class ArtistsFragment extends Fragment {

    ExpandableListAdapter ela;
    ExpandableListView elv;
    DataStore ds;
    Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("ArtistFragment", "OnCreateView Entered");
        View rootView = (ViewGroup)inflater.inflate(R.layout.fragment_artists, container, false);
        DataStore cds = ((MainActivity)getActivity()).ds;
//        ctx = cds.ctx;
        elv = (ExpandableListView)rootView.findViewById(R.id.id_artists);
        Log.i("ArtistFragment Artists",cds.artists.toString());
        ela = new ExpandableListAdapter(getActivity(), cds.artists, cds.artist_albums);
        elv.setAdapter(ela);

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                DataStore cds = ((MainActivity) getActivity()).ds;
                if(!cds.player_state.equals("stopped") && !cds.player_state.equals("null")){
                    Toast.makeText(getActivity(), "Please stop music first before changing the playlist", Toast.LENGTH_LONG).show();
                    return false;
                }
                cds.play_list_type = "album";
                cds.album_selected = cds.artist_albums.get(cds.artists.get(groupPosition)).get(childPosition);
                Log.i("ArtistFragment", cds.album_selected);
                getActivity().getActionBar().setSelectedNavigationItem(0);
                ((MainActivity) getActivity()).mAdapter.notifyDataSetChanged();
                return false;
            }
        });

        elv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);
                Log.i("ArtistFragment", "Longclick");

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    DataStore cds = ((MainActivity) getActivity()).ds;
                    String album_selected = cds.artist_albums.get(cds.artists.get(groupPosition)).get(childPosition);
                    Log.i("ArtistFragmet Longclick", album_selected);

                    //do your per-item callback here
                    return false; //true if we consumed the click, false if not

                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    //do your per-group callback here
                    return false; //true if we consumed the click, false if not

                } else {
                    // null item; we don't consume the click
                    return false;
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


}
