

package com.abc.sharefilesz.task;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.abc.sharefilesz.activity.AddDevicesToTransferActivity;
import com.abc.sharefilesz.activity.ViewTransferActivity;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.backgroundservice.AttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.AttachedTaskListener;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.io.DocumentFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class OrganizeSharingTask extends AttachableBgTask<AttachedTaskListener>
{
    private List<Uri> mUriList;

    public OrganizeSharingTask(List<Uri> fileUris)
    {
        mUriList = fileUris;
    }

    @Override
    public void onRun() throws InterruptedException
    {
        final SQLiteDatabase db = kuick().getWritableDatabase();
        final TransferGroup group = new TransferGroup(AppUtils.getUniqueNumber());
        final List<TransferObject> list = new ArrayList<>();

        progress().addToTotal(mUriList.size());
        publishStatus();

        for (Uri uri : mUriList) {
            if (isInterrupted())
                throw new InterruptedException();

            progress().addToCurrent(1);

            try {
                DocumentFile file = FileUtils.fromUri(getService(), uri);
                setOngoingContent(file.getName());
                publishStatus();

                if (file.isDirectory())
                    TransferUtils.createFolderStructure(list, group.id, file, file.getName(), this,
                            progressListener());
                else
                    list.add(TransferObject.from(file, group.id, null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (list.size() > 0) {
            kuick().insert(db, list, group, progressListener());
            kuick().insert(db, group, null, progressListener());
            addCloser((userAction) -> kuick().remove(db, new SQLQuery.Select(Kuick.TABLE_TRANSFER)
                    .setWhere(String.format("%s = ?", Kuick.FIELD_TRANSFER_GROUPID), String.valueOf(group.id))));

            ViewTransferActivity.startInstance(getService(), group);
            AddDevicesToTransferActivity.startInstance(getService(), group, true);
            kuick().broadcast();
        }
    }

    @Override
    public String getDescription()
    {
        return getService().getString(R.string.mesg_organizingFiles);
    }

    @Override
    public String getTitle()
    {
        return getService().getString(R.string.mesg_organizingFiles);
    }
}
