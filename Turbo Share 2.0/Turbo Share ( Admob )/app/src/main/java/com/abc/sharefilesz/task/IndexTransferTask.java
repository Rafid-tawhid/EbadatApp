

package com.abc.sharefilesz.task;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.TransferAssignee;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.object.*;
import com.abc.sharefilesz.util.DynamicNotification;
import com.genonbeta.android.database.Progress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IndexTransferTask extends BackgroundTask
{
    private long mGroupId;
    private boolean mNoPrompt;
    private DeviceConnection mConnection;
    private Device mDevice;
    private String mJsonIndex;

    public IndexTransferTask(final long groupId, final String jsonIndex, final Device device,
                             final DeviceConnection connection, final boolean noPrompt)
    {
        mGroupId = groupId;
        mJsonIndex = jsonIndex;
        mDevice = device;
        mConnection = connection;
        mNoPrompt = noPrompt;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        final SQLiteDatabase db = kuick().getWritableDatabase();
        final JSONArray jsonArray;
        TransferGroup group = new TransferGroup(mGroupId);
        TransferAssignee assignee = new TransferAssignee(group, mDevice, TransferObject.Type.INCOMING, mConnection);
        final DynamicNotification notification = getNotificationHelper().notifyPrepareFiles(group, mDevice);

        notification.setProgress(0, 0, true);

        try {
            jsonArray = new JSONArray(mJsonIndex);
        } catch (Exception e) {
            notification.cancel();
            e.printStackTrace();
            return;
        }

        notification.setProgress(0, 0, false);
        boolean usePublishing = false;

        try {
            kuick().reconstruct(group);
            usePublishing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        kuick().publish(group);
        kuick().publish(assignee);

        long uniqueId = System.currentTimeMillis(); // The uniqueIds
        List<TransferObject> pendingRegistry = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            if (isInterrupted())
                break;

            try {
                if (!(jsonArray.get(i) instanceof JSONObject))
                    continue;

                JSONObject index = jsonArray.getJSONObject(i);

                if (index != null && index.has(Keyword.INDEX_FILE_NAME)
                        && index.has(Keyword.INDEX_FILE_SIZE) && index.has(Keyword.INDEX_FILE_MIME)
                        && index.has(Keyword.TRANSFER_REQUEST_ID)) {

                    TransferObject transferObject = new TransferObject(index.getLong(Keyword.TRANSFER_REQUEST_ID),
                            mGroupId, index.getString(Keyword.INDEX_FILE_NAME),
                            "." + (uniqueId++) + "." + AppConfig.EXT_FILE_PART,
                            index.getString(Keyword.INDEX_FILE_MIME), index.getLong(Keyword.INDEX_FILE_SIZE),
                            TransferObject.Type.INCOMING);

                    if (index.has(Keyword.INDEX_DIRECTORY))
                        transferObject.directory = index.getString(Keyword.INDEX_DIRECTORY);

                    pendingRegistry.add(transferObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // TODO: 25.03.2020 Use native progressListener
        Progress.Listener progressUpdater = new Progress.SimpleListener()
        {
            long lastNotified = System.currentTimeMillis();

            @Override
            public boolean onProgressChange(Progress progress)
            {
                if ((System.currentTimeMillis() - lastNotified) > 1000) {
                    lastNotified = System.currentTimeMillis();
                    notification.updateProgress(progress.getTotal(), progress.getCurrent(), false);
                }

                return !isInterrupted();
            }
        };

        if (pendingRegistry.size() > 0) {
            if (usePublishing)
                kuick().publish(db, pendingRegistry, group, progressUpdater);
            else
                kuick().insert(db, pendingRegistry, group, progressUpdater);
        }

        notification.cancel();

        if (isInterrupted())
            kuick().remove(group);
        else if (pendingRegistry.size() > 0) {
            getService().sendBroadcast(new Intent(BackgroundService.ACTION_INCOMING_TRANSFER_READY)
                    .putExtra(BackgroundService.EXTRA_GROUP, group)
                    .putExtra(BackgroundService.EXTRA_DEVICE, mDevice));

            if (mNoPrompt)
                try {
                    getService().run(FileTransferTask.createFrom(kuick(), group, mDevice,
                            TransferObject.Type.INCOMING));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else
                getNotificationHelper().notifyTransferRequest(mDevice, group, TransferObject.Type.INCOMING,
                        pendingRegistry);
        }

        kuick().broadcast();
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
