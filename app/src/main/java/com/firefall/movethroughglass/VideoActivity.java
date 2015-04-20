package com.firefall.movethroughglass;

import android.util.Log;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.net.Uri;


public class VideoActivity extends Activity implements TextureView.SurfaceTextureListener {


    private MediaPlayer mMediaPlayer = null;
    private TextureView mPreview;
    private Intent backgroundAudio;
    private GestureDetector mGestureDetector;
    private Routine routine;
    private Controller theController;

    //private final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i("FUNCTION_CALL", "VideoActivity onCreate()");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Don't let screen dim during use
        theController = Controller.getController();
        routine = theController.getRoutine();
//        routine.dumpRoutine();
        backgroundAudio = new Intent(this, AudioService.class);
        mGestureDetector = createGestureDetector(this);
        mPreview = new TextureView(this);
        mPreview.setSurfaceTextureListener(this);
        setContentView(mPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("FUNCTION_CALL", "VideoActivity onDestroy()");

        // Stop and release resources on activity destroyed
        if ( mMediaPlayer != null ) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();

            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if ( mGestureDetector != null ) mGestureDetector = null;
    }


    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureAvailable.");

        try {
            // If mMediaPlayer isn't instantiated, create the media player and prepare it.
            if ( this.mMediaPlayer == null ) {
                Surface s = new Surface(surface);
                mMediaPlayer = new MediaPlayer();
                Log.v("VideoActivity",routine.getVideoUrl());
                mMediaPlayer.setDataSource(this,Uri.parse(routine.getVideoUrl()));

                //mMediaPlayer.setDataSource(this,Uri.parse("android.resource://"+getPackageName()+"/raw/balance_me_lallegro")); // WORKS
                //mMediaPlayer.setDataSource(getAssets().openFd("Balance_Me_LAllegro.mp4").getFileDescriptor()); // DOESN'T WORK
                //mMediaPlayer.setDataSource(this, Uri.parse("file:///android_assets/Balance_Me_LAllegro.mp4"));  //DOESN'T WORK
                //mMediaPlayer.setDataSource(this,Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.balance_me_lallegro)); //WORKS

                mMediaPlayer.setSurface(s);
                mMediaPlayer.prepare();

                // If video set is not "Walk With Me" then set volume, else keep it muted for Walk With Me
                if (!routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                else if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger)) && routine.getVideoPosition() == 0) mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                else mMediaPlayer.setVolume(0, 0);

                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Log.i("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureAvailable OnPrepared.");
                        handleBackgroundAudio("start");
                        mediaPlayer.start();
                    }
                });
            }

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureAvailable OnCompletion.");
                    // Stop and release resources
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();

                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;

                        if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger)) && routine.getVideoPosition() == 0) finishWalkWithMeVideo("forward");
                        else finishVideo("forward");
                    }
                }
            });
        }
        catch (Exception e) {
            Log.e("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureAvailable Exception Thrown.");
            e.printStackTrace();
            if ( mMediaPlayer != null ) {
                if ( mMediaPlayer.isPlaying() ) mMediaPlayer.stop();

                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    // BEGIN: Required stubs for TextureView
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureSizeChanged.");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i("FUNCTION_CALL", "VideoActivity TextureView onSurfaceTextureDestroyed.");
        if ( mMediaPlayer == null || !mMediaPlayer.isPlaying() ) return true;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Updates every single frame -- nothing needs to go here
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.SWIPE_DOWN) {
                    Log.i("GESTURE_EVENT", "VideoActivity.createGestureDetector() onGesture() SWIPE DOWN");
//                    routine.dumpRoutine();

                    handleBackgroundAudio("stop");

                    // If
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishWalkWithMeVideo("backward");
                    else finishVideo("backward");
                    return true;
                } else if (gesture == Gesture.TAP) {
                    Log.i("GESTURE_EVENT", "VideoActivity.createGestureDetector() onGesture() TAP");
//                    routine.dumpRoutine();

//                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger)) && routine.getVideoPosition() == 0) finishWalkWithMeVideo("forward");
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishWalkWithMeVideo("forward");
                    else finishVideo("forward");
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }


    // Send generic motion events to the gesture detector
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return ((mGestureDetector != null) ? mGestureDetector.onMotionEvent(event) : false);
    }

    // edge case -- intro causes Walk with me to diverge from all other logic. JUST intro to slow video.
    private void finishWalkWithMeVideo(String direction) {
        Intent i = new Intent(theController, VideoActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (direction.equals("forward")) {
            handleBackgroundAudio("stop");
            routine.setVideoPosition(Controller.moveToNext());
        }

        else routine.setVideoPosition(Controller.moveToPrevious());

        startActivity(i);
        finish();
    }

    private void finishVideo(String direction) {
        Intent i = new Intent(theController, TransitionActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            if (direction.equals("forward")) {
                if (routine.getVideoPosition() == 0 && routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                    startActivity(i);
                    finish();
                }
//                else if (routine.getVideoPosition() == 4 && routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
//                    startActivity(i);
//                    finish();
//                }
                else {
                    try{
                        routine.setVideoPosition(Controller.moveToNext());
                    } catch (Exception e) {
                        System.out.println("Oh Uh");
                    }
                }
            }
            else routine.setVideoPosition(Controller.moveToPrevious());
        } catch (Exception e) {
            Log.wtf("ERROR", "Exiting with an error.");
            System.exit(0);
        }

        startActivity(i);
        finish();
    }

    private void handleBackgroundAudio(String state) {
        if (state.equals("start")) {
            // If routine is Walk With Me, start the background audio
            if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger)) && routine.getVideoPosition() > 0) {
                if(!theController.isServiceRunning(AudioService.class)) {
                    Log.i("FUNCTION_CALL", "Building Walk With Me Audio Content content.");
                    startService(backgroundAudio);
                }
            }
        } else if (state.equals("stop")) {
            // Tear down background audio service if it happens to be running.
            if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                Log.i("FUNCTION_CALL", "Tearing down Walk With Me Audio Content");
                if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(VideoActivity.this, AudioService.class));
            }
        }
    }
}