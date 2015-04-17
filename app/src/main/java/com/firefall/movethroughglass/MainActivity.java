package com.firefall.movethroughglass;

import com.google.android.glass.view.WindowUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Requests a voice menu on this activity. As for any other
        // window feature, be sure to request this before
        // setContentView() is called
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().requestFeature(Window.FEATURE_OPTIONS_PANEL);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        } else if (keycode == KeyEvent.KEYCODE_BACK) {
            //finish();
            // Sleep??
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
            Intent i;

            switch (item.getItemId()) {
                case R.id.balance_menu_item:
                    i = new Intent(this, TransitionActivity.class);
                    i.putExtra("voicetrigger", R.xml.balance);
                    startActivity(i);
                    finish();
                    break;
                case R.id.unfreeze_menu_item:
                    i = new Intent(this, TransitionActivity.class);
                    i.putExtra("voicetrigger", R.xml.unfreeze);
                    startActivity(i);
                    finish();
                    break;
                case R.id.warm_menu_item:
                    i = new Intent(this, TransitionActivity.class);
                    i.putExtra("voicetrigger", R.xml.warm);
                    startActivity(i);
                    finish();
                    break;
                case R.id.walk_menu_item:
                    i = new Intent(this, TransitionActivity.class);
                    i.putExtra("voicetrigger", R.xml.walk);
                    startActivity(i);
                    finish();
                    break;
                case R.id.volume_menu_item:
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS), 0);
                    break;

                default:
                    i = new Intent(this,MainActivity.class);
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }
}