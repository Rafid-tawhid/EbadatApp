

package com.abc.sharefilesz.adapter;

import android.content.Context;

import com.abc.sharefilesz.object.ShowingAssignee;
import com.abc.sharefilesz.R;

import java.io.File;

/**

 * date: 3/11/19 7:39 PM
 */
public class TransferPathResolverRecyclerAdapter extends PathResolverRecyclerAdapter<String>
{
    private ShowingAssignee mAssignee;
    private String mHomeName;

    public TransferPathResolverRecyclerAdapter(Context context)
    {
        super(context);
        mHomeName = context.getString(R.string.text_home);
    }

    @Override
    public Index<String> onFirstItem()
    {
        if (mAssignee != null)
            return new Index<>(mAssignee.device.nickname, R.drawable.ic_device_hub_white_24dp, null);

        return new Index<>(mHomeName, R.drawable.ic_home_white_24dp, null);
    }

    public void goTo(ShowingAssignee assignee, String[] paths)
    {
        mAssignee = assignee;

        StringBuilder mergedPath = new StringBuilder();
        initAdapter();

        synchronized (getList()) {
            if (paths != null)
                for (String path : paths) {
                    if (path.length() == 0)
                        continue;

                    if (mergedPath.length() > 0)
                        mergedPath.append(File.separator);

                    mergedPath.append(path);

                    getList().add(new Index<>(path, mergedPath.toString()));
                }
        }
    }
}
