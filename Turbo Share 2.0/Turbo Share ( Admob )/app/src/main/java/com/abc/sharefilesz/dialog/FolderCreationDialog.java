

package com.abc.sharefilesz.dialog;

import android.content.Context;
import android.widget.Toast;

import com.abc.sharefilesz.R;
import com.genonbeta.android.framework.io.DocumentFile;

/**

 * Date: 5/30/17 12:18 PM
 */

public class FolderCreationDialog extends AbstractSingleTextInputDialog
{
    public FolderCreationDialog(final Context context, final DocumentFile currentFolder,
                                final OnFolderCreatedListener createdListener)
    {
        super(context);

        setTitle(R.string.text_createFolder);
        setOnProceedClickListener(R.string.butn_create, dialog -> {
            String fileName = getEditText().getText().toString();

            if (fileName.length() == 0)
                return false;

            DocumentFile createdFile = currentFolder.createDirectory(fileName);

            if (createdFile == null) {
                Toast.makeText(getContext(), R.string.mesg_folderCreateError, Toast.LENGTH_SHORT).show();
                return false;
            }

            createdListener.onFolderCreated(createdFile);
            dialog.dismiss();

            return true;
        });
    }

    public interface OnFolderCreatedListener
    {
        void onFolderCreated(DocumentFile directoryFile);
    }
}
