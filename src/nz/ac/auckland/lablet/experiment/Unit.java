/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import nz.ac.auckland.lablet.misc.WeakListenable;


public class Unit extends WeakListenable<Unit.IListener> {
    public interface IListener {
        public void onPrefixChanged();
    }

    final private String base;
    private String prefix = "";

    public Unit(String base) {
        this.base = base;
    }

    public String getBase() {
        return base;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        notifyPrefixChanged();
    }

    public String getUnit() {
        return getPrefix() + getBase();
    }

    private void notifyPrefixChanged() {
        for (IListener listener : getListeners())
            listener.onPrefixChanged();
    }
}
