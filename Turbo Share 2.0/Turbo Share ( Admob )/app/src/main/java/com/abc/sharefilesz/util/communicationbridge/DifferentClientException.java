

package com.abc.sharefilesz.util.communicationbridge;

import com.abc.sharefilesz.object.Device;

public class DifferentClientException extends CommunicationException
{
    public Device expected;
    public Device got;

    public DifferentClientException(Device expected, Device got)
    {
        super();
        this.expected = expected;
        this.got = got;
    }
}
