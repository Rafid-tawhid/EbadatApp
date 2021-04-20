

package com.abc.sharefilesz.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.graphics.drawable.TextDrawable;
import com.abc.sharefilesz.object.ShowingAssignee;
import com.abc.sharefilesz.object.TransferObject;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NetworkDeviceLoader;
import com.abc.sharefilesz.R;

import java.util.ArrayList;
import java.util.List;

/**

 * date: 4/4/19 10:06 AM
 */
public class ChooseAssigneeDialog extends AlertDialog.Builder
{
    private List<ShowingAssignee> mList = new ArrayList<>();
    private LayoutInflater mInflater;
    private TextDrawable.IShapeBuilder mIconBuilder;

    public ChooseAssigneeDialog(@NonNull Activity activity, List<ShowingAssignee> assigneeList,
                                DialogInterface.OnClickListener clickListener)
    {
        super(activity);

        mList.addAll(assigneeList);
        mInflater = LayoutInflater.from(activity);
        mIconBuilder = AppUtils.getDefaultIconBuilder(activity);

        if (assigneeList.size() > 0)
            setAdapter(new ListAdapter(), clickListener);
        else
            setMessage(R.string.text_listEmpty);

        setTitle(R.string.butn_useKnownDevice);
        setNegativeButton(R.string.butn_close, null);
    }

    private class ListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mList.get(position);
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
                convertView = mInflater.inflate(R.layout.list_assignee_selector, parent, false);

            ShowingAssignee assignee = (ShowingAssignee) getItem(position);
            ImageView image = convertView.findViewById(R.id.image);
            ImageView actionImage = convertView.findViewById(R.id.actionImage);
            TextView text = convertView.findViewById(R.id.text);

            text.setText(assignee.device.nickname);
            actionImage.setImageResource(TransferObject.Type.INCOMING.equals(assignee.type)
                    ? R.drawable.ic_arrow_down_white_24dp : R.drawable.ic_arrow_up_white_24dp);
            NetworkDeviceLoader.showPictureIntoView(assignee.device, image, mIconBuilder);

            return convertView;
        }
    }
}
