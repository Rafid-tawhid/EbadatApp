

package com.abc.sharefilesz.object;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.adapter.TransferGroupListAdapter;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

/**

 * date: 7/24/19 6:08 PM
 */
public final class IndexOfTransferGroup implements GroupEditableListAdapter.GroupEditable, DatabaseObject<Device>
{
    public static final String TAG = IndexOfTransferGroup.class.getSimpleName();

    public int viewType;
    public String representativeText;

    public int numberOfOutgoing;
    public int numberOfIncoming;
    public int numberOfOutgoingCompleted;
    public int numberOfIncomingCompleted;
    public long bytesOutgoing;
    public long bytesIncoming;
    public long bytesOutgoingCompleted;
    public long bytesIncomingCompleted;
    public boolean isRunning;
    public boolean hasIssues;
    public TransferGroup group = new TransferGroup();
    public ShowingAssignee[] assignees = new ShowingAssignee[0];

    private boolean mIsSelected = false;

    public IndexOfTransferGroup()
    {
        group = new TransferGroup();
    }

    public IndexOfTransferGroup(TransferGroup group)
    {
        this.group = group;
    }

    public IndexOfTransferGroup(String representativeText)
    {
        this.viewType = TransferGroupListAdapter.VIEW_TYPE_REPRESENTATIVE;
        this.representativeText = representativeText;
    }

    @Override
    public boolean applyFilter(String[] filteringKeywords)
    {
        ShowingAssignee[] copyAssignees = assignees;

        for (String keyword : filteringKeywords)
            for (ShowingAssignee assignee : copyAssignees)
                if (assignee.device.nickname.toLowerCase().contains(keyword.toLowerCase()))
                    return true;

        return false;
    }

    public long bytesTotal()
    {
        return bytesOutgoing + bytesIncoming;
    }

    public long bytesCompleted()
    {
        return bytesOutgoingCompleted + bytesIncomingCompleted;
    }

    public long bytesPending()
    {
        return bytesTotal() - bytesCompleted();
    }

    @Override
    public boolean comparisonSupported()
    {
        return true;
    }

    public boolean hasIncoming()
    {
        return numberOfIncoming > 0;
    }

    public boolean hasOutgoing()
    {
        return numberOfOutgoing > 0;
    }

    public String getAssigneesAsTitle()
    {
        ShowingAssignee[] copyAssignees = assignees;
        StringBuilder title = new StringBuilder();

        for (ShowingAssignee assignee : copyAssignees) {
            if (title.length() > 0)
                title.append(", ");
            title.append(assignee.device.nickname);
        }

        return title.toString();
    }

    public String getAssigneesAsTitle(Context context)
    {
        ShowingAssignee[] copyAssignees = assignees;

        if (copyAssignees.length == 1)
            return copyAssignees[0].device.nickname;

        return context.getResources().getQuantityString(R.plurals.text_devices,
                copyAssignees.length, copyAssignees.length);
    }

    @Override
    public String getComparableName()
    {
        return getSelectableTitle();
    }

    @Override
    public long getComparableDate()
    {
        return group.dateCreated;
    }

    @Override
    public long getComparableSize()
    {
        return bytesTotal();
    }

    @Override
    public long getId()
    {
        return group.id;
    }

    @Override
    public void setId(long id)
    {
        group.id = id;
    }

    @Override
    public String getSelectableTitle()
    {
        String title = getAssigneesAsTitle();
        String size = FileUtils.sizeExpression(bytesOutgoing + bytesOutgoing, false);

        return title.length() > 0 ? String.format("%s (%s)", title, size) : size;
    }

    @Override
    public boolean isSelectableSelected()
    {
        return mIsSelected;
    }

    @Override
    public int getRequestCode()
    {
        return 0;
    }

    @Override
    public int getViewType()
    {
        return viewType;
    }

    public int numberOfTotal()
    {
        return numberOfOutgoing + numberOfIncoming;
    }

    public int numberOfCompleted()
    {
        return numberOfOutgoingCompleted + numberOfIncomingCompleted;
    }

    public double percentage()
    {
        long total = bytesTotal();
        long completed = bytesCompleted();
        return total == 0 ? 1 : (completed == 0 ? 0 : (double) completed / total);
    }

    @Override
    public String getRepresentativeText()
    {
        return representativeText;
    }

    @Override
    public void setRepresentativeText(CharSequence text)
    {
        this.representativeText = String.valueOf(text);
    }

    @Override
    public boolean isGroupRepresentative()
    {
        return representativeText != null;
    }

    @Override
    public void setDate(long date)
    {
        group.dateCreated = date;
    }

    @Override
    public boolean setSelectableSelected(boolean selected)
    {
        if (isGroupRepresentative())
            return false;
        mIsSelected = selected;
        return true;
    }

    @Override
    public void setSize(long size)
    {
        Log.e(TAG, "setSize: This is not implemented");
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {
        group.onCreateObject(db, kuick, parent, listener);
    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {
        group.onUpdateObject(db, kuick, parent, listener);
    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {
        group.onRemoveObject(db, kuick, parent, listener);
    }

    @Override
    public ContentValues getValues()
    {
        return group.getValues();
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return group.getWhere();
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        group.reconstruct(db, kuick, item);
    }
}
