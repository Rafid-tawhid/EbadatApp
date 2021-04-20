package com.abc.sharefilesz.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.abc.sharefilesz.object.DeviceAddress;
import com.abc.sharefilesz.service.backgroundservice.BaseAttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.task.DeviceIntroductionTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IpAddressConnectionActivity extends Activity implements DeviceIntroductionTask.ResultListener
{
    public static final String
            TAG = IpAddressConnectionActivity.class.getSimpleName(),
            EXTRA_DEVICE = "extraDevice",
            EXTRA_CONNECTION = "extraConnection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_address_connection);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        findViewById(R.id.confirm_button).setOnClickListener((v) -> {
            AppCompatEditText editText = findViewById(R.id.editText);
            String ipAddress = editText.getText().toString();

            if (ipAddress.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
                try {
                    runUiTask(new DeviceIntroductionTask(InetAddress.getByName(ipAddress), -1));
                } catch (UnknownHostException e) {
                    editText.setError(getString(R.string.mesg_unknownHostError));
                }
            } else
                editText.setError(getString(R.string.mesg_errorNotAnIpAddress));
        });
    }

    @Override
    protected void onAttachTasks(List<BaseAttachableBgTask> taskList)
    {
        super.onAttachTasks(taskList);

        boolean hasDeviceIntroductionTask = false;
        for (BaseAttachableBgTask task : taskList)
            if (task instanceof DeviceIntroductionTask) {
                hasDeviceIntroductionTask = true;
                ((DeviceIntroductionTask) task).setAnchor(this);
            }

        setShowProgress(hasDeviceIntroductionTask);
    }

    @Override
    public void onTaskStateChanged(BaseAttachableBgTask task)
    {
        setShowProgress(task instanceof DeviceIntroductionTask && !task.isFinished());
    }

    @Override
    public boolean onTaskMessage(TaskMessage message)
    {
        return false;
    }

    private void setShowProgress(boolean show)
    {
        findViewById(R.id.progressBar).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDeviceReached(DeviceAddress deviceAddress)
    {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_DEVICE, deviceAddress.device)
                .putExtra(EXTRA_CONNECTION, deviceAddress.connection));
        finish();
    }
}
