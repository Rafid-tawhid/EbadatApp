

package com.abc.sharefilesz.service.backgroundservice;

public abstract class BaseAttachableBgTask extends BackgroundTask
{
    public abstract boolean hasAnchor();

    public abstract void removeAnchor();
}
