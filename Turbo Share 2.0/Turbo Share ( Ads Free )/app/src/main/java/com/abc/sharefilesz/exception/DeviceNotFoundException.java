

package com.abc.sharefilesz.exception;

import com.abc.sharefilesz.object.Device;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 6.01.2018 22:26
 */

public class DeviceNotFoundException extends ReconstructionFailedException
{
    public Device device;

    public DeviceNotFoundException(Device device)
    {
        super(device.id + " doesn't point to a device");
        this.device = device;
    }
}
