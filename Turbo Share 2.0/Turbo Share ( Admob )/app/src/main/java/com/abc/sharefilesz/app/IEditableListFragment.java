

package com.abc.sharefilesz.app;

import com.abc.sharefilesz.object.Editable;
import com.abc.sharefilesz.view.EditableListFragmentViewBase;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;

public interface IEditableListFragment<T extends Editable, V extends RecyclerViewAdapter.ViewHolder>
        extends EditableListFragmentBase<T>, EditableListFragmentViewBase<V>
{
    boolean performLayoutClickOpen(V holder);

    boolean performLayoutClickOpen(V holder, T object);

    boolean performDefaultLayoutClick(V holder, T object);

    boolean performDefaultLayoutLongClick(V holder, T object);
}
