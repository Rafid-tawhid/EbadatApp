

package com.abc.sharefilesz.service.backgroundservice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.DrawableRes;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.activity.HomeActivity;
import com.abc.sharefilesz.util.DynamicNotification;
import com.abc.sharefilesz.util.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskMessageImpl implements TaskMessage
{
    private String mTitle;
    private String mMessage;
    private Tone mTone = Tone.Neutral;
    private final List<Action> mActions = new ArrayList<>();

    @Override
    public TaskMessage addAction(Action action)
    {
        synchronized (mActions) {
            mActions.add(action);
        }
        return this;
    }

    @Override
    public TaskMessage addAction(Context context, int nameRes, Callback callback)
    {
        return addAction(context.getString(nameRes), callback);
    }

    @Override
    public TaskMessage addAction(String name, Callback callback)
    {
        return addAction(name, Tone.Neutral, callback);
    }

    @Override
    public TaskMessage addAction(Context context, int nameRes, Tone tone, Callback callback)
    {
        return addAction(context.getString(nameRes), tone, callback);
    }

    @Override
    public TaskMessage addAction(String name, Tone tone, Callback callback)
    {
        Action action = new Action();
        action.name = name;
        action.tone = tone;
        action.callback = callback;
        return addAction(action);
    }

    @Override
    public List<Action> getActionList()
    {
        synchronized (mActions) {
            return new ArrayList<>(mActions);
        }
    }

    public String getMessage()
    {
        return mMessage;
    }

    public String getTitle()
    {
        return mTitle;
    }

    @DrawableRes
    public static int iconFor(Tone tone)
    {
        switch (tone) {
            case Confused:
                return R.drawable.ic_help_white_24_static;
            case Positive:
                return R.drawable.ic_check_white_24dp_static;
            case Negative:
                return R.drawable.ic_close_white_24dp_static;
            default:
            case Neutral:
                return R.mipmap.ic_launcher;
        }
    }

    @Override
    public TaskMessage removeAction(Action action)
    {
        synchronized (mActions) {
            mActions.remove(action);
        }
        return this;
    }

    @Override
    public TaskMessage setMessage(Context context, int msgRes)
    {
        return setMessage(context.getString(msgRes));
    }

    @Override
    public TaskMessage setMessage(String msg)
    {
        mMessage = msg;
        return this;
    }

    @Override
    public TaskMessage setTitle(Context context, int titleRes)
    {
        return setTitle(context.getString(titleRes));
    }

    @Override
    public TaskMessage setTitle(String title)
    {
        mTitle = title;
        return this;
    }

    @Override
    public TaskMessage setTone(Tone tone)
    {
        mTone = tone;
        return this;
    }

    @Override
    public DynamicNotification toNotification(BackgroundTask task)
    {
        Context context = task.getService().getApplicationContext();
        NotificationUtils utils = task.getNotificationHelper().getUtils();
        DynamicNotification notification = utils.buildDynamicNotification(task.hashCode(),
                NotificationUtils.NOTIFICATION_CHANNEL_HIGH);

        notification.setSmallIcon(iconFor(mTone))
                .setGroup(task.getTaskGroup())
                .setContentTitle(mTitle)
                .setContentText(mMessage);

        for (Action action : mActions)
            notification.addAction(iconFor(action.tone), action.name, PendingIntent.getActivity(context,
                    0, new Intent(context, HomeActivity.class), 0));

        return notification;
    }
}
