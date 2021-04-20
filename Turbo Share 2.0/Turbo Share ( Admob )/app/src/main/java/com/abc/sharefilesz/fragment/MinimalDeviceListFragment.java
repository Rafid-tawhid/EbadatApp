

package com.abc.sharefilesz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.R;

/**

 * date: 3/11/19 7:43 PM
 */
public class MinimalDeviceListFragment extends DeviceListFragment
{
    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        setHiddenDeviceTypes(new Device.Type[]{Device.Type.WEB});
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
        setFilteringSupported(false);
        setUseDefaultPaddingDecoration(false);
        setUseDefaultPaddingDecorationSpaceForEdges(false);
        setSwipeRefreshEnabled(false);
        setDeviceScanAllowed(false);

        if (isScreenLarge())
            setDefaultViewingGridSize(4, 5);
        else if (isScreenNormal())
            setDefaultViewingGridSize(3, 4);
        else
            setDefaultViewingGridSize(2, 3);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        getListView().setNestedScrollingEnabled(true);
        setDividerVisible(false);

        if (getContext() != null) {
            float padding = getContext().getResources().getDimension(R.dimen.short_content_width_padding);

            getListView().setClipToPadding(false);
            getListView().setPadding((int) padding, 0, (int) padding, 0);
        }
    }
}
