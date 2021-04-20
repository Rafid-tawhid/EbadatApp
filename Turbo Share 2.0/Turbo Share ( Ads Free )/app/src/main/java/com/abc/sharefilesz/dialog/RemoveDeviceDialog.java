

package com.abc.sharefilesz.dialog;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.R;

public class RemoveDeviceDialog extends AlertDialog.Builder
{
    public RemoveDeviceDialog(@NonNull final Activity activity, final Device device)
    {
        super(activity);

        setTitle(R.string.ques_removeDevice);
        setMessage(R.string.text_removeDeviceNotice);
        setNegativeButton(R.string.butn_cancel, null);
        setPositiveButton(R.string.butn_proceed, (dialog, which) -> AppUtils.getKuick(getContext()).removeAsynchronous(
                activity, device, null));
    }
}
