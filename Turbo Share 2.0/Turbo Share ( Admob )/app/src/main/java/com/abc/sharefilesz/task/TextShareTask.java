

package com.abc.sharefilesz.task;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.abc.sharefilesz.util.ConnectionUtils;
import org.json.JSONObject;
import org.monora.coolsocket.core.session.ActiveConnection;

public class TextShareTask extends BackgroundTask
{
    private final Device mDevice;
    private final DeviceConnection mConnection;
    private final String mText;

    public TextShareTask(Device device, DeviceConnection connection, String text)
    {
        mDevice = device;
        mConnection = connection;
        mText = text;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        CommunicationBridge bridge = new CommunicationBridge(kuick());

        try (ActiveConnection activeConnection = bridge.communicate(mDevice, mConnection)) {
            final JSONObject jsonRequest = new JSONObject()
                    .put(Keyword.REQUEST, Keyword.REQUEST_CLIPBOARD)
                    .put(Keyword.TRANSFER_CLIPBOARD_TEXT, mText);

            activeConnection.reply(jsonRequest.toString());

            JSONObject response = activeConnection.receive().getAsJson();

            if (response.has(Keyword.RESULT) && response.getBoolean(Keyword.RESULT)) {
                // TODO: 31.03.2020 implement
            } else
                ConnectionUtils.throwCommunicationError(response, mDevice);
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
}
