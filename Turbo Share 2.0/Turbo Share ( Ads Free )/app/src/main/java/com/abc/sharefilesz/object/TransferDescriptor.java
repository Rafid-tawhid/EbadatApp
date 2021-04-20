

package com.abc.sharefilesz.object;

import android.content.Context;
import android.net.Uri;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.framework.io.DocumentFile;

import java.io.FileNotFoundException;

public class TransferDescriptor
{
    public String directory;
    public String title;
    public DocumentFile file;

    public TransferDescriptor(DocumentFile documentFile, String directory)
    {
        this.file = documentFile;
        this.directory = directory;
        this.title = file.getName();
    }

    public TransferDescriptor(Context context, Uri uri, String directory) throws FileNotFoundException
    {
        this(FileUtils.fromUri(context, uri), directory);
    }
}