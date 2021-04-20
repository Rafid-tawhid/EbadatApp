

package com.abc.sharefilesz.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import com.abc.sharefilesz.database.Kuick;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

import java.util.List;

/**

 * date: 06.04.2018 09:37
 */
public final class TransferGroup implements DatabaseObject<Device>, Parcelable
{
    public long id;
    public long dateCreated;
    public String savePath;
    public boolean isPaused;
    public boolean isServedOnWeb;
    public boolean deleteFilesOnRemoval;

    public TransferGroup()
    {
    }

    public TransferGroup(long id)
    {
        this.id = id;
    }

    protected TransferGroup(Parcel in)
    {
        id = in.readLong();
        dateCreated = in.readLong();
        savePath = in.readString();
        isPaused = in.readByte() != 0;
        isServedOnWeb = in.readByte() != 0;
        deleteFilesOnRemoval = in.readByte() != 0;
    }

    public static final Creator<TransferGroup> CREATOR = new Creator<TransferGroup>()
    {
        @Override
        public TransferGroup createFromParcel(Parcel in)
        {
            return new TransferGroup(in);
        }

        @Override
        public TransferGroup[] newArray(int size)
        {
            return new TransferGroup[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TransferGroup && ((TransferGroup) obj).id == id;
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFERGROUP_ID, id);
        values.put(Kuick.FIELD_TRANSFERGROUP_SAVEPATH, savePath);
        values.put(Kuick.FIELD_TRANSFERGROUP_DATECREATED, dateCreated);
        values.put(Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB, isServedOnWeb ? 1 : 0);
        values.put(Kuick.FIELD_TRANSFERGROUP_ISPAUSED, isPaused ? 1 : 0);

        return values;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFERGROUP)
                .setWhere(Kuick.FIELD_TRANSFERGROUP_ID + "=?", String.valueOf(id));
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.id = item.getAsLong(Kuick.FIELD_TRANSFERGROUP_ID);
        this.savePath = item.getAsString(Kuick.FIELD_TRANSFERGROUP_SAVEPATH);
        this.dateCreated = item.getAsLong(Kuick.FIELD_TRANSFERGROUP_DATECREATED);
        this.isServedOnWeb = item.getAsInteger(Kuick.FIELD_TRANSFERGROUP_ISSHAREDONWEB) == 1;
        this.isPaused = item.getAsInteger(Kuick.FIELD_TRANSFERGROUP_ISPAUSED) == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeLong(dateCreated);
        dest.writeString(savePath);
        dest.writeByte((byte) (isPaused ? 1 : 0));
        dest.writeByte((byte) (isServedOnWeb ? 1 : 0));
        dest.writeByte((byte) (deleteFilesOnRemoval ? 1 : 0));
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {
        this.dateCreated = System.currentTimeMillis();
    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, Device parent, Progress.Listener listener)
    {
        SQLQuery.Select objectSelection = new SQLQuery.Select(Kuick.TABLE_TRANSFER).setWhere(
                String.format("%s = ?", Kuick.FIELD_TRANSFER_GROUPID), String.valueOf(id));

        kuick.remove(db, new SQLQuery.Select(Kuick.TABLE_TRANSFERASSIGNEE).setWhere(
                String.format("%s = ?", Kuick.FIELD_TRANSFERASSIGNEE_GROUPID), String.valueOf(id)));

        if (deleteFilesOnRemoval) {
            List<TransferObject> objects = kuick.castQuery(db, objectSelection, TransferObject.class,
                    null);

            for (TransferObject object : objects)
                object.setDeleteOnRemoval(true);

            kuick.remove(db, objects, this, listener);
        } else
            kuick.removeAsObject(db, objectSelection, TransferObject.class, this, listener,
                    null);
    }
}
