/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.os.Bundle;
import junit.framework.TestCase;
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.*;
import java.util.Arrays;
import java.util.Set;

public class PersistentBundleTest extends TestCase {

    private final Bundle primitiveBundle = new Bundle();
    private final Bundle arrayBundle = new Bundle();
    private final Bundle mixedBundle = new Bundle();
    private final Bundle bundleBundle = new Bundle();

    public void setUp() throws Exception {
        super.setUp();

        String stringArray[] = new String[] {"Hello", "how", "are", "you", "?"};
        int intArray[] = new int[] {1, 2, 3, 4, 5};
        boolean boolArray[] = new boolean[] {true, false, true, false, false};
        float floatArray[] = new float[] {121.2143f, 2.132f, 3f, 4f, 5.2f};
        double doubleArray[] = new double[] {1432.324d, 2.5d, 3.7d, 4.7d, 5d};

        primitiveBundle.putString("string", stringArray[0]);
        primitiveBundle.putInt("int", intArray[0]);
        primitiveBundle.putBoolean("bool", boolArray[0]);
        primitiveBundle.putFloat("float", floatArray[0]);
        primitiveBundle.putDouble("double", doubleArray[0]);

        arrayBundle.putStringArray("stringArray", stringArray);
        arrayBundle.putIntArray("intArray", intArray);
        arrayBundle.putBooleanArray("boolArray", boolArray);
        arrayBundle.putFloatArray("floatArray", floatArray);
        arrayBundle.putDoubleArray("doubleArray", doubleArray);

        mixedBundle.putString("string", stringArray[0]);
        mixedBundle.putInt("int", intArray[0]);
        mixedBundle.putBoolean("bool", boolArray[0]);
        mixedBundle.putFloat("float", floatArray[0]);
        mixedBundle.putDouble("double", doubleArray[0]);

        mixedBundle.putStringArray("stringArray", stringArray);
        mixedBundle.putIntArray("intArray", intArray);
        mixedBundle.putBooleanArray("boolArray", boolArray);
        mixedBundle.putFloatArray("floatArray", floatArray);
        mixedBundle.putDoubleArray("doubleArray", doubleArray);

        // create subBundles
        Bundle subBundle1 = new Bundle();
        subBundle1.putString("string", stringArray[1]);
        subBundle1.putInt("int", intArray[1]);
        subBundle1.putFloatArray("floatArray", floatArray);
        subBundle1.putDoubleArray("doubleArray", doubleArray);

        Bundle subBundle2 = new Bundle();
        subBundle2.putBoolean("bool", boolArray[1]);
        subBundle2.putFloat("float", floatArray[1]);
        subBundle2.putDouble("double", doubleArray[1]);
        subBundle2.putStringArray("stringArray", stringArray);
        subBundle2.putIntArray("intArray", intArray);
        subBundle2.putBooleanArray("boolArray", boolArray);

        subBundle1.putBundle("bundle", subBundle2);
        mixedBundle.putBundle("bundle2", subBundle1);

        bundleBundle.putBundle("bundle", subBundle2);
    }

    public void testPrimitiveBundle() throws Exception {
        assertTrue(testBundle(primitiveBundle));
    }

    public void testArrayBundle() throws Exception {
        assertTrue(testBundle(arrayBundle));
    }

    public void testBundleBundle() throws Exception {
        assertTrue(testBundle(bundleBundle));
    }

    public void testMixedBundle() throws Exception {
        assertTrue(testBundle(mixedBundle));
    }

    private boolean testBundle(Bundle bundle) throws Exception {
        // pack
        StringWriter writer = new StringWriter();
        PersistentBundle packer = new PersistentBundle();
        packer.flattenBundle(bundle, writer);

        String result = writer.toString();

        // unpack
        InputStream inputStream = new ByteArrayInputStream(result.getBytes());
        PersistentBundle unPacker = new PersistentBundle();
        Bundle unpackedBundle = unPacker.unflattenBundle(inputStream);

        return compareBundles(bundle, unpackedBundle);
    }

    private boolean compareBundles(Bundle bundle1, Bundle bundle2) throws InvalidObjectException {
        if (bundle1.size() != bundle2.size())
            return false;

        final Set<String> keySet = bundle1.keySet();
        for(String key : keySet) {
            final Object value1 = bundle1.get(key);
            final Object value2 = bundle2.get(key);
            if (value1 == null || value2 == null)
                return false;
            // bundle
            if (value1 instanceof Bundle && value2 instanceof Bundle) {
                if (!compareBundles((Bundle)value1, (Bundle)value2)) {
                    return false;
                }
            }
            // array
            else if (value1.getClass().isArray() && value2.getClass().isArray()) {
                if (value1.getClass().getComponentType() == String.class) {
                    if (!Arrays.equals((String[])value1, (String[])value2))
                        return false;
                } else if (value1.getClass().getComponentType() == int.class) {
                    if (!Arrays.equals((int[])value1, (int[])value2))
                        return false;
                } else if (value1.getClass().getComponentType() == boolean.class) {
                    if (!Arrays.equals((boolean[])value1, (boolean[])value2))
                        return false;
                } else if (value1.getClass().getComponentType() == float.class) {
                    if (!Arrays.equals((float[])value1, (float[])value2))
                        return false;
                } else if (value1.getClass().getComponentType() == double.class) {
                    if (!Arrays.equals((double[])value1, (double[])value2))
                        return false;
                } else
                    throw new InvalidObjectException("Array type is not know to this test!");

            }
            // must be a normal value
            else if (!value1.equals(value2))
                return false;
        }

        return true;
    }
}