

package com.abc.sharefilesz.migration.db.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.migration.db.Migration;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

/**

 * date: 7/31/19 11:02 AM
 */
public class TransferGroupV12 implements DatabaseObject<NetworkDeviceV12>
{
    public long groupId;
    public long dateCreated;
    public String savePath;
    public boolean isServedOnWeb;

    public TransferGroupV12()
    {
    }

    public TransferGroupV12(long groupId)
    {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TransferGroupV12 && ((TransferGroupV12) obj).groupId == groupId;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.groupId = item.getAsLong(Kuick.FIELD_TRANSFERGROUP_ID);
        this.savePath = item.getAsString(Kuick.FIELD_TRANSFERGROUP_SAVEPATH);
        this.dateCreated = item.getAsLong(Kuick.FIELD_TRANSFERGROUP_DATECREATED);
        this.isServedOnWeb = item.getAsInteger(Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB) == 1;
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFERGROUP_ID, groupId);
        values.put(Kuick.FIELD_TRANSFERGROUP_SAVEPATH, savePath);
        values.put(Kuick.FIELD_TRANSFERGROUP_DATECREATED, dateCreated);
        values.put(Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB, isServedOnWeb ? 1 : 0);

        return values;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFERGROUP)
                .setWhere(Kuick.FIELD_TRANSFERGROUP_ID + "=?", String.valueOf(groupId));
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {
        this.dateCreated = System.currentTimeMillis();
    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {
        kuick.remove(db, new SQLQuery.Select(Migration.v12.TABLE_DIVISTRANSFER)
                .setWhere(String.format("%s = ?", Kuick.FIELD_TRANSFER_GROUPID), String.valueOf(groupId)));

        kuick.remove(db, new SQLQuery.Select(Kuick.TABLE_TRANSFERASSIGNEE)
                .setWhere(Kuick.FIELD_TRANSFERASSIGNEE_GROUPID + "=?", String.valueOf(groupId)));

        kuick.removeAsObject(db, new SQLQuery.Select(Kuick.TABLE_TRANSFER)
                        .setWhere(Kuick.FIELD_TRANSFER_GROUPID + "=?", String.valueOf(groupId)),
                TransferObjectV12.class, this, listener, null);
    }
}