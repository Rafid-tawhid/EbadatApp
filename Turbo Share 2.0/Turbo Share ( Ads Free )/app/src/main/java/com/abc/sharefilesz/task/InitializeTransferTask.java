

package com.abc.sharefilesz.task;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.TransferAssignee;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.genonbeta.android.framework.util.Stoppable;
import org.json.JSONObject;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.io.IOException;

public class InitializeTransferTask extends BackgroundTask
{
    private final Device mDevice;
    private final DeviceConnection mConnection;
    private final TransferAssignee mAssignee;

    public InitializeTransferTask(Device device, DeviceConnection connection, TransferAssignee assignee)
    {
        mDevice = device;
        mConnection = connection;
        mAssignee = assignee;
    }

    @Override
    protected void onRun()
    {
        CommunicationBridge bridge = new CommunicationBridge(kuick());

        try (final ActiveConnection activeConnection = bridge.communicate(mDevice, mConnection)) {
            Stoppable.Closer connectionCloser = userAction -> {
                try {
                    activeConnection.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            addCloser(connectionCloser);

            JSONObject jsonRequest = new JSONObject()
                    .put(Keyword.REQUEST, Keyword.REQUEST_TRANSFER_JOB)
                    .put(Keyword.TRANSFER_GROUP_ID, mAssignee.groupId);

            activeConnection.reply(jsonRequest.toString());

            final JSONObject responseJSON = activeConnection.receive().getAsJson();
            activeConnection.getSocket().close();
            removeCloser(connectionCloser);

            // FIXME: 21.03.2020 How to achieve these back?
            /*
            if (!responseJSON.getBoolean(Keyword.RESULT) && !activity.isFinishing())
                activity.runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    @StringRes int msg = R.string.mesg_somethingWentWrong;
                    String errorMsg = Keyword.ERROR_UNKNOWN;

                    try {
                        errorMsg = responseJSON.getString(Keyword.ERROR);
                    } catch (JSONException e) {
                        // do nothing
                    }

                    switch (errorMsg) {
                        case Keyword.ERROR_NOT_FOUND:
                            msg = R.string.mesg_notValidTransfer;
                            break;
                        case Keyword.ERROR_REQUIRE_TRUST:
                            msg = R.string.mesg_errorNotTrusted;
                            break;
                        case Keyword.ERROR_NOT_ALLOWED:
                            msg = R.string.mesg_notAllowed;
                            break;
                    }

                    builder.setMessage(getService().getString(msg));
                    builder.setNegativeButton(R.string.butn_close, null);
                    builder.setPositiveButton(R.string.butn_retry,
                            (dialog, which) -> rerun(AppUtils.getBgService(dialog)));

                    builder.show();
                });

             */
        } catch (Exception e) {
            // FIXME: 21.03.2020
            /*
            if (!activity.isFinishing())
                activity.runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setMessage(getService().getString(R.string.mesg_connectionFailure));
                    builder.setNegativeButton(R.string.butn_close, null);

                    builder.setPositiveButton(R.string.butn_retry,
                            (dialog, which) -> rerun(AppUtils.getBgService(dialog)));

                    builder.show();
                });
             */
        }
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getTitle()
    {
        return null;
    }
}
