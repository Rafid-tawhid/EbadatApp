

package com.abc.sharefilesz.app;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;

import java.util.Map;

/**

 * date: 30.03.2018 16:10
 */

public abstract class GroupEditableListFragment<T extends GroupEditableListAdapter.GroupEditable,
        V extends GroupEditableListAdapter.GroupViewHolder, E extends GroupEditableListAdapter<T, V>>
        extends EditableListFragment<T, V, E>
{
    private Map<String, Integer> mGroupingOptions = new ArrayMap<>();
    private int mDefaultGroupingCriteria = GroupEditableListAdapter.MODE_GROUP_BY_NOTHING;

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == GroupEditableListAdapter.VIEW_TYPE_REPRESENTATIVE
                || viewType == GroupEditableListAdapter.VIEW_TYPE_ACTION_BUTTON
                ? currentSpanSize : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        if (!isUsingLocalSelection() || !isLocalSelectionActivated()) {
            Map<String, Integer> options = new ArrayMap<>();

            onGroupingOptions(options);

            mGroupingOptions.clear();
            mGroupingOptions.putAll(options);

            if (mGroupingOptions.size() > 0) {
                inflater.inflate(R.menu.actions_abs_group_shareable_list, menu);
                MenuItem groupingItem = menu.findItem(R.id.actions_abs_group_shareable_grouping);

                if (groupingItem != null)
                    applyDynamicMenuItems(groupingItem, R.id.actions_abs_group_shareable_group_grouping,
                            mGroupingOptions);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        if (!isUsingLocalSelection() || !isLocalSelectionActivated()) {
            checkPreferredDynamicItem(menu.findItem(R.id.actions_abs_group_shareable_grouping), getGroupingCriteria(),
                    mGroupingOptions);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getGroupId() == R.id.actions_abs_group_shareable_group_grouping)
            changeGroupingCriteria(item.getOrder());
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    public void onGroupingOptions(Map<String, Integer> options)
    {
    }

    public void changeGroupingCriteria(int criteria)
    {
        getViewPreferences().edit()
                .putInt(getUniqueSettingKey("GroupBy"), criteria)
                .apply();

        getAdapter().setGroupBy(criteria);

        refreshList();
    }

    public int getGroupingCriteria()
    {
        return getViewPreferences().getInt(getUniqueSettingKey("GroupBy"), mDefaultGroupingCriteria);
    }

    public void setDefaultGroupingCriteria(int groupingCriteria)
    {
        mDefaultGroupingCriteria = groupingCriteria;
    }

    @Override
    protected void setListAdapter(E adapter, boolean hadAdapter)
    {
        super.setListAdapter(adapter, hadAdapter);
        adapter.setGroupBy(getGroupingCriteria());
    }
}
