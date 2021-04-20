

package com.abc.sharefilesz.io;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Containable implements Parcelable
{
    public static final Creator<Containable> CREATOR = new Creator<Containable>()
    {
        @Override
        public Containable createFromParcel(Parcel source)
        {
            return new Containable(source);
        }

        @Override
        public Containable[] newArray(int size)
        {
            return new Containable[size];
        }
    };

    public Uri targetUri;
    public Uri[] children;

    public Containable(Parcel in)
    {
        ClassLoader uriClassLoader = Uri.class.getClassLoader();

        targetUri = in.readParcelable(uriClassLoader);
        children = in.createTypedArray(Uri.CREATOR);
    }

    public Containable(Uri targetUri, List<Uri> children)
    {
        this.targetUri = targetUri;
        this.children = new Uri[children.size()];

        children.toArray(this.children);
    }

    public Containable(Uri targetUri, Uri[] children) {
        this.targetUri = targetUri;
        this.children = children;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Containable ? targetUri.equals(((Containable) obj).targetUri) : super.equals(obj);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(targetUri, flags);
        dest.writeTypedArray(children, flags);
    }
}
