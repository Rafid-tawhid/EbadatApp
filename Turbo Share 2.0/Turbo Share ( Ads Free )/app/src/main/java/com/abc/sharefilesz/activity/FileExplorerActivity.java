

package com.abc.sharefilesz.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.fragment.FileExplorerFragment;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.framework.io.DocumentFile;

import java.io.FileNotFoundException;

public class FileExplorerActivity extends Activity
{
    public static final String EXTRA_FILE_PATH = "filePath";

    private FileExplorerFragment mFragmentFileExplorer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragmentFileExplorer = (FileExplorerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_file_explorer_fragment_files);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        checkRequestedPath(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (!mFragmentFileExplorer.onBackPressed())
            super.onBackPressed();
    }

    public void checkRequestedPath(Intent intent)
    {
        if (intent == null)
            return;

        if (intent.hasExtra(EXTRA_FILE_PATH)) {
            Uri directoryUri = intent.getParcelableExtra(EXTRA_FILE_PATH);

            try {
                openFolder(FileUtils.fromUri(getApplicationContext(), directoryUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else
            openFolder(null);
    }

    private void openFolder(@Nullable DocumentFile requestedFolder)
    {
        if (requestedFolder != null)
            mFragmentFileExplorer.requestPath(requestedFolder);
    }
}
