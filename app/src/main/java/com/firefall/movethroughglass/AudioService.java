package com.firefall.movethroughglass;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class AudioService extends IntentService {

    /*
    *  NOTE: Any References to AUDIO_SPEED_FLAG should be using R.string.*_speed or get the value from Routine object
    */
    private MediaPlayer player;
    private String absolutePath;
    private Controller theController;
    private String[] audioFiles;
    private Resources res;
    private Routine routine;


    public AudioService() {
        super("");
        Log.i("FUNCTION_CALL", "Audio Create");
        theController = Controller.getController();
        routine = theController.getRoutine();
        //absoluePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        absolutePath = "android.resource://"+theController.getPackageName()+"/raw/";

        res = Controller.getController().getResources();
        audioFiles = this.res.getStringArray(R.array.walk_with_me_audio);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("FUNCTION_CALL", "AudioService onBind()");
        return null;
    }


    @Override
    public void onCreate() {
        Log.i("FUNCTION_CALL", "AudioService onCreate()");
        super.onCreate();

        try {
            player = new MediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("FUNCTION_CALL", "AudioService onStartCommand()");
        Uri currentAudioFile = Uri.parse(absolutePath + audioFiles[routine.getVideoPosition()]);
        System.out.println(currentAudioFile);

        try {
            if ( player != null && !player.isPlaying() ) {
                player.setDataSource(String.valueOf(currentAudioFile));
                player.setLooping(true);
                player.prepare();
                player.start();
            }
        } catch (Exception e) {
            Log.e("FUNCTION_CALL", "AudioService.onStartCommand() EXCEPTION");
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }


    public void onStart(Intent intent, int startId) {
        // TO DO
        Log.i("FUNCTION_CALL", "AudioService.onStart()");
    }


    @Override
    protected void onHandleIntent(Intent i) {
        Log.i("FUNCTION_CALL", "AudioService.onHandleIntent()");

        if ( player.isPlaying() ) {
            player.stop();
            player.release();
        } else {
            player.release();
        }
    }


    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        Log.i("FUNCTION_CALL", "AudioService.onUnBind()");
        return null;
    }


    public void onStop() {
        Log.i("FUNCTION_CALL", "AudioService.onStop()");
        player.release();
    }


    public void onPause() {
        Log.i("FUNCTION_CALL", "AudioService.onPause()");
        player.release();
    }


    @Override
    public void onDestroy() {
        Log.i("FUNCTION_CALL", "AudioService.onDestroy()");

        if ( player.isPlaying() ) {
            player.stop();
            player.release();
        } else {
            player.release();
        }
        super.onDestroy();
    }


    @Override
    public void onLowMemory() {
        Log.e("FUNCTION_CALL", "AudioService.onLowMemory()");
    }
}