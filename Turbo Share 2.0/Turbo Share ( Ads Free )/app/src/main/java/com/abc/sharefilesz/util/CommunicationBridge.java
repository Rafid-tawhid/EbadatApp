

package com.abc.sharefilesz.util;

import android.content.Context;

import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.config.Keyword;
import com.abc.sharefilesz.util.communicationbridge.CommunicationException;
import com.abc.sharefilesz.util.communicationbridge.DifferentClientException;
import com.genonbeta.android.database.exception.ReconstructionFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.monora.coolsocket.core.session.ActiveConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

/**

 * date: 11.02.2018 15:07
 */

public class CommunicationBridge
{
    public static final String TAG = CommunicationBridge.class.getSimpleName();

    private final Kuick mKuick;
    private Device mDevice;
    private int mPin = -1;

    public CommunicationBridge(Kuick kuick)
    {
        mKuick = kuick;
    }

    public CommunicationBridge(Kuick kuick, int pin)
    {
        this(kuick);
        setPin(pin);
    }

    public ActiveConnection communicate(Device targetDevice, DeviceConnection targetConnection)
            throws IOException, TimeoutException, CommunicationException, JSONException
    {
        return communicate(targetDevice, targetConnection, false);
    }

    public ActiveConnection communicate(Device targetDevice, DeviceConnection targetConnection,
                                        boolean handshakeOnly)
            throws IOException, TimeoutException, CommunicationException, JSONException
    {
        setDevice(targetDevice);
        return communicate(targetConnection.toInet4Address(), handshakeOnly);
    }

    public ActiveConnection communicate(InetAddress address, boolean handshakeOnly) throws IOException,
            TimeoutException, CommunicationException, JSONException
    {
        ActiveConnection activeConnection = connectWithHandshake(address, handshakeOnly);
        communicate(activeConnection, handshakeOnly);
        return activeConnection;
    }

    public static CommunicationBridge connect(Kuick kuick, final ConnectionHandler handler)
    {
        final CommunicationBridge communicationBridge = new CommunicationBridge(kuick);
        new Thread(() -> handler.onConnect(communicationBridge)).start();
        return communicationBridge;
    }


    public void communicate(ActiveConnection activeConnection, boolean handshakeOnly) throws IOException,
            TimeoutException, CommunicationException, JSONException
    {
        boolean keyNotSent = getDevice() == null;
        updateDeviceIfOkay(activeConnection);

        if (!handshakeOnly && keyNotSent) {
            activeConnection.reply(new JSONObject().put(Keyword.DEVICE_INFO_KEY, getDevice().secureKey)
                    .toString());
            activeConnection.receive(); // STUB
        }
    }

    public ActiveConnection connect(InetAddress inetAddress) throws IOException
    {
        if (!inetAddress.isReachable(1000))
            throw new IOException("Ping test before connection to the address has failed");

        return openConnection(inetAddress);
    }

    public ActiveConnection connect(DeviceConnection connection) throws IOException
    {
        return connect(connection.toInet4Address());
    }

    public ActiveConnection connectWithHandshake(DeviceConnection connection, boolean handshakeOnly)
            throws IOException, TimeoutException, JSONException
    {
        return connectWithHandshake(connection.toInet4Address(), handshakeOnly);
    }

    public ActiveConnection connectWithHandshake(InetAddress inetAddress, boolean handshakeOnly)
            throws IOException, TimeoutException, JSONException
    {
        return handshake(connect(inetAddress), handshakeOnly);
    }

    public Context getContext()
    {
        return getKuick().getContext();
    }

    public Kuick getKuick()
    {
        return mKuick;
    }

    public Device getDevice()
    {
        return mDevice;
    }

    public ActiveConnection handshake(ActiveConnection activeConnection, boolean handshakeOnly) throws IOException,
            TimeoutException, JSONException
    {
        JSONObject reply = new JSONObject()
                .put(Keyword.HANDSHAKE_REQUIRED, true)
                .put(Keyword.HANDSHAKE_ONLY, handshakeOnly)
                .put(Keyword.DEVICE_INFO_SERIAL, AppUtils.getDeviceId(getContext()))
                .put(Keyword.DEVICE_PIN, mPin);

        AppUtils.applyDeviceToJSON(getContext(), reply, mDevice != null ? mDevice.secureKey : -1);
        activeConnection.reply(reply.toString());

        return activeConnection;
    }

    public Device loadDevice(ActiveConnection activeConnection) throws TimeoutException, IOException,
            CommunicationException
    {
        try {
            return NetworkDeviceLoader.loadFrom(getKuick(), activeConnection.receive().getAsJson());
        } catch (JSONException e) {
            throw new CommunicationException("Cannot read the device from JSON");
        }
    }

    public static ActiveConnection openConnection(InetAddress inetAddress)
            throws IOException
    {
        return ActiveConnection.connect(new InetSocketAddress(inetAddress, AppConfig.SERVER_PORT_COMMUNICATION),
                AppConfig.DEFAULT_SOCKET_TIMEOUT);
    }

    public void setDevice(Device device)
    {
        mDevice = device;
    }

    public void setPin(int pin)
    {
        mPin = pin;
    }

    protected void updateDeviceIfOkay(ActiveConnection activeConnection) throws IOException,
            TimeoutException, CommunicationException
    {
        Device loadedDevice = loadDevice(activeConnection);

        NetworkDeviceLoader.processConnection(getKuick(), loadedDevice, activeConnection.getAddress().getHostAddress());

        if (getDevice() != null && !getDevice().id.equals(loadedDevice.id))
            throw new DifferentClientException(getDevice(), loadedDevice);

        if (loadedDevice.clientVersion >= 1) {
            if (getDevice() == null) {
                try {
                    Device existingDevice = new Device(loadedDevice.id);

                    getKuick().reconstruct(existingDevice);
                    setDevice(existingDevice);
                } catch (ReconstructionFailedException ignored) {
                    loadedDevice.secureKey = AppUtils.generateKey();
                }
            }

            if (getDevice() != null) {
                loadedDevice.applyPreferences(getDevice());

                loadedDevice.secureKey = getDevice().secureKey;
                loadedDevice.isRestricted = false;
            } else
                loadedDevice.isLocal = AppUtils.getDeviceId(getContext()).equals(loadedDevice.id);
        }

        loadedDevice.lastUsageTime = System.currentTimeMillis();

        getKuick().publish(loadedDevice);
        getKuick().broadcast();
        setDevice(loadedDevice);
    }

    public interface ConnectionHandler
    {
        void onConnect(CommunicationBridge bridge);
    }
}
