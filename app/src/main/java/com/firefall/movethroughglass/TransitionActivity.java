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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class TransitionActivity extends Activity {

    // Transition Card Names
    private static final int[] COUNTDOWN_CARDS = { R.drawable.transition_0, R.drawable.transition_1, R.drawable.transition_2, R.drawable.transition_3, R.drawable.transition_4, R.drawable.transition_5 };
    private static final int[] WALK_WITH_ME_TRANSITION_CARDS = { R.drawable.walk_with_me_gentle_transition, R.drawable.walk_with_me_medium_transition, R.drawable.walk_with_me_medium_fast_transition, R.drawable.walk_with_me_fast_transition };

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
        theController = Controller.getController();
        int currentVoiceTrigger;
        mGestureDetector = createGestureDetector(this);

        try {
            Log.i("LOG", "TransitionActivity onCreate() - Establishing Voice Trigger, Creating Routine Object and setting up View");

            ActivityInfo activityInfo = this.getPackageManager().getActivityInfo(this.getComponentName(), PackageManager.GET_META_DATA);
            currentVoiceTrigger = (activityInfo.metaData != null) ? (activityInfo.metaData.getInt("com.google.android.glass.VoiceTrigger")) : 0;

            routine = (theController.getRoutine() == null) ? theController.createRoutine(currentVoiceTrigger) : theController.getRoutine();
            routine.dumpRoutine();
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
                finishTransitionForVideo();
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
        videoName.setText(routine.getVideoName());

        switch( routine.getVoiceTriggerInt() ) {
            case R.xml.balance:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Balance Me");
                moduleName.setText("Balance Me");
                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.warm:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Warm Me Up");
                moduleName.setText("Warm Me Up");
                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.unfreeze:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Unfreeze Me");
                moduleName.setText("Unfreeze Me");
                videoName.setText(routine.getVideoName());
                videoCount.setText(((routine.getVideoPosition() + 1) + " of " + routine.getVideoSetLength()));
                runCountdown();
                break;
            case R.xml.walk:
                Log.i("TRANSITION FUNCTION", "TransitionActivity setCardText() Walk With Me");
                videoName.setText(routine.getVideoName());
                // If first Walk With Me file, its video intro not text card -- do finishTransitionForVideo()
                if (routine.getVideoPosition() == 0) finishTransitionForVideo();
                else if (routine.getVideoPosition() >= 4) {
                    tapText = (TextView)findViewById(R.id.tap_text);
                    tapText.setText("Tap to quit");
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
                    // If this is Walk With Me stop our background Audio Process, else clear text on transition card layout
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                        if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(TransitionActivity.this, AudioService.class));
                    }
                    else {
                        moduleName.setText("");
                        videoCount.setText("");
                        videoName.setText("Loading...");
                    }
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishTransitionForVideo();
                    else finishTransitionForNextTransition("backward");
                    return true;
                } else if (gesture == Gesture.TAP) { // Go Forward in all other sets
                    Log.i("GESTURE_EVENT", "TransitionActivity createGestureDetector() onGesture() TAP");
//                    routine.setVideoPosition(Controller.moveToNext());
                    routine.dumpRoutine();

                    // If this is Walk With Me stop our background Audio Process, else clear text on transition card layout
                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) {
                        if (theController.isServiceRunning(AudioService.class)) stopService(new Intent(TransitionActivity.this, AudioService.class));
                    }
                    else {
                        moduleName.setText("");
                        videoCount.setText("");
                        videoName.setText("Loading...");
                    }

                    if (routine.getVideoSetName().equals(Controller.getController().getResources().getString(R.string.walk_voice_trigger))) finishTransitionForVideo();
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

    private void finishTransitionForVideo() {
        Log.i("TRANSITION FUNCTION", "TransitionActivity finishTransitionForVideo()");
        Intent i = new Intent(theController, VideoActivity.class);
        theController.setRoutine(routine);
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
