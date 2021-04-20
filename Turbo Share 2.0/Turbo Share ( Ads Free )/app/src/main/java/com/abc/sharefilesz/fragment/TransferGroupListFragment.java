

package com.abc.sharefilesz.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.activity.AddDeviceActivity;
import com.abc.sharefilesz.activity.ContentSharingActivity;
import com.abc.sharefilesz.activity.ViewTransferActivity;
import com.abc.sharefilesz.adapter.TransferGroupListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.IndexOfTransferGroup;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.EditableListFragment;
import com.abc.sharefilesz.app.GroupEditableListFragment;
import com.abc.sharefilesz.dialog.DialogUtils;
import com.abc.sharefilesz.task.FileTransferTask;
import com.abc.sharefilesz.ui.callback.IconProvider;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.ListUtils;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**

 * date: 10.11.2017 00:15
 */

public class TransferGroupListFragment extends GroupEditableListFragment<IndexOfTransferGroup,
        GroupEditableListAdapter.GroupViewHolder, TransferGroupListAdapter> implements IconProvider
{
    private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Kuick.ACTION_DATABASE_CHANGE.equals(intent.getAction())) {
                Kuick.BroadcastData data = Kuick.toData(intent);
                if (data != null && (Kuick.TABLE_TRANSFERGROUP.equals(data.tableName)
                        || Kuick.TABLE_TRANSFER.equals(data.tableName)))
                    refreshList();
            } else if (BackgroundService.ACTION_TASK_CHANGE.equals(intent.getAction()))
                updateTasks();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setLayoutResId(R.layout.layout_transfer_group);
        setFilteringSupported(true);
        setDefaultOrderingCriteria(TransferGroupListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(TransferGroupListAdapter.MODE_SORT_BY_DATE);
        setDefaultGroupingCriteria(TransferGroupListAdapter.MODE_GROUP_BY_DATE);
        setUseDefaultPaddingDecoration(true);
        setUseDefaultPaddingDecorationSpaceForEdges(true);
        setDefaultPaddingDecorationSize(getResources().getDimension(R.dimen.padding_list_content_parent_layout));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new TransferGroupListAdapter(this));
        setEmptyListImage(R.drawable.ic_compare_arrows_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyTransfer));

        view.findViewById(R.id.sendLayoutButton).setOnClickListener(v -> startActivity(
                new Intent(getContext(), ContentSharingActivity.class)));
        view.findViewById(R.id.receiveLayoutButton)
                .setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mFilter.addAction(Kuick.ACTION_DATABASE_CHANGE);
    }

    @Nullable
    @Override
    public PerformerMenu onCreatePerformerMenu(Context context)
    {
        return new PerformerMenu(getContext(), new SelectionCallback(getActivity(), this));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        requireContext().registerReceiver(mReceiver, mFilter);
        updateTasks();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().unregisterReceiver(mReceiver);
    }

    @Override
    public void onSortingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_sortByDate), TransferGroupListAdapter.MODE_SORT_BY_DATE);
        options.put(getString(R.string.text_sortBySize), TransferGroupListAdapter.MODE_SORT_BY_SIZE);
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_groupByNothing), TransferGroupListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), TransferGroupListAdapter.MODE_GROUP_BY_DATE);
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_transfers);
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_swap_vert_white_24dp;
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             IndexOfTransferGroup object)
    {
        ViewTransferActivity.startInstance(requireActivity(), object.group);
        return true;
    }

    public void updateTasks()
    {
        try {
            BackgroundService service = AppUtils.getBgService(requireActivity());
            List<FileTransferTask> tasks = service.getTaskListOf(FileTransferTask.class);
            List<Long> activeTaskList = new ArrayList<>();
            for (FileTransferTask task : tasks)
                if (task.group != null)
                    activeTaskList.add(task.group.id);

            getAdapter().updateActiveList(activeTaskList);
            getAdapter().notifyDataSetChanged();
        } catch (IllegalStateException ignored) {

        }
    }

    private static class SelectionCallback extends EditableListFragment.SelectionCallback
    {

        public SelectionCallback(Activity activity, PerformerEngineProvider provider)
        {
            super(activity, provider);
        }

        @Override
        public boolean onPerformerMenuList(PerformerMenu performerMenu, MenuInflater inflater, Menu targetMenu)
        {
            super.onPerformerMenuList(performerMenu, inflater, targetMenu);
            inflater.inflate(R.menu.action_mode_group, targetMenu);
            return true;
        }

        @Override
        public boolean onPerformerMenuSelected(PerformerMenu performerMenu, MenuItem item)
        {
            int id = item.getItemId();
            Kuick kuick = AppUtils.getKuick(getActivity());
            IPerformerEngine engine = getPerformerEngine();

            if (engine == null)
                return false;

            List<Selectable> genericList = new ArrayList<>(engine.getSelectionList());
            List<IndexOfTransferGroup> indexList = ListUtils.typedListOf(genericList, IndexOfTransferGroup.class);

            if (id == R.id.action_mode_group_delete) {
                List<TransferGroup> groupList = new ArrayList<>();
                for (IndexOfTransferGroup index : indexList)
                    groupList.add(index.group);

                DialogUtils.showRemoveTransferGroupListDialog(getActivity(), groupList);
                return true;
            } else if (id == R.id.action_mode_group_serve_on_web || id == R.id.action_mode_group_hide_on_web) {
                boolean served = id == R.id.action_mode_group_serve_on_web;
                List<IndexOfTransferGroup> changedList = new ArrayList<>();

                for (IndexOfTransferGroup index : indexList) {
                    if (!index.hasOutgoing() || index.group.isServedOnWeb == served)
                        continue;

                    index.group.isServedOnWeb = served;
                    changedList.add(index);
                }

                kuick.update(changedList);
                kuick.broadcast();
            } else
                super.onPerformerMenuSelected(performerMenu, item);

            return false;
        }
    }
}