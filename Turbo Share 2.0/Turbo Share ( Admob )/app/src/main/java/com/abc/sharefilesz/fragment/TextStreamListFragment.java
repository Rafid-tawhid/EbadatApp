

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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.activity.ShareActivity;
import com.abc.sharefilesz.activity.TextEditorActivity;
import com.abc.sharefilesz.adapter.TextStreamListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.TextStreamObject;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.EditableListFragment;
import com.abc.sharefilesz.app.GroupEditableListFragment;
import com.abc.sharefilesz.ui.callback.IconProvider;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.SelectionUtils;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**

 * date: 30.12.2017 13:25
 */

public class TextStreamListFragment extends GroupEditableListFragment<TextStreamObject,
        GroupEditableListAdapter.GroupViewHolder, TextStreamListAdapter> implements IconProvider
{
    private StatusReceiver mStatusReceiver = new StatusReceiver();

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        setHasBottomSpace(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setLayoutResId(R.layout.layout_text_stream);
        setFilteringSupported(true);
        setDefaultOrderingCriteria(TextStreamListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(TextStreamListAdapter.MODE_SORT_BY_DATE);
        setDefaultGroupingCriteria(TextStreamListAdapter.MODE_GROUP_BY_DATE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new TextStreamListAdapter(this));
        setEmptyListImage(R.drawable.ic_forum_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyTextStream));

        view.findViewById(R.id.layout_text_stream_fab)
                .setOnClickListener(v -> startActivity(new Intent(getActivity(), TextEditorActivity.class)
                        .setAction(TextEditorActivity.ACTION_EDIT_TEXT)));
    }

    @Nullable
    @Override
    public PerformerMenu onCreatePerformerMenu(Context context)
    {
        return new PerformerMenu(context, new SelectionCallback(getActivity(), this));
    }

    @Override
    public void onSortingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_sortByName), TextStreamListAdapter.MODE_SORT_BY_NAME);
        options.put(getString(R.string.text_sortByDate), TextStreamListAdapter.MODE_SORT_BY_DATE);
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        options.put(getString(R.string.text_groupByNothing), TextStreamListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), TextStreamListAdapter.MODE_GROUP_BY_DATE);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        requireContext().registerReceiver(mStatusReceiver, new IntentFilter(Kuick.ACTION_DATABASE_CHANGE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().unregisterReceiver(mStatusReceiver);
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_short_text_white_24dp;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_textStream);
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder, TextStreamObject object)
    {
        startActivity(new Intent(getContext(), TextEditorActivity.class)
                .setAction(TextEditorActivity.ACTION_EDIT_TEXT)
                .putExtra(TextEditorActivity.EXTRA_CLIPBOARD_ID, object.id)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }

    private static class SelectionCallback extends EditableListFragment.SelectionCallback
    {
        private MenuItem mShareWithTurboShare;
        private MenuItem mShareWithOthers;

        public SelectionCallback(Activity activity, PerformerEngineProvider provider)
        {
            super(activity, provider);
        }

        @Override
        public boolean onPerformerMenuList(PerformerMenu performerMenu, MenuInflater inflater, Menu targetMenu)
        {
            super.onPerformerMenuList(performerMenu, inflater, targetMenu);

            // Sharing text with this menu is unnecessary since only one item can be sent at a time. So, this will be
            // disabled until it is possible to send multiple items.
            //inflater.inflate(R.menu.action_mode_share, targetMenu);
            inflater.inflate(R.menu.action_mode_text_stream, targetMenu);

            mShareWithTurboShare = targetMenu.findItem(R.id.action_mode_share_TurboShare);
            mShareWithOthers = targetMenu.findItem(R.id.action_mode_share_all_apps);
            updateShareMethods(getPerformerEngine());

            return true;
        }

        @Override
        public boolean onPerformerMenuSelected(PerformerMenu performerMenu, MenuItem item)
        {
            int id = item.getItemId();

            IPerformerEngine engine = getPerformerEngine();
            if (engine == null)
                return false;

            List<Selectable> genericSelectionList = new ArrayList<>(engine.getSelectionList());
            List<TextStreamObject> selectionList = new ArrayList<>();
            Kuick kuick = AppUtils.getKuick(getActivity());
            Context context = getActivity();

            for (Selectable selectable : genericSelectionList)
                if (selectable instanceof TextStreamObject)
                    selectionList.add((TextStreamObject) selectable);

            if (id == R.id.action_mode_text_stream_delete) {
                kuick.remove(selectionList);
                kuick.broadcast();
                return true;
            } else if (id == R.id.action_mode_share_all_apps || id == R.id.action_mode_share_TurboShare) {
                if (selectionList.size() == 1) {
                    TextStreamObject streamObject = selectionList.get(0);
                    boolean shareLocally = id == R.id.action_mode_share_TurboShare;
                    Intent intent = (shareLocally ? new Intent(context, ShareActivity.class) : new Intent())
                            .setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, streamObject.text)
                            .setType("text/*");

                    getActivity().startActivity(shareLocally ? intent : Intent.createChooser(intent, context.getString(
                            R.string.text_fileShareAppChoose)));
                } else
                    Toast.makeText(context, R.string.mesg_textShareLimit, Toast.LENGTH_SHORT).show();
            } else
                return super.onPerformerMenuSelected(performerMenu, item);

            return false;
        }

        @Override
        public void onPerformerMenuItemSelected(PerformerMenu performerMenu, IPerformerEngine engine,
                                                IBaseEngineConnection owner, Selectable selectable, boolean isSelected,
                                                int position)
        {
            super.onPerformerMenuItemSelected(performerMenu, engine, owner, selectable, isSelected, position);
            updateShareMethods(engine);
        }

        @Override
        public void onPerformerMenuItemSelected(PerformerMenu performerMenu, IPerformerEngine engine,
                                                IBaseEngineConnection owner, List<? extends Selectable> selectableList,
                                                boolean isSelected, int[] positions)
        {
            super.onPerformerMenuItemSelected(performerMenu, engine, owner, selectableList, isSelected, positions);
            updateShareMethods(engine);
        }

        private void updateShareMethods(IPerformerEngine engine)
        {
            int totalSelections = SelectionUtils.getTotalSize(engine);

            if (mShareWithOthers != null)
                mShareWithOthers.setEnabled(totalSelections == 1);

            if (mShareWithTurboShare != null)
                mShareWithTurboShare.setEnabled(totalSelections == 1);
        }
    }

    private class StatusReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Kuick.ACTION_DATABASE_CHANGE.equals(intent.getAction())) {
                Kuick.BroadcastData data = Kuick.toData(intent);
                if (Kuick.TABLE_CLIPBOARD.equals(data.tableName))
                    refreshList();
            }
        }
    }
}