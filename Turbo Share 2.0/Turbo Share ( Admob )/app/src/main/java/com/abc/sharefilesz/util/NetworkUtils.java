

package com.abc.sharefilesz.util;

import android.annotation.SuppressLint;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.adapter.ActiveConnectionListAdapter;
import com.abc.sharefilesz.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class NetworkUtils
{
    public static boolean compareAddressRanges(NetworkInterface networkInterface, Inet4Address b)
    {
        Enumeration<InetAddress> addressList = networkInterface.getInetAddresses();

        while (addressList.hasMoreElements()) {
            InetAddress address = addressList.nextElement();
            if (!address.isLoopbackAddress() && (address instanceof Inet4Address)
                    && compareAddressRanges((Inet4Address) address, b))
                return true;
        }

        return false;
    }

    public static boolean compareAddressRanges(Inet4Address a, Inet4Address b)
    {
        byte[] ba = a.getAddress();
        byte[] bb = b.getAddress();

        for (int i = 0; i < 2; i++)
            if (ba[i] != bb[i])
                return false;

        return true;
    }

    @SuppressLint("DefaultLocale")
    public static Inet4Address convertInet4Address(int address) throws UnknownHostException
    {
        return (Inet4Address) InetAddress.getByAddress(new byte[]{(byte) (address & 0xff), (byte) (address >> 8 & 0xff),
                (byte) (address >> 16 & 0xff), (byte) (address >> 24 & 0xff)});
    }

    @Nullable
    public static NetworkInterface findNetworkInterface(Inet4Address address)
    {
        List<NetworkInterface> interfaceList = NetworkUtils.getInterfaces(true,
                AppConfig.DEFAULT_DISABLED_INTERFACES);

        for (NetworkInterface networkInterface : interfaceList) {
            if (NetworkUtils.compareAddressRanges(networkInterface, address))
                return networkInterface;
        }

        return null;
    }

    public static List<String> getMACAddressList(String interfaceName)
    {
        List<String> macAddressList = new ArrayList<>();

        try {
            List<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaceList) {
                if (interfaceName != null) {
                    if (!networkInterface.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }

                byte[] hardwareAddress = networkInterface.getHardwareAddress();

                if (hardwareAddress == null)
                    continue;

                StringBuilder stringBuilder = new StringBuilder();

                for (byte partedHardwareAddress : hardwareAddress)
                    stringBuilder.append(String.format("%02X:", partedHardwareAddress));

                if (stringBuilder.length() > 0)
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                macAddressList.add(stringBuilder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return macAddressList;
    }

    public static Inet4Address getFirstInet4Address(
            @NonNull ActiveConnectionListAdapter.EditableNetworkInterface networkInterface)
    {
        return getFirstInet4Address(networkInterface.getInterface());
    }

    public static Inet4Address getFirstInet4Address(@NonNull NetworkInterface networkInterface)
    {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();

            if (address instanceof Inet4Address)
                return (Inet4Address) address;
        }

        return null;
    }

    public static List<NetworkInterface> getInterfaces(boolean ipV4only, String[] avoidedInterfaces)
    {
        List<NetworkInterface> filteredInterfaceList = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                boolean avoidedInterface = false;

                if (avoidedInterfaces != null && avoidedInterfaces.length > 0)
                    for (String match : avoidedInterfaces)
                        if (networkInterface.getDisplayName().startsWith(match))
                            avoidedInterface = true;

                if (avoidedInterface)
                    continue;

                Enumeration<InetAddress> addressList = networkInterface.getInetAddresses();

                while (addressList.hasMoreElements()) {
                    InetAddress address = addressList.nextElement();
                    if (!address.isLoopbackAddress() && (address instanceof Inet4Address || !ipV4only)) {
                        filteredInterfaceList.add(networkInterface);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return filteredInterfaceList;
    }

    public static boolean ping(String ipV4address, int timeout)
    {
        try {
            return ping(InetAddress.getByName(ipV4address), timeout);
        } catch (UnknownHostException e) {
            Log.d(NetworkUtils.class.getSimpleName(), "ping: Unknown host " + ipV4address);
        }

        return false;
    }

    public static boolean ping(InetAddress inetAddress, int timeout)
    {
        String binary = "/system/bin/ping";

        try {
            if (!new File(binary).canExecute())
                throw new IOException("Ping binary (assumed path) cannot be executed. Using 'isReachable' method.");

            return Runtime.getRuntime().exec(binary + " -c 1 -w " + timeout + " "
                    + inetAddress.getHostAddress()).waitFor() == 0;
        } catch (Exception e) {
            try {
                return inetAddress.isReachable(timeout);
            } catch (IOException ignored) {
            }
        }

        return false;
    }

    public static boolean testSocket(String ip, int port)
    {
        InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
        Socket socket = new Socket();

        try {
            socket.bind(null);
            socket.connect(socketAddress);
            socket.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
