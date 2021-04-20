

package com.abc.sharefilesz.fragment;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.activity.FilePickerActivity;
import com.abc.sharefilesz.adapter.TransferListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.IndexOfTransferGroup;
import com.abc.sharefilesz.object.ShowingAssignee;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.app.EditableListFragment;
import com.abc.sharefilesz.app.GroupEditableListFragment;
import com.abc.sharefilesz.dialog.ChooseAssigneeDialog;
import com.abc.sharefilesz.dialog.DialogUtils;
import com.abc.sharefilesz.dialog.TransferInfoDialog;
import com.abc.sharefilesz.task.ChangeSaveDirectoryTask;
import com.abc.sharefilesz.ui.callback.TitleProvider;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransferListFragment extends GroupEditableListFragment<TransferListAdapter.GenericItem,
        GroupEditableListAdapter.GroupViewHolder, TransferListAdapter> implements TitleProvider,
        Activity.OnBackPressedListener
{
    public static final String TAG = "TransferListFragment";

    public static final String ARG_DEVICE_ID = "argDeviceId";
    public static final String ARG_GROUP_ID = "argGroupId";
    public static final String ARG_TYPE = "argType";
    public static final String ARG_PATH = "argPath";

    public static final int REQUEST_CHOOSE_FOLDER = 1;

    private TransferGroup mGroup;
    private IndexOfTransferGroup mIndex;
    private String mLastKnownPath;

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Kuick.ACTION_DATABASE_CHANGE.equals(intent.getAction())) {
                Kuick.BroadcastData data = Kuick.toData(intent);
                if (Kuick.TABLE_TRANSFER.equals(data.tableName) || Kuick.TABLE_TRANSFERGROUP.equals(data.tableName))
                    refreshList();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(TransferListAdapter.MODE_SORT_ORDER_ASCENDING);
        setDefaultSortingCriteria(TransferListAdapter.MODE_SORT_BY_NAME);
        setDefaultGroupingCriteria(TransferListAdapter.MODE_GROUP_BY_DEFAULT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new TransferListAdapter(this));
        setEmptyListImage(R.drawable.ic_compare_arrows_white_24dp);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_GROUP_ID)) {
            goPath(args.getString(ARG_PATH), args.getLong(ARG_GROUP_ID), args.getString(ARG_DEVICE_ID),
                    args.getString(ARG_TYPE));
        }
    }

    @Nullable
    @Override
    public PerformerMenu onCreatePerformerMenu(Context context)
    {
        return new PerformerMenu(context, new SelectionCallback(getActivity(), this));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        requireContext().registerReceiver(mReceiver, new IntentFilter(Kuick.ACTION_DATABASE_CHANGE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().unregisterReceiver(mReceiver);
    }

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == TransferListAdapter.VIEW_TYPE_REPRESENTATIVE ? currentSpanSize
                : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    protected void onListRefreshed()
    {
        super.onListRefreshed();

        String pathOnTrial = getAdapter().getPath();

        if (!(mLastKnownPath == null && getAdapter().getPath() == null)
                && (mLastKnownPath != null && !mLastKnownPath.equals(pathOnTrial)))
            getListView().scrollToPosition(0);

        mLastKnownPath = pathOnTrial;
    }

    @Override
    public boolean onBackPressed()
    {
        String path = getAdapter().getPath();

        if (path == null)
            return false;

        int slashPos = path.lastIndexOf(File.separator);

        goPath(slashPos == -1 && path.length() > 0 ? null : path.substring(0, slashPos));

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == Activity.RESULT_OK && requestCode == REQUEST_CHOOSE_FOLDER
                && data.hasExtra(FilePickerActivity.EXTRA_CHOSEN_PATH)) {
            final Uri selectedPath = data.getParcelableExtra(FilePickerActivity.EXTRA_CHOSEN_PATH);

            if (selectedPath == null) {
                createSnackbar(R.string.mesg_somethingWentWrong).show();
            } else if (selectedPath.toString().equals(getGroup().savePath)) {
                createSnackbar(R.string.mesg_pathSameError).show();
            } else {
                ChangeSaveDirectoryTask task = new ChangeSaveDirectoryTask(mGroup, selectedPath);
                new AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.ques_checkOldFiles)
                        .setMessage(R.string.text_checkOldFiles)
                        .setNeutralButton(R.string.butn_cancel, null)
                        .setNegativeButton(R.string.butn_skip, (dialogInterface, i) -> BackgroundService.run(
                                requireActivity(), task.setSkipMoving(true)))
                        .setPositiveButton(R.string.butn_proceed, (dialogInterface, i) -> BackgroundService.run(
                                requireActivity(), task))
                        .show();
            }
        }
    }

    public void changeSavePath(String initialPath)
    {
        startActivityForResult(new Intent(getActivity(), FilePickerActivity.class)
                .setAction(FilePickerActivity.ACTION_CHOOSE_DIRECTORY)
                .putExtra(FilePickerActivity.EXTRA_START_PATH, initialPath)
                .putExtra(FilePickerActivity.EXTRA_ACTIVITY_TITLE, getString(R.string.butn_saveTo)), REQUEST_CHOOSE_FOLDER);
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_transfers);
    }

    public TransferGroup getGroup()
    {
        if (mGroup == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mGroup = new TransferGroup(arguments.getLong(ARG_GROUP_ID, -1));
                try {
                    AppUtils.getKuick(getContext()).reconstruct(mGroup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return mGroup;
    }

    public IndexOfTransferGroup getIndex()
    {
        if (mIndex == null)
            mIndex = new IndexOfTransferGroup(getGroup());
        return mIndex;
    }

    public void goPath(String path, long groupId, String deviceId, String type)
    {
        if (deviceId != null && type != null)
            try {
                ShowingAssignee assignee = new ShowingAssignee(groupId, deviceId, TransferObject.Type.valueOf(type));

                AppUtils.getKuick(getContext()).reconstruct(assignee);
                TransferUtils.loadAssigneeInfo(getContext(), assignee);

                getAdapter().setAssignee(assignee);
            } catch (Exception ignored) {
            }

        goPath(path, groupId);
    }

    public void goPath(String path, long groupId)
    {
        getAdapter().setGroupId(groupId);
        goPath(path);
    }

    public void goPath(String path)
    {
        getAdapter().setPath(path);
        refreshList();
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             TransferListAdapter.GenericItem object)
    {
        if (object instanceof TransferListAdapter.DetailsTransferFolder) {
            final List<ShowingAssignee> list = TransferUtils.loadAssigneeList(getContext(),
                    getGroup().id, null);

            DialogInterface.OnClickListener listClickListener = (dialog, which) -> {
                getAdapter().setAssignee(list.get(which));
                getAdapter().setPath(getAdapter().getPath());
                refreshList();
            };

            DialogInterface.OnClickListener noLimitListener = (dialog, which) -> {
                getAdapter().setAssignee(null);
                getAdapter().setPath(getAdapter().getPath());
                refreshList();
            };

            ChooseAssigneeDialog dialog = new ChooseAssigneeDialog(requireActivity(), list, listClickListener);

            dialog.setTitle(R.string.text_limitTo)
                    .setNeutralButton(R.string.butn_none, noLimitListener)
                    .show();
        } else if (object instanceof TransferListAdapter.StorageStatusItem) {
            final TransferListAdapter.StorageStatusItem statusItem = (TransferListAdapter.StorageStatusItem) object;

            if (statusItem.hasIssues(getAdapter()))
                new AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.mesg_notEnoughSpace))
                        .setNegativeButton(R.string.butn_close, null)
                        .setPositiveButton(R.string.butn_saveTo, (dialog, which) -> changeSavePath(statusItem.directory))
                        .show();
            else
                changeSavePath(statusItem.directory);
        } else if (object instanceof TransferListAdapter.TransferFolder) {
            getAdapter().setPath(object.directory);
            refreshList();
            AppUtils.showFolderSelectionHelp(this);
        } else
            new TransferInfoDialog(requireActivity(), getIndex(), object,
                    getAdapter().getDeviceId()).show();

        return true;
    }

    public void updateSavePath(String selectedPath)
    {
        requireActivity().runOnUiThread(() -> createSnackbar(R.string.mesg_pathSaved).show());
    }

    private static class SelectionCallback extends EditableListFragment.SelectionCallback
    {
        public SelectionCallback(android.app.Activity activity, PerformerEngineProvider provider)
        {
            super(activity, provider);
        }

        @Override
        public boolean onPerformerMenuList(PerformerMenu performerMenu, MenuInflater inflater, Menu targetMenu)
        {
            super.onPerformerMenuList(performerMenu, inflater, targetMenu);
            inflater.inflate(R.menu.action_mode_transfer, targetMenu);
            return true;
        }

        @Override
        public boolean onPerformerMenuSelected(PerformerMenu performerMenu, MenuItem item)
        {
            int id = item.getItemId();
            IPerformerEngine engine = getPerformerEngine();

            if (engine == null)
                return false;

            List<Selectable> genericList = new ArrayList<>(engine.getSelectionList());
            List<TransferListAdapter.GenericItem> selectionList = new ArrayList<>();

            for (Selectable selectable : genericList)
                if (selectable instanceof TransferListAdapter.GenericItem)
                    selectionList.add((TransferListAdapter.GenericItem) selectable);

            if (id == R.id.action_mode_transfer_delete) {
                DialogUtils.showRemoveTransferObjectListDialog(getActivity(), selectionList);


                final InterstitialAd mInterstitial = new InterstitialAd(getActivity());
                mInterstitial.setAdUnitId(getActivity().getString(R.string.interstitial_ad_unit));
                mInterstitial.loadAd(new AdRequest.Builder().build());
                mInterstitial.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // TODO Auto-generated method stub
                        super.onAdLoaded();
                        if (mInterstitial.isLoaded()) {
                            mInterstitial.show();
                        }
                    }
                });




                return true;
            } else
                return super.onPerformerMenuSelected(performerMenu, item);
        }
    }
}
