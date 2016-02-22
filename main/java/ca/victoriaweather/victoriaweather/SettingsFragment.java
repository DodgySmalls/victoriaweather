package ca.victoriaweather.victoriaweather;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        setRetainInstance(true);
    }
}
