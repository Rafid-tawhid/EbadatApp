

package com.abc.sharefilesz.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.BuildConfig;
import com.abc.sharefilesz.R;
import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.util.Stoppable;
import com.genonbeta.android.framework.util.StoppableImpl;

import java.io.File;

public class ShareAppDialog extends AlertDialog.Builder
{
    public ShareAppDialog(@NonNull final Context context)
    {
        super(context);

        setMessage(R.string.ques_shareAsApkOrLink);

        setNegativeButton(R.string.butn_cancel, null);
        setNeutralButton(R.string.butn_asApk, (dialogInterface, i) -> shareAsApk(context));
        setPositiveButton(R.string.butn_asLink, (dialogInterface, i) -> shareAsLink(context));
    }

    private void shareAsApk(@NonNull final Context context)
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Stoppable interrupter = new StoppableImpl();

                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(context.getApplicationInfo().packageName, 0);

                String fileName = packageInfo.applicationInfo.loadLabel(pm) + "_" + packageInfo.versionName + ".apk";

                DocumentFile storageDirectory = FileUtils.getApplicationDirectory(context.getApplicationContext());
                DocumentFile codeFile = DocumentFile.fromFile(new File(context.getApplicationInfo().sourceDir));
                DocumentFile cloneFile = storageDirectory.createFile(null, FileUtils.getUniqueFileName(
                        storageDirectory, fileName, true));

                FileUtils.copy(context, codeFile, cloneFile, interrupter);

                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_STREAM, FileUtils.getSecureUri(context, cloneFile))
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .setType(cloneFile.getType());

                    context.startActivity(Intent.createChooser(sendIntent, context.getString(
                            R.string.text_fileShareAppChoose)));
                } catch (IllegalArgumentException e) {
                    Toast.makeText(context, R.string.mesg_providerNotAllowedError, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void shareAsLink(@NonNull final Context context)
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                String textToShare = context.getString(R.string.text_linkTurboShare, "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);

                Intent sendIntent = new Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, textToShare)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setType("text/plain");

                context.startActivity(Intent.createChooser(sendIntent, context.getString(
                        R.string.text_fileShareAppChoose)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
