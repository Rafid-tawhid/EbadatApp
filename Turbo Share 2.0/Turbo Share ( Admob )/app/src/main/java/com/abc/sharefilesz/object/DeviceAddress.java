

package com.abc.sharefilesz.object;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceAddress implements Parcelable
{
    public Device device;
    public DeviceConnection connection;

    public DeviceAddress(Device device, DeviceConnection connection)
    {
        this.device = device;
        this.connection = connection;
    }

    protected DeviceAddress(Parcel in)
    {
        device = in.readParcelable(Device.class.getClassLoader());
        connection = in.readParcelable(DeviceConnection.class.getClassLoader());
    }

    public static final Creator<DeviceAddress> CREATOR = new Creator<DeviceAddress>()
    {
        @Override
        public DeviceAddress createFromParcel(Parcel in)
        {
            return new DeviceAddress(in);
        }

        @Override
        public DeviceAddress[] newArray(int size)
        {
            return new DeviceAddress[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(device, flags);
        dest.writeParcelable(connection, flags);
    }
}
