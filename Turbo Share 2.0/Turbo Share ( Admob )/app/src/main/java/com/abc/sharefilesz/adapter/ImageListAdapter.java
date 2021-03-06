

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
import com.abc.sharefilesz.util.TimeUtils;
import com.genonbeta.android.framework.util.listing.Merger;

/**

 * date: 18.11.2017 13:32
 */

public class ImageListAdapter extends GalleryGroupEditableListAdapter<ImageListAdapter.ImageHolder,
        GroupEditableListAdapter.GroupViewHolder>
{
    private ContentResolver mResolver;
    private int mSelectedInset;

    public ImageListAdapter(IEditableListFragment<ImageHolder, GroupViewHolder> fragment)
    {
        super(fragment, MODE_GROUP_BY_ALBUM);
        mResolver = getContext().getContentResolver();
        mSelectedInset = (int) getContext().getResources().getDimension(R.dimen.space_list_grid);
    }

    @Override
    protected void onLoad(GroupLister<ImageHolder> lister)
    {
        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                int displayIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int albumIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

                do {
                    ImageHolder holder = new ImageHolder(cursor.getLong(idIndex), cursor.getString(titleIndex),
                            cursor.getString(displayIndex), cursor.getString(albumIndex), cursor.getString(typeIndex),
                            cursor.getLong(dateAddedIndex) * 1000, cursor.getLong(sizeIndex),
                            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(idIndex)));

                    holder.dateTakenString = String.valueOf(TimeUtils.formatDateTime(getContext(), holder.date));

                    lister.offerObliged(this, holder);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_REPRESENTATIVE)
            return new GroupViewHolder(getInflater().inflate(R.layout.layout_list_title, parent, false),
                    R.id.layout_list_title_text);

        GroupViewHolder holder = new GroupViewHolder(getInflater().inflate(isGridLayoutRequested()
                ? R.layout.list_image_grid : R.layout.list_image, parent, false));

        getFragment().registerLayoutViewClicks(holder);
        View visitView = holder.itemView.findViewById(R.id.visitView);
        visitView.setOnClickListener(v -> getFragment().performLayoutClickOpen(holder));
        visitView.setOnLongClickListener(v -> getFragment().performLayoutLongClick(holder));
        holder.itemView.findViewById(isGridLayoutRequested() ? R.id.selectorContainer : R.id.selector)
                .setOnClickListener(v -> getFragment().setItemSelected(holder, true));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position)
    {
        try {
            final View parentView = holder.itemView;
            final ImageHolder object = getItem(position);

            if (!holder.tryBinding(object)) {
                ViewGroup container = parentView.findViewById(R.id.container);
                ImageView image = parentView.findViewById(R.id.image);
                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);

                text1.setText(object.friendlyName);
                text2.setText(object.dateTakenString);

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
    protected ImageHolder onGenerateRepresentative(String text, Merger<ImageHolder> merger)
    {
        return new ImageHolder(text);
    }

    public static class ImageHolder extends GalleryGroupEditableListAdapter.GalleryGroupShareable
    {
        public String dateTakenString;

        public ImageHolder(String representativeText)
        {
            super(VIEW_TYPE_REPRESENTATIVE, representativeText);
        }

        public ImageHolder(long id, String title, String fileName, String albumName, String mimeType, long date,
                           long size, Uri uri)
        {
            super(id, title, fileName, albumName, mimeType, date, size, uri);
        }
    }
}
