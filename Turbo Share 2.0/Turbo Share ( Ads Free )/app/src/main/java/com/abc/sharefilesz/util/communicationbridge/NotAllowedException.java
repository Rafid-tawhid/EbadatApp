

package com.abc.sharefilesz.util.communicationbridge;

import com.abc.sharefilesz.object.Device;

public class NotAllowedException extends CommunicationException
{
    public Device device;

    public NotAllowedException(Device device)
    {
        super();
        this.device = device;
    }
}
