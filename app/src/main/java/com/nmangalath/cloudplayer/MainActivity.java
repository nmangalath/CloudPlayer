package com.nmangalath.cloudplayer;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;

import com.nmangalath.cloudplayer.PlayerService.MusicBinder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    public TabsPagerAdapter mAdapter;
    private android.app.ActionBar actionBar;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private static MainActivity instance;
    public DataStore ds = new DataStore(this);
    public Boolean download_complete;

    private Handler mHandler = new Handler();
    SharedPreferences myspref;
    boolean from_notification;
    boolean cameFromNotification;

    private PlayerService musicSrv;
    private Intent playIntent;

    private boolean musicBound = false;
    String BCAST = "com.nmangalath.CloudPlayer";
    BroadcastReceiver receiver;
    IntentFilter filter = new IntentFilter(BCAST);

    String BROADCAST_PROGRESS = "com.nmangalath.CloudPlayer.progress";
//    Intent progressIntent;
    BroadcastReceiver progressReceiver;
    IntentFilter progresFilter = new IntentFilter(BROADCAST_PROGRESS);
    int mediaPosition;
    int mediaMax;

    ImageButton play_button;
    TextView artist_name, album_name1, album_name2, song_info;
    public MediaPlayer mp;

    // Tab titles
    private String[] tabs = {"Play", "Artists ", "Songs", "Ragas"};
    public String[] data_structure_files = {"date", "raga_tracks", "track_id_info", "track_location", "song_tracks", "album_id_info", "album_id_tracks", "artist_id_albums", "artist_id_name"};
    private static String music_url = "http://5mr.mooo.com:8001/owncloud/index.php/s/QSAPS2zQ9gDnhIP/download?path=%2Fcarnatic_music";
    private static String data_structure_url = "http://5mr.mooo.com:8001/owncloud/index.php/s/QSAPS2zQ9gDnhIP/download?path=%2Fdata_structure%2F";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        Log.i("CP:Main", "onCreate Entered");
        instance = this;
//        progressIntent = new Intent(this, PlayerService.class);

        download_complete = true;
//        downloadFiles();
        ds.create_data_structure();

        actionBar = getActionBar();
        ColorDrawable d = new ColorDrawable();
        d.setColor(0xff607d8B);
        actionBar.setBackgroundDrawable(d);

        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the Tabs
        for (String tab_name : tabs) {
            Tab tab = actionBar.newTab();

            tab.setText(tab_name);
            tab.setTabListener(this);
            actionBar.addTab(tab);
        }
        //Checked if my service is running
//        if (!isPlayerServiceRunning()) {
//            startService(new Intent(this,PlayerService.class));
//        }

        startService();
