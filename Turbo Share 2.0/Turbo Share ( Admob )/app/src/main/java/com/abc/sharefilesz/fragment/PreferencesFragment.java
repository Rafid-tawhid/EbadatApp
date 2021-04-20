

package com.abc.sharefilesz.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.fragment.inner.LookPreferencesFragment;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preferences_main_app);
        addPreferencesFromResource(R.xml.preferences_main_notification);
        addPreferencesFromResource(R.xml.preferences_main_advanced);

        LookPreferencesFragment.loadThemeOptionsTo(requireContext(), findPreference("theme"));
    }
}