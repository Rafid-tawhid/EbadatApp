

package com.abc.sharefilesz.migration.db.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.migration.db.Migration;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

/**

 * date: 8/3/19 1:14 PM
 */
public class TransferAssigneeV12 implements DatabaseObject<NetworkDeviceV12>
{
    public long groupId;
    public String deviceId;
    public String connectionAdapter;

    public TransferAssigneeV12()
    {

    }

    public TransferAssigneeV12(long groupId, String deviceId)
    {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public TransferAssigneeV12(@NonNull TransferGroupV12 group, @NonNull NetworkDeviceV12 device)
    {
        this(group.groupId, device.deviceId);
    }

    public TransferAssigneeV12(long groupId, String deviceId, String connectionAdapter)
    {
        this(groupId, deviceId);
        this.connectionAdapter = connectionAdapter;
    }

    public TransferAssigneeV12(@NonNull TransferGroupV12 group, @NonNull NetworkDeviceV12 device,
                               @NonNull DeviceConnection connection)
    {
        this(group.groupId, device.deviceId, connection.adapterName);
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj instanceof TransferAssigneeV12) {
            TransferAssigneeV12 otherAssignee = (TransferAssigneeV12) obj;
            return otherAssignee.groupId == groupId && deviceId.equals(otherAssignee.deviceId);
        }

        return super.equals(obj);
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFERASSIGNEE)
                .setWhere(Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID + "=? AND " +
                        Kuick.FIELD_TRANSFERASSIGNEE_GROUPID + "=?", deviceId, String.valueOf(groupId));
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID, deviceId);
        values.put(Kuick.FIELD_TRANSFERASSIGNEE_GROUPID, groupId);
        values.put(Kuick.FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER, connectionAdapter);
        values.put(Migration.v12.FIELD_TRANSFERASSIGNEE_ISCLONE, 1);

        return values;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.deviceId = item.getAsString(Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID);
        this.groupId = item.getAsLong(Kuick.FIELD_TRANSFERASSIGNEE_GROUPID);
        this.connectionAdapter = item.getAsString(Kuick.FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER);
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {

    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {

    }
}