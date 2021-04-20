

package com.abc.sharefilesz.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.abc.sharefilesz.adapter.FileListAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.fragment.FileExplorerFragment;
import com.abc.sharefilesz.util.FileUtils;
import com.genonbeta.android.framework.io.DocumentFile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**

 * Date: 5/29/17 3:18 PM
 */

public class FilePickerActivity extends Activity
{
    public static final String ACTION_CHOOSE_DIRECTORY = "com.genonbeta.intent.action.CHOOSE_DIRECTORY";
    public static final String ACTION_CHOOSE_FILE = "com.genonbeta.intent.action.CHOOSE_FILE";

    public static final String EXTRA_ACTIVITY_TITLE = "activityTitle";
    public static final String EXTRA_START_PATH = "startPath";
    // belongs to returned result intent
    public static final String EXTRA_CHOSEN_PATH = "chosenPath";

    private FileExplorerFragment mFileExplorerFragment;
    private FloatingActionButton mFAB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        mFileExplorerFragment = (FileExplorerFragment) getSupportFragmentManager().findFragmentById(
                R.id.activitiy_filepicker_fragment_files);
        mFAB = findViewById(R.id.content_fab);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (getIntent() != null) {
            boolean hasTitlesDefined = false;

            if (getIntent() != null && getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if (hasTitlesDefined = getIntent().hasExtra(EXTRA_ACTIVITY_TITLE))
                    getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_ACTIVITY_TITLE));

            }

            if (ACTION_CHOOSE_DIRECTORY.equals(getIntent().getAction())) {
                if (getSupportActionBar() != null) {
                    if (!hasTitlesDefined)
                        getSupportActionBar().setTitle(R.string.text_chooseFolder);
                    else
                        getSupportActionBar().setSubtitle(R.string.text_chooseFolder);
                }

                mFileExplorerFragment.getAdapter()
                        .setConfiguration(true, false, null);
                mFileExplorerFragment.refreshList();

                RecyclerView recyclerView = mFileExplorerFragment.getListView();
                recyclerView.setPadding(0, 0, 0, 200);
                recyclerView.setClipToPadding(false);

                mFAB.show();
                mFAB.setOnClickListener(v -> {
                    DocumentFile selectedPath = mFileExplorerFragment.getAdapter().getPath();

                    if (selectedPath != null && selectedPath.canWrite())
                        finishWithResult(selectedPath);
                    else
                        Snackbar.make(v, R.string.mesg_currentPathUnavailable, Snackbar.LENGTH_SHORT).show();
                });
            } else if (ACTION_CHOOSE_FILE.equals(getIntent().getAction())) {
                if (getSupportActionBar() != null) {
                    if (!hasTitlesDefined)
                        getSupportActionBar().setTitle(R.string.text_chooseFile);
                    else
                        getSupportActionBar().setSubtitle(R.string.text_chooseFolder);
                }

                mFileExplorerFragment.setLayoutClickListener((listFragment, holder, longClick) -> {
                    if (longClick)
                        return false;
                    FileListAdapter.FileHolder fileHolder = mFileExplorerFragment.getAdapter().getItem(holder);

                    if (fileHolder.file.isFile()) {
                        finishWithResult(fileHolder.file);
                        return true;
                    }
                    return false;
                });
            } else
                finish();

            if (!isFinishing())
                if (getIntent().hasExtra(EXTRA_START_PATH)) {
                    try {
                        mFileExplorerFragment.goPath(FileUtils.fromUri(this,
                                Uri.parse(getIntent().getStringExtra(EXTRA_START_PATH))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        } else
            finish();
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
        if (mFileExplorerFragment == null || !mFileExplorerFragment.onBackPressed())
            super.onBackPressed();
    }

    private void finishWithResult(DocumentFile file)
    {
        setResult(Activity.RESULT_OK, new Intent(ACTION_CHOOSE_DIRECTORY)
                .putExtra(EXTRA_CHOSEN_PATH, file.getUri()));
        finish();
    }
}
