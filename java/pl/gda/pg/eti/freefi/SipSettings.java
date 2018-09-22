package pl.gda.pg.eti.freefi;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Handles SIP authentication settings for local profile
 */
public class SipSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.local_profile);
    }
}
