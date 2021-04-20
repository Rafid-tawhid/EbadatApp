

package com.abc.sharefilesz.object;

import androidx.annotation.Nullable;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;

import java.util.ArrayList;
import java.util.List;

public class MappedSelectable<T extends Selectable> implements Selectable
{
    public IEngineConnection<T> engineConnection;
    public T selectable;

    public MappedSelectable(T selectable, IEngineConnection<T> engineConnection)
    {
        this.selectable = selectable;
        this.engineConnection = engineConnection;
    }

    private static <T extends Selectable> void addToMappedObjectList(List<MappedSelectable<?>> list,
                                                                     IEngineConnection<T> connection)
    {
        for (T selectable : connection.getSelectedItemList())
            list.add(new MappedSelectable<>(selectable, connection));
    }

    public static List<MappedSelectable<?>> compileFrom(@Nullable IPerformerEngine engine)
    {
        List<MappedSelectable<?>> list = new ArrayList<>();

        if (engine != null)
            for (IBaseEngineConnection baseEngineConnection : engine.getConnectionList())
                if (baseEngineConnection instanceof IEngineConnection<?>)
                    addToMappedObjectList(list, (IEngineConnection<?>) baseEngineConnection);

        return list;
    }

    @Override
    public String getSelectableTitle()
    {
        return selectable.getSelectableTitle();
    }

    @Override
    public boolean isSelectableSelected()
    {
        return selectable.isSelectableSelected();
    }

    @Override
    public boolean setSelectableSelected(boolean selected)
    {
        return engineConnection.setSelected(selectable, selected);
    }
}