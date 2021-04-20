

package com.abc.sharefilesz.dialog;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.abc.sharefilesz.activity.ManageDevicesActivity;
import com.abc.sharefilesz.callback.OnConnectionSelectionListener;
import com.abc.sharefilesz.config.AppConfig;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.abc.sharefilesz.util.TimeUtils;
import com.abc.sharefilesz.R;
import com.genonbeta.android.database.SQLQuery;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

/**

 * Date: 5/19/17 12:18 AM
 */

public class ConnectionChooserDialog extends AlertDialog.Builder
{
    private final List<DeviceConnection> mConnections = new ArrayList<>();
    private final List<NetworkInterface> mNetworkInterfaces = new ArrayList<>();

    private Device mDevice;

    @ColorInt
    private int mActiveColor;

    @ColorInt
    private int mPassiveColor;

    public ConnectionChooserDialog(final Activity activity, Device device, OnConnectionSelectionListener listener)
    {
        super(activity);

        mDevice = device;
        mActiveColor = ContextCompat.getColor(activity, AppUtils.getReference(activity, R.attr.colorAccent));
        mPassiveColor = ContextCompat.getColor(activity, AppUtils.getReference(activity, R.attr.colorControlNormal));

        ConnectionListAdapter adapter = new ConnectionListAdapter();

        if (mConnections.size() > 0)
            setAdapter(adapter, (dialog, which) -> listener.onConnectionSelection(mConnections.get(which)));
        else
            setMessage(R.string.text_noNetworkAvailable);

        setTitle(getContext().getString(R.string.text_availableNetworks, device.nickname));
        setNegativeButton(R.string.butn_cancel, null);
        setNeutralButton(R.string.text_manageDevices, (dialog, which) -> activity.startActivity(new Intent(activity,
                ManageDevicesActivity.class)));
    }

    private class ConnectionListAdapter extends BaseAdapter
    {
        public ConnectionListAdapter()
        {
            mConnections.addAll(AppUtils.getKuick(getContext()).castQuery(
                    new SQLQuery.Select(Kuick.TABLE_DEVICECONNECTION)
                            .setWhere(Kuick.FIELD_DEVICECONNECTION_DEVICEID + "=?", mDevice.id)
                            .setOrderBy(Kuick.FIELD_DEVICECONNECTION_LASTCHECKEDDATE + " DESC"),
                    DeviceConnection.class));

            mNetworkInterfaces.addAll(NetworkUtils.getInterfaces(true, AppConfig.DEFAULT_DISABLED_INTERFACES));
        }

        @Override
        public int getCount()
        {
            return mConnections.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mConnections.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_available_interface, parent,
                        false);

            DeviceConnection address = (DeviceConnection) getItem(position);

            TextView textView1 = convertView.findViewById(R.id.pending_available_interface_text1);
            TextView textView2 = convertView.findViewById(R.id.pending_available_interface_text2);
            TextView textView3 = convertView.findViewById(R.id.pending_available_interface_text3);

            boolean accessible = false;

            for (NetworkInterface networkInterface : mNetworkInterfaces)
                if (address.adapterName.equals(networkInterface.getDisplayName())) {
                    accessible = true;
                    break;
                }

            textView1.setTextColor(accessible ? mActiveColor : mPassiveColor);
            textView1.setText(TextUtils.getAdapterName(getContext(), address));
            textView2.setText(address.ipAddress);
            textView3.setText(TimeUtils.getTimeAgo(getContext(), address.lastCheckedDate));

            return convertView;
        }
    }
}
