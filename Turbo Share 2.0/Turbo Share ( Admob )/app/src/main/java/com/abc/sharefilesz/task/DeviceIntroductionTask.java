

package com.abc.sharefilesz.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.abc.sharefilesz.adapter.NetworkDeviceListAdapter;
import com.abc.sharefilesz.object.DeviceAddress;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.service.backgroundservice.AttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.AttachedTaskListener;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.util.CommonErrorHelper;
import com.abc.sharefilesz.util.ConnectionUtils;
import com.abc.sharefilesz.util.communicationbridge.CommunicationException;
import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class DeviceIntroductionTask extends AttachableBgTask<DeviceIntroductionTask.ResultListener>
{
    public static final String TAG = DeviceIntroductionTask.class.getSimpleName();

    private NetworkDeviceListAdapter.NetworkDescription mDescription;
    private InetAddress mAddress;
    private int mPin;
    private BroadcastReceiver mReceiver = null;

    public DeviceIntroductionTask(InetAddress address, int pin)
    {
        assert address != null;

        mAddress = address;
        mPin = pin;
    }

    public DeviceIntroductionTask(DeviceConnection connection, int pin) throws UnknownHostException
    {
        this(connection.toInet4Address(), pin);
    }

    public DeviceIntroductionTask(NetworkDeviceListAdapter.NetworkDescription description, int pin)
    {
        assert description != null;

        mDescription = description;
        mPin = pin;
    }

    @Override
    public void onRun()
    {
        TaskMessage.Callback retryCallback = (service, msg, action) -> rerun(service);

        try {
            if (mAddress == null)
                connectToNetwork();

            DeviceAddress deviceAddress = ConnectionUtils.setupConnection(getService(), mAddress, mPin);

            if (hasAnchor())
                post(() -> getAnchor().onDeviceReached(deviceAddress));
            Log.d(TAG, "onRun: Found device - " + deviceAddress.device.nickname);
        } catch (CommunicationException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (SuggestNetworkException e) {
            post(CommonErrorHelper.messageOf(e, getService()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ConnectionUtils.WifiInaccessibleException e) {
            e.printStackTrace();
        } finally {
            if (mReceiver != null)
                getService().unregisterReceiver(mReceiver);
        }
    }

    private void connectToNetwork() throws SuggestNetworkException, ConnectionUtils.WifiInaccessibleException,
            TimeoutException, InterruptedException
    {
        ConnectionUtils utils = new ConnectionUtils(getService());

        if (Build.VERSION.SDK_INT >= 29) {
            mReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    if (WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION.equals(intent.getAction()))
                        DeviceIntroductionTask.this.notify();
                }
            };

            getService().registerReceiver(mReceiver, new IntentFilter(
                    WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION));

            int status = utils.suggestNetwork(mDescription);
            switch (status) {
                case WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP:
                    throw new SuggestNetworkException(mDescription, SuggestNetworkException.Type.ExceededLimit);
                case WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED:
                    throw new SuggestNetworkException(mDescription, SuggestNetworkException.Type.AppDisallowed);
                case WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL:
                    throw new SuggestNetworkException(mDescription, SuggestNetworkException.Type.ErrorInternal);
                case WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE:
                case WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS:
                default:
                    setOngoingContent(getService().getString(R.string.mesg_connectingToSelfHotspot));
                    publishStatus();
                    wait(AppConfig.DEFAULT_SOCKET_TIMEOUT_LARGE);

                    if (!utils.isConnectedToNetwork(mDescription))
                        throw new SuggestNetworkException(mDescription, SuggestNetworkException.Type.DidNotConnect);
            }
        }

        mAddress = utils.establishHotspotConnection(this, mDescription);
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

    public interface ResultListener extends AttachedTaskListener
    {
        void onDeviceReached(DeviceAddress deviceAddress);
    }

    public static class SuggestNetworkException extends Exception
    {
        public NetworkDeviceListAdapter.NetworkDescription description;
        public Type type;

        public SuggestNetworkException(NetworkDeviceListAdapter.NetworkDescription description, Type type)
        {
            this.description = description;
            this.type = type;
        }

        public enum Type
        {
            ExceededLimit,
            ErrorInternal,
            AppDisallowed,
            NetworkDuplicate,
            DidNotConnect
        }
    }
}
