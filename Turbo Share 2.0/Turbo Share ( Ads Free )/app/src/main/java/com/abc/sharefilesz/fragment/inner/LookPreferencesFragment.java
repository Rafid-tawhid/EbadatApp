

package com.abc.sharefilesz.fragment.inner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.abc.sharefilesz.R;

import java.util.ArrayList;
import java.util.List;

public class LookPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preference_introduction_look);
        loadThemeOptionsTo(getContext(), findPreference("theme"));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (("custom_fonts".equals(key) || "theme".equals(key) || "amoled_theme".equals(key))
                && getActivity() != null)
            getActivity().recreate();
    }

    public static void loadThemeOptionsTo(Context context, ListPreference themePreference)
    {
        List<String> valueList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();

        valueList.add("light");
        valueList.add("dark");

        titleList.add(context.getString(R.string.text_lightTheme));
        titleList.add(context.getString(R.string.text_darkTheme));

        if (Build.VERSION.SDK_INT >= 26) {
            valueList.add("system");
            titleList.add(context.getString(R.string.text_followSystemTheme));
        } else if (Build.VERSION.SDK_INT >= 21) {
            valueList.add("battery");
            titleList.add(context.getString(R.string.text_batterySaverTheme));
        }

        CharSequence[] values = new String[valueList.size()];
        CharSequence[] titles = new String[titleList.size()];

        valueList.toArray(values);
        titleList.toArray(titles);

        themePreference.setEntries(titles);
        themePreference.setEntryValues(values);
    }
}
