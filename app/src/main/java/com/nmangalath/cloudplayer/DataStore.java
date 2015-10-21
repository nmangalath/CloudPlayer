package com.nmangalath.cloudplayer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Narayanan on 8/08/2015.
 */
public class DataStore {
    Context ctx;
    public String play_list_type = "None";
    public String raga_selected = "None";
    public String song_selected = "None";
    public String album_selected = "None";
    public static String music_url = "http://5mr.mooo.com:8001//owncloud/index.php/s/QSAPS2zQ9gDnhIP/download?path=%2Fcarnatic_music";

    public String play_location="None";

    List<String> ragas = new ArrayList<String>();
    List<String> songs = new ArrayList<String>();
    List<String> artists = new ArrayList<String>();

    HashMap<String, String> track_id_info = new HashMap<>();
    HashMap<String, String> album_id_info = new HashMap<>();
    HashMap<String, ArrayList<String>> raga_tracks = new HashMap<>();
    HashMap<String, ArrayList<String>> song_tracks = new HashMap<>();
    HashMap<String, ArrayList<String>> album_id_tracks = new HashMap<>();
    HashMap<String, String> track_location = new HashMap<>();
    HashMap<String, ArrayList<String>> artist_id_albums = new HashMap<>();
    HashMap<String, List<String>> artist_albums = new HashMap<>();
    HashMap<String, String> artist_id_name = new HashMap<String, String>();

    ArrayList<String> playlist = new ArrayList<String>();
    ArrayList<String> playlist_selected = new ArrayList<>();
    ArrayList<String> prev_list = new ArrayList<String>();
    ArrayList<Integer> prev_checked = new ArrayList<Integer>();
    HashMap<Integer, Boolean> myChecked = new HashMap<Integer, Boolean>();
    ArrayList<Integer> checked_positions = new ArrayList<Integer>();

    public String play_song_selected = "";
    public int song_index = 0;
    public String player_state = "null";
    public boolean manual_click = false;
    Boolean plaFragmentLive = false;

    DataStore(Context context){

        ctx = context;
    }

    public void create_data_structure(){

        create_raga_tracks();
        create_ragas();
        create_song_tracks();
        create_songs();
        create_track_id_info();
        create_track_location();
        create_artist_id_name();
        create_artists();
        create_artist_id_albums();
        create_album_id_info();
        create_artist_albums();
        create_album_id_tracks();
    }
    public void create_raga_tracks(){

        Log.i("create_raga_tracks", "Entered");
        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "raga_tracks");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String raga = TextUtils.split(line, ":")[0];
                String tracks_csv = TextUtils.split(line, ":")[1];
                ArrayList<String>tracks = new ArrayList<String>(Arrays.asList(tracks_csv.split(",")));
                raga_tracks.put(raga, tracks);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public void create_ragas(){

        Log.i("create_ragas", "Entered");
        ragas.clear();
        for (String raga : raga_tracks.keySet()){
            ragas.add(raga);
        }
        Collections.sort(ragas);
    }

    public void create_song_tracks(){

        Log.i("create_song_tracks", "Entered");
        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "song_tracks");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String song = TextUtils.split(line, ":")[0];
                String tracks_csv = TextUtils.split(line, ":")[1];
                ArrayList<String>tracks = new ArrayList<String>(Arrays.asList(tracks_csv.split(",")));
                song_tracks.put(song, tracks);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void create_songs(){

        songs.clear();
        for (String song : song_tracks.keySet()){
            songs.add(song);
        }
        Collections.sort(songs);
    }

    public void create_track_id_info(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "track_id_info");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String track_id = TextUtils.split(line, ":")[0];
                String info = TextUtils.split(line, ":")[1];
                track_id_info.put(track_id, info);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void create_track_location(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "track_location");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String track_id = TextUtils.split(line, ":")[0];
                String location = TextUtils.split(line, ":")[1];
                track_location.put(track_id, location);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void create_artist_id_name(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "artist_id_name");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String artist_id = TextUtils.split(line, ":")[0];
                String name = TextUtils.split(line, ":")[1];
                artist_id_name.put(artist_id, name);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void create_artists(){

        artists.clear();
        for (String artist : artist_id_name.values()){
            artists.add(artist);
        }
        Collections.sort(artists);
    }

    public void create_artist_id_albums(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "artist_id_albums");
        StringBuilder text = new StringBuilder();
        artist_id_albums.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String artist = TextUtils.split(line, ":")[0];
                String albums_csv = TextUtils.split(line, ":")[1];
                ArrayList<String>albums = new ArrayList<String>(Arrays.asList(albums_csv.split(",")));
                artist_id_albums.put(artist, albums);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void create_artist_albums() {

        artist_albums.clear();
        ArrayList<String> album_ids = new ArrayList<String>();
        for (String artist_id : artist_id_name.keySet()) {
            String artist = artist_id_name.get(artist_id);
            album_ids = artist_id_albums.get(artist_id);

            ArrayList albums = new ArrayList<String>();
            albums.clear();
            for (String album_id : album_ids) {
                String album_full = album_id_info.get(album_id);
                String album = album_full.split("-")[0];
                albums.add(album);
            }
            artist_albums.put(artist, albums);
        }
    }

    public void create_album_id_info(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "album_id_info");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String album_id = TextUtils.split(line, ":")[0];
                String info = TextUtils.split(line, ":")[1];
                album_id_info.put(album_id, info);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void create_album_id_tracks(){

        String files_dir = ctx.getFilesDir().toString();
        File file = new File(files_dir, "album_id_tracks");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String album_id = TextUtils.split(line, ":")[0];
                String tracks_csv = TextUtils.split(line, ":")[1];
                ArrayList<String>tracks = new ArrayList<String>(Arrays.asList(tracks_csv.split(",")));
                album_id_tracks.put(album_id, tracks);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}


