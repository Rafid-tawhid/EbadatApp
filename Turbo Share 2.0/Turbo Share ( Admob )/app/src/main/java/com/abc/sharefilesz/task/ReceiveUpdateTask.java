

package com.abc.sharefilesz.task;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.service.backgroundservice.BackgroundTask;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.communicationbridge.CommunicationException;
import com.genonbeta.android.framework.io.DocumentFile;
import org.json.JSONException;
import org.json.JSONObject;
import org.monora.coolsocket.core.response.Response;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

public class ReceiveUpdateTask extends BackgroundTask
{
    private final Device mDevice;
    private final DeviceConnection mConnection;

    public ReceiveUpdateTask(Device device, DeviceConnection connection)
    {
        mDevice = device;
        mConnection = connection;
    }

    @Override
    public void onRun() throws InterruptedException
    {
        int versionCode;
        long updateSize;

        try (ActiveConnection activeConnection = CommunicationBridge.openConnection(mConnection.toInet4Address())) {
            setOngoingContent(getService().getString(R.string.mesg_waiting));

            activeConnection.reply(new JSONObject()
                    .put(Keyword.REQUEST, Keyword.REQUEST_UPDATE_V2)
                    .toString());

            {
                Response response = activeConnection.receive();
                JSONObject responseJSON = response.getAsJson();

                if (!responseJSON.getBoolean(Keyword.RESULT))
                    throw new CommunicationException("Update request was denied by the target");

                versionCode = responseJSON.getInt(Keyword.APP_INFO_VERSION_CODE);
                updateSize = responseJSON.getLong(Keyword.INDEX_FILE_SIZE);

                if (updateSize < 1)
                    throw new IOException("The target did not report update size");
            }

            {
                activeConnection.reply(new JSONObject()
                        .put(Keyword.RESULT, true)
                        .toString());
            }

            progress().addToTotal(100);
            setOngoingContent(getService().getString(R.string.text_receiving));
            publishStatus();

            DocumentFile dir = FileUtils.getApplicationDirectory(getService());
            String fileName = FileUtils.getUniqueFileName(dir, getService().getString(R.string.app_name)
                    + "_v" + versionCode + ".apk", true);
            DocumentFile tmpFile = dir.createFile(null, fileName);

            InputStream inputStream = activeConnection.getSocket().getInputStream();
            OutputStream outputStream = getService().getContentResolver().openOutputStream(tmpFile.getUri());

            if (outputStream == null)
                throw new IOException("Could open a file to save the update.");

            long receivedBytes = 0;
            long lastRead = System.nanoTime();
            int len;
            byte[] buffer = new byte[AppConfig.BUFFER_LENGTH_DEFAULT];

            while (receivedBytes < updateSize) {
                throwIfInterrupted();

                if ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();

                    receivedBytes += len;
                    lastRead = System.nanoTime();

                    progress().setTotal((int) ((100 / updateSize) * receivedBytes));
                    publishStatus();
                }

                if (System.nanoTime() - lastRead > AppConfig.DEFAULT_SOCKET_TIMEOUT * 1e6)
                    throw new TimeoutException("Did not read for 5secs");
            }

            post(TaskMessage.newInstance()
                    .setTone(TaskMessage.Tone.Positive)
                    .setTitle(getService(), R.string.text_taskCompleted)
                    .setMessage(getService(), R.string.mesg_updateDownloadComplete)
                    .addAction(getService(), R.string.butn_open, TaskMessage.Tone.Positive,
                            (service, msg, action) -> FileUtils.openUriForeground(getService(), tmpFile)));
        } catch (IOException | CommunicationException | JSONException | TimeoutException e) {
            e.printStackTrace();

            post(TaskMessage.newInstance()
                    .setTone(TaskMessage.Tone.Negative)
                    .setTitle(getService(), R.string.mesg_fileReceiveError)
                    .setMessage(getService(), R.string.mesg_updateDownloadError)
                    .addAction(getService(), R.string.butn_retry, TaskMessage.Tone.Positive,
                            (service, msg, action) -> rerun(service)));
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
        return getService().getString(R.string.mesg_ongoingUpdateDownload);
    }
}
