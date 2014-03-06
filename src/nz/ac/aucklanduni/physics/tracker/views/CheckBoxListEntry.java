/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views;


public class CheckBoxListEntry {
    private boolean selected = false;
    private String name;

    public interface OnCheckBoxListEntryListener {
        public void onSelected(CheckBoxListEntry entry);
    }
    static private OnCheckBoxListEntryListener listener = null;
    public static void setListener(OnCheckBoxListEntryListener l) {
        listener = l;
    }

    public CheckBoxListEntry(String name) {
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (listener != null)
            listener.onSelected(this);
    }

    public boolean getSelected() {
        return selected;
    }
}
