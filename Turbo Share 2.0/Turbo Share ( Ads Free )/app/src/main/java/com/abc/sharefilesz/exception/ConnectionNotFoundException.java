

package com.abc.sharefilesz.exception;

import com.abc.sharefilesz.object.DeviceConnection;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 6.01.2018 22:25
 */

public class ConnectionNotFoundException extends ReconstructionFailedException
{
    public DeviceConnection connection;

    public ConnectionNotFoundException(DeviceConnection connection)
    {
        super(connection.adapterName + " connection is not found");
        this.connection = connection;
    }
}
