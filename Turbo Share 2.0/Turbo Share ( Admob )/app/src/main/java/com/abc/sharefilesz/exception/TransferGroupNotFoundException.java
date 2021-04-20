

package com.abc.sharefilesz.exception;

import com.abc.sharefilesz.object.TransferGroup;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 6.01.2018 22:26
 */

public class TransferGroupNotFoundException extends ReconstructionFailedException
{
    public TransferGroup group;

    public TransferGroupNotFoundException(TransferGroup group)
    {
        super("Transfer group not found");
        this.group = group;
    }
}
