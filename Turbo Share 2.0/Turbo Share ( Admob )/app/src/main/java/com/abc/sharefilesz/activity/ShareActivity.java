

package com.abc.sharefilesz.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.abc.sharefilesz.service.BackgroundService;
import com.abc.sharefilesz.service.backgroundservice.AttachedTaskListener;
import com.abc.sharefilesz.service.backgroundservice.BaseAttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.task.OrganizeSharingTask;
import com.genonbeta.android.framework.ui.callback.SnackbarPlacementProvider;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends Activity implements SnackbarPlacementProvider, AttachedTaskListener
{
    public static final String TAG = "ShareActivity";

    public static final String
            ACTION_SEND = "genonbeta.intent.action.TurboShare_SEND",
            ACTION_SEND_MULTIPLE = "genonbeta.intent.action.TurboShare_SEND_MULTIPLE",
            EXTRA_DEVICE_ID = "extraDeviceId";

    private ProgressBar mProgressBar;
    private TextView mProgressTextCurrent;
    private TextView mProgressTextTotal;
    private TextView mTextMain;
    private List<Uri> mFileUris;
    private boolean mHadTask = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        mHadTask = savedInstanceState.getBoolean("had_task", mHadTask);
        String action = getIntent() != null ? getIntent().getAction() : null;

        if (ACTION_SEND.equals(action) || ACTION_SEND_MULTIPLE.equals(action) || Intent.ACTION_SEND.equals(action)
                || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
                startActivity(new Intent(ShareActivity.this, TextEditorActivity.class)
                        .setAction(TextEditorActivity.ACTION_EDIT_TEXT)
                        .putExtra(TextEditorActivity.EXTRA_TEXT_INDEX, getIntent().getStringExtra(Intent.EXTRA_TEXT)));
                finish();
            } else {
                List<Uri> fileUris = new ArrayList<>();

                if (ACTION_SEND_MULTIPLE.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    List<Uri> pendingFileUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (pendingFileUris != null)
                        fileUris.addAll(pendingFileUris);
                } else {
                    fileUris.add(getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
                }

                if (fileUris.size() == 0) {
                    Toast.makeText(this, R.string.mesg_nothingToShare, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    mProgressBar = findViewById(R.id.progressBar);
                    mProgressTextCurrent = findViewById(R.id.text1);
                    mProgressTextTotal = findViewById(R.id.text2);
                    mTextMain = findViewById(R.id.textMain);
                    mFileUris = fileUris;

                    findViewById(R.id.cancelButton).setOnClickListener(v -> interruptAllTasks(true));
                }
            }
        } else {
            Toast.makeText(this, R.string.mesg_formatNotSupported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("had_task", mHadTask);
    }

    @Override
    public void onTaskStateChanged(BaseAttachableBgTask task)
    {
        if (task instanceof OrganizeSharingTask) {
            if (task.isFinished()) {
                finish();
                return;
            }

            int progress = task.progress().getCurrent();
            int total = task.progress().getTotal();

            runOnUiThread(() -> {
                mTextMain.setText(task.getCurrentContent());
                mProgressTextCurrent.setText(String.valueOf(progress));
                mProgressTextTotal.setText(String.valueOf(total));
            });

            mProgressBar.setProgress(progress);
            mProgressBar.setMax(total);
        }
    }

    @Override
    public boolean onTaskMessage(TaskMessage message)
    {
        return false;
    }

    @Override
    protected void onAttachTasks(List<BaseAttachableBgTask> taskList)
    {
        super.onAttachTasks(taskList);
        for (BaseAttachableBgTask task : taskList)
            if (task instanceof OrganizeSharingTask)
                ((OrganizeSharingTask) task).setAnchor(this);

        if (!hasTaskOf(OrganizeSharingTask.class)) {
            if (mHadTask)
                finish();
            else {
                BackgroundService.run(this, new OrganizeSharingTask(mFileUris));
                mHadTask = true;
            }
        }
    }

    public Snackbar createSnackbar(int resId, Object... objects)
    {
        return Snackbar.make(getWindow().getDecorView(), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

    public ProgressBar getProgressBar()
    {
        return mProgressBar;
    }
}

