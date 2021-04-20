

package com.abc.sharefilesz.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.abc.sharefilesz.adapter.SmartFragmentPagerAdapter;
import com.abc.sharefilesz.fragment.ApplicationListFragment;
import com.abc.sharefilesz.fragment.AudioListFragment;
import com.abc.sharefilesz.fragment.FileExplorerFragment;
import com.abc.sharefilesz.fragment.ImageListFragment;
import com.abc.sharefilesz.fragment.VideoListFragment;
import com.abc.sharefilesz.widget.EditableListAdapterBase;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.app.EditableListFragmentBase;
import com.abc.sharefilesz.fragment.*;
import com.abc.sharefilesz.ui.callback.SharingPerformerMenuCallback;
import com.abc.sharefilesz.util.SelectionUtils;
import com.genonbeta.android.framework.ui.PerformerMenu;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;
import com.google.android.material.tabs.TabLayout;

/**

 * date: 13/04/18 19:45
 */
public class ContentSharingActivity extends Activity implements PerformerEngineProvider
{
    public static final String TAG = ContentSharingActivity.class.getSimpleName();

    private Activity.OnBackPressedListener mBackPressedListener;
    private PerformerEngine mPerformerEngine = new PerformerEngine();
    private SharingPerformerMenuCallback mMenuCallback = new SharingPerformerMenuCallback(this, this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_sharing);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

	    //todo: The menu view has an arbitrary position. Can this be fixed by using what Snackbar does?
        ActionMenuView menuView = findViewById(R.id.menu_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        PerformerMenu performerMenu = new PerformerMenu(this, mMenuCallback);

        mMenuCallback.setCancellable(false);
        performerMenu.load(menuView.getMenu());
        performerMenu.setUp(mPerformerEngine);

        final TabLayout tabLayout = findViewById(R.id.activity_content_sharing_tab_layout);
        final ViewPager viewPager = findViewById(R.id.activity_content_sharing_view_pager);

        final SmartFragmentPagerAdapter pagerAdapter = new SmartFragmentPagerAdapter(this,
                getSupportFragmentManager())
        {
            @Override
            public void onItemInstantiated(StableItem item)
            {
                Fragment fragment = item.getInitiatedItem();

                if (fragment instanceof EditableListFragmentBase<?>) {
                    EditableListFragmentBase<?> fragmentImpl = (EditableListFragmentBase<?>) fragment;

                    if (viewPager.getCurrentItem() == item.getCurrentPosition())
                        attachListeners(fragmentImpl);
                }
            }
        };

        Bundle arguments = new Bundle();
        arguments.putBoolean(FileExplorerFragment.ARG_SELECT_BY_CLICK, true);
        arguments.putBoolean(FileExplorerFragment.ARG_HAS_BOTTOM_SPACE, true);

        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(0, ApplicationListFragment.class, arguments));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(1, FileExplorerFragment.class, arguments).setTitle(getString(
                R.string.text_files)));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(2, AudioListFragment.class, arguments));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(3, ImageListFragment.class, arguments));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(4, VideoListFragment.class, arguments));

        pagerAdapter.createTabs(tabLayout, false, true);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
                Fragment fragment = pagerAdapter.getItem(tab.getPosition());

                if (fragment instanceof EditableListFragmentBase<?>) {
                    EditableListFragmentBase<?> editableListFragment = (EditableListFragmentBase<?>) fragment;
                    EditableListAdapterBase<?> adapter = editableListFragment.getAdapterImpl();

                    attachListeners(editableListFragment);

                    if (editableListFragment.getAdapterImpl() != null)
                        new Handler(Looper.getMainLooper()).postDelayed(adapter::syncAllAndNotify, 200);
                }
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });
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
        if (mBackPressedListener == null || !mBackPressedListener.onBackPressed()) {
            if (SelectionUtils.getTotalSize(mPerformerEngine) > 0) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.ques_cancelSelection)
                        .setNegativeButton(R.string.butn_no, null)
                        .setPositiveButton(R.string.butn_yes, (dialog, which) -> finish())
                        .show();
            } else
                super.onBackPressed();
        }
    }

    public void attachListeners(EditableListFragmentBase<?> fragment)
    {
        mMenuCallback.setForegroundConnection(fragment.getEngineConnection());
        mBackPressedListener = fragment instanceof OnBackPressedListener ? (OnBackPressedListener) fragment : null;
    }

    @Nullable
    @Override
    public IPerformerEngine getPerformerEngine()
    {
        return mPerformerEngine;
    }
}
