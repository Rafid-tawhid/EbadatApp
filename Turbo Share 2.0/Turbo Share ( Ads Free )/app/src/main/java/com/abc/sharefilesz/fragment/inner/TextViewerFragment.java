

package com.abc.sharefilesz.fragment.inner;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.abc.sharefilesz.R;
import com.abc.sharefilesz.ui.callback.IconProvider;
import com.abc.sharefilesz.ui.callback.TitleProvider;
import com.genonbeta.android.framework.app.Fragment;

/**

 * date: 9/4/18 12:03 AM
 */
public class TextViewerFragment extends Fragment implements IconProvider, TitleProvider
{
    private TextView mMainText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.layout_text_viewer, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_text_viewer_fragment, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mMainText = view.findViewById(R.id.layout_text_viewer_text);
    }

    @DrawableRes
    @Override
    public int getIconRes()
    {
        return R.drawable.ic_forum_white_24dp;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_shareTextShort);
    }
}
