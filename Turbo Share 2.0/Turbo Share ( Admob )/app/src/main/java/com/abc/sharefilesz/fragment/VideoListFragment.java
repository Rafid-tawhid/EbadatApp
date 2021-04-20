

package com.abc.sharefilesz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.adapter.VideoListAdapter;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.GalleryGroupEditableListFragment;

public class VideoListFragment extends GalleryGroupEditableListFragment<VideoListAdapter.VideoHolder,
        GroupEditableListAdapter.GroupViewHolder, VideoListAdapter>
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(VideoListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(VideoListAdapter.MODE_SORT_BY_DATE);
        setDefaultViewingGridSize(3, 5);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new VideoListAdapter(this));
        setEmptyListImage(R.drawable.ic_video_library_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyVideo));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        requireContext().getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true, getDefaultContentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        requireContext().getContentResolver().unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == VideoListAdapter.VIEW_TYPE_TITLE ? currentSpanSize
                : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_video);
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             VideoListAdapter.VideoHolder object)
    {
        return performLayoutClickOpen(holder, object);
    }
}
