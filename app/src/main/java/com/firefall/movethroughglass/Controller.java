package com.firefall.movethroughglass;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class Controller extends Application {

    public static final int FIRST_CARD = 0;
    public static final int SECOND_CARD = 1;
    public static final int THIRD_CARD = 2;
    public static final int FOURTH_CARD = 3;

    public static int currentCard;
    private static int routineLength;
    private static String routineName;
    private static Controller controller;

    private Routine routine;

    public void onCreate() {
        super.onCreate();
        controller = this;
        currentCard = 0;
        Log.i("CONTROLLER FUNCTION", "Controller onCreate()");
    }

    public static Controller getController() {
        Log.i("CONTROLLER FUNCTION", "Controller getController()");
        return controller;
    }

    public static int getCurrent() {
        Log.i("CONTROLLER FUNCTION", "Controller getCurrent()");
        return currentCard;
    }

    public static int moveToNext() {
        Log.i("CONTROLLER FUNCTION", "Controller moveToNext()");
        int temp = routineLength;
        if (currentCard >= --temp) System.exit(0); // All prior activities cleared with FLAG_ACTIVITY_CLEAR_TOP in finish
        return ++currentCard;
    }

    public static int moveToPrevious() {
        Log.i("CONTROLLER FUNCTION", "Controller moveToPrevious()");
        if (currentCard == 0) System.exit(0); // All prior activities cleared with FLAG_ACTIVITY_CLEAR_TOP in finish
        else if (routineName.equals("Walk with me") && currentCard == 1) System.exit(0);
        return --currentCard;
    }

    public Routine createRoutine(int voiceTrigger) {
        Log.i("CONTROLLER FUNCTION", "Controller createRoutine()");
        routine = new Routine(voiceTrigger); // Fill up routine
        routineLength = routine.getVideoSetLength();
        routineName = routine.getVideoSetName();
        return routine;
    }

    public Routine getRoutine() {
        Log.i("CONTROLLER FUNCTION", "Controller getRoutine()");
        return routine;
    }

    public void setRoutine(Routine existingRoutine) {
        Log.i("CONTROLLER FUNCTION", "Controller setRoutine()");
        routine = existingRoutine;
        routineLength = routine.getVideoSetLength();
        routineName = routine.getVideoSetName();
    }

    // Used as a check for background audio service
    public boolean isServiceRunning(Class<?> serviceClass) {
        Log.i("FUNCTION CALL", "Controller isServiceRunning().");
        ActivityManager manager = (ActivityManager) controller.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
