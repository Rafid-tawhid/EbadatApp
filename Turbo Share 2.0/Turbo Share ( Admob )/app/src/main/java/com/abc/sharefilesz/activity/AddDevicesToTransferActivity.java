

package com.abc.sharefilesz.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.abc.sharefilesz.object.Device;
import com.abc.sharefilesz.object.DeviceConnection;
import com.abc.sharefilesz.object.TransferGroup;
import com.abc.sharefilesz.service.backgroundservice.AttachedTaskListener;
import com.abc.sharefilesz.service.backgroundservice.BaseAttachableBgTask;
import com.abc.sharefilesz.service.backgroundservice.TaskMessage;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.database.Kuick;
import com.abc.sharefilesz.fragment.TransferAssigneeListFragment;
import com.abc.sharefilesz.task.AddDeviceTask;
import com.abc.sharefilesz.util.AppUtils;
import com.genonbeta.android.framework.ui.callback.SnackbarPlacementProvider;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AddDevicesToTransferActivity extends Activity implements SnackbarPlacementProvider, AttachedTaskListener
{
    public static final String
            TAG = AddDevicesToTransferActivity.class.getSimpleName(),
            EXTRA_DEVICE = "extraDevice",
            EXTRA_GROUP = "extraGroup",
            EXTRA_FLAGS = "extraFlags";

    public static final int
            REQUEST_CODE_CHOOSE_DEVICE = 0,
            FLAG_LAUNCH_DEVICE_CHOOSER = 1;

    private TransferGroup mGroup = null;
    private ExtendedFloatingActionButton mActionButton;
    private ProgressBar mProgressBar;
    private ViewGroup mLayoutStatusContainer;
    private TextView mProgressTextCurrent;
    private TextView mProgressTextTotal;
    private int mColorActive;
    private int mColorNormal;
    private IntentFilter mFilter = new IntentFilter(Kuick.ACTION_DATABASE_CHANGE);
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Kuick.ACTION_DATABASE_CHANGE.equals(intent.getAction())) {
                Kuick.BroadcastData data = Kuick.toData(intent);
                if (Kuick.TABLE_TRANSFERGROUP.equals(data.tableName) && !checkGroupIntegrity())
                    finish();
            }
        }
    };

    public static void startInstance(Context context, TransferGroup group, boolean addingNewDevice)
    {
        context.startActivity(new Intent(context, AddDevicesToTransferActivity.class)
                .putExtra(EXTRA_GROUP, group)
                .putExtra(EXTRA_FLAGS, addingNewDevice ? FLAG_LAUNCH_DEVICE_CHOOSER : 0)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_devices_to_transfer);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (checkGroupIntegrity()) {
            int flags = getIntent().getIntExtra(EXTRA_FLAGS, 0);
            if ((flags & FLAG_LAUNCH_DEVICE_CHOOSER) != 0)
                startConnectionManagerActivity();
        } else
            return;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle assigneeFragmentArgs = new Bundle();
        assigneeFragmentArgs.putLong(TransferAssigneeListFragment.ARG_GROUP_ID, mGroup.id);
        assigneeFragmentArgs.putBoolean(TransferAssigneeListFragment.ARG_USE_HORIZONTAL_VIEW, false);

        mColorActive = ContextCompat.getColor(this, AppUtils.getReference(this, R.attr.colorError));
        mColorNormal = ContextCompat.getColor(this, AppUtils.getReference(this, R.attr.colorAccent));
        mProgressBar = findViewById(R.id.progressBar);
        mProgressTextCurrent = findViewById(R.id.text1);
        mProgressTextTotal = findViewById(R.id.text2);
        mActionButton = findViewById(R.id.content_fab);
        mLayoutStatusContainer = findViewById(R.id.layoutStatusContainer);

        TransferAssigneeListFragment assigneeListFragment = (TransferAssigneeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.assigneeListFragment);

        if (assigneeListFragment == null) {
            assigneeListFragment = (TransferAssigneeListFragment) getSupportFragmentManager().getFragmentFactory()
                    .instantiate(this.getClassLoader(), TransferAssigneeListFragment.class.getName());
            assigneeListFragment.setArguments(assigneeFragmentArgs);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.add(R.id.assigneeListFragment, assigneeListFragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home || id == R.id.actions_add_devices_done) {
            interruptAllTasks(true);
            finish();
        } else if (id == R.id.actions_add_devices_help) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.text_help)
                    .setMessage(R.string.text_addDeviceHelp)
                    .setPositiveButton(R.string.butn_close, null)
                    .show();
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.actions_add_devices, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == android.app.Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHOOSE_DEVICE && data != null
                    && data.hasExtra(AddDeviceActivity.EXTRA_DEVICE)
                    && data.hasExtra(AddDeviceActivity.EXTRA_CONNECTION)) {
                Device device = data.getParcelableExtra(AddDeviceActivity.EXTRA_DEVICE);
                DeviceConnection connection = data.getParcelableExtra(AddDeviceActivity.EXTRA_CONNECTION);

                if (device != null && connection != null)
                    runUiTask(new AddDeviceTask(mGroup, device, connection));
            }
        }
    }

    @Override
    protected void onAttachTasks(List<BaseAttachableBgTask> taskList)
    {
        super.onAttachTasks(taskList);

        boolean hasOngoing = false;
        for (BaseAttachableBgTask task : taskList)
            if (task instanceof AddDeviceTask) {
                ((AddDeviceTask) task).setAnchor(this);
                hasOngoing = true;
            }

        if (!hasOngoing)
            setNowAdding(false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mFilter);

        if (!checkGroupIntegrity())
            finish();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onTaskStateChanged(BaseAttachableBgTask task)
    {
        if (task instanceof AddDeviceTask) {
            if (task.isFinished())
                setNowAdding(false);
            else {
                int progress = task.progress().getCurrent();
                int total = task.progress().getTotal();

                runOnUiThread(() -> {
                    mProgressTextCurrent.setText(String.valueOf(progress));
                    mProgressTextTotal.setText(String.valueOf(total));
                });

                mProgressBar.setProgress(progress);
                mProgressBar.setMax(total);
            }
        }
    }

    @Override
    public boolean onTaskMessage(TaskMessage message)
    {
        return false;
    }

    public boolean checkGroupIntegrity()
    {
        try {
            if (getIntent() == null || !getIntent().hasExtra(EXTRA_GROUP))
                throw new Exception(getString(R.string.text_empty));

            if (mGroup == null)
                mGroup = getIntent().getParcelableExtra(EXTRA_GROUP);

            try {
                if (mGroup == null)
                    throw new Exception();

                getDatabase().reconstruct(mGroup);
            } catch (Exception e) {
                throw new Exception(getString(R.string.mesg_notValidTransfer));
            }

            return true;
        } catch (Exception e) {
            Toast.makeText(AddDevicesToTransferActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

        return false;
    }

    public Snackbar createSnackbar(final int resId, final Object... objects)
    {
        return Snackbar.make(findViewById(R.id.container), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

    public void setNowAdding(boolean adding)
    {
        mLayoutStatusContainer.setVisibility(adding ? View.VISIBLE : View.GONE);
        mActionButton.setIconResource(adding ? R.drawable.ic_close_white_24dp : R.drawable.ic_add_white_24dp);
        mActionButton.setText(adding ? R.string.butn_cancel : R.string.butn_addMore);
        mActionButton.setBackgroundTintList(ColorStateList.valueOf(adding ? mColorActive : mColorNormal));
        mActionButton.setOnClickListener(v -> {
            if (adding)
                interruptAllTasks(true);
            else
                startConnectionManagerActivity();
        });
    }

    private void startConnectionManagerActivity()
    {
        startActivityForResult(new Intent(this, AddDeviceActivity.class), REQUEST_CODE_CHOOSE_DEVICE);
    }
}

