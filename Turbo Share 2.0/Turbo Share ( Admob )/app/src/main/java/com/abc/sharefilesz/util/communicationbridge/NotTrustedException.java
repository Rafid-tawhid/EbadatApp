

package com.abc.sharefilesz.util.communicationbridge;

import com.abc.sharefilesz.object.Device;

public class NotTrustedException extends CommunicationException
{
    public Device device;

    public NotTrustedException(Device device)
    {
        super();
        this.device = device;
    }
}