//        registerReceiver(progressReceiver, progresFilter);
//        Log.i("CP:Main", "registered");

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        receiver = new BroadcastReceiver () {
            @Override
            public void onReceive(Context context, Intent intent) {
                nextTrack() ;
            }
        };
        // This notofication is coming from Player Service LogMediaPosition to update the
        // Now Playing information. Update only if the Play fragment is visible
        progressReceiver = new BroadcastReceiver () {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(ds.plaFragmentLive){
                    updateUI(intent);
                    if(download_complete) {
                        display_nowPlaying();
                    }
                }
            }
        };

    }

    public void startService(){
        if (!isPlayerServiceRunning()) {
            startService(new Intent(this,PlayerService.class));
        }
    }

    private boolean isPlayerServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (PlayerService.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void nextTrack(){
        if(ds.song_index < ds.playlist_selected.size() - 1) {
            Log.i("CP:Main:nextTrack",ds.song_index + ":"+ds.playlist_selected.toString());

            ds.song_index++;
            ds.player_state = "playing";
            play_button = (ImageButton) findViewById(R.id.media_play);
            play_button.setImageResource(R.drawable.pause);
            playTrack();
            display_nowPlaying(); // ???? is this needed?
        }
        else{
            Toast.makeText(getApplicationContext(), "At the end of List\nNo Next Song", Toast.LENGTH_LONG).show();
        }
    }

    public void prevTrack(){
        if(ds.song_index > 0) {
            ds.song_index--;
            ds.player_state = "playing";
            play_button = (ImageButton) findViewById(R.id.media_play);
            play_button.setImageResource(R.drawable.pause);
            playTrack();
            display_nowPlaying();
        }
        else{
            Toast.makeText(getApplicationContext(), "At the end of List\nNo Previous Song", Toast.LENGTH_LONG).show();
        }
    }

    public void playTrack(){
        musicSrv.playSong();
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent progressIntent) {
//            Log.i("CP:Main", "receiver Entered");
//            updateUI(progressIntent);
//        }
//    };

    private void updateUI(Intent intent) {
//        Log.i("CP:Main", "updateUI Entered");
        long counter = intent.getLongExtra("counter", 1L);
        long mediamax = intent.getLongExtra("mediamax", 1L);
        TextView curr_pos = (TextView) findViewById(R.id.play_curr_pos);
        TextView total_dur = (TextView) findViewById(R.id.play_dur);
        curr_pos.setText(" "+ milliSecondsToTimer(counter));
        total_dur.setText(" " + milliSecondsToTimer(mediamax));
    }

    public void updateProgressbar(){
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {

        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();
            TextView curr_pos = (TextView) findViewById(R.id.play_curr_pos);
            TextView total_dur = (TextView) findViewById(R.id.play_dur);
//            SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
            curr_pos.setText(" "+ milliSecondsToTimer(currentDuration));
            total_dur.setText(" " + milliSecondsToTimer(totalDuration));
//            int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
//            seekbar.setProgress(progress);
            mHandler.postDelayed(this, 100);
        }
    };

    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();

    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public void display_nowPlaying(){

        artist_name = (TextView) findViewById(R.id.play_artist_name);
        album_name1 =(TextView) findViewById(R.id.play_album_name1);
        album_name2 =(TextView) findViewById(R.id.play_album_name2);
        song_info = (TextView) findViewById(R.id.play_song);
        ds.play_song_selected = ds.playlist_selected.get(ds.song_index);
        String track_id = ds.play_song_selected.substring(0, 8) + ds.play_song_selected.substring(10, 12);
        String track_info = ds.track_id_info.get(track_id);
//        String track_location = ds.track_location.get(track_id);
//        ds.play_location = ds.music_url + track_location;
        // Display Artist and Album Info
        String artist_id = ds.play_song_selected.substring(0, 3);
        String artist = ds.artist_id_name.get(artist_id);
        artist_name.setText(artist);

        String album_id = ds.play_song_selected.substring(0, 8);
        String [] album_info = ds.album_id_info.get(album_id).split("-");
        String album_info1 = album_info[0];
        album_name1.setText(album_info1);
        if(album_info.length>1) {
            String album_info2 = album_info[1];
            album_name2.setText(album_info2);
        }
        song_info.setText(track_info);
    }

    ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            musicSrv = binder.getService();
            Log.i("CP:Main", "Service Connected");
            musicBound = true;
            mp = musicSrv.mp;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }


    };

    @Override
    protected void onStart() {
        Log.i("CP:Main", "onStart Entered");
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("CP:Main", "onResume Entered");
        registerReceiver(receiver, filter);
        registerReceiver(progressReceiver, progresFilter);
        doBindService();
    }

    private void doBindService() {
        bindService(new Intent(this, PlayerService.class), musicConnection,
                Context.BIND_AUTO_CREATE);
    }
    private void doUnbindService() {
        if (musicBound) {
            unbindService(musicConnection);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onPause(){
        doUnbindService();
        super.onPause();
        Log.i("CP:Main", "onPause Entered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("CP:Main", "OnDestroy Entered");
        unregisterReceiver(receiver);
        unregisterReceiver(progressReceiver);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {

        viewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // https://developer.android.com/training/basics/actionbar/adding-buttons.html
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "This is not implemented yet",
                    Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_exit:
                doExit();
                return true;
            case R.id.action_refresh:
                doRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void doExit(){
        Log.i("CP:Main", "doExit entered");

        ds.playlist_selected.clear();
        ds.checked_positions.clear();
        ds.song_index = 0;
        ds.player_state = "stopped";

        for(int i = 0; i < ds.checked_positions.size(); i++){
            ds.manual_click = false;
            int j = ds.checked_positions.get(i);
            mAdapter.pf.playAdapter.toggleChecked(j);
        }
        musicSrv.stopSong();
        mp.release();
        musicSrv.cancelNotification();
        stopService(new Intent(this, PlayerService.class));
        finish();
    }

    public void doRefresh(){
        download_complete = false;
        downloadFiles();
    }

    private void downloadFiles() {
        String[] param = new String[9];

        for (int i = 0; i < 9; i++) {
            param[i] = data_structure_url + data_structure_files[i];
        }
        new DownloadFileFromURL().execute(param);
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... param) {

            int count;
            Log.i("CP:Main", "In doInBackground");
            try {
                for (int i = 0; i < param.length; i++) {

                    URL url = new URL(param[i]);
                    Log.i("CP:Main", url.toString());
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();    // get file length

                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    String files_dir = getFilesDir().toString();
                    String out_file = files_dir + '/' + data_structure_files[i];
                    OutputStream output = new FileOutputStream(out_file);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                        output.write(data, 0, count);
                    }

                    output.flush();

                    output.close();
                    input.close();

                    if (data_structure_files[i].equals( "date")) {
                        String date = files_dir + '/' + "date";
                        String old_date = files_dir + '/' + "old_date";
                        File file1 = new File(date);
                        File file2 = new File(old_date);
                        if (compareDates(date, old_date)) {
                            Log.i("CP:Main", "dates equal");
                            file2.delete();
                            file1.renameTo(file2);
                            break;
                        }

                        file2.delete();
                        file1.renameTo(file2);

                    }
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            download_complete = true;

            ds.create_data_structure();
            // Display Artist Fragment
            getActionBar().setSelectedNavigationItem(1);
            mAdapter.notifyDataSetChanged();

//            if (!isPlayerServiceRunning()) {
//                startService(new Intent(this,PlayerService.class));
//            }
//            startService();
        }

        public boolean compareDates(String date, String old_date)
                throws IOException {

            File file1 = new File(date);
            File file2 = new File(old_date);

            if (!file2.exists()) {
                return false;
            }

            BufferedReader br1 = new BufferedReader(new FileReader(file1));
            BufferedReader br2 = new BufferedReader(new FileReader(file2));

            String dateLine = br1.readLine();
            String old_dateLine = br2.readLine();

            Log.i("CP:Main", dateLine + ":" + old_dateLine);

            br1.close();
            br2.close();

            return dateLine.equals(old_dateLine);
        }
    }

    public void playButtonClicked(View view) {
        Log.i("CP:Main:Playbtn click", ds.player_state + ":"+ds.playlist_selected.size());

        play_button = (ImageButton) findViewById(R.id.media_play);
        switch (ds.player_state){
            case "playing":
                play_button.setImageResource(R.drawable.play);
                ds.player_state = "paused";
                musicSrv.pauseSong();
                break;
            case "paused":
                play_button.setImageResource(R.drawable.pause);
                ds.player_state = "playing";
                musicSrv.resumeSong();
                break;
            case "stopped":
            case "null":
                if(ds.playlist_selected.size() == 0){
                    Toast.makeText(getApplicationContext(), "Please select songs first", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    play_button.setImageResource(R.drawable.pause);
                    ds.player_state = "playing";
                    musicSrv.playSong();
                }
                break;
            default:
                break;
        }
    }

    public void prevButtonClicked(View view) {
        Log.i("CP:Main", "Prev button clicked");

        switch (ds.player_state) {
            case "playing":
            case "paused":
                musicSrv.stopSong();
                break;
            case "stopped":
            case "null":
                    Toast.makeText(getApplicationContext(), "Please select songs first", Toast.LENGTH_LONG).show();
                    return;
            default:
                break;
        }
        prevTrack();
    }

    public void nextButtonClicked(View view) {
        Log.i("CP:Main", "Next button clicked");

        switch (ds.player_state) {
            case "playing":
            case "paused":
                musicSrv.stopSong();
                break;
            case "stopped":
            case "null":
                if(ds.playlist_selected.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Please select songs first", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    ds.song_index = -1;
                }
            default:
                break;
        }
        nextTrack();
    }

    public void stopButtonClicked(View view) {
        Log.i("CP:Main", "Stop button clicked");

        play_button = (ImageButton) findViewById(R.id.media_play);
        play_button.setImageResource(R.drawable.play);

        for(int i = 0; i < ds.checked_positions.size(); i++){
            ds.manual_click = false;
            int j = ds.checked_positions.get(i);
            mAdapter.pf.playAdapter.toggleChecked(j);
        }
        ds.playlist_selected.clear();
        ds.checked_positions.clear();
        ds.song_index = 0;

//        for(int i = 0; i < ds.playlist.size(); i++ ){
//            if(ds.checked_positions.contains(i)){
//                ds.manual_click = false;
//                mAdapter.pf.playAdapter.toggleChecked(i);
//            }
//        }
        if(ds.player_state.equals("playing") || ds.player_state.equals("paused")) {
            musicSrv.stopSong();
        }
        ds.player_state = "stopped";
    }

    public static MainActivity getInstance(){
        return instance;
    }
}
