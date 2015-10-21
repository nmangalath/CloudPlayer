package com.nmangalath.cloudplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Narayanan on 9/6/2015.
 * Based on : http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 * http://stackoverflow.com/questions/17146822/when-is-a-started-and-bound-service-destroyed
 */
public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public MediaPlayer mp;
    IBinder musicBind = new MusicBinder();
    private static final int NOTIFY_ID = 1;
    //    private boolean paused = false;
    SharedPreferences myspref;

    public static final String BROADCAST_PROGRESS = "com.nmangalath.CloudPlayer.progress";
    private final Handler handler = new Handler();
    Intent progressIntent;
    long mediaPosition;
    long mediaMax;

    DataStore cds;
    Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("CP:PlayerSrv", "onCreate entered");
        cds = MainActivity.getInstance().ds;
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        progressIntent = new Intent(BROADCAST_PROGRESS);
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("CP:PlayerSrv", "onStartCommand()");

        setupHandler();
        return START_STICKY;
    }

    private void setupHandler() {
        Log.i("CP:PlayerSrv", "entered setupHandler");
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000);

        }
    };

    private void LogMediaPosition() {
        if (cds.player_state.equals("playing")) {
            mediaPosition = mp.getCurrentPosition();
            mediaMax = mp.getDuration();
            progressIntent.putExtra("counter", mediaPosition);
            progressIntent.putExtra("mediamax", mediaMax);
            getApplicationContext().sendBroadcast(progressIntent);
        }
    }

    @Override
    public void onDestroy(){
        Log.i("CP:CP:PlayerSrv", "onDestroy Entered");
        handler.removeCallbacks(sendUpdatesToUI);
    }

    public void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(NOTIFY_ID);
    }

    public void initMediaPlayer() {
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("CP:CP:PlayerSrv", "onBind Entered");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("CP:CP:PlayerSrv", "onUnbind Entered");
//        mp.stop();
//        mp.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("CP:CP:PlayerSrv", "onPrepared Entered");
        mp.start();
//        MainActivity.getInstance().updateProgressbar();
//        Log.i("CP:PlaySrv", String.valueOf(mp.getDuration()));
        cds.player_state = "playing";
        setupNotification();
    }

    public void setupNotification(){
        //  From Google: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.earphone)
                        .setContentTitle("Cloud Player")
                        .setContentText(cds.playlist_selected.get(cds.song_index));
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        myspref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        SharedPreferences.Editor editor = myspref.edit();
        editor.putBoolean("NOTIFICATION", true);
        editor.apply();

        resultIntent.putExtra("fromNotification", true);

        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public class MusicBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("CP:CP:PlayerSrv", "onCompletion Entered");
        String BCAST = "com.nmangalath.CloudPlayer";
        Intent complete = new Intent(BCAST);
        cds.player_state = "null";
        getApplicationContext().sendBroadcast(complete);
    }

    public void playSong() {
        Log.i("CP:CP:PlayerSrv", "onPlaySong Entered");

        cds = MainActivity.getInstance().ds;
        cds.play_song_selected = cds.playlist_selected.get(cds.song_index);
        String track_id = cds.play_song_selected.substring(0, 8) + cds.play_song_selected.substring(10, 12);
        String track_location = cds.track_location.get(track_id);
        cds.play_location = cds.music_url + track_location;
        Log.i("CP:CP:PlayerSrv", cds.play_location);

        mp.reset();
        try {
            mp.setDataSource(cds.play_location);
        } catch (Exception e) {
            Log.e("CP:CP:PlayerSrv", "Error setting data source", e);
        }
        try {
            mp.prepare();   // prepareAsync was skipping songs
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resumeSong(){
        Log.i("CP:CP:PlayerSrv", "resumeSong entered");

        mp.start();
    }

    public void pauseSong() {
        Log.i("CP:CP:PlayerSrv", "pauseSong entered");

        mp.pause();
    }

    public void stopSong() {
        Log.i("CP:CP:PlayerSrv", "stopSong entered");

        mp.stop();
    }
}
