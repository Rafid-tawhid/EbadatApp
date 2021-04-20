

package com.abc.sharefilesz.task;

import android.net.Uri;

import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.framework.io.DocumentFile;

import java.io.IOException;
import java.util.List;

public class ChangeSaveDirectoryTask extends BackgroundTask
{
    private TransferGroup mGroup;
    private Uri mNewSavePath;
    private boolean mSkipMoving = false;

    public ChangeSaveDirectoryTask(TransferGroup group, Uri newSavePath)
    {
        mGroup = group;
        mNewSavePath = newSavePath;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        // TODO: 31.03.2020 Should we stop the tasks or not allow this operation while there are ongoing tasks?
        for (BackgroundTask task : getService().findTasksBy(FileTransferTask.identifyWith(mGroup.id,
                TransferObject.Type.INCOMING)))
            task.interrupt(true);

        List<TransferObject> checkList = AppUtils.getKuick(getService()).castQuery(
                TransferUtils.createIncomingSelection(mGroup.id), TransferObject.class);
        TransferGroup pseudoGroup = new TransferGroup(mGroup.id);

        try {
            if (!mSkipMoving) {
                // Illustrate new change to build the structure accordingly
                kuick().reconstruct(pseudoGroup);
                pseudoGroup.savePath = mNewSavePath.toString();

                for (TransferObject transferObject : checkList) {
                    throwIfInterrupted();

                    setOngoingContent(transferObject.name);
                    publishStatus();

                    try {
                        DocumentFile file = FileUtils.getIncomingPseudoFile(getService(), transferObject, mGroup,
                                false);
                        DocumentFile pseudoFile = FileUtils.getIncomingPseudoFile(getService(), transferObject,
                                pseudoGroup, true);

                        if (file != null && pseudoFile != null) {
                            if (file.canWrite())
                                FileUtils.move(getService(), file, pseudoFile, this);
                            else
                                throw new IOException("Failed to access: " + file.getUri());
                        }
                    } catch (Exception e) {
                        // TODO: 31.03.2020 Show the errors to the user
                    }
                }
            }

            mGroup.savePath = mNewSavePath.toString();
            kuick().publish(mGroup);
            kuick().broadcast();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public ChangeSaveDirectoryTask setSkipMoving(boolean skip)
    {
        mSkipMoving = skip;
        return this;
    }
}
