/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


/**
 * Manages a set of script components.
 *
 * ScriptComponentContainer listen to all its sub components for state changes. If the states of all sub components
 * changed from or to >= 0 an installed IItemContainerListener is notified.
 *
 * @param <ItemType> derived class from {@link ScriptComponent}
 */
public class ScriptComponentContainer<ItemType extends ScriptComponent>
        implements ScriptComponent.IScriptComponentListener {
    private List<ItemType> items = new ArrayList<>();
    private IItemContainerListener listener = null;
    private boolean allItemsDone = false;
    private String lastErrorMessage = "";

    /**
     * Listener interface for the script component container.
     */
    public interface IItemContainerListener {
        void onAllItemStatusChanged(boolean allDone);
    }

    public boolean initCheck() {
        for (ItemType item : items) {
            if (!item.initCheck()) {
                lastErrorMessage = item.getLastErrorMessage();
                return false;
            }
        }
        return true;
    }

    public void setListener(IItemContainerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStateChanged(ScriptComponent item, int state) {
        boolean allItemsWereDone = allItemsDone;

        if (state < 0)
            allItemsDone = false;
        else if (!allItemsWereDone)
            allItemsDone = calculateAllItemsDone();

        if (listener == null)
            return;
        if (allItemsDone != allItemsWereDone)
            listener.onAllItemStatusChanged(allItemsDone);
    }

    public void addItem(ItemType item) {
        items.add(item);
        item.setListener(this);

        onStateChanged(item, item.getState());
    }

    public List<ItemType> getItems() {
        return items;
    }

    private boolean calculateAllItemsDone() {
        for (ScriptComponent item : items) {
            if (item.getState() < 0)
                return false;
        }
        return true;
    }

    public void toBundle(Bundle bundle) {
        int i = 0;
        for (ScriptComponent item : items) {
            Bundle childBundle = new Bundle();
            item.toBundle(childBundle);
            String key = "child";
            key += i;

            bundle.putBundle(key, childBundle);
            i++;
        }
    }

    public boolean fromBundle(Bundle bundle) {
        int i = 0;
        for (ScriptComponent item : items) {
            String key = "child";
            key += i;

            Bundle childBundle = bundle.getBundle(key);
            if (childBundle == null)
                return false;
            if (!item.fromBundle(childBundle))
                return false;

            i++;
        }
        allItemsDone = calculateAllItemsDone();
        return true;
    }
}

