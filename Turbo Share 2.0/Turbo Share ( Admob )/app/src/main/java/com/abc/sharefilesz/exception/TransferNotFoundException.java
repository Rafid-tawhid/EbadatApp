

package com.abc.sharefilesz.exception;

import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 6.01.2018 22:26
 */

public class TransferNotFoundException extends ReconstructionFailedException
{
    public TransferNotFoundException()
    {
        super("Transfer not found");
    }
}
