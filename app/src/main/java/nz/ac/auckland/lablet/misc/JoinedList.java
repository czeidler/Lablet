/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JoinedList<T> implements Iterable<T> {
    private List<List<? extends T>> allLists = new ArrayList<>();

    public void addList(List<? extends T> list) {
        allLists.add(list);
    }

    public JoinedList(List<? extends T>... lists) {
        for (List<? extends T> list : lists)
            addList(list);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<List<? extends T>> allListIterator = allLists.iterator();
            Iterator<? extends T> currentListIterator = null;

            {
                advanceToNextNoneEmptyList();
            }

            private void advanceToNextNoneEmptyList() {
                while (allListIterator.hasNext()) {
                    currentListIterator = allListIterator.next().iterator();
                    if (currentListIterator.hasNext())
                        break;
                }
            }

            @Override
            public boolean hasNext() {
                return currentListIterator != null && currentListIterator.hasNext();
            }

            @Override
            public T next() {
                T next = currentListIterator.next();
                if (!currentListIterator.hasNext())
                    advanceToNextNoneEmptyList();
                return next;
            }

            @Override
            public void remove() {

            }
        };
    }
}
