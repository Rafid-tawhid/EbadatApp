

package com.abc.sharefilesz.app;

import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;

import com.abc.sharefilesz.object.Editable;
import com.abc.sharefilesz.widget.EditableListAdapterBase;
import com.abc.sharefilesz.ui.callback.TitleProvider;
import com.genonbeta.android.framework.app.ListFragmentBase;
import com.genonbeta.android.framework.util.actionperformer.IEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;
import com.genonbeta.android.framework.util.actionperformer.SelectableHost;
import com.genonbeta.android.framework.util.actionperformer.SelectableProvider;


public interface EditableListFragmentBase<T extends Editable> extends ListFragmentBase<T>, PerformerEngineProvider,
        IEngineConnection.SelectionListener<T>, SelectableProvider<T>, SelectableHost<T>, TitleProvider
{
    void applyViewingChanges(int gridSize);

    void changeGridViewSize(int gridSize);

    void changeOrderingCriteria(int id);

    void changeSortingCriteria(int id);

    EditableListAdapterBase<T> getAdapterImpl();

    IEngineConnection<T> getEngineConnection();

    EditableListFragment.FilteringDelegate<T> getFilteringDelegate();

    RecyclerView getListView();

    int getOrderingCriteria();

    int getSortingCriteria();

    String getUniqueSettingKey(String setting);

    boolean isGridSupported();

    boolean isLocalSelectionActivated();

    boolean isRefreshRequested();

    boolean isSortingSupported();

    boolean isUsingLocalSelection();

    boolean loadIfRequested();

    boolean openUri(Uri uri);

    void setFilteringDelegate(EditableListFragment.FilteringDelegate<T> delegate);
}
