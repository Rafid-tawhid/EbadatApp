

package com.abc.sharefilesz.service.backgroundservice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import androidx.annotation.Nullable;

import com.abc.sharefilesz.object.Identifiable;
import com.abc.sharefilesz.object.Identifier;
import com.abc.sharefilesz.object.Identity;
import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.util.AppUtils;
import com.abc.sharefilesz.util.DynamicNotification;
import com.abc.sharefilesz.util.NotificationHelper;
import com.abc.sharefilesz.util.StoppableJob;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.framework.util.Stoppable;
import com.genonbeta.android.framework.util.StoppableImpl;

import java.util.List;

public abstract class BackgroundTask extends StoppableJob implements Stoppable, Identifiable
{
    public static final String TASK_GROUP_DEFAULT = "TASK_GROUP_DEFAULT";

    private Kuick mKuick;
    private BackgroundService mService;
    private Stoppable mStoppable;
    private PendingIntent mActivityIntent;
    private DynamicNotification mCustomNotification; // The notification that is not part of the default notification.
    private ProgressListener mProgressListener = new ProgressListener();
    private String mCurrentContent;
    private boolean mPublishRequested = false;
    private boolean mFinished = false;
    private boolean mStarted = false;
    private boolean mScheduleRerun = false;
    private int mHash = 0;

    protected abstract void onRun() throws InterruptedException;

    protected void onProgressChange(Progress progress)
    {

    }

    @Override
    public boolean addCloser(Closer closer)
    {
        return getStoppable().addCloser(closer);
    }

    public boolean consumeChanges()
    {
        if (mPublishRequested) {
            mPublishRequested = false;
            return true;
        }
        return false;
    }

    public void forceQuit()
    {

    }

    @Override
    public List<Closer> getClosers()
    {
        return getStoppable().getClosers();
    }

    @Nullable
    public PendingIntent getContentIntent()
    {
        return mActivityIntent;
    }

    public String getCurrentContent()
    {
        return mCurrentContent;
    }

    @Nullable
    public DynamicNotification getCustomNotification()
    {
        return mCustomNotification;
    }

    public abstract String getDescription();

    @Override
    public Identity getIdentity()
    {
        return Identity.withORs(Identifier.from(Id.HashCode, hashCode()));
    }

    protected MediaScannerConnection getMediaScanner()
    {
        return getService().getMediaScanner();
    }

    protected NotificationHelper getNotificationHelper()
    {
        return getService().getNotificationHelper();
    }

    protected BackgroundService getService()
    {
        return mService;
    }

    private Stoppable getStoppable()
    {
        if (mStoppable == null)
            mStoppable = new StoppableImpl();

        return mStoppable;
    }

    public String getTaskGroup()
    {
        return TASK_GROUP_DEFAULT;
    }

    public abstract String getTitle();

    @Override
    public boolean hasCloser(Closer closer)
    {
        return getStoppable().hasCloser(closer);
    }

    @Override
    public int hashCode()
    {
        return mHash != 0 ? mHash : super.hashCode();
    }

    public boolean interrupt()
    {
        return getStoppable().interrupt();
    }

    public boolean interrupt(boolean userAction)
    {
        return getStoppable().interrupt(userAction);
    }

    public boolean isFinished()
    {
        return mFinished;
    }

    @Override
    public boolean isInterrupted()
    {
        return getStoppable().isInterrupted();
    }

    @Override
    public boolean isInterruptedByUser()
    {
        return getStoppable().isInterruptedByUser();
    }

    public boolean isStarted()
    {
        return mStarted;
    }

    public Kuick kuick()
    {
        if (mKuick == null)
            mKuick = AppUtils.getKuick(getService());
        return mKuick;
    }

    public void post(TaskMessage message)
    {
        DynamicNotification notification = message.toNotification(this).show();
        setCustomNotification(notification);
    }

    public Progress progress()
    {
        return Progress.dissect(mProgressListener);
    }

    public Progress.Listener progressListener()
    {
        // This ensures when Progress.Listener.getProgress() is called, it doesn't return a null object.
        // Of course, if the user needs the progress itself, then, he or she should use #progress() method.
        progress();
        return mProgressListener;
    }

    public boolean publishStatus()
    {
        mPublishRequested = true;
        return false;
    }

    @Override
    public boolean removeCloser(Closer closer)
    {
        return getStoppable().removeCloser(closer);
    }

    public void rerun(BackgroundService service)
    {
        if (!isStarted() || isFinished()) {
            reset(true);
            service.run(this);
        } else
            mScheduleRerun = true;
    }

    @Override
    public void reset()
    {
        resetInternal();
        getStoppable().reset();
    }

    @Override
    public void reset(boolean resetClosers)
    {
        resetInternal();
        getStoppable().reset(resetClosers);
    }

    private void resetInternal()
    {
        if (isStarted() && !isFinished())
            throw new IllegalStateException("Can't reset when the task is running");

        setStarted(false);
        setFinished(false);
        progressListener().setProgress(null);
    }

    @Override
    public void removeClosers()
    {
        getStoppable().removeClosers();
    }

    public void run(BackgroundService service)
    {
        if (isStarted() || isFinished() || isInterrupted())
            throw new IllegalStateException(getClass().getName() + " is already in interrupted state. To run it "
                    + "again with the same configuration you need to use rerun().");

        setStarted(true);
        setService(service);
        publishStatus();

        try {
            run(getStoppable());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            setFinished(true);
            publishStatus();
            setService(null);
        }

        if (mScheduleRerun)
            rerun(service);
    }

    public void setContentIntent(PendingIntent intent)
    {
        mActivityIntent = intent;
    }

    public void setContentIntent(Context context, Intent intent)
    {
        mHash = BackgroundService.hashIntent(intent);
        setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
    }

    public void setCustomNotification(DynamicNotification notification)
    {
        mCustomNotification = notification;
    }

    private void setFinished(boolean finished)
    {
        mFinished = finished;
    }

    public void setOngoingContent(String content)
    {
        mCurrentContent = content;
    }

    private void setService(@Nullable BackgroundService service)
    {
        mService = service;
    }

    private void setStarted(boolean started)
    {
        mStarted = started;
    }

    public void setStoppable(Stoppable stoppable)
    {
        mStoppable = stoppable;
    }

    public void throwIfInterrupted() throws InterruptedException
    {
        if (isInterrupted())
            throw new InterruptedException("This task been interrupted by user=" + isInterruptedByUser());
    }

    private class ProgressListener extends Progress.SimpleListener
    {
        @Override
        public boolean onProgressChange(Progress progress)
        {
            BackgroundTask.this.onProgressChange(progress);
            publishStatus();
            return !isInterrupted();
        }
    }

    public enum Id
    {
        HashCode
    }
}
