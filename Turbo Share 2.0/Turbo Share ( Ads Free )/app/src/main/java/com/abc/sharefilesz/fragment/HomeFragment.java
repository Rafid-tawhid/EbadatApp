

package com.abc.sharefilesz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.abc.sharefilesz.adapter.SmartFragmentPagerAdapter;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.app.Activity;
import com.abc.sharefilesz.ui.callback.TitleProvider;
import com.genonbeta.android.framework.app.Fragment;
import com.genonbeta.android.framework.ui.callback.SnackbarPlacementProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment implements SnackbarPlacementProvider, TitleProvider,
        Activity.OnBackPressedListener
{
    private ViewPager mViewPager;
    private SmartFragmentPagerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        final BottomNavigationView bottomNavigationView = view.findViewById(R.id.layout_home_bottom_navigation_view);
        mViewPager = view.findViewById(R.id.layout_home_view_pager);
        mAdapter = new SmartFragmentPagerAdapter(getContext(), getChildFragmentManager());

        mAdapter.add(new SmartFragmentPagerAdapter.StableItem(0, TransferGroupListFragment.class,
                null));
        mAdapter.add(new SmartFragmentPagerAdapter.StableItem(1, ActiveConnectionListFragment.class,
                null));
       // mAdapter.add(new SmartFragmentPagerAdapter.StableItem(2, FileExplorerFragment.class, null));
      //  mAdapter.add(new SmartFragmentPagerAdapter.StableItem(3, TextStreamListFragment.class, null));

        mAdapter.createTabs(bottomNavigationView);
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i1)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                bottomNavigationView.setSelectedItemId(i);
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            mViewPager.setCurrentItem(menuItem.getOrder());
            return true;
        });

        return view;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_home);
    }

    @Override
    public boolean onBackPressed()
    {
        Object activeItem = mAdapter.getItem(mViewPager.getCurrentItem());

        if ((activeItem instanceof Activity.OnBackPressedListener
                && ((Activity.OnBackPressedListener) activeItem).onBackPressed()))
            return true;

        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(0, true);
            return true;
        }

        return false;
    }
}
