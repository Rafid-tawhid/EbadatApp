

package com.abc.sharefilesz.receiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.WindowManager;
import com.abc.sharefilesz.R;

public class DialogEventReceiver extends BroadcastReceiver
{
    public final static String ACTION_DIALOG = "com.abc.sharefilesz.action.makeDialog";

    public final static String EXTRA_TITLE = "title";
    public final static String EXTRA_MESSAGE = "message";
    public final static String EXTRA_POSITIVE_INTENT = "positive";
    public final static String EXTRA_NEGATIVE_INTENT = "negative";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (ACTION_DIALOG.equals(intent.getAction()) && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
            showDialog(context, intent.getStringExtra(EXTRA_TITLE), intent.getStringExtra(EXTRA_MESSAGE),
                    intent.getParcelableExtra(EXTRA_POSITIVE_INTENT),
                    intent.getParcelableExtra(EXTRA_NEGATIVE_INTENT));
    }

    public void showDialog(Context context, String title, String message, final PendingIntent accept, final PendingIntent reject)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        if (title != null)
            dialogBuilder.setTitle(title);

        if (message != null)
            dialogBuilder.setMessage(message);

        if (accept != null)
            dialogBuilder.setPositiveButton(android.R.string.ok, (p1, p2) -> {
                        try {
                            accept.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
            );

        if (reject != null)
            dialogBuilder.setNegativeButton(android.R.string.cancel, (p1, p2) -> {
                        try {
                            reject.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
            );
        else
            dialogBuilder.setNegativeButton(R.string.butn_close, null);

        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
