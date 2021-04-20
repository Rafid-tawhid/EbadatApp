

package com.abc.sharefilesz.dialog;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.abc.sharefilesz.activity.HomeActivity;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.R;

/**

 * date: 18.11.2017 20:16
 */

public class RationalePermissionRequest extends AlertDialog.Builder
{
    public PermissionRequest mPermissionQueue;

    public RationalePermissionRequest(final Activity activity, @NonNull PermissionRequest permission,
                                      boolean killActivityOtherwise)
    {
        super(activity);

        mPermissionQueue = permission;

        setCancelable(false);
        setTitle(permission.title);
        setMessage(permission.message);

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermissionQueue.permission))
            setNeutralButton(R.string.butn_settings, (dialogInterface, i) -> AppUtils.startApplicationDetails(activity));

        setPositiveButton(R.string.butn_ask, (dialogInterface, i) -> ActivityCompat.requestPermissions(activity,
                new String[]{mPermissionQueue.permission}, HomeActivity.REQUEST_PERMISSION_ALL));

        if (killActivityOtherwise)
            setNegativeButton(R.string.butn_reject, (dialogInterface, i) -> activity.finish());
        else
            setNegativeButton(R.string.butn_close, null);
    }

    public static AlertDialog requestIfNecessary(Activity activity, PermissionRequest permissionQueue,
                                                 boolean killActivityOtherwise)
    {
        return ActivityCompat.checkSelfPermission(activity, permissionQueue.permission)
                == PackageManager.PERMISSION_GRANTED ? null : new RationalePermissionRequest(activity, permissionQueue,
                killActivityOtherwise).show();
    }

    public static class PermissionRequest
    {
        public String permission;
        public String title;
        public String message;
        public boolean required;

        public PermissionRequest(String permission, String title, String message)
        {
            this(permission, title, message, true);
        }

        public PermissionRequest(String permission, String title, String message, boolean required)
        {
            this.permission = permission;
            this.title = title;
            this.message = message;
            this.required = required;
        }

        public PermissionRequest(Context context, String permission, int titleRes, int messageRes)
        {
            this(permission, context.getString(titleRes), context.getString(messageRes));
        }
    }
}