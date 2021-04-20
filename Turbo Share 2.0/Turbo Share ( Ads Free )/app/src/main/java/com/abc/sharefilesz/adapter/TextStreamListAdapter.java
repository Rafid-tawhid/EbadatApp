

package com.abc.sharefilesz.adapter;

import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.object.TextStreamObject;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.util.AppUtils;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.util.listing.Merger;

/**

 * date: 30.12.2017 13:25
 */

public class TextStreamListAdapter extends GroupEditableListAdapter<TextStreamObject,
        GroupEditableListAdapter.GroupViewHolder>
{
    public TextStreamListAdapter(IEditableListFragment<TextStreamObject, GroupViewHolder> fragment)
    {
        super(fragment, MODE_GROUP_BY_DATE);
    }

    @Override
    protected void onLoad(GroupLister<TextStreamObject> lister)
    {
        for (TextStreamObject object : AppUtils.getKuick(getContext()).castQuery(
                new SQLQuery.Select(Kuick.TABLE_CLIPBOARD), TextStreamObject.class))
            lister.offerObliged(this, object);
    }

    @Override
    protected TextStreamObject onGenerateRepresentative(String text, Merger<TextStreamObject> merger)
    {
        return new TextStreamObject(text);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        GroupViewHolder holder = viewType == VIEW_TYPE_DEFAULT ? new GroupViewHolder(getInflater().inflate(
                R.layout.list_text_stream, parent, false))
                : createDefaultViews(parent, viewType, false);

        if (!holder.isRepresentative())
            getFragment().registerLayoutViewClicks(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position)
    {
        try {
            TextStreamObject object = getItem(position);

            if (!holder.tryBinding(object)) {
                View parentView = holder.itemView;
                String text = object.text.replace("\n", " ").trim();

                TextView text1 = parentView.findViewById(R.id.text);
                TextView text2 = parentView.findViewById(R.id.text2);
                TextView text3 = parentView.findViewById(R.id.text3);

                parentView.setSelected(object.isSelectableSelected());

                text1.setText(text);
                text2.setText(DateUtils.formatDateTime(getContext(), object.date, DateUtils.FORMAT_SHOW_TIME));
                text3.setVisibility(getGroupBy() != MODE_GROUP_BY_DATE ? View.VISIBLE : View.GONE);

                if (getGroupBy() != MODE_GROUP_BY_DATE)
                    text3.setText(getSectionNameDate(object.date));
            }
        } catch (Exception ignored) {

        }
    }
}