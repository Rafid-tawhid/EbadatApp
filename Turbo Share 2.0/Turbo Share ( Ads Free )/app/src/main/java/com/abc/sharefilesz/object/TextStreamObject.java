

package com.abc.sharefilesz.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.widget.GroupEditableListAdapter;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

/**

 * date: 30.12.2017 13:19
 */

public class TextStreamObject extends GroupEditableListAdapter.GroupShareable implements DatabaseObject<Object>
{
    public String text;

    public TextStreamObject()
    {
    }

    public TextStreamObject(String representativeText)
    {
        super(GroupEditableListAdapter.VIEW_TYPE_REPRESENTATIVE, representativeText);
    }

    public TextStreamObject(long id)
    {
        setId(id);
    }

    public TextStreamObject(long id, String index)
    {
        initialize(id, index, index, "text/plain", System.currentTimeMillis(), index.length(), null);
        this.text = index;
    }

    @Override
    public boolean applyFilter(String[] filteringKeywords)
    {
        if (super.applyFilter(filteringKeywords))
            return true;

        for (String keyword : filteringKeywords)
            if (text.toLowerCase().contains(keyword.toLowerCase()))
                return true;

        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TextStreamObject && ((TextStreamObject) obj).id == id;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_CLIPBOARD)
                .setWhere(Kuick.FIELD_CLIPBOARD_ID + "=?", String.valueOf(getId()));
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_CLIPBOARD_ID, id);
        values.put(Kuick.FIELD_CLIPBOARD_TIME, date);
        values.put(Kuick.FIELD_CLIPBOARD_TEXT, text);

        return values;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.id = item.getAsLong(Kuick.FIELD_CLIPBOARD_ID);
        this.text = item.getAsString(Kuick.FIELD_CLIPBOARD_TEXT);
        this.date = item.getAsLong(Kuick.FIELD_CLIPBOARD_TIME);
        this.mimeType = "text/plain";
        this.size = text.length();
        this.friendlyName = text;
        this.fileName = text;
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
