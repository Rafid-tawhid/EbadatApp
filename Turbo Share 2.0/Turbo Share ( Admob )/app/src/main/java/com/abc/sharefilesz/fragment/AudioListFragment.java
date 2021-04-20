

package com.abc.sharefilesz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.adapter.AudioListAdapter;
import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.GroupEditableListFragment;

import java.util.Map;

public class AudioListFragment extends GroupEditableListFragment<AudioListAdapter.AudioItemHolder,
        GroupEditableListAdapter.GroupViewHolder, AudioListAdapter>
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultGroupingCriteria(AudioListAdapter.MODE_GROUP_BY_ALBUM);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(new AudioListAdapter(this));
        setEmptyListImage(R.drawable.ic_library_music_white_24dp);
        setEmptyListText(getString(R.string.text_listEmptyMusic));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        requireContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, getDefaultContentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        requireContext().getContentResolver().unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public void onGroupingOptions(Map<String, Integer> options)
    {
        super.onGroupingOptions(options);

        options.put(getString(R.string.text_groupByNothing), AudioListAdapter.MODE_GROUP_BY_NOTHING);
        options.put(getString(R.string.text_groupByDate), AudioListAdapter.MODE_GROUP_BY_DATE);
        options.put(getString(R.string.text_groupByAlbum), AudioListAdapter.MODE_GROUP_BY_ALBUM);
        options.put(getString(R.string.text_groupByArtist), AudioListAdapter.MODE_GROUP_BY_ARTIST);
        options.put(getString(R.string.text_groupByFolder), AudioListAdapter.MODE_GROUP_BY_FOLDER);
    }

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == FileListAdapter.VIEW_TYPE_REPRESENTATIVE ? currentSpanSize
                : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    public boolean performDefaultLayoutClick(GroupEditableListAdapter.GroupViewHolder holder,
                                             AudioListAdapter.AudioItemHolder object)
    {
        return performLayoutClickOpen(holder, object);
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_music);
    }
}
