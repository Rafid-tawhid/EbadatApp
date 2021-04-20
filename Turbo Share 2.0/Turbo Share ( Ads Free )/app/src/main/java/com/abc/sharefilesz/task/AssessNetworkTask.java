

package com.abc.sharefilesz.task;

import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.service.backgroundservice.AttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.AttachedTaskListener;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.CommunicationBridge;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.util.MathUtils;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AssessNetworkTask extends AttachableBgTask<AssessNetworkTask.CalculationResultListener>
{
    private final Device mDevice;

    public AssessNetworkTask(Device device)
    {
        mDevice = device;
    }

    @Override
    protected void onRun() throws InterruptedException
    {
        List<DeviceConnection> knownConnectionList = AppUtils.getKuick(getService()).castQuery(
                new SQLQuery.Select(Kuick.TABLE_DEVICECONNECTION)
                        .setWhere(Kuick.FIELD_DEVICECONNECTION_DEVICEID + "=?", mDevice.id)
                        .setOrderBy(Kuick.FIELD_DEVICECONNECTION_LASTCHECKEDDATE + " DESC"), DeviceConnection.class);
        ConnectionResult[] results = new ConnectionResult[knownConnectionList.size()];

        progress().addToTotal(knownConnectionList.size());
        publishStatus();

        if (results.length > 0) {
            for (int i = 0; i < results.length; i++) {
                throwIfInterrupted();

                ConnectionResult connectionResult = results[i] = new ConnectionResult(knownConnectionList.get(i));

                setOngoingContent(connectionResult.connection.adapterName);
                progress().addToCurrent(1);
                publishStatus();

                try {
                    CommunicationBridge client = new CommunicationBridge(kuick());
                    long startTime = System.nanoTime();
                    ActiveConnection connection = client.connectWithHandshake(connectionResult.connection,
                            true);
                    connectionResult.pingTime = System.nanoTime() - startTime;
                    connectionResult.successful = true;

                    connection.getSocket().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Comparator<ConnectionResult> connectionComparator = (resultFirst, resultLast) -> {
                // make sure we are not comparing unsuccessful attempts with their pingTime values.
                if (resultFirst.successful != resultLast.successful)
                    return resultFirst.successful ? 1 : -1;

                return MathUtils.compare(resultLast.pingTime, resultFirst.pingTime);
            };

            Arrays.sort(results, connectionComparator);
        }

        if (hasAnchor())
            post(() -> getAnchor().onCalculationResult(results));
    }

    public static List<ConnectionResult> getAvailableList(ConnectionResult[] results)
    {
        List<ConnectionResult> availableList = new ArrayList<>();
        for (ConnectionResult result : results)
            if (result.successful)
                availableList.add(result);
        return availableList;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getTitle()
    {
        return getService().getString(R.string.text_connectionTest);
    }

    public interface CalculationResultListener extends AttachedTaskListener
    {
        void onCalculationResult(ConnectionResult[] connectionResults);
    }

    public static class ConnectionResult
    {
        public DeviceConnection connection;
        public long pingTime = 0; // nanoseconds

        public boolean successful = false;

        public ConnectionResult(DeviceConnection connection)
        {
            this.connection = connection;
        }
    }
}
