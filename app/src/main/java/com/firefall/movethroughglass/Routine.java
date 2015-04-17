package com.firefall.movethroughglass;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;


public class Routine {
    private String[] videoSet;
    private String[] speedSet;
    private String[] videoFileNames;
    private String[] audioFileNames;
    private String videoSetName;
    private String videoFileName;
    private String videoUrl;
    private String speed;         //
    private int videoPosition; // i.e. "play count" -- is this the second or third video, etc
    private int voiceTrigger;
    private int layout;
    private int isLastVideo;
    private Resources res;

    private final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();


    public Routine(int voiceTrigger) {
        Log.i("ROUTINE FUNCTION", "Begin building of subroutine video content.");
        res = Controller.getController().getResources();
        initialLoadContentFromTrigger(voiceTrigger);
    }

    // build object attributes from provided voice trigger
    private void initialLoadContentFromTrigger(int voiceTrigger) {
        this.videoPosition = 0;
        this.isLastVideo = 0;
        this.videoFileName = "";
        this.voiceTrigger = voiceTrigger;
        this.speedSet = this.res.getStringArray(R.array.speeds);
        this.speed = this.speedSet[0];
        this.layout =  R.layout.default_transition;  // default, gets update if Walk With Me is chosen


        // Evaluate what voice trigger was used when the activity is called. If it is 0 then we have been redirected from PostVideoActivity.
        switch( voiceTrigger ) {
            case R.xml.balance:
                Log.i("ROUTINE FUNCTION", "Building \"Balance Me\" content.");
                this.voiceTrigger = R.xml.balance;
                this.videoSetName = res.getString(R.string.balance_voice_trigger);
                this.videoSet = this.res.getStringArray(R.array.balance_me_videos);
                this.videoFileNames = this.res.getStringArray(R.array.balance_me_file_names);
                break;
            case R.xml.warm:
                Log.i("ROUTINE FUNCTION", "Building \"Warm Me Up\" content.");
                this.voiceTrigger = R.xml.warm;
                this.videoSetName = res.getString(R.string.warm_voice_trigger);
                this.videoSet = this.res.getStringArray(R.array.warm_me_up_videos);
                this.videoFileNames = this.res.getStringArray(R.array.warm_me_up_file_names);
                break;
            case R.xml.unfreeze:
                Log.i("ROUTINE FUNCTION", "Building \"Unfreeze Me\" content.");
                this.voiceTrigger = R.xml.unfreeze;
                this.videoSetName = res.getString(R.string.unfreeze_voice_trigger);
                this.videoSet = this.res.getStringArray(R.array.unfreeze_me_videos);
                this.videoFileNames = this.res.getStringArray(R.array.unfreeze_me_file_names);
                break;
            case R.xml.walk:
                Log.i("ROUTINE FUNCTION", "Building \"Walk With Me\" content.");
                this.voiceTrigger = R.xml.walk;
                this.videoSetName = res.getString(R.string.walk_voice_trigger);
                this.videoSet = this.res.getStringArray(R.array.walk_with_me_videos);
                this.videoFileNames = this.res.getStringArray(R.array.walk_with_me_file_names);
                this.audioFileNames = this.res.getStringArray(R.array.walk_with_me_audio);
                this.layout =  R.layout.walk_with_me_transition;
                break;
            default:
                Log.wtf("CRITICAL_ERROR", "We should never be here, based off of voice trigger logic.");
                this.voiceTrigger = 0;
                this.videoUrl = null;
                this.videoSet = null;
                this.videoSetName = null;
                this.videoFileNames = null;
                System.exit(1); // Hacky, remove later
        }
        setVideoUrl();
    }

    private void setVideoUrl() {
        this.videoUrl = absolutePath + this.videoSet[this.videoPosition]; // Set the Video URL based off of a given videoPosition inside videoSet
    }

    public String getVideoUrl() {
        return this.videoUrl;
    }

    private void setVideoName() {
//        int temp = this.videoPosition - 1; // Handle Walk With Me edge case - intro video as first "transition card" throws off array logic all other modules share
//        if (this.videoSetName.equals(res.getString(R.string.walk_voice_trigger))) this.videoFileName = this.videoFileNames[temp];
//        else this.videoFileName = this.videoFileNames[this.videoPosition];
        this.videoFileName = this.videoFileNames[this.videoPosition];
    }

    public String getVideoName() {
        return this.videoFileName;
    }

    // gets an int from Controller.moveNext() or movePrevious()
    public void setVideoPosition(int position) {
        // Perform updates
        this.videoPosition = position;
        setVideoUrl();
        setSpeed();
        setVideoName();
    }

    public int getVoiceTriggerInt() {
        return this.voiceTrigger;
    }

    public int getVideoPosition() {
        return this.videoPosition;
    }

    public int getVideoSetLength() {
        return this.videoSet.length;
    }

    // this method is for Walk With Me only, really.
    public String getSpeed() {
        return this.speed;
    }

    private void setSpeed() {
        this.speed = this.speedSet[this.videoPosition]; // Based on position (0-4) grab the "speed value" from speedSet
    }

    // layout will never change on an instantiated routine
    public int getLayout() {
        return this.layout;
    }

    public String getVideoSetName() {
        return this.videoSetName;
    }

    public void dumpRoutine() {
        Log.i("ROUTINE FUNCTION", "****BEGIN: DUMP ROUTINE****");
        Log.i("ROUTINE INFO", "Layout: " + getLayout());
        Log.i("ROUTINE INFO", "Voice Trigger: " + getVoiceTriggerInt());
        Log.i("ROUTINE INFO", "Set Name: " + getVideoSetName());
        Log.i("ROUTINE INFO", "Set Length: " + getVideoSetLength());
        Log.i("ROUTINE INFO", "Speed: " + getSpeed());
        Log.i("ROUTINE INFO", "Video Position: " + getVideoPosition());
        Log.i("ROUTINE INFO", "Video Name: " + getVideoName());
        Log.i("ROUTINE INFO", "Video URL: " + getVideoUrl());
        Log.i("ROUTINE FUNCTION", "****END: DUMP ROUTINE****");
    }

    // Handle Last Walk With Me -- Last Video nav issues
    public void setLast(int isLast) {
        this.isLastVideo = isLast;
    }

    public int isLast() {
        return this.isLastVideo;
    }
}
