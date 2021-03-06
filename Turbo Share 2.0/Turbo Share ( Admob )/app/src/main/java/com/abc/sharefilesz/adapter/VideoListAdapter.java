

package com.abc.sharefilesz.adapter;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.widget.GalleryGroupEditableListAdapter;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.GlideApp;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TimeUtils;
import com.genonbeta.android.framework.util.listing.Merger;

/**

 * date: 18.11.2017 13:32
 */

public class VideoListAdapter extends GalleryGroupEditableListAdapter<VideoListAdapter.VideoHolder,
        GroupEditableListAdapter.GroupViewHolder>
{
    public static final int VIEW_TYPE_TITLE = 1;

    private ContentResolver mResolver;
    private int mSelectedInset;

    public VideoListAdapter(IEditableListFragment<VideoHolder, GroupViewHolder> fragment)
    {
        super(fragment, MODE_GROUP_BY_DATE);
        mResolver = getContext().getContentResolver();
        mSelectedInset = (int) getContext().getResources().getDimension(R.dimen.space_list_grid);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        GroupViewHolder holder = viewType == VIEW_TYPE_DEFAULT ? new GroupViewHolder(getInflater().inflate(
                isGridLayoutRequested() ? R.layout.list_video_grid : R.layout.list_video, parent, false))
                : createDefaultViews(parent, viewType, false);

        if (!holder.isRepresentative()) {
            getFragment().registerLayoutViewClicks(holder);

            View visitView = holder.itemView.findViewById(R.id.visitView);
            visitView.setOnClickListener(v -> getFragment().performLayoutClickOpen(holder));
            visitView.setOnLongClickListener(v -> getFragment().performLayoutLongClick(holder));

            holder.itemView.findViewById(isGridLayoutRequested() ? R.id.selectorContainer
                    : R.id.selector).setOnClickListener(v -> getFragment().setItemSelected(holder, true));
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position)
    {
        try {
            final VideoHolder object = this.getItem(position);
            final View parentView = holder.itemView;

            if (!holder.tryBinding(object)) {
                ViewGroup container = parentView.findViewById(R.id.container);
                ImageView image = parentView.findViewById(R.id.image);
                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);
                TextView text3 = parentView.findViewById(R.id.text3);

                text1.setText(object.friendlyName);
                text2.setText(object.duration);
                text3.setText(FileUtils.sizeExpression(object.size, false));

                parentView.setSelected(object.isSelectableSelected());

                GlideApp.with(getContext())
                        .load(object.uri)
                        .override(300)
                        .centerCrop()
                        .into(image);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onLoad(GroupLister<VideoHolder> lister)
    {
        Cursor cursor = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int displayIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                int albumIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                int lengthIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                int dateIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);

                do {
                    VideoHolder holder = new VideoHolder(cursor.getInt(idIndex), cursor.getString(titleIndex),
                            cursor.getString(displayIndex), cursor.getString(albumIndex), cursor.getString(typeIndex),
                            cursor.getLong(lengthIndex), cursor.getLong(dateIndex) * 1000,
                            cursor.getLong(sizeIndex), Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/"
                            + cursor.getInt(idIndex)));

                    lister.offerObliged(this, holder);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
    }

    @Override
    protected VideoHolder onGenerateRepresentative(String text, Merger<VideoHolder> merger)
    {
        return new VideoHolder(text);
    }

    public static class VideoHolder extends GalleryGroupEditableListAdapter.GalleryGroupShareable
    {
        public String duration;

        public VideoHolder(String representativeText)
        {
            super(VIEW_TYPE_REPRESENTATIVE, representativeText);
        }

        public VideoHolder(long id, String friendlyName, String fileName, String albumName, String mimeType,
                           long duration, long date, long size, Uri uri)
        {
            super(id, friendlyName, fileName, albumName, mimeType, date, size, uri);
            this.duration = TimeUtils.getDuration(duration);
        }
    }
}
