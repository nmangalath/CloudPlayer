package com.nmangalath.cloudplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Narayanan on 8/30/2015.
 * Based on http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/ *
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context ctx;
    private List<String> artists;
    private HashMap<String, List<String>> albums;

    public ExpandableListAdapter(Context ctx, List<String> artists, HashMap<String, List<String>> albums ){

        Log.i("ELSConstructor", "Entered");
        this.ctx = ctx;
        this.artists = artists;
        this.albums = albums;
    }
    @Override
    public int getGroupCount() {
        return artists.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return albums.get(artists.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return artists.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return albums.get(artists.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.i("getGroupView", "Entered");
        String artist = (String) getGroup(groupPosition);
        Log.i("getGroupView", artist);
        if(convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.artist_item, null);
        }
        TextView artist_view = (TextView) convertView.findViewById(R.id.artist_item);
        artist_view.setText(artist);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        Log.i("getChildView", "Entered");
        String album = (String)getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.album_list_item, null);
        }

        TextView album_view = (TextView) convertView.findViewById(R.id.albums_item);

        album_view.setText(album);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
