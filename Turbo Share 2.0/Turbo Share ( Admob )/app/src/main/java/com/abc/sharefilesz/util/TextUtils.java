

package com.abc.sharefilesz.util;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.config.AppConfig;

import java.net.NetworkInterface;
import java.text.NumberFormat;
import java.util.Map;

/**

 * date: 12.11.2017 11:14
 */

public class TextUtils
{
    public static int getAdapterName(String adapterName)
    {
        Map<String, Integer> associatedNames = new ArrayMap<>();

        associatedNames.put("wlan", R.string.text_interfaceWireless);
        associatedNames.put("p2p", R.string.text_interfaceWifiDirect);
        associatedNames.put("bt-pan", R.string.text_interfaceBluetooth);
        associatedNames.put("eth", R.string.text_interfaceEthernet);
        associatedNames.put("tun", R.string.text_interfaceVPN);
        associatedNames.put("unk", R.string.text_interfaceUnknown);

        for (String displayName : associatedNames.keySet())
            if (adapterName.startsWith(displayName))
                return associatedNames.get(displayName);

        return -1;
    }

    public static String getAdapterName(Context context, DeviceConnection connection)
    {
        return getAdapterName(context, connection.adapterName);
    }

    public static String getAdapterName(Context context, NetworkInterface networkInterface)
    {
        return getAdapterName(context, networkInterface.getDisplayName());
    }

    public static String getAdapterName(Context context, String adapterName)
    {
        int adapterNameResource = getAdapterName(adapterName);

        if (adapterNameResource == -1)
            return adapterName;

        return context.getString(adapterNameResource);
    }

    public static String getLetters(String text, int length)
    {
        if (text == null || text.length() == 0)
            text = "?";

        int breakAfter = --length;
        StringBuilder stringBuilder = new StringBuilder();

        for (String letter : text.split(" ")) {
            if (stringBuilder.length() > breakAfter)
                break;

            if (letter.length() == 0)
                continue;

            stringBuilder.append(letter.charAt(0));
        }

        return stringBuilder.toString().toUpperCase();
    }

    public static String getTransactionFlagString(Context context, TransferObject object, NumberFormat percentFormat,
                                                  @Nullable String deviceId)
    {
        // Because it takes more arguments when the 'object' is 'Type.OUTGOING', it will be
        // chosen by the appropriate value that it may contain.
        TransferObject.Flag flag;

        if (TransferObject.Type.OUTGOING.equals(object.type)) {
            TransferObject.Flag[] flags = object.getFlags();

            if (deviceId != null)
                flag = object.getFlag(deviceId);
            else if (flags.length < 1)
                flag = TransferObject.Flag.PENDING;
            else if (flags.length == 1)
                flag = flags[0];
            else {
                flag = TransferObject.Flag.PENDING;
                int pos = 0;

                for (TransferObject.Flag flagTesting : flags) {
                    int relativeOrdinal;

                    switch (flagTesting) {
                        default:
                        case DONE:
                            relativeOrdinal = 0;
                            break;
                        case PENDING:
                            relativeOrdinal = 1;
                            break;
                        case REMOVED:
                            relativeOrdinal = 2;
                            break;
                        case INTERRUPTED:
                            relativeOrdinal = 3;
                            break;
                        case IN_PROGRESS:
                            relativeOrdinal = 4;
                    }

                    if (pos <= relativeOrdinal) {
                        pos = relativeOrdinal;
                        flag = flagTesting;
                    }
                }
            }
        } else
            flag = object.getFlag();

        switch (flag) {
            case DONE:
                return percentFormat.format(1.0);
            case IN_PROGRESS:
                return percentFormat.format(object.size == 0 || flag.getBytesValue() == 0 ? 0
                        : Long.valueOf(flag.getBytesValue()).doubleValue() / Long.valueOf(object.size).doubleValue());
            default:
                return context.getString(getTransactionFlagString(flag));
        }
    }

    public static int getTransactionFlagString(TransferObject.Flag flag)
    {
        switch (flag) {
            case PENDING:
                return R.string.text_flagPending;
            case DONE:
                return R.string.text_taskCompleted;
            case INTERRUPTED:
                return R.string.text_flagInterrupted;
            case IN_PROGRESS:
                return R.string.text_flagRunning;
            case REMOVED:
                return R.string.text_flagRemoved;
            default:
                return R.string.text_unknown;
        }
    }

    public static String makeWebShareLink(Context context, String address)
    {
        return context.getString(R.string.mode_webShareAddress, address, AppConfig.SERVER_PORT_WEBSHARE);
    }

    public static boolean searchWord(String word, String searchThis)
    {
        return searchThis == null || searchThis.length() == 0 || word.toLowerCase().contains(searchThis.toLowerCase());
    }

    public static String trimText(String text, int length)
    {
        if (text == null || text.length() <= length)
            return text;

        return text.substring(0, length);
    }
}
