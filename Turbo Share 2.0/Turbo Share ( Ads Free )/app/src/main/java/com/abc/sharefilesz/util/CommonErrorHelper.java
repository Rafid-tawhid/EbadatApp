

package com.abc.sharefilesz.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.task.DeviceIntroductionTask;
import com.abc.sharefilesz.R;

public class CommonErrorHelper
{
    public static TaskMessage messageOf(Exception e)
    {
        return TaskMessage.newInstance();
    }

    public static TaskMessage messageOf(DeviceIntroductionTask.SuggestNetworkException e, Context appContext)
    {
        TaskMessage message = TaskMessage.newInstance()
                .setTitle(appContext, R.string.text_error);

        switch (e.type) {
            case ExceededLimit:
                message.setMessage(appContext, R.string.text_errorExceededMaximumSuggestions)
                        .addAction(appContext, R.string.butn_openSettings, TaskMessage.Tone.Positive,
                                (context, msg, action) -> context.startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS)));
                break;
            case AppDisallowed:
                message.setMessage(appContext, R.string.text_errorNetworkSuggestionsDisallowed)
                        .addAction(appContext, R.string.butn_openSettings, TaskMessage.Tone.Positive,
                                (context, msg, action) -> AppUtils.startApplicationDetails(context));
                break;

        }

        return message;
    }
}
