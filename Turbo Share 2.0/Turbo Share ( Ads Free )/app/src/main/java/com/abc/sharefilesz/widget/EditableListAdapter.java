

package com.abc.sharefilesz.widget;

import android.text.format.DateUtils;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abc.sharefilesz.object.Editable;
import com.abc.sharefilesz.app.IEditableListFragment;
import com.abc.sharefilesz.util.FileUtils;
import com.abc.sharefilesz.util.TextUtils;
import com.genonbeta.android.framework.util.MathUtils;
import com.genonbeta.android.framework.widget.RecyclerViewAdapter;
import com.genonbeta.android.framework.widget.recyclerview.fastscroll.SectionTitleProvider;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/**

 * date: 12.01.2018 16:55
 */

public abstract class EditableListAdapter<T extends Editable, V extends RecyclerViewAdapter.ViewHolder>
        extends RecyclerViewAdapter<T, V> implements EditableListAdapterBase<T>, SectionTitleProvider
{
    public static final int VIEW_TYPE_DEFAULT = 0;

    public static final int MODE_SORT_BY_NAME = 100;
    public static final int MODE_SORT_BY_DATE = 110;
    public static final int MODE_SORT_BY_SIZE = 120;

    public static final int MODE_SORT_ORDER_ASCENDING = 100;
    public static final int MODE_SORT_ORDER_DESCENDING = 110;

    private IEditableListFragment<T, V> mFragment;
    private Collator mCollator;
    private final List<T> mItemList = new ArrayList<>();
    private int mSortingCriteria = MODE_SORT_BY_NAME;
    private int mSortingOrderAscending = MODE_SORT_ORDER_ASCENDING;
    private boolean mGridLayoutRequested = false;

    public EditableListAdapter(IEditableListFragment<T, V> fragment)
    {
        super(fragment.getContext());
        setHasStableIds(true);
        setFragment(fragment);
    }

    @Override
    public void onUpdate(List<T> passedItem)
    {
        synchronized (mItemList) {
            mItemList.clear();
            mItemList.addAll(passedItem);

            syncSelectionList();
        }
    }

    @Override
    public int compare(T compare, T compareTo)
    {
        boolean sortingAscending = getSortingOrder(compare, compareTo) == MODE_SORT_ORDER_ASCENDING;

        T obj1 = sortingAscending ? compare : compareTo;
        T obj2 = sortingAscending ? compareTo : compare;

        if (obj1.comparisonSupported() == obj2.comparisonSupported() && !obj1.comparisonSupported())
            return 0;
        else if (!compare.comparisonSupported())
            return 1;
        else if (!compareTo.comparisonSupported())
            return -1;

        return compareItems(getSortingCriteria(compare, compareTo), getSortingOrder(), obj1, obj2);
    }

    public int compareItems(int sortingCriteria, int sortingOrder, T obj1, T obj2)
    {
        switch (sortingCriteria) {
            case MODE_SORT_BY_DATE:
                return MathUtils.compare(obj1.getComparableDate(), obj2.getComparableDate());
            case MODE_SORT_BY_SIZE:
                return MathUtils.compare(obj1.getComparableSize(), obj2.getComparableSize());
            case MODE_SORT_BY_NAME:
                return getDefaultCollator().compare(obj1.getComparableName(), obj2.getComparableName());
        }

        throw new IllegalStateException("Asked for " + sortingCriteria + " which isn't known.");
    }

    public boolean filterItem(T item)
    {
        String[] filteringKeywords = getFragment().getFilteringDelegate()
                .getFilteringKeyword(getFragment());

        return filteringKeywords == null || filteringKeywords.length <= 0 || item.applyFilter(filteringKeywords);
    }

    public boolean isGridLayoutRequested()
    {
        return mGridLayoutRequested;
    }

    @Override
    public int getCount()
    {
        return getList().size();
    }

    public Collator getDefaultCollator()
    {
        if (mCollator == null) {
            mCollator = Collator.getInstance();
            mCollator.setStrength(Collator.TERTIARY);
        }

        return mCollator;
    }

    public IEditableListFragment<T, V> getFragment()
    {
        return mFragment;
    }

    @Override
    public int getItemCount()
    {
        return getCount();
    }

    public T getItem(int position)
    {
        synchronized (mItemList) {
            return mItemList.get(position);
        }
    }

    public T getItem(V holder)
    {
        int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION)
            throw new IllegalStateException();
        return getItem(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position)
    {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public List<T> getList()
    {
        return mItemList;
    }

    @Override
    public List<T> getSelectableList()
    {
        return getList();
    }

    @NonNull
    public String getSectionName(int position, T object)
    {
        switch (getSortingCriteria()) {
            case MODE_SORT_BY_NAME:
                return getSectionNameTrimmedText(object.getComparableName());
            case MODE_SORT_BY_DATE:
                return getSectionNameDate(object.getComparableDate());
            case MODE_SORT_BY_SIZE:
                return FileUtils.sizeExpression(object.getComparableSize(), false);
        }

        return String.valueOf(position);
    }

    public String getSectionNameDate(long date)
    {
        return String.valueOf(DateUtils.formatDateTime(getContext(), date, DateUtils.FORMAT_SHOW_DATE));
    }

    public String getSectionNameTrimmedText(String text)
    {
        return TextUtils.trimText(text, 1).toUpperCase();
    }

    @Override
    public String getSectionTitle(int position)
    {
        return getSectionName(position, getItem(position));
    }

    public int getSortingCriteria(T objectOne, T objectTwo)
    {
        return getSortingCriteria();
    }

    public int getSortingCriteria()
    {
        return mSortingCriteria;
    }

    public int getSortingOrder(T objectOne, T objectTwo)
    {
        return getSortingOrder();
    }

    public int getSortingOrder()
    {
        return mSortingOrderAscending;
    }

    public void notifyGridSizeUpdate(int gridSize, boolean isScreenLarge)
    {
        mGridLayoutRequested = (!isScreenLarge && gridSize > 1) || gridSize > 2;
    }

    public void setFragment(IEditableListFragment<T, V> fragmentImpl)
    {
        mFragment = fragmentImpl;
    }

    public void setSortingCriteria(int sortingCriteria, int sortingOrder)
    {
        mSortingCriteria = sortingCriteria;
        mSortingOrderAscending = sortingOrder;
    }

    @Override
    public void syncAndNotify(int adapterPosition)
    {
        syncSelection(adapterPosition);
        notifyItemChanged(adapterPosition);
    }

    @Override
    public void syncAllAndNotify()
    {
        syncSelectionList();
        notifyDataSetChanged();
    }

    public synchronized void syncSelection(int adapterPosition)
    {
        T item = getItem(adapterPosition);
        item.setSelectableSelected(mFragment.getEngineConnection().isSelectedOnHost(item));
    }

    public synchronized void syncSelectionList()
    {
        List<T> itemList = new ArrayList<>(getList());
        for (T item : itemList)
            item.setSelectableSelected(mFragment.getEngineConnection().isSelectedOnHost(item));
    }
}
