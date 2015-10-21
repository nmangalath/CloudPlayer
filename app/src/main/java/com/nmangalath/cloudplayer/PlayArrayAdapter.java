package com.nmangalath.cloudplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Narayanan on 15/09/2015.
 */
public class PlayArrayAdapter extends ArrayAdapter<String> {

//    private HashMap<Integer, Boolean> myChecked = new HashMap<Integer, Boolean>();
    private ArrayList<String> playlist;
    private LayoutInflater inflater;
    DataStore cds;
    Context ctx;

    public PlayArrayAdapter(Context context, int resource,
                            int textViewResourceId, ArrayList<String> objects) {
        super(context, resource, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
        this.playlist = objects;
        ctx = context.getApplicationContext();
        cds = MainActivity.getInstance().ds;
//        Log.i("PlayArrayAdapter", this.playlist.toString());

        for (int i = 0; i < objects.size(); i++) {
            cds.myChecked.put(i, false);
        }
    }

    public void toggleChecked(int position) {
        Log.i("CP:PAA", cds.player_state+":"+cds.manual_click);
        if (!cds.player_state.equals("null") && !cds.player_state.equals("stopped") && cds.manual_click){
            Toast.makeText(ctx, "Please stop music before new selection",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (cds.myChecked.get(position)) {
            cds.myChecked.put(position, false);
            cds.playlist_selected.remove(this.playlist.get(position));
        } else {
            cds.myChecked.put(position, true);
            cds.playlist_selected.add(this.playlist.get(position));
        }
        Log.i("CP:PAA",cds.playlist_selected.toString());

                notifyDataSetChanged();

    }

    public ArrayList<Integer> getCheckedItemPositions() {
        ArrayList<Integer> checkedItemPositions = new ArrayList<Integer>();

        for (int i = 0; i < cds.myChecked.size(); i++) {
            if (cds.myChecked.get(i)) {
                (checkedItemPositions).add(i);
            }
        }

        return checkedItemPositions;
    }
//
//    public List<String> getCheckedItems() {
//        List<String> checkedItems = new ArrayList<String>();
//
//        for (int i = 0; i < myChecked.size(); i++) {
//            if (myChecked.get(i)) {
////                (checkedItems).add(this.playlist.get(i));
//                cds.playlist_selected.add(this.playlist.get(i));
//                Log.i("PlayArrayAdapter", cds.playlist_selected.toString());
//            }
//        }
//
//        return checkedItems;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
//            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.play_list_item, parent, false);
        }

        CheckedTextView checkedTextView = (CheckedTextView) row.findViewById(R.id.play_list_item);
        checkedTextView.setText(this.playlist.get(position));

        Boolean checked = cds.myChecked.get(position);
        if (checked != null) {
            checkedTextView.setChecked(checked);
        }

        return row;
    }

}

