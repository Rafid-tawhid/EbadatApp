

package com.abc.sharefilesz.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.util.TransferUtils;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 8/3/19 1:35 PM
 */
public class TransferAssignee implements DatabaseObject<TransferGroup>
{
    public long groupId;
    public String deviceId;
    public String connectionAdapter;
    public TransferObject.Type type;

    public TransferAssignee()
    {

    }

    public TransferAssignee(long groupId, String deviceId, TransferObject.Type type)
    {
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.type = type;
    }

    public TransferAssignee(@NonNull TransferGroup group, @NonNull Device device, @NonNull TransferObject.Type type)
    {
        this(group.id, device.id, type);
    }

    public TransferAssignee(long groupId, String deviceId, TransferObject.Type type, String connectionAdapter)
    {
        this(groupId, deviceId, type);
        this.connectionAdapter = connectionAdapter;
    }
    public TransferAssignee(@NonNull TransferGroup group, @NonNull Device device,
                            @NonNull TransferObject.Type type, @NonNull DeviceConnection connection)
    {
        this(group.id, device.id, type, connection.adapterName);
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj instanceof TransferAssignee) {
            TransferAssignee otherAssignee = (TransferAssignee) obj;
            return otherAssignee.groupId == groupId && deviceId.equals(otherAssignee.deviceId)
                    && type.equals(otherAssignee.type);
        }

        return super.equals(obj);
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFERASSIGNEE).setWhere(
                Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID + "=? AND "
                        + Kuick.FIELD_TRANSFERASSIGNEE_GROUPID + "=? AND "
                        + Kuick.FIELD_TRANSFERASSIGNEE_TYPE + "=?", deviceId,
                String.valueOf(groupId), type.toString());
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID, deviceId);
        values.put(Kuick.FIELD_TRANSFERASSIGNEE_GROUPID, groupId);
        values.put(Kuick.FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER, connectionAdapter);
        values.put(Kuick.FIELD_TRANSFERASSIGNEE_TYPE, type.toString());

        return values;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.deviceId = item.getAsString(Kuick.FIELD_TRANSFERASSIGNEE_DEVICEID);
        this.groupId = item.getAsLong(Kuick.FIELD_TRANSFERASSIGNEE_GROUPID);
        this.connectionAdapter = item.getAsString(Kuick.FIELD_TRANSFERASSIGNEE_CONNECTIONADAPTER);

        // Added in DB version 13 and might be null and may throw an error since ContentValues doesn't like it when
        // when the requested column name doesn't exist or has type different than requested.
        if (item.containsKey(Kuick.FIELD_TRANSFERASSIGNEE_TYPE))
            this.type = TransferObject.Type.valueOf(item.getAsString(Kuick.FIELD_TRANSFERASSIGNEE_TYPE));
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {

    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {
        if (!TransferObject.Type.INCOMING.equals(type))
            return;

        try {
            if (parent == null) {
                parent = new TransferGroup(groupId);
                kuick.reconstruct(db, parent);
            }

            SQLQuery.Select selection = TransferUtils.createIncomingSelection(groupId, TransferObject.Flag.INTERRUPTED,
                    true);

            kuick.removeAsObject(db, selection, TransferObject.class, parent, listener, null);
        } catch (ReconstructionFailedException e) {
            e.printStackTrace();
        }
    }
}