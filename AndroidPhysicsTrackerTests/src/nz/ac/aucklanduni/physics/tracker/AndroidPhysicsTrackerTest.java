package nz.ac.aucklanduni.physics.tracker;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class nz.ac.aucklanduni.physics.tracker.AndroidPhysicsTrackerTest \
 * nz.ac.aucklanduni.physics.tracker.tests/android.test.InstrumentationTestRunner
 */
public class AndroidPhysicsTrackerTest extends ActivityInstrumentationTestCase2<ExperimentHomeActivity> {

    public AndroidPhysicsTrackerTest() {
        super("nz.ac.aucklanduni.physics.tracker", ExperimentHomeActivity.class);
    }

}
