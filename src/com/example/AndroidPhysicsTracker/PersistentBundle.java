package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class PersistentBundle {
    public Bundle unflattenBundle(InputStream input) throws IOException, XmlPullParserException {
        Bundle bundle= new Bundle();
        XmlPullParserFactory pullParserFactory;
        XmlPullParser parser;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new XmlPullParserException("PersistentBundle");
        }

        parser.setInput(input, null);

        unflattenBundle(parser, bundle);

        return bundle;
    }

    public void flattenBundle(Bundle bundle, Writer output) throws NotSerializableException {
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(output);
            serializer.startDocument("UTF-8", true);

            flattenBundle(bundle, serializer);

            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotSerializableException();
        }
    }

    private void unflattenBundle(XmlPullParser parser, Bundle bundle) throws XmlPullParserException, IOException {
        int tag = parser.getEventType();
        int endTagCount = 0;
        while (tag != XmlPullParser.END_DOCUMENT) {
            switch (tag){
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    endTagCount--;
                    String key = parser.getName();
                    if (key.equals("key")){
                        String keyName = parser.getAttributeValue("", "name");
                        String typeName = parser.getAttributeValue("", "type");
                        if (typeName.equals("string"))
                            handleValue(new StringHandlerHelper(), parser, keyName, bundle);
                        else if (typeName.equals("int"))
                            handleValue(new IntegerHandlerHelper(), parser, keyName, bundle);
                        else if (typeName.equals("boolean"))
                            handleValue(new BooleanHandlerHelper(), parser, keyName, bundle);
                        else if (typeName.equals("float"))
                            handleValue(new FloatHandlerHelper(), parser, keyName, bundle);
                        else if (typeName.equals("double"))
                            handleValue(new DoubleHandlerHelper(), parser, keyName, bundle);
                        else if (typeName.equals("string_array")) {
                            handleArray(new StringHandlerHelper(), parser, keyName, bundle);
                            // handleArray read its own end tag
                            endTagCount++;
                        } else if (typeName.equals("int_array")) {
                            handleArray(new IntegerHandlerHelper(), parser, keyName, bundle);
                            // handleArray read its own end tag
                            endTagCount++;
                        } else if (typeName.equals("boolean_array")) {
                            handleArray(new BooleanHandlerHelper(), parser, keyName, bundle);
                            // handleArray read its own end tag
                            endTagCount++;
                        } else if (typeName.equals("float_array")) {
                            handleArray(new FloatHandlerHelper(), parser, keyName, bundle);
                            // handleArray read its own end tag
                            endTagCount++;
                        } else if (typeName.equals("double_array")) {
                            handleArray(new DoubleHandlerHelper(), parser, keyName, bundle);
                            // handleArray read its own end tag
                            endTagCount++;
                        } else if (typeName.equals("bundle")) {
                            handleBundle(parser, keyName, bundle);
                            // handleBundle read its own end tag
                            endTagCount++;
                        } else
                            throw new NotSerializableException();
                    }
                    break;

                case XmlPullParser.END_TAG:
                    endTagCount++;
                    break;
            }
            if (endTagCount > 0)
                break;
            tag = parser.next();
        }
    }

    private void flattenBundle(Bundle bundle, XmlSerializer serializer) throws IOException {
        Set<String> keys = bundle.keySet();

        for (String key : keys) {
            Object o = bundle.get(key);
            if (o.getClass().isArray()) {
                if (o.getClass().getComponentType() == String.class) {
                    writeArray(new StringHandlerHelper(), key, bundle, serializer);
                } else if (o.getClass().getComponentType() == int.class) {
                    writeArray(new IntegerHandlerHelper(), key, bundle, serializer);
                } else if (o.getClass().getComponentType() == boolean.class) {
                    writeArray(new BooleanHandlerHelper(), key, bundle, serializer);
                } else if (o.getClass().getComponentType() == float.class) {
                    writeArray(new FloatHandlerHelper(), key, bundle, serializer);
                } else if (o.getClass().getComponentType() == double.class) {
                    writeArray(new DoubleHandlerHelper(), key, bundle, serializer);
                } else {
                    throw new NotSerializableException();
                }
            } else if (o.getClass() == String.class) {
                writeValue(new StringHandlerHelper(), key, bundle, serializer);
            } else if (o.getClass() == Integer.class) {
                writeValue(new IntegerHandlerHelper(), key, bundle, serializer);
            } else if (o.getClass() == Boolean.class) {
                writeValue(new BooleanHandlerHelper(), key, bundle, serializer);
            } else if (o.getClass() == Float.class) {
                writeValue(new FloatHandlerHelper(), key, bundle, serializer);
            } else if (o.getClass() == Double.class) {
                writeValue(new DoubleHandlerHelper(), key, bundle, serializer);
            } else if (o.getClass() == Bundle.class) {
                startKeyTag(key, "bundle", serializer);
                flattenBundle(bundle.getBundle(key), serializer);
                endKeyTag(serializer);
            } else {
                throw new NotSerializableException();
            }
        }
    }

    private void startKeyTag(String key, String type, XmlSerializer serializer) throws IOException {
        serializer.startTag("", "key");
        serializer.attribute("", "name", key);
        serializer.attribute("", "type", type);
    }

    private void endKeyTag(XmlSerializer serializer) throws IOException {
        serializer.endTag("", "key");
    }

    private <T> void writeValue(HandlerHelper<T> helper, String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        T value = helper.getValue(bundle, key);
        startKeyTag(key, helper.getTypeName(), serializer);
        serializer.startTag("", "value");
        serializer.text(helper.toString(value));
        serializer.endTag("", "value");
        endKeyTag(serializer);
    }

    private <T> void writeArray(HandlerHelper<T> helper, String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        T[] array = helper.getArray(bundle, key);
        startKeyTag(key, helper.getArrayTypeName(), serializer);
        assert array != null;
        for (T value : array) {
            serializer.startTag("", "value");
            serializer.text(helper.toString(value));
            serializer.endTag("", "value");
        }
        endKeyTag(serializer);
    }

    private <T> void handleValue(HandlerHelper<T> helper, XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean valueTag = false;
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName.equals("value"))
                        valueTag = true;
                    break;
                case XmlPullParser.TEXT:
                    if (!valueTag)
                        break;
                    String value = parser.getText();
                    helper.putValue(bundle, key, helper.parseValue(value));
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }

    private <T> void handleArray(HandlerHelper<T> helper, XmlPullParser parser, String keyName, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<T> array = new ArrayList<T>();
        int endTagCount = 0;
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    endTagCount--;
                    String tagName = parser.getName();
                    if (tagName.equals("value"))
                        inValueTag = true;
                    break;
                case XmlPullParser.TEXT:
                    if (!inValueTag)
                        break;
                    String stringValue = parser.getText();
                    T value = helper.parseValue(stringValue);
                    array.add(value);
                    break;
                case XmlPullParser.END_TAG:
                    endTagCount++;
                    inValueTag = false;
                    break;
            }
            if (endTagCount > 0)
                break;
            tag = parser.next();
        }

        helper.putArray(bundle, keyName, array);
    }

    private void handleBundle(XmlPullParser parser, String keyName, Bundle bundle) throws IOException, XmlPullParserException {
        Bundle subBundle = new Bundle();
        parser.next();
        unflattenBundle(parser, subBundle);
        bundle.putBundle(keyName, subBundle);
    }

    abstract class HandlerHelper<T> {
        abstract public T parseValue(String string);
        public String toString(T value) {
            String string = new String();
            string += value;
            return string;
        }
        abstract public void putValue(Bundle bundle, String key, T value);
        abstract public void putArray(Bundle bundle, String key, List<T> list);
        abstract public T getValue(Bundle bundle, String key);
        abstract public T[] getArray(Bundle bundle, String key);
        abstract public String getTypeName();
        public String getArrayTypeName() {
            return getTypeName() + "_array";
        }
    }

    class FloatHandlerHelper extends HandlerHelper<Float> {
        @Override
        public Float parseValue(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public void putValue(Bundle bundle, String key, Float value) {
            bundle.putFloat(key, value);
        }

        @Override
        public void putArray(Bundle bundle, String key, List<Float> list) {
            float[] a = new float[list.size()];
            for (int i = 0; i < list.size(); i++)
                a[i] = list.get(i);
            bundle.putFloatArray(key, a);
        }

        @Override
        public Float getValue(Bundle bundle, String key) {
            return bundle.getFloat(key);
        }

        @Override
        public Float[] getArray(Bundle bundle, String key) {
            float[] array = bundle.getFloatArray(key);
            Float[] outArray = new Float[array.length];
            for (int i = 0; i < outArray.length; i++)
                outArray[i] = array[i];
            return outArray;
        }

        @Override
        public String getTypeName() {
            return "float";
        }
    }

    class IntegerHandlerHelper extends HandlerHelper<Integer> {
        @Override
        public Integer parseValue(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public void putValue(Bundle bundle, String key, Integer value) {
            bundle.putInt(key, value);
        }

        @Override
        public void putArray(Bundle bundle, String key, List<Integer> list) {
            int[] a = new int[list.size()];
            for (int i = 0; i < list.size(); i++)
                a[i] = list.get(i);
            bundle.putIntArray(key, a);
        }

        @Override
        public Integer getValue(Bundle bundle, String key) {
            return bundle.getInt(key);
        }

        @Override
        public Integer[] getArray(Bundle bundle, String key) {
            int[] array = bundle.getIntArray(key);
            Integer[] outArray = new Integer[array.length];
            for (int i = 0; i < outArray.length; i++)
                outArray[i] = array[i];
            return outArray;
        }

        @Override
        public String getTypeName() {
            return "int";
        }
    }

    class BooleanHandlerHelper extends HandlerHelper<Boolean> {
        @Override
        public Boolean parseValue(String string) {
            return Boolean.parseBoolean(string);
        }

        @Override
        public void putValue(Bundle bundle, String key, Boolean value) {
            bundle.putBoolean(key, value);
        }

        @Override
        public void putArray(Bundle bundle, String key, List<Boolean> list) {
            boolean[] a = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++)
                a[i] = list.get(i);
            bundle.putBooleanArray(key, a);
        }

        @Override
        public Boolean getValue(Bundle bundle, String key) {
            return bundle.getBoolean(key);
        }

        @Override
        public Boolean[] getArray(Bundle bundle, String key) {
            boolean[] array = bundle.getBooleanArray(key);
            Boolean[] outArray = new Boolean[array.length];
            for (int i = 0; i < outArray.length; i++)
                outArray[i] = array[i];
            return outArray;
        }

        @Override
        public String getTypeName() {
            return "boolean";
        }
    }

    class DoubleHandlerHelper extends HandlerHelper<Double> {
        @Override
        public Double parseValue(String string) {
            return Double.parseDouble(string);
        }

        @Override
        public void putValue(Bundle bundle, String key, Double value) {
            bundle.putDouble(key, value);
        }

        @Override
        public void putArray(Bundle bundle, String key, List<Double> list) {
            double[] a = new double[list.size()];
            for (int i = 0; i < list.size(); i++)
                a[i] = list.get(i);
            bundle.putDoubleArray(key, a);
        }

        @Override
        public Double getValue(Bundle bundle, String key) {
            return bundle.getDouble(key);
        }

        @Override
        public Double[] getArray(Bundle bundle, String key) {
            double[] array = bundle.getDoubleArray(key);
            Double[] outArray = new Double[array.length];
            for (int i = 0; i < outArray.length; i++)
                outArray[i] = array[i];
            return outArray;
        }

        @Override
        public String getTypeName() {
            return "double";
        }
    }

    class StringHandlerHelper extends HandlerHelper<String> {
        @Override
        public String parseValue(String string) {
            return string;
        }

        @Override
        public String toString(String value) {
            return value;
        }

        @Override
        public void putValue(Bundle bundle, String key, String value) {
            bundle.putString(key, value);
        }

        @Override
        public void putArray(Bundle bundle, String key, List<String> list) {
            String[] a = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
                a[i] = list.get(i);
            bundle.putStringArray(key, a);
        }

        @Override
        public String getValue(Bundle bundle, String key) {
            return bundle.getString(key);
        }

        @Override
        public String[] getArray(Bundle bundle, String key) {
            return bundle.getStringArray(key);
        }

        @Override
        public String getTypeName() {
            return "string";
        }
    }

}
