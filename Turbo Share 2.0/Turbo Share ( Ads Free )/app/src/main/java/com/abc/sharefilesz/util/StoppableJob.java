

package com.abc.sharefilesz.util;

import com.genonbeta.android.framework.util.Stoppable;

/**

 * date: 11.02.2018 19:37
 */

public abstract class StoppableJob
{
    protected abstract void onRun() throws InterruptedException;

    protected void run(Stoppable stoppable) throws InterruptedException
    {
        onRun();
        stoppable.removeClosers();
    }
}
