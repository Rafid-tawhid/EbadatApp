

package com.abc.sharefilesz.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.config.AppConfig;
import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.util.Stoppable;

import java.io.File;
import java.io.IOException;

public class FileUtils extends com.genonbeta.android.framework.util.FileUtils
{
    public static void copy(Context context, DocumentFile source, DocumentFile destination, Stoppable stoppable)
            throws Exception
    {
        copy(context, source, destination, stoppable, AppConfig.BUFFER_LENGTH_DEFAULT,
                AppConfig.DEFAULT_SOCKET_TIMEOUT);
    }

    public static DocumentFile getApplicationDirectory(Context context)
    {
        File defaultPath = getDefaultApplicationDirectoryPath(context);
        SharedPreferences defaultPreferences = AppUtils.getDefaultPreferences(context);

        if (defaultPreferences.contains("storage_path")) {
            try {
                DocumentFile savePath = fromUri(context, Uri.parse(defaultPreferences.getString("storage_path",
                        null)));

                if (savePath.isDirectory() && savePath.canWrite())
                    return savePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (defaultPath.isFile())
            defaultPath.delete();

        if (!defaultPath.isDirectory())
            defaultPath.mkdirs();

        return DocumentFile.fromFile(defaultPath);
    }

    @SuppressWarnings("deprecation")
    public static File getDefaultApplicationDirectoryPath(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29)
            return context.getNoBackupFilesDir();

        File primaryDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if ((!primaryDir.isDirectory() && !primaryDir.mkdirs()) || !primaryDir.canWrite())
            primaryDir = Environment.getExternalStorageDirectory();

        return new File(primaryDir + File.separator + context.getString(R.string.app_name));
    }

    public static String getFileFormat(String fileName)
    {
        final int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1).toLowerCase() : null;
    }

    public static DocumentFile getIncomingPseudoFile(Context context, TransferObject transferObject,
                                                     TransferGroup group, boolean createIfNotExists) throws IOException
    {
        return fetchFile(getSavePath(context, group), transferObject.directory, transferObject.file, createIfNotExists);
    }

    public static DocumentFile getIncomingFile(Context context, TransferObject transferObject, TransferGroup group)
            throws IOException
    {
        DocumentFile pseudoFile = getIncomingPseudoFile(context, transferObject, group, true);

        if (!pseudoFile.canWrite())
            throw new IOException("File cannot be created or you don't have permission write on it");

        return pseudoFile;
    }

    public static String getReadableUri(String uri)
    {
        return getReadableUri(Uri.parse(uri), uri);
    }

    public static String getReadableUri(Uri uri)
    {
        return getReadableUri(uri, uri.toString());
    }

    public static String getReadableUri(Uri uri, @Nullable String defaultValue)
    {
        return uri.getPath() == null ? defaultValue : uri.getPath();
    }

    public static boolean move(Context context, DocumentFile targetFile, DocumentFile destinationFile,
                               Stoppable stoppable) throws Exception
    {
        return move(context, targetFile, destinationFile, stoppable, AppConfig.BUFFER_LENGTH_DEFAULT,
                AppConfig.DEFAULT_SOCKET_TIMEOUT);
    }

    public static DocumentFile getSavePath(Context context, TransferGroup group)
    {
        DocumentFile defaultFolder = FileUtils.getApplicationDirectory(context);

        if (group.savePath != null) {
            try {
                DocumentFile savePath = fromUri(context, Uri.parse(group.savePath));

                if (savePath.isDirectory() && savePath.canWrite())
                    return savePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            group.savePath = defaultFolder.getUri().toString();
            AppUtils.getKuick(context).publish(group);
        }

        return defaultFolder;
    }

    public static boolean openUriForeground(Context context, DocumentFile file)
    {
        if (!openUri(context, file)) {
            Toast.makeText(context, context.getString(R.string.mesg_openFailure, file.getName()), Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        return true;
    }

    /**
     * When the transfer is done, this saves the uniquely named file to its actual name held in {@link TransferObject}.
     *
     * @param savePath       The save path that contains currentFile
     * @param currentFile    The file that should be renamed
     * @param transferObject The transfer request
     * @return File moved to its actual name
     * @throws IOException Thrown when rename fails
     */
    public static DocumentFile saveReceivedFile(DocumentFile savePath, DocumentFile currentFile,
                                                TransferObject transferObject) throws Exception
    {
        String uniqueName = FileUtils.getUniqueFileName(savePath, transferObject.name, true);

        // FIXME: 7/30/19 The rename always fails when renaming TreeDocumentFile
        if (!currentFile.renameTo(uniqueName))
            throw new IOException("Failed to rename object: " + currentFile);

        transferObject.file = uniqueName;
        savePath.sync();

        return savePath.findFile(uniqueName);
    }
}
