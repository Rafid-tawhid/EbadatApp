

package com.abc.sharefilesz.dialog;

import android.content.Context;

import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.R;
import com.genonbeta.android.framework.io.DocumentFile;

import java.util.ArrayList;
import java.util.List;

/**

 * date: 26.02.2018 08:53
 */

public class FileRenameDialog extends AbstractSingleTextInputDialog
{
    public static final String TAG = FileRenameDialog.class.getSimpleName();

    private List<FileListAdapter.FileHolder> mItemList = new ArrayList<>();

    public FileRenameDialog(Context context, List<? extends FileListAdapter.FileHolder> itemList,
                            final OnFileRenameListener renameListener)
    {
        super(context);

        mItemList.addAll(itemList);

        setTitle(mItemList.size() > 1 ? R.string.text_renameMultipleItems : R.string.text_rename);
        getEditText().setText(mItemList.size() > 1 ? "%d" : mItemList.get(0).fileName);

        setOnProceedClickListener(R.string.butn_rename, dialog -> {
            final String renameTo = getEditText().getText().toString();

            if (mItemList.size() > 1) {
            }

            try {
                String.format(renameTo, mItemList.size());
            } catch (Exception e) {
                return false;
            }

            return true;
        });
    }

    public interface OnFileRenameListener
    {
        void onFileRename(DocumentFile file, String displayName);

        void onFileRenameCompleted(Context context);
    }
}
