package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class ScriptComponentItem {
    protected ScriptComponentItemContainer container = null;
    private int state = ScriptComponent.SCRIPT_STATE_ONGOING;

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
}

class ScriptComponentItemContainer<ItemType extends ScriptComponentItem> {
    private List<ItemType> items = new ArrayList<ItemType>();
    private IItemContainerListener listener = null;
    private boolean allItemsDone = false;

    public interface IItemContainerListener {
        public void onAllItemStatusChanged(boolean allDone);
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
}

abstract class ScriptComponentItemViewHolder extends ScriptComponentItem {
    abstract public View createView(Context context);
}
