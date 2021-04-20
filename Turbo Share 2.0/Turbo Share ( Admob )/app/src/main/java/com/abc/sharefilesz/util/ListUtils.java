

package com.abc.sharefilesz.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils
{
    @SuppressWarnings("unchecked")
    public static <E, T extends E> List<T> typedListOf(List<E> list, Class<T> klass)
    {
        List<T> typedList = new ArrayList<>();
        for (E item : list)
            if (klass.isInstance(item))
                typedList.add((T) item);
        return typedList;
    }
}
