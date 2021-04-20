

package com.abc.sharefilesz.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.task.AssessNetworkTask;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.abc.sharefilesz.R;

public class ConnectionTestDialog extends AlertDialog.Builder
{
    private final AssessNetworkTask.ConnectionResult[] mResults;

    @ColorInt
    private int mActiveColor;

    @ColorInt
    private int mPassiveColor;

    public ConnectionTestDialog(Context context, Device device, AssessNetworkTask.ConnectionResult[] results)
    {
        super(context);

        mResults = results;
        mActiveColor = ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorAccent));
        mPassiveColor = ContextCompat.getColor(context, AppUtils.getReference(context, R.attr.colorControlNormal));

        setTitle(context.getString(R.string.text_connectionTest, device.nickname));
        setNegativeButton(R.string.butn_close, null);

        if (results.length < 1)
            setMessage(R.string.text_empty);
        else
            setAdapter(new ConnectionListAdapter(), null);
    }

    private class ConnectionListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mResults.length;
        }

        @Override
        public Object getItem(int position)
        {
            return mResults[position];
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_available_interface, parent,
                        false);

            AssessNetworkTask.ConnectionResult result = (AssessNetworkTask.ConnectionResult) getItem(position);

            TextView textView1 = convertView.findViewById(R.id.pending_available_interface_text1);
            TextView textView2 = convertView.findViewById(R.id.pending_available_interface_text2);
            TextView textView3 = convertView.findViewById(R.id.pending_available_interface_text3);

            textView1.setTextColor(result.successful ? mActiveColor : mPassiveColor);
            textView1.setText(TextUtils.getAdapterName(getContext(), result.connection));
            textView2.setText(result.connection.ipAddress);

            if (result.successful)
                textView3.setText(getContext().getString(R.string.text_textMillisecond,
                        (long) (result.pingTime / 1e6)));
            else
                textView3.setText(R.string.text_error);

            return convertView;
        }
    }
}
