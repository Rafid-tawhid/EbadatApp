

package com.abc.sharefilesz.app;

import android.content.SharedPreferences;

import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.NotificationUtils;

/**

 * date: 31.03.2018 15:23
 */
public abstract class Service extends android.app.Service
{
    private NotificationUtils mNotificationUtils;

    public Kuick getKuick()
    {
        return AppUtils.getKuick(this);
    }

    public SharedPreferences getDefaultPreferences()
    {
        return AppUtils.getDefaultPreferences(getApplicationContext());
    }

    public NotificationUtils getNotificationUtils()
    {
        if (mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils(getApplicationContext(), getKuick(), getDefaultPreferences());

        return mNotificationUtils;
    }
}
