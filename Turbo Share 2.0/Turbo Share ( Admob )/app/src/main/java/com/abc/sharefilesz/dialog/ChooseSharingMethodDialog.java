

package com.abc.sharefilesz.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.abc.sharefilesz.object.Shareable;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.task.LocalShareRunningTask;
import com.abc.sharefilesz.R;

import java.util.ArrayList;
import java.util.List;

public class ChooseSharingMethodDialog extends AlertDialog.Builder
{
    private LayoutInflater mLayoutInflater;
    private final ArrayList<SharingMethod> mSharingMethodList = new ArrayList<>();

    public ChooseSharingMethodDialog(Activity activity, final List<? extends Shareable> list)
    {
        super(activity);

        mLayoutInflater = LayoutInflater.from(getContext());

        mSharingMethodList.add(new SharingMethod(R.drawable.ic_web_white_24dp, R.string.butn_webShare, () ->
                BackgroundService.run(activity, new LocalShareRunningTask(list, false, true))));

        mSharingMethodList.add(new SharingMethod(R.drawable.ic_compare_arrows_white_24dp,
                R.string.text_devicesWithAppInstalled, () -> BackgroundService.run(activity, new LocalShareRunningTask(
                list, true, false))));

        setTitle(R.string.text_chooseSharingMethod);
        setAdapter(new SharingMethodListAdapter(), (dialog, which) -> mSharingMethodList.get(which).call());
        setNegativeButton(R.string.butn_cancel, null);
    }

    private class SharingMethodListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSharingMethodList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mSharingMethodList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            // Since the list will be a static one, item ids do not have importance.
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.list_sharing_method, parent, false);

            SharingMethod sharingMethod = (SharingMethod) getItem(position);
            ImageView image = convertView.findViewById(R.id.image);
            TextView text1 = convertView.findViewById(R.id.text1);

            image.setImageResource(sharingMethod.mIconRes);
            text1.setText(getContext().getString(sharingMethod.mTitleRes));

            return convertView;
        }
    }

    private static class SharingMethod
    {
        @DrawableRes
        private int mIconRes;
        @StringRes
        private int mTitleRes;
        private Listener mCallable;

        private SharingMethod(@DrawableRes int iconRes, @StringRes int titleRes, Listener callable)
        {
            mIconRes = iconRes;
            mTitleRes = titleRes;
            mCallable = callable;
        }

        private void call()
        {
            mCallable.call();
        }
    }

    private interface Listener
    {
        void call();
    }
}
