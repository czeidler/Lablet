package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

abstract class ScriptComponentItem {
    protected ScriptComponentItemContainer container = null;
    private int state = ScriptComponent.SCRIPT_STATE_ONGOING;
    protected String lastErrorMessage = "";

    abstract public boolean initCheck();
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setContainer(ScriptComponentItemContainer container) {
        this.container = container;
    }

    // state < 0 means item is not done yet
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        if (container != null)
            container.onItemStateChanged(this, state);
    }

    public void toBundle(Bundle bundle) {
        bundle.putInt("state", state);
    }

    public boolean fromBundle(Bundle bundle) {
        if (!bundle.containsKey("state"))
            return false;
        state = bundle.getInt("state");
        return true;
    }
}

class ScriptComponentItemContainer<ItemType extends ScriptComponentItem> {
    private List<ItemType> items = new ArrayList<ItemType>();
    private IItemContainerListener listener = null;
    private boolean allItemsDone = false;
    private String lastErrorMessage = "";

    public interface IItemContainerListener {
        public void onAllItemStatusChanged(boolean allDone);
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

    public void onItemStateChanged(ScriptComponentItem item, int state) {
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
        item.setContainer(this);

        onItemStateChanged(item, item.getState());
    }

    public List<ItemType> getItems() {
        return items;
    }

    public boolean allItemsDone() {
        return allItemsDone;
    }

    private boolean calculateAllItemsDone() {
        for (ScriptComponentItem item : items) {
            if (item.getState() < 0)
                return false;
        }
        return true;
    }

    public void toBundle(Bundle bundle) {
        int i = 0;
        for (ScriptComponentItem item : items) {
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
        for (ScriptComponentItem item : items) {
            String key = "child";
            key += i;

            Bundle childBundle = bundle.getBundle(key);
            if (childBundle == null)
                return false;
            if (!item.fromBundle(childBundle))
                return false;

            i++;
        }
        return true;
    }
}

abstract class ScriptComponentItemViewHolder extends ScriptComponentItem {
    abstract public View createView(Context context);
}
