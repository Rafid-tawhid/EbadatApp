

package com.abc.sharefilesz.object;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has two modes. One is the AND values and the other is the OR values. The AND values are the first
 * to be tried. If they don't match, which is when they don't have any values or the same value size, or one of the
 * values not matching, the OR values are tried and if any of them matches with something in the list then the match
 * is considered to be true. When using this, try to avoid using default values like 0, false, or "". The values
 * you put should be unique, so that it doesn't match with a random identifier. This class can be transferred with
 * intents.
 */
public class Identity implements Parcelable
{
    private final List<Identifier> mValueListOR;
    private final List<Identifier> mValueListAND;

    public Identity()
    {
        mValueListOR = new ArrayList<>();
        mValueListAND = new ArrayList<>();
    }

    protected Identity(Parcel in)
    {
        mValueListOR = in.createTypedArrayList(Identifier.CREATOR);
        mValueListAND = in.createTypedArrayList(Identifier.CREATOR);
    }

    public static final Creator<Identity> CREATOR = new Creator<Identity>()
    {
        @Override
        public Identity createFromParcel(Parcel in)
        {
            return new Identity(in);
        }

        @Override
        public Identity[] newArray(int size)
        {
            return new Identity[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj instanceof Identity) {
            Identity identity = (Identity) obj;

            if (!isAllTrue(identity)) {
                synchronized (mValueListOR) {
                    synchronized (identity.mValueListOR) {
                        for (Identifier identifier : identity.mValueListOR)
                            if (mValueListOR.contains(identifier))
                                return true;
                    }

                    return false;
                }
            }
        }
        return super.equals(obj);
    }

    // AND
    private boolean isAllTrue(Identity identity)
    {
        if (mValueListAND.size() <= 0 || identity.mValueListAND.size() <= 0)
            return false;

        synchronized (mValueListAND) {
            synchronized (identity.mValueListAND) {
                for (Identifier identifier : identity.mValueListAND)
                    if (!mValueListAND.contains(identifier))
                        return false;
            }
        }

        return true;
    }

    public void putAND(Identifier identifier)
    {
        synchronized (mValueListAND) {
            mValueListAND.add(identifier);
        }
    }

    public void putOR(Identifier identifier)
    {
        synchronized (mValueListOR) {
            mValueListOR.add(identifier);
        }
    }

    public static Identity withANDs(Identifier... ands)
    {
        Identity identity = new Identity();
        for (Identifier and : ands)
            identity.putAND(and);
        return identity;
    }

    public static Identity withORs(Identifier... ors)
    {
        Identity identity = new Identity();
        for (Identifier or : ors)
            identity.putOR(or);
        return identity;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(mValueListOR);
        dest.writeTypedList(mValueListAND);
    }
}
