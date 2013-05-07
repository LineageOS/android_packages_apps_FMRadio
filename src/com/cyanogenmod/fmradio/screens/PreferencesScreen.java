package com.cyanogenmod.fmradio.screens;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.cyanogenmod.fmradio.R;

/**
 * Preferences Screen. Preferences helper methods are at com.cyanogenmod.fmradio.utils.Prefs
 */
public class PreferencesScreen extends PreferenceActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}