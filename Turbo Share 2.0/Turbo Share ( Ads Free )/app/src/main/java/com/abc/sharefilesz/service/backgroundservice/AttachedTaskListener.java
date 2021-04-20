

package com.abc.sharefilesz.service.backgroundservice;

public interface AttachedTaskListener
{
    void onTaskStateChanged(BaseAttachableBgTask task);

    /**
     * When {@link AttachableBgTask#post(TaskMessage)} is called, this will be invoked when available.
     *
     * @param message to be handled
     * @return false if you didn't process the message
     */
    boolean onTaskMessage(TaskMessage message);
}
