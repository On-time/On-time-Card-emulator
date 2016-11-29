package net.aliveplex.alive.on_timecardemulator;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by snail on 11/29/2016.
 */

public class myPreference extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference_data);
    }
}
