/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.test;

import android.test.suitebuilder.annotation.SmallTest;
import junit.framework.TestCase;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.lang.ref.WeakReference;


public class WeakListenerTest extends TestCase {
    interface IListener {
        void onEvent();
    }

    class DataClass extends WeakListenable<IListener> {

    }

    class ListenerClass {
        IListener listener = new IListener() {
            @Override
            public void onEvent() {

            }
        };

        public ListenerClass(DataClass data) {
            data.addListener(listener);
        }
    }

    @SmallTest
    public void test() {
        DataClass data = new DataClass();

        ListenerClass listener = new ListenerClass(data);
        ListenerClass listener2 = new ListenerClass(data);

        assertEquals(2, data.getListeners().size());

        // check if de-referring a listener makes it disappear from the listener list
        listener = null;
        System.gc();
        assertEquals(1, data.getListeners().size());

        // check if the remaining listener prevents the data class from being deleted
        WeakReference<DataClass> weakReference = new WeakReference<>(data);
        data = null;
        System.gc();
        assertEquals(null, weakReference.get());
    }
}
