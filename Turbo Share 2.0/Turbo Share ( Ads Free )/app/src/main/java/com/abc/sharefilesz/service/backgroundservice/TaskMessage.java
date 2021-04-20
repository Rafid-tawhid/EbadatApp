

package com.abc.sharefilesz.service.backgroundservice;

import android.content.Context;

import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.util.DynamicNotification;

import java.util.List;

public interface TaskMessage
{
    static TaskMessage newInstance()
    {
        return new TaskMessageImpl();
    }

    TaskMessage addAction(Action action);

    TaskMessage addAction(Context context, int nameRes, Callback callback);

    TaskMessage addAction(String name, Callback callback);

    TaskMessage addAction(Context context, int nameRes, Tone tone, Callback callback);

    TaskMessage addAction(String name, Tone tone, Callback callback);

    List<Action> getActionList();

    TaskMessage removeAction(Action action);

    TaskMessage setMessage(Context context, int msgRes);

    TaskMessage setMessage(String msg);

    TaskMessage setTitle(Context context, int titleRes);

    TaskMessage setTitle(String title);

    TaskMessage setTone(Tone tone);

    DynamicNotification toNotification(BackgroundTask task);

    enum Tone {
        Positive,
        Confused,
        Neutral,
        Negative
    }

    class Action
    {
        public Tone tone;
        public String name;
        public Callback callback;
    }

    interface Callback
    {
        void call(BackgroundService service, TaskMessage msg, Action action);
    }
}
