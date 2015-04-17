package com.firefall.movethroughglass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class TransitionActivity extends Activity {

    // Transition Card Names
    private static final int[] COUNTDOWN_CARDS = { R.drawable.transition_0, R.drawable.transition_1, R.drawable.transition_2, R.drawable.transition_3, R.drawable.transition_4, R.drawable.transition_5 };

    private Controller theController;
    private Routine routine;
    private GestureDetector mGestureDetector;
    private ImageView background;
    private TextView countdownText;
    private TextView moduleName;
    private TextView tapText;
    private TextView videoName;
    private TextView videoCount;
    private CountDownTimer transitionTimer;

    // Parse voice trigger, set up routine with context of theController and then retrieve proper layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TRANSITION FUNCTION", "TransitionActivity onCreate()");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Don't let screen dim during use
        theController = Controller.getController();
        int currentVoiceTrigger;
        mGestureDetector = createGestureDetector(this);

        try {
            Log.i("LOG", "TransitionActivity onCreate() - Establishing Voice Trigger, Creating Routine Object and setting up View");

            ActivityInfo activityInfo = this.getPackageManager().getActivityInfo(this.getComponentName(), PackageManager.GET_META_DATA);
            currentVoiceTrigger = (activityInfo.metaData != null) ? (activityInfo.metaData.getInt("com.google.android.glass.VoiceTrigger")) : 0;

            routine = (theController.getRoutine() == null) ? theController.createRoutine(currentVoiceTrigger) : theController.getRoutine();
            setContentView(routine.getLayout());
        } catch ( Exception e ) {
            e.printStackTrace();
            Log.wtf("ERROR", "Something bad happened and we never received a valid voice trigger or there was an ActivityInfo error");
            System.exit(1); // Hacky and should eventually be removed
        }

        moduleName = (TextView)findViewById(R.id.module_name);
        videoName = (TextView)findViewById(R.id.video_name);
        videoCount = (TextView)findViewById(R.id.video_count);
        background = (ImageView)findViewById(R.id.background);

        // Set up transition countdown timer, but don't start
        transitionTimer = new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int currentSeconds = safeLongToInt(((millisUntilFinished-1000)/1000));
                currentSeconds = (currentSeconds < 0) ? 0 : currentSeconds;
                background.setBackgroundResource(COUNTDOWN_CARDS[currentSeconds]); // Set background image
            }

            @Override
            public void onFinish() {
                Log.i("TRANSITION FUNCTION", " TransitionActivity runCountdown() onFinish()");
                finishTransitionForVideo("forward");
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TRANSITION FUNCTION", "TransitionActivity onResume()");
        setMainCardText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("TRANSITION FUNCTION", "TransitionActivity onDestroy()");

        if ( mGestureDetector != null ) mGestureDetector = null;
    }

    // Begin countdown to next video
    private void runCountdown() {
        Log.i("TRANSITION FUNCTION", "TransitionActivity runCountdown()");
        transitionTimer.start();
    }

    // long to int helper function for runCountdown()
    // Grabbed from here http://stackoverflow.com/a/1590842/873177
    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) throw new IllegalArgumentException (l + " cannot be cast to int without changing its value.");
        return (int) l;
    }

    // Handle all the settable text that appears on layout
    private void setMainCardText() {
        Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText()");

        switch( routine.getVoiceTriggerInt() ) {
            case R.xml.balance:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Balance Me");
                moduleName.setText("Balance Me");
                videoName.setText(routine.getVideoName());

                // Weird bug -- fast hacky fix -- on very 1st occurrence of routine being started, first dance name doesn't show. If you nav back to position 0 mid-routine the name DOES show, though.
                if (routine.getVideoPosition() == 0) videoName.setText("Sailor\'s \nDance");

                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.warm:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Warm Me Up");
                moduleName.setText("Warm Me Up");
                videoName.setText(routine.getVideoName());

                // Weird bug -- fast hacky fix -- on very 1st occurrence of routine being started, first dance name doesn't show. If you nav back to position 0 mid-routine the name DOES show, though.
                if (routine.getVideoPosition() == 0) videoName.setText("Mountains");

                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.unfreeze:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Unfreeze Me");
                moduleName.setText("Unfreeze Me");
                videoName.setText(routine.getVideoName());

                // Weird bug -- fast hacky fix -- on very 1st occurrence of routine being started, first dance name doesn't show. If you nav back to position 0 mid-routine the name DOES show, though.
                if (routine.getVideoPosition() == 0) videoName.setText("March");

                videoName.setText(routine.getVideoName());
                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.walk:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Walk With Me");
                //videoName.setText(routine.getVideoName());

                // If first Walk With Me file, its video intro not text card -- do finishTransitionForVideo()
                if (routine.getVideoPosition() == 0) finishTransitionForVideo("forward");
                else {

                    routine.dumpRoutine();

                    switch (routine.getVideoPosition()) {
                        case 2:
                            System.out.println("Loading Walk With Me -- Gentle Card");
                            background.setBackgroundResource(R.drawable.walk_with_me_gentle_transition); // Set background image
                            break;
                        case 3:
                            System.out.println("Loading Walk With Me -- Medium Card");
                            background.setBackgroundResource(R.drawable.walk_with_me_medium_transition); // Set background image
                            break;
                        case 4:
                            System.out.println("Loading Walk With Me -- Medium Fast Card");

                            background.setBackgroundResource(R.drawable.walk_with_me_medium_fast_transition); // Set background image
//
//                            if (routine.getVideoUrl().equals("/storage/emulated/0/DCIM/Camera/WWM_128_bpm.mp4")) {
//                                background.setBackgroundResource(R.drawable.walk_with_me_fast_transition);
//                            } else {
//                                background.setBackgroundResource(R.drawable.walk_with_me_medium_fast_transition); // Set background image
//                            }

                            break;
//                        case 5:
//                            System.out.println("Loading Walk With Me -- Fast Card");
//                            background.setBackgroundResource(R.drawable.walk_with_me_fast_transition); // Set background image
//                            break;
                        default:
                            // Hacky Fix to circumvent issues from Walk With Me Nav -- This will technically play the last "transition card"
                            System.out.println("Loading Walk With Me -- No Card -- Technically \"Last card\"");
                            background.setBackgroundResource(R.drawable.walk_with_me_fast_transition);
                            routine.setVideoPosition(4);
                            routine.setLast(1);
                    }
                }
                break;
            default:
                Log.wtf("CRITICAL_ERROR", "We should never be here, based off of voice trigger logic.");
                System.exit(1); // Hacky, remove later
        }
    }


    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.SWIPE_DOWN) { // Go Backwards in all other sets
                    Log.i("GESTURE_EVENT", "TransitionActivity createGestureDetector() onGesture() SWIPE DOWN");
//                    routine.dumpRoutine();

                    // If this is Walk With Me stop our background Audio Process, else clear text on transition card layout
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                        //if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(TransitionActivity.this, AudioService.class));
                    }
                    else {
                        moduleName.setText("");
                        videoCount.setText("");
                        videoName.setText("Loading...");
                    }

                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishTransitionForVideo("backward");
                    else finishTransitionForNextTransition("backward");

                    return true;
                } else if (gesture == Gesture.TAP) { // Go Forward in all other sets
                    Log.i("GESTURE_EVENT", "TransitionActivity createGestureDetector() onGesture() TAP");
//                    routine.setVideoPosition(Controller.moveToNext());
//                    routine.dumpRoutine();

                    // If this is Walk With Me stop our background Audio Process, else clear text on transition card layout
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                        if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(TransitionActivity.this, AudioService.class));
                    }
                    else {
                        moduleName.setText("");
                        videoCount.setText("");
                        videoName.setText("Loading...");
                    }

                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishTransitionForVideo("forward");
                    else finishTransitionForNextTransition("forward");

