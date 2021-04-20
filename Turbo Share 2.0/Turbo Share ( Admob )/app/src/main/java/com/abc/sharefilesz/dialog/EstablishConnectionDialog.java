

package com.abc.sharefilesz.dialog;

import android.app.Activity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.app.ProgressDialog;
import com.abc.sharefilesz.callback.OnConnectionSelectionListener;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.service.backgroundservice.BaseAttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.task.AssessNetworkTask;
import com.abc.sharefilesz.R;
import com.genonbeta.android.framework.util.Stoppable;
import com.genonbeta.android.framework.util.StoppableImpl;

import java.util.List;

public class EstablishConnectionDialog extends ProgressDialog
{
    EstablishConnectionDialog(Activity activity)
    {
        super(activity);
    }

    public static void show(Activity activity, Device device, @Nullable OnConnectionSelectionListener listener)
    {
        Stoppable stoppable = new StoppableImpl();

        EstablishConnectionDialog dialog = new EstablishConnectionDialog(activity);
        LocalTaskBinder binder = new LocalTaskBinder(activity, dialog, device, listener);
        AssessNetworkTask task = new AssessNetworkTask(device);

        task.setAnchor(binder);
        task.setStoppable(stoppable);

        dialog.setTitle(R.string.text_automaticNetworkConnectionOngoing);
        dialog.setOnDismissListener((dialog1 -> stoppable.interrupt()));
        dialog.setOnCancelListener(dialog1 -> stoppable.interrupt());
        dialog.show();

        BackgroundService.run(activity, task);
    }

    static class LocalTaskBinder implements AssessNetworkTask.CalculationResultListener
    {
        Activity activity;
        EstablishConnectionDialog dialog;
        Device device;
        OnConnectionSelectionListener listener;

        LocalTaskBinder(Activity activity, EstablishConnectionDialog dialog, Device device,
                        OnConnectionSelectionListener listener)
        {
            this.activity = activity;
            this.dialog = dialog;
            this.device = device;
            this.listener = listener;
        }

        @Override
        public void onTaskStateChanged(BaseAttachableBgTask task)
        {
            if (task.isFinished()) {
                dialog.dismiss();
            } else {
                dialog.setMax(task.progress().getTotal());
                dialog.setProgress(task.progress().getTotal());
            }
        }

        @Override
        public boolean onTaskMessage(TaskMessage message)
        {
            return false;
        }

        @Override
        public void onCalculationResult(AssessNetworkTask.ConnectionResult[] connectionResults)
        {
            if (connectionResults.length <= 0)
                return;

            List<AssessNetworkTask.ConnectionResult> availableList = AssessNetworkTask.getAvailableList(connectionResults);
            if (availableList.size() <= 0) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.text_error)
                        .setMessage(R.string.text_automaticNetworkConnectionFailed)
                        .setNeutralButton(R.string.butn_close, null)
                        .setPositiveButton(R.string.butn_retry,
                                (dialog, which) -> EstablishConnectionDialog.show(activity, device, listener));
            } else if (listener == null) {
                new ConnectionTestDialog(activity, device, connectionResults).show();
            } else
                listener.onConnectionSelection(availableList.get(0).connection);
        }
    }
}