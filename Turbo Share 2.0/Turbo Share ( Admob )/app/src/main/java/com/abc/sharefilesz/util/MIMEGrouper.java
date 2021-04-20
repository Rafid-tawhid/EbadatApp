

package com.abc.sharefilesz.util;

import java.io.File;

public class MIMEGrouper
{
    public static final String TYPE_GENERIC = "*";

    private String mMajor;
    private String mMinor;
    private boolean mLocked;

    public boolean isLocked()
    {
        return mLocked;
    }

    public String getMajor()
    {
        return mMajor == null ? TYPE_GENERIC : mMajor;
    }

    public String getMinor()
    {
        return mMinor == null ? TYPE_GENERIC : mMinor;
    }

    public void process(String mimeType)
    {
        if (mimeType == null || mimeType.length() < 3 || !mimeType.contains(File.separator))
            return;

        String[] splitMIME = mimeType.split(File.separator);

        process(splitMIME[0], splitMIME[1]);
    }

    public void process(String major, String minor)
    {
        if (mMajor == null || mMinor == null) {
            mMajor = major;
            mMinor = minor;
        } else if (getMajor().equals(TYPE_GENERIC))
            mLocked = true;
        else if (!getMajor().equals(major)) {
            mMajor = TYPE_GENERIC;
            mMinor = TYPE_GENERIC;

            mLocked = true;
        } else if (!getMinor().equals(minor)) {
            mMinor = TYPE_GENERIC;
        }
    }

    @Override
    public String toString()
    {
        return getMajor() + File.separator + getMinor();
    }
}
