

package com.abc.sharefilesz.fragment;

/**
 * Created by gabm on 11/01/18.
 */

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.adapter.ImageListAdapter;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.GalleryGroupEditableListFragment;

public class ImageListFragment extends GalleryGroupEditableListFragment<ImageListAdapter.ImageHolder,
        GroupEditableListAdapter.GroupViewHolder, ImageListAdapter>
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(ImageListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(ImageListAdapter.MODE_SORT_BY_DATE);
        setDefaultViewingGridSize(3, 5);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new ImageListAdapter(this));
        setEmptyListImage(R.drawable.ic_photo_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyImage));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        requireContext().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true, getDefaultContentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().getContentResolver().unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_photo);
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             ImageListAdapter.ImageHolder object)
    {
        return performLayoutClickOpen(holder, object);
    }
}
