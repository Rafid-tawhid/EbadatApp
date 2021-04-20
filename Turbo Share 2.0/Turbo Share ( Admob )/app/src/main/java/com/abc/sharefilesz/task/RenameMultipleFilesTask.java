

package com.abc.sharefilesz.task;

import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.dialog.FileRenameDialog;
import com.abc.sharefilesz.util.FileUtils;

import java.util.List;

public class RenameMultipleFilesTask extends BackgroundTask
{
    private List<FileListAdapter.FileHolder> mList;
    private String mNewName;

    public RenameMultipleFilesTask(List<FileListAdapter.FileHolder> fileList, String renameTo)
    {
        mList = fileList;
        mNewName = renameTo;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        if (mList.size() <= 0)
            return;

        progress().addToTotal(mList.size());

        for (int i = 0; i < mList.size(); i++) {
            FileListAdapter.FileHolder fileHolder = mList.get(i);

            setOngoingContent(fileHolder.friendlyName);
            progress().addToCurrent(1);
            publishStatus();

            String ext = FileUtils.getFileFormat(fileHolder.file.getName());
            ext = ext != null ? String.format(".%s", ext) : "";

            // TODO: 1.04.2020 Use listener
            renameFile(kuick(), fileHolder, String.format("%s%s", String.format(mNewName, i), ext), null);
        }


        //if (renameListener != null)
        //    renameListener.onFileRenameCompleted(getService());
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getTitle()
    {
        return getService().getString(R.string.text_renameMultipleItems);
    }

    public boolean renameFile(Kuick kuick, FileListAdapter.FileHolder holder, String renameTo,
                              FileRenameDialog.OnFileRenameListener renameListener)
    {
        try {
            if (FileListAdapter.FileHolder.Type.Bookmarked.equals(holder.getType())
                    || FileListAdapter.FileHolder.Type.Mounted.equals(holder.getType())) {
                holder.friendlyName = renameTo;

                kuick.publish(holder);
                kuick.broadcast();
            } else if (holder.file.canWrite() && holder.file.renameTo(renameTo)) {
                if (renameListener != null)
                    renameListener.onFileRename(holder.file, renameTo);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
