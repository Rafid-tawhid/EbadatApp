

package com.abc.sharefilesz.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.adapter.ApplicationListAdapter;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.GroupEditableListFragment;
import com.abc.sharefilesz.util.AppUtils;

import java.util.Map;

public class ApplicationListFragment extends GroupEditableListFragment<ApplicationListAdapter.PackageHolder,
        GroupEditableListAdapter.GroupViewHolder, ApplicationListAdapter>
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setFilteringSupported(true);
        setHasOptionsMenu(true);
        setDefaultOrderingCriteria(ApplicationListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(ApplicationListAdapter.MODE_SORT_BY_DATE);
        setDefaultGroupingCriteria(ApplicationListAdapter.MODE_GROUP_BY_DATE);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new ApplicationListAdapter(this));
        setEmptyListImage(R.drawable.ic_android_head_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyApp));
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        super.onGroupingOptions(options);
        options.put(getString(R.string.text_groupByNothing), ApplicationListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), ApplicationListAdapter.MODE_GROUP_BY_DATE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_application, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.show_system_apps) {
            boolean isShowingSystem = !AppUtils.getDefaultPreferences(getContext()).getBoolean("show_system_apps",
                    false);

            AppUtils.getDefaultPreferences(getContext()).edit()
                    .putBoolean("show_system_apps", isShowingSystem)
                    .apply();

            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuSystemApps = menu.findItem(R.id.show_system_apps);
        menuSystemApps.setChecked(AppUtils.getDefaultPreferences(getContext()).getBoolean("show_system_apps",
                false));
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_application);
    }

    @Override
    public boolean performLayoutClickOpen(GroupEditableListAdapter.GroupViewHolder holder,
                                          ApplicationListAdapter.PackageHolder object)
    {
        try {
            Intent launchIntent = requireContext().getPackageManager()
                    .getLaunchIntentForPackage(object.packageName);

            if (launchIntent != null) {
                new AlertDialog.Builder(requireActivity())
                        .setMessage(R.string.ques_launchApplication)
                        .setNegativeButton(R.string.butn_cancel, null)
                        .setPositiveButton(R.string.butn_appLaunch, (dialog, which) -> startActivity(launchIntent))
                        .show();
            } else
                Toast.makeText(getActivity(), R.string.mesg_launchApplicationError, Toast.LENGTH_SHORT).show();

            return true;
        } catch (Exception ignore) {
        }

        return false;
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             ApplicationListAdapter.PackageHolder object)
    {
        return performLayoutClickOpen(holder, object);
    }
}
