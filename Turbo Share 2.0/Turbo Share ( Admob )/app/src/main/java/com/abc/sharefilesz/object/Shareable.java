

package com.abc.sharefilesz.object;

import android.net.Uri;
import com.abc.sharefilesz.util.TextUtils;

/**

 * date: 19.11.2017 16:50
 */

public abstract class Shareable implements Editable
{
    public long id;
    public String friendlyName;
    public String fileName;
    public String mimeType;
    public Uri uri;
    public long date;
    public long size;

    private boolean isSelected = false;

    public Shareable()
    {
    }

    @Override
    public boolean applyFilter(String[] filteringKeywords)
    {
        for (String keyword : filteringKeywords)
            if (TextUtils.searchWord(friendlyName, keyword))
                return true;

        return false;
    }

    @Override
    public boolean comparisonSupported()
    {
        return true;
    }

    protected void initialize(long id, String friendlyName, String fileName, String mimeType, long date, long size,
                              Uri uri)
    {
        this.id = id;
        this.friendlyName = friendlyName;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.date = date;
        this.size = size;
        this.uri = uri;
    }

    @Override
    public boolean isSelectableSelected()
    {
        return isSelected;
    }

    @Override
    public String getComparableName()
    {
        return getSelectableTitle();
    }

    @Override
    public long getComparableDate()
    {
        return this.date;
    }

    @Override
    public long getComparableSize()
    {
        return this.size;
    }

    @Override
    public long getId()
    {
        return this.id;
    }

    @Override
    public String getSelectableTitle()
    {
        return this.friendlyName;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Shareable ? ((Shareable) obj).uri.equals(uri) : super.equals(obj);
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }

    @Override
    public boolean setSelectableSelected(boolean selected)
    {
        isSelected = selected;
        return true;
    }
}