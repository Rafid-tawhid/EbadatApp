

package com.abc.sharefilesz.util;

import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;

public class SelectionUtils
{
    public static int getTotalSize(IPerformerEngine engine)
    {
        int selectedTotal = 0;
        for (IBaseEngineConnection connection : engine.getConnectionList())
            selectedTotal += connection.getGenericSelectedItemList().size();
        return selectedTotal;
    }
}
