

package com.abc.sharefilesz.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.abc.sharefilesz.activity.AddDevicesToTransferActivity;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.TransferAssignee;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.service.backgroundservice.AttachableBgTask;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.object.*;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.abc.sharefilesz.util.ConnectionUtils;
import com.abc.sharefilesz.util.communicationbridge.CommunicationException;
import com.abc.sharefilesz.util.communicationbridge.NotAllowedException;
import com.abc.sharefilesz.util.communicationbridge.NotTrustedException;
import com.abc.sharefilesz.util.communicationbridge.UnknownCommunicationException;
import com.genonbeta.android.database.SQLQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.monora.coolsocket.core.response.Response;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static android.app.Activity.RESULT_OK;

public class AddDeviceTask extends AttachableBgTask<AddDevicesToTransferActivity>
{
    private final TransferGroup mGroup;
    private final Device mDevice;
    private final DeviceConnection mConnection;

    public AddDeviceTask(TransferGroup group, Device device, DeviceConnection connection)
    {
        mGroup = group;
        mDevice = device;
        mConnection = connection;
    }

    @Override
    public void onRun()
    {
        // TODO: 27.03.2020 Is nested transaction calls possible?
        Context context = getService().getApplicationContext();
        Kuick kuick = AppUtils.getKuick(context);
        SQLiteDatabase db = kuick.getWritableDatabase();
        CommunicationBridge bridge = new CommunicationBridge(kuick());
        ConnectionUtils utils = new ConnectionUtils(getService());
        boolean update = false;

        try {
            TransferAssignee assignee = new TransferAssignee(mGroup, mDevice, TransferObject.Type.OUTGOING,
                    mConnection);
            List<TransferObject> objectList = kuick.castQuery(db, new SQLQuery.Select(Kuick.TABLE_TRANSFER)
                            .setWhere(Kuick.FIELD_TRANSFER_GROUPID + "=? AND " + Kuick.FIELD_TRANSFER_TYPE
                                    + "=?", String.valueOf(mGroup.id), TransferObject.Type.OUTGOING.toString()),
                    TransferObject.class, null);

            try {
                // Checks if the current assignee is already on the list, if so, update
                kuick.reconstruct(db, assignee);
                update = true;
            } catch (Exception ignored) {
            }

            if (objectList.size() == 0)
                throw new Exception("Empty share holder id: " + mGroup.id);

            JSONArray filesArray = new JSONArray();

            for (TransferObject transferObject : objectList) {
                setOngoingContent(transferObject.name);
                transferObject.putFlag(assignee.deviceId, TransferObject.Flag.PENDING);

                if (isInterrupted())
                    throw new InterruptedException("Interrupted by user");

                try {
                    JSONObject json = new JSONObject()
                            .put(Keyword.INDEX_FILE_NAME, transferObject.name)
                            .put(Keyword.INDEX_FILE_SIZE, transferObject.size)
                            .put(Keyword.TRANSFER_REQUEST_ID, transferObject.id)
                            .put(Keyword.INDEX_FILE_MIME, transferObject.mimeType);

                    if (transferObject.directory != null)
                        json.put(Keyword.INDEX_DIRECTORY, transferObject.directory);

                    filesArray.put(json);
                } catch (Exception e) {
                    Log.e(AddDevicesToTransferActivity.TAG, "Sender error on fileUri: "
                            + e.getClass().getName() + " : " + transferObject.name);
                }
            }

            // so that if the user rejects, it won't be removed from the sender
            JSONObject jsonObject = new JSONObject()
                    .put(Keyword.REQUEST, Keyword.REQUEST_TRANSFER)
                    .put(Keyword.TRANSFER_GROUP_ID, mGroup.id)
                    .put(Keyword.FILES_INDEX, filesArray.toString());

            final ActiveConnection activeConnection = bridge.communicate(mDevice, mConnection);

            addCloser(userAction -> {
                try {
                    activeConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            activeConnection.reply(jsonObject.toString());

            Response response = activeConnection.receive();
            activeConnection.getSocket().close();

            JSONObject clientResponse = response.getAsJson();

            if (clientResponse.has(Keyword.RESULT) && clientResponse.getBoolean(Keyword.RESULT)) {
                setOngoingContent(context.getString(R.string.mesg_organizingFiles));

                if (update)
                    kuick.update(db, assignee, mGroup, progressListener());
                else
                    kuick.insert(db, assignee, mGroup, progressListener());

                addCloser(userAction -> kuick.remove(assignee));
                kuick.update(db, objectList, mGroup, progressListener());
                kuick.broadcast();

                post(new Call<AddDevicesToTransferActivity>(TaskId.Finalize, OVERRIDE_BY_SELF)
                {
                    @Override
                    public void now(AddDevicesToTransferActivity anchor)
                    {
                        anchor.setResult(RESULT_OK, new Intent()
                                .putExtra(AddDevicesToTransferActivity.EXTRA_DEVICE, mDevice)
                                .putExtra(AddDevicesToTransferActivity.EXTRA_GROUP, mGroup));

                        anchor.finish();
                    }
                });
            } else
                ConnectionUtils.throwCommunicationError(clientResponse, mDevice);
        } catch (UnknownCommunicationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NotTrustedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NotAllowedException e) {
            e.printStackTrace();
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

    private enum TaskId
    {
        Finalize
    }
}