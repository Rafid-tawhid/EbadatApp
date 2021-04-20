

package com.abc.sharefilesz.exception;

import com.abc.sharefilesz.object.TransferAssignee;
import com.genonbeta.android.database.exception.ReconstructionFailedException;

/**

 * date: 06.04.2018 11:20
 */
public class AssigneeNotFoundException extends ReconstructionFailedException
{
    public TransferAssignee assignee;

    public AssigneeNotFoundException(TransferAssignee assignee)
    {
        super("Assignee with deviceId=" + assignee.deviceId + " and groupId=" + assignee.groupId + " is not valid");
        this.assignee = assignee;
    }
}
