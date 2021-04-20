

package com.abc.sharefilesz.migration.db.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

import static com.abc.sharefilesz.migration.db.Migration.v12.*;

/**

 * date: 16.02.2018 12:56
 */

public class WritablePathObjectV12 implements DatabaseObject<Object>
{
    public String title;
    public Uri path;

    public WritablePathObjectV12()
    {
    }

    public WritablePathObjectV12(Uri path)
    {
        this.path = path;
    }

    public WritablePathObjectV12(String title, Uri path)
    {
        this(path);
        this.title = title;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(TABLE_WRITABLEPATH)
                .setWhere(FIELD_WRITABLEPATH_PATH + "=?", path.toString());
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FIELD_WRITABLEPATH_TITLE, title);
        contentValues.put(FIELD_WRITABLEPATH_PATH, path.toString());

        return contentValues;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.title = item.getAsString(FIELD_WRITABLEPATH_TITLE);
        this.path = Uri.parse(item.getAsString(FIELD_WRITABLEPATH_PATH));
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, Object parent, Progress.Listener listener)
    {

    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, Object parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, Object parent, Progress.Listener listener)
    {

    }
}