//                  if ( getIntent().getStringExtra(VIDEO_SET).equals("WALK_WITH_ME_FILES") && Integer.parseInt(getIntent().getExtras().get(PLAY_COUNT).toString()) >= 2 ) {
//                      stopService(new Intent(PostVideoActivity.this, AudioService.class));
//                      finish();
//                  } else if( getIntent().getStringExtra(VIDEO_SET).equals("WALK_WITH_ME_FILES") ) {
//                      finishit();
//                  }
                }
                return true;
            }
        });
        return gestureDetector;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return ((mGestureDetector != null) ? mGestureDetector.onMotionEvent(event) : false);
    }


    private void finishTransitionForVideo(String direction) {
        Log.i("TRANSITION FUNCTION", "TransitionActivity finishTransitionForVideo()");
        Intent i = new Intent(theController, VideoActivity.class);
//        theController.setRoutine(routine);

        System.out.println(direction + "   " + routine.getVideoUrl());

        // for walk with me navigation edge case
//        if (direction.equals("forward") && routine.getVideoUrl().equals("/storage/emulated/0/DCIM/Camera/WWM_128_bpm.mp4")) {
//            System.exit(1);
//        }
        if (routine.isLast() == 1 && direction.equals("forward")) {
            // Clean up any residual audio
            if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(TransitionActivity.this, AudioService.class));
            }
            System.exit(1);
        } else if (direction.equals("backward")) {
            routine.setLast(0);
            routine.setVideoPosition(Controller.moveToPrevious());
        }
        startActivity(i);
        finish();
    }

    private void finishTransitionForNextTransition(String direction) {
        Log.i("TRANSITION FUNCTION", "TransitionActivity finishTransitionForNextTransition()");
        Intent i = new Intent(theController, TransitionActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        System.out.println("Direction is " + direction);

        try {
            if (direction.equals("forward")) routine.setVideoPosition(Controller.moveToNext());
            else routine.setVideoPosition(Controller.moveToPrevious());
        } catch (Exception e) {
            Log.wtf("ERROR", "Exiting with an error.");
            System.exit(0);
        }

        theController.setRoutine(routine);
        transitionTimer.cancel();
        startActivity(i);
        finish();
    }
}
