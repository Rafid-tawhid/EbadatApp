

package com.abc.sharefilesz.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.object.ShowingAssignee;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.widget.EditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.graphics.drawable.TextDrawable;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkDeviceLoader;
import com.abc.sharefilesz.util.TextUtils;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;

import java.util.List;

/**

 * date: 06.04.2018 12:46
 */
public class TransferAssigneeListAdapter extends EditableListAdapter<ShowingAssignee, RecyclerViewAdapter.ViewHolder>
{
    private TransferGroup mGroup;
    private TextDrawable.IShapeBuilder mIconBuilder;

    public TransferAssigneeListAdapter(IEditableListFragment<ShowingAssignee, ViewHolder> fragment, TransferGroup group)
    {
        super(fragment);
        mIconBuilder = AppUtils.getDefaultIconBuilder(fragment.getContext());
        mGroup = group;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ViewHolder holder = new ViewHolder(getInflater().inflate(isHorizontalOrientation() || isGridLayoutRequested()
                ? R.layout.list_assignee_grid : R.layout.list_assignee, parent, false));

        getFragment().registerLayoutViewClicks(holder);
        holder.itemView.findViewById(R.id.menu)
                .setOnClickListener(v -> getFragment().performLayoutLongClick(holder));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position)
    {
        ShowingAssignee assignee = getList().get(position);

        ImageView image = holder.itemView.findViewById(R.id.image);
        TextView text1 = holder.itemView.findViewById(R.id.text1);
        TextView text2 = holder.itemView.findViewById(R.id.text2);

        text1.setText(assignee.device.nickname);
        text2.setText(TextUtils.getAdapterName(getContext(), assignee.connection));
        NetworkDeviceLoader.showPictureIntoView(assignee.device, image, mIconBuilder);
    }

    @Override
    public List<ShowingAssignee> onLoad()
    {
        return TransferUtils.loadAssigneeList(getContext(), mGroup.id, null);
    }
}
