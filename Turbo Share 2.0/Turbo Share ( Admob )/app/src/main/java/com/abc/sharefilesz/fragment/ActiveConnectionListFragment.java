

package com.abc.sharefilesz.fragment;

import android.bluetooth.BluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.transition.TransitionManager;

import com.abc.sharefilesz.adapter.ActiveConnectionListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.EditableListFragment;
import com.abc.sharefilesz.dialog.WebShareDetailsDialog;
import com.abc.sharefilesz.ui.callback.IconProvider;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;

/**

 * date: 4/7/19 10:59 PM
 */
public class ActiveConnectionListFragment extends EditableListFragment<
        ActiveConnectionListAdapter.EditableNetworkInterface, RecyclerViewAdapter.ViewHolder,
        ActiveConnectionListAdapter> implements IconProvider
{
    private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (HotspotManagerFragment.WIFI_AP_STATE_CHANGED.equals(intent.getAction())
                    || ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                    || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
                    || WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction())
                    || BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction()))
                refreshList();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setLayoutResId(R.layout.fragment_active_connection);
        setSortingSupported(false);
        setFilteringSupported(true);
        setUseDefaultPaddingDecoration(true);
        setUseDefaultPaddingDecorationSpaceForEdges(true);
        setDefaultPaddingDecorationSize(getResources().getDimension(R.dimen.padding_list_content_parent_layout));

        mFilter.addAction(HotspotManagerFragment.WIFI_AP_STATE_CHANGED);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new ActiveConnectionListAdapter(this));
        setEmptyListImage(R.drawable.ic_share_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyConnection));

        final CardView webShareInfo = view.findViewById(R.id.card_web_share_info);
        Button webShareInfoHideButton = view.findViewById(R.id.card_web_share_info_hide_button);
        final String helpWebShareInfo = "help_webShareInfo";

        if (AppUtils.getDefaultPreferences(getContext()).getBoolean(helpWebShareInfo, true)) {
            webShareInfo.setVisibility(View.VISIBLE);
            webShareInfoHideButton.setOnClickListener(v -> {
                webShareInfo.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition((ViewGroup) webShareInfo.getParent());

                AppUtils.getDefaultPreferences(getContext()).edit()
                        .putBoolean(helpWebShareInfo, false)
                        .apply();
            });
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        requireContext().registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().unregisterReceiver(mReceiver);
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_web_white_24dp;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_webShare);
    }

    @Override
    public boolean performDefaultLayoutClick(RecyclerViewAdapter.ViewHolder holder, ActiveConnectionListAdapter.EditableNetworkInterface object)
    {
        new WebShareDetailsDialog(requireActivity(), TextUtils.makeWebShareLink(requireContext(),
                NetworkUtils.getFirstInet4Address(object).getHostAddress())).show();
        return true;
    }

    @Override
    public boolean performLayoutClickOpen(RecyclerViewAdapter.ViewHolder holder, ActiveConnectionListAdapter.EditableNetworkInterface object)
    {
        if (!super.performLayoutClickOpen(holder, object))
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(TextUtils.makeWebShareLink(requireContext(),
                    NetworkUtils.getFirstInet4Address(object).getHostAddress()))));

        return true;
    }
}
