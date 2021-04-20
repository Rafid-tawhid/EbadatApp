

package com.abc.sharefilesz.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.activity.ViewTransferActivity;
import com.abc.sharefilesz.graphics.drawable.TextDrawable;
import com.abc.sharefilesz.object.IndexOfTransferGroup;
import com.abc.sharefilesz.object.ShowingAssignee;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkDeviceLoader;
import com.abc.sharefilesz.util.TransferUtils;
import com.abc.sharefilesz.R;

public class ToggleMultipleTransferDialog extends AlertDialog.Builder
{
    private ViewTransferActivity mActivity;
    private ShowingAssignee[] mAssignees;
    private LayoutInflater mInflater;
    private TextDrawable.IShapeBuilder mIconBuilder;

    public ToggleMultipleTransferDialog(@NonNull final ViewTransferActivity activity, final IndexOfTransferGroup index)
    {
        super(activity);

        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mIconBuilder = AppUtils.getDefaultIconBuilder(activity);
        mAssignees = index.assignees;

        if (mAssignees.length > 0)
            setAdapter(new ActiveListAdapter(), (dialog, which) -> startTransfer(activity, index, mAssignees[which]));

        setNegativeButton(R.string.butn_close, null);

        if (index.hasOutgoing())
            setNeutralButton(R.string.butn_addDevices, (dialog, which) -> activity.startDeviceAddingActivity());

        ShowingAssignee senderAssignee = null;

        for (ShowingAssignee assignee : index.assignees)
            if (TransferObject.Type.INCOMING.equals(assignee.type)) {
                senderAssignee = assignee;
                break;
            }

        if (index.hasIncoming() && senderAssignee != null) {
            ShowingAssignee finalSenderAssignee = senderAssignee;
            setPositiveButton(R.string.butn_receive, (dialog, which) -> startTransfer(activity, index,
                    finalSenderAssignee));
        }
    }

    private void startTransfer(ViewTransferActivity activity, IndexOfTransferGroup index, ShowingAssignee assignee)
    {
        if (mActivity.isDeviceRunning(assignee.deviceId))
            TransferUtils.pauseTransfer(activity, assignee);
        else
            TransferUtils.startTransferWithTest(activity, index.group, assignee);
    }

    private class ActiveListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mAssignees.length;
        }

        @Override
        public Object getItem(int position)
        {
            return mAssignees[position];
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
                convertView = mInflater.inflate(R.layout.list_toggle_transfer, parent, false);

            ShowingAssignee assignee = (ShowingAssignee) getItem(position);
            ImageView image = convertView.findViewById(R.id.image);
            TextView text = convertView.findViewById(R.id.text);
            ImageView actionImage = convertView.findViewById(R.id.actionImage);

            text.setText(assignee.device.nickname);
            actionImage.setImageResource(mActivity.isDeviceRunning(assignee.deviceId) ? R.drawable.ic_pause_white_24dp
                    : (TransferObject.Type.INCOMING.equals(assignee.type) ? R.drawable.ic_arrow_down_white_24dp
                    : R.drawable.ic_arrow_up_white_24dp));
            NetworkDeviceLoader.showPictureIntoView(assignee.device, image, mIconBuilder);

            return convertView;
        }
    }
}
