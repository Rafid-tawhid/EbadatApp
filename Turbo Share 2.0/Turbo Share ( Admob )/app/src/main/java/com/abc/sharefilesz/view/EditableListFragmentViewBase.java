

package com.abc.sharefilesz.view;

import com.abc.sharefilesz.app.EditableListFragment;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;

/**

 * date: 8/24/18 1:36 PM
 */
public interface EditableListFragmentViewBase<V extends RecyclerViewAdapter.ViewHolder>
{
    boolean performLayoutClick(V holder);

    boolean performLayoutLongClick(V holder);

    void registerLayoutViewClicks(final V holder);

    boolean setItemSelected(V holder);

    boolean setItemSelected(V holder, boolean force);

    void setLayoutClickListener(EditableListFragment.LayoutClickListener<V> clickListener);
}
