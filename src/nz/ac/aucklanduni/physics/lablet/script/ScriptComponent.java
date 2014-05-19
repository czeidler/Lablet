/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.script;

import android.os.Bundle;

import java.lang.ref.WeakReference;


/**
 * Abstract base class for component in a script. A component has a listener interface a state and can be saved/loaded
 * to/from a {@link Bundle}.
 *
 * A state value smaller zero means the script component is not done. All values greater zero means the component has
 * been handled, e.g. all questions in a component have been answered.
 */
abstract public class ScriptComponent {
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    public interface IScriptComponentListener {
        public void onStateChanged(ScriptComponent item, int state);
    }

    private WeakReference<IScriptComponentListener> listener = null;
    private int state = SCRIPT_STATE_ONGOING;
    protected String lastErrorMessage = "";

    /**
     * Checks if the component has all information to operate correctly, i.e., everything is setup correctly in the
     * script.
     * @return true if status is ok.
     */
    abstract public boolean initCheck();
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setListener(IScriptComponentListener listener) {
        this.listener = new WeakReference<IScriptComponentListener>(listener);
    }

    public int getState() {
        return state;
    }

    /**
     * Set the state of the component and notifies potential listeners.
     * @param state new state of the component
     */
    public void setState(int state) {
        this.state = state;
        if (listener != null) {
            IScriptComponentListener listenerHard = listener.get();
            if (listenerHard != null)
                listenerHard.onStateChanged(this, state);
        }
    }

    /**
     * Save the complete state of a component to a bundle (not only the state value). A classes that override this
     * method has to call the super method.
     * @param bundle to store the component state in
     */
    public void toBundle(Bundle bundle) {
        bundle.putInt("state", state);
    }

    /**
     * Restore a component from a bundle. A classes that override this method has to call the super method.
     *
     * @param bundle that contains the component state
     * @return true if the state was restored
     */
    public boolean fromBundle(Bundle bundle) {
        if (!bundle.containsKey("state"))
            return false;
        state = bundle.getInt("state");
        return true;
    }
}
