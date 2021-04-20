

package com.abc.sharefilesz.widget;

import com.abc.sharefilesz.object.Editable;
import com.genonbeta.android.framework.util.actionperformer.SelectableProvider;
import com.genonbeta.android.framework.widget.ListAdapterBase;

import java.util.Comparator;

/**

 * date: 14/04/18 00:51
 */
public interface EditableListAdapterBase<T extends Editable> extends ListAdapterBase<T>, SelectableProvider<T>,
        Comparator<T>
{
    boolean filterItem(T item);

    T getItem(int position);

    void syncAndNotify(int adapterPosition);

    void syncAllAndNotify();
}
