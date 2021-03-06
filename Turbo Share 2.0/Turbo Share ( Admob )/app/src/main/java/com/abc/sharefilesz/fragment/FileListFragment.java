

package com.abc.sharefilesz.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Editable;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.app.EditableListFragmentBase;
import com.abc.sharefilesz.app.GroupEditableListFragment;
import com.abc.sharefilesz.dialog.FileDeletionDialog;
import com.abc.sharefilesz.dialog.FileRenameDialog;
import com.abc.sharefilesz.ui.callback.SharingPerformerMenuCallback;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.io.LocalDocumentFile;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class FileListFragment extends GroupEditableListFragment<FileListAdapter.FileHolder,
        GroupEditableListAdapter.GroupViewHolder, FileListAdapter>
{
    public static final String TAG = FileListFragment.class.getSimpleName();

    public final static int REQUEST_WRITE_ACCESS = 264;

    public final static String ACTION_FILE_LIST_CHANGED = "com.abc.sharefilesz.action.FILE_LIST_CHANGED";
    public final static String EXTRA_FILE_PARENT = "extraPath";
    public final static String EXTRA_FILE_NAME = "extraFile";
    public final static String EXTRA_FILE_LOCATION = "extraFileLocation";

    private DocumentFile mLastKnownPath;
    private IntentFilter mIntentFilter = new IntentFilter();
    private MediaScannerConnection mMediaScanner;
    private OnPathChangedListener mPathChangedListener;
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        private Snackbar mUpdateSnackbar;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if ((ACTION_FILE_LIST_CHANGED.equals(intent.getAction()) && intent.hasExtra(EXTRA_FILE_PARENT))) {
                try {
                    Object parentUri = intent.getParcelableExtra(EXTRA_FILE_PARENT);

                    if (parentUri == null && getAdapter().getPath() == null) {
                        refreshList();
                    } else if (parentUri != null) {
                        final DocumentFile parentFile = FileUtils.fromUri(getContext(), (Uri) parentUri);

                        if (getAdapter().getPath() != null && parentFile.getUri().equals(getAdapter().getPath().getUri()))
                            refreshList();
                        else if (intent.hasExtra(EXTRA_FILE_NAME)) {
                            if (mUpdateSnackbar == null)
                                mUpdateSnackbar = createSnackbar(R.string.mesg_newFilesReceived);

                            mUpdateSnackbar
                                    .setText(getString(R.string.mesg_fileReceived, intent.getStringExtra(EXTRA_FILE_NAME)))
                                    .setAction(R.string.butn_show, v -> goPath(parentFile))
                                    .show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (getAdapter().getPath() == null && Kuick.ACTION_DATABASE_CHANGE.equals(intent.getAction())) {
                Kuick.BroadcastData data = Kuick.toData(intent);
                if (Kuick.TABLE_FILEBOOKMARK.equals(data.tableName))
                    refreshList();
            }
        }
    };

    public static boolean handleEditingAction(MenuItem item, final FileListFragment fragment,
                                              List<FileListAdapter.FileHolder> selectedItemList)
    {
        final FileListAdapter adapter = fragment.getAdapter();
        final int id = item.getItemId();

        if (id == R.id.action_mode_file_delete) {
            new FileDeletionDialog(fragment.getContext(), selectedItemList, new FileDeletionDialog.Listener()
            {
                @Override
                public void onFileDeletion(BackgroundTask runningTask, Context context, DocumentFile file)
                {
                    fragment.scanFile(file);
                }

                @Override
                public void onCompleted(BackgroundTask runningTask, Context context, int fileSize)
                {
                    context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                            .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null ? null
                                    : adapter.getPath().getUri()));
                }
            }).show();
        } else if (id == R.id.action_mode_file_rename) {
            new FileRenameDialog(fragment.getContext(), selectedItemList, new FileRenameDialog.OnFileRenameListener()
            {
                @Override
                public void onFileRename(DocumentFile file, String displayName)
                {
                    fragment.scanFile(file);
                }

                @Override
                public void onFileRenameCompleted(Context context)
                {
                    context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                            .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null ? null
                                    : adapter.getPath().getUri()));
                }
            }).show();
        } else if (id == R.id.action_mode_file_copy_here) {
            //todo: implement file copying
        } else
            return false;

        return true;
    }

    public static <T extends Editable> void shortcutItem(EditableListFragmentBase<T> fragment, FileListAdapter.FileHolder holder)
    {
        Kuick kuick = AppUtils.getKuick(fragment.getContext());

        try {
            kuick.reconstruct(holder);
            kuick.remove(holder);
            fragment.createSnackbar(R.string.mesg_removed).show();
        } catch (Exception e) {
            kuick.insert(holder);
            fragment.createSnackbar(R.string.mesg_added).show();
        } finally {
            kuick.broadcast();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(FileListAdapter.MODE_SORT_ORDER_ASCENDING);
        setDefaultSortingCriteria(FileListAdapter.MODE_SORT_BY_NAME);
        setDefaultGroupingCriteria(FileListAdapter.MODE_GROUP_BY_DEFAULT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new FileListAdapter(this));
        setEmptyListImage(R.drawable.ic_folder_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyFiles));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mMediaScanner = new MediaScannerConnection(getActivity(), null);

        mIntentFilter.addAction(ACTION_FILE_LIST_CHANGED);
        mIntentFilter.addAction(Kuick.ACTION_DATABASE_CHANGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_WRITE_ACCESS) {
                Uri pathUri = data.getData();

                if (Build.VERSION.SDK_INT >= 21 && pathUri != null && getContext() != null) {
                    getContext().getContentResolver().takePersistableUriPermission(pathUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        Kuick kuick = AppUtils.getKuick(getContext());
                        DocumentFile file = DocumentFile.fromUri(getContext(), pathUri, true);
                        kuick.publish(new FileListAdapter.FileHolder(getContext(), file));
                        kuick.broadcast();
                        goPath(null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_file_list, menu);

        MenuItem mountDirectory = menu.findItem(R.id.actions_file_list_mount_directory);

        if (Build.VERSION.SDK_INT >= 21 && mountDirectory != null)
            mountDirectory.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.actions_file_list_mount_directory) {
            requestMountStorage();
        } else if (id == R.id.actions_file_list_toggle_shortcut && getAdapter().getPath() != null) {
            shortcutItem(this, new FileListAdapter.FileHolder(getContext(), getAdapter().getPath()));
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        MenuItem shortcutMenuItem = menu.findItem(R.id.actions_file_list_toggle_shortcut);

        if (shortcutMenuItem != null) {
            boolean hasPath = getAdapter().getPath() != null;
            shortcutMenuItem.setEnabled(hasPath);

            if (hasPath)
                try {
                    AppUtils.getKuick(getContext()).reconstruct(new FileListAdapter.FileHolder(getContext(), getAdapter().getPath()));
                    shortcutMenuItem.setTitle(R.string.butn_removeShortcut);
                } catch (Exception e) {
                    shortcutMenuItem.setTitle(R.string.butn_addShortcut);
                }
        }
    }

    @Nullable
    @Override
    public PerformerMenu onCreatePerformerMenu(Context context)
    {
        return new PerformerMenu(context, new SelectionCallback(this, this));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        requireActivity().registerReceiver(mReceiver, mIntentFilter);
        mMediaScanner.connect();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireActivity().unregisterReceiver(mReceiver);
        mMediaScanner.disconnect();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (getAdapter().getPath() != null)
            outState.putString(EXTRA_FILE_LOCATION, getAdapter().getPath().getUri().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_FILE_LOCATION)) {
            try {
                goPath(FileUtils.fromUri(getContext(), Uri.parse(savedInstanceState.getString(EXTRA_FILE_LOCATION))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onListRefreshed()
    {
        super.onListRefreshed();

        // If the current path is different from the older one, move the scroll position
        // to the top.
        DocumentFile pathOnTrial = getAdapter().getPath();

        if (!(mLastKnownPath == null && getAdapter().getPath() == null)
                && (mLastKnownPath != null && !mLastKnownPath.equals(pathOnTrial)))
            getListView().scrollToPosition(0);

        mLastKnownPath = pathOnTrial;
    }

    public void goPath(DocumentFile file)
    {
        if (file != null && !file.canRead()) {
            createSnackbar(R.string.mesg_errorReadFolder, file.getName())
                    .show();

            return;
        }

        if (mPathChangedListener != null)
            mPathChangedListener.onPathChanged(file);

        getAdapter().goPath(file);
        refreshList();
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder, FileListAdapter.FileHolder object)
    {
        if (object.getViewType() == GroupEditableListAdapter.VIEW_TYPE_ACTION_BUTTON
                && object.getRequestCode() == FileListAdapter.REQUEST_CODE_MOUNT_FOLDER)
            requestMountStorage();
        else if (object.file != null && object.file.isDirectory()) {
            FileListFragment.this.goPath(object.file);
            AppUtils.showFolderSelectionHelp(this);
        } else
            performLayoutClickOpen(holder, object);

        return true;
    }

    @Override
    public boolean performLayoutClickOpen(GroupEditableListAdapter.GroupViewHolder holder, FileListAdapter.FileHolder object)
    {
        return FileUtils.openUriForeground(getActivity(), object.file) || super.performLayoutClickOpen(holder, object);
    }

    public void requestMountStorage()
    {
        if (Build.VERSION.SDK_INT < 21)
            return;

        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_WRITE_ACCESS);
        Toast.makeText(getActivity(), R.string.mesg_mountDirectoryHelp, Toast.LENGTH_LONG).show();
    }


    public void scanFile(DocumentFile file)
    {
        // FIXME: 9/11/18 There should be insert, remove, update
        if (file instanceof LocalDocumentFile && mMediaScanner.isConnected()) {
            String filePath = ((LocalDocumentFile) file).getFile().getAbsolutePath();
            mMediaScanner.scanFile(filePath, file.isDirectory() ? file.getType() : null);
        }
    }

    public void setOnPathChangedListener(OnPathChangedListener pathChangedListener)
    {
        mPathChangedListener = pathChangedListener;
    }

    public interface OnPathChangedListener
    {
        void onPathChanged(DocumentFile file);
    }

    private static class SelectionCallback extends SharingPerformerMenuCallback
    {
        private FileListFragment mFragment;

        public SelectionCallback(FileListFragment fragment, PerformerEngineProvider provider)
        {
            super(fragment.getActivity(), provider);
            mFragment = fragment;
        }

        @Override
        public boolean onPerformerMenuList(PerformerMenu performerMenu, MenuInflater inflater, Menu targetMenu)
        {
            super.onPerformerMenuList(performerMenu, inflater, targetMenu);
            inflater.inflate(R.menu.action_mode_file, targetMenu);
            return true;
        }

        @Override
        public boolean onPerformerMenuSelected(PerformerMenu performerMenu, MenuItem item)
        {
            IPerformerEngine performerEngine = getPerformerEngine();

            if (performerEngine == null)
                return false;

            List<? extends Selectable> selectableList = new ArrayList<>(performerEngine.getSelectionList());
            List<FileListAdapter.FileHolder> fileList = new ArrayList<>();

            for (Selectable selectable : selectableList)
                if (selectable instanceof FileListAdapter.FileHolder)
                    fileList.add((FileListAdapter.FileHolder) selectable);

            if (fileList.size() <= 0 || !handleEditingAction(item, mFragment, fileList))
                return super.onPerformerMenuSelected(performerMenu, item);

            // Currently, copy, rename, and delete is in-place which are all making changes and thus should end the
            // selection session. If something that does not make changes needs to implemented, it should be considered
            // if it makes more sense to define it instead of inside the 'handleEditionAction' method which is also
            // invoked by the individual item using popup menus.
            return true;
        }
    }
}