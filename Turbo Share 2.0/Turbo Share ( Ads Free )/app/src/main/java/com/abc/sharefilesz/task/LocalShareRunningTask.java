

package com.abc.sharefilesz.task;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.abc.sharefilesz.activity.AddDevicesToTransferActivity;
import com.abc.sharefilesz.activity.ViewTransferActivity;
import com.abc.sharefilesz.activity.WebShareActivity;
import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.io.Containable;
import com.abc.sharefilesz.object.Shareable;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.object.Container;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.framework.io.DocumentFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LocalShareRunningTask extends BackgroundTask
{
    public static final String TAG = LocalShareRunningTask.class.getSimpleName();

    public List<? extends Shareable> mList;
    private boolean mFlagAddNewDevice;
    private boolean mFlagWebShare;

    public LocalShareRunningTask(List<? extends Shareable> list, boolean addNewDevice, boolean webShare)
    {
        mList = list;
        mFlagAddNewDevice = addNewDevice;
        mFlagWebShare = webShare;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        if (mList.size() <= 0)
            return;

        final Kuick kuick = AppUtils.getKuick(getService());
        final SQLiteDatabase db = kuick.getWritableDatabase();
        final TransferGroup group = new TransferGroup(AppUtils.getUniqueNumber());
        final List<TransferObject> list = new ArrayList<>();

        for (Shareable shareable : mList) {
            Containable containable = shareable instanceof Container ? ((Container) shareable).expand() : null;

            if (isInterrupted())
                throw new InterruptedException();

            if (shareable instanceof FileListAdapter.FileHolder) {
                DocumentFile file = ((FileListAdapter.FileHolder) shareable).file;
                TransferUtils.createFolderStructure(list, group.id, file, shareable.fileName, this,
                        null);
            } else
                list.add(TransferObject.from(shareable, group.id, containable == null ? null : shareable.friendlyName));

            if (containable != null)
                for (Uri uri : containable.children)
                    try {
                        list.add(TransferObject.from(FileUtils.fromUri(getService(), uri), group.id,
                                shareable.friendlyName));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
        }

        if (list.size() <= 0) {
            // TODO: 9.03.2020 Make this more sophisticated. User may not be able to understand that there is no content.
            Log.d(TAG, "onRun: No content is located with uri data");
            return;
        }

        addCloser((userAction -> kuick.remove(db, group, null, null)));
        kuick.insert(db, list, group, null);

        if (mFlagWebShare) {
            group.isServedOnWeb = true;

            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getService(),
                    R.string.text_transferSharedOnBrowser, Toast.LENGTH_SHORT).show());
        }

        kuick.insert(db, group, null, null);
        ViewTransferActivity.startInstance(getService(), group);

        if (mFlagWebShare)
            getService().startActivity(new Intent(getService(), WebShareActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK));
        else
            AddDevicesToTransferActivity.startInstance(getService(), group, mFlagAddNewDevice);

        kuick.broadcast();
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

}
