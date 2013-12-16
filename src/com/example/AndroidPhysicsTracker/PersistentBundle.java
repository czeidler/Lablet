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

/**
 * Created by lec on 12/12/13.
 */
public class PersistentBundle {
    public Bundle unflattenBundle(InputStream input) throws IOException, XmlPullParserException {
        Bundle bundle= new Bundle();
        XmlPullParserFactory pullParserFactory;
        XmlPullParser parser = null;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new XmlPullParserException("PersistentBundle");
        }

        parser.setInput(input, null);

        int tag = parser.getEventType();
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
        while (tag != XmlPullParser.END_TAG){
            switch (tag){
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    String key = parser.getName();
                    if (key == "key"){
                        String keyName = parser.getAttributeValue("", "name");
                        String typeName = parser.getAttributeValue("", "type");
                        if (typeName == "string")
                            handleStringValue(parser, keyName, bundle);
                        else if (typeName == "int")
                            handleIntegerValue(parser, keyName, bundle);
                        else if (typeName == "boolean")
                            handleBooleanValue(parser, keyName, bundle);
                        else if (typeName == "float")
                            handleFloatValue(parser, keyName, bundle);
                        else if (typeName == "double")
                            handleDoubleValue(parser, keyName, bundle);
                        else if (typeName == "string_array")
                            handleStringArray(parser, keyName, bundle);
                        else if (typeName == "int_array")
                            handleIntegerArray(parser, keyName, bundle);
                        else if (typeName == "boolean_array")
                            handleBooleanArray(parser, keyName, bundle);
                        else if (typeName == "float_array")
                            handleFloatArray(parser, keyName, bundle);
                        else if (typeName == "double_array")
                            handleDoubleArray(parser, keyName, bundle);
                        else if (typeName == "bundle")
                            handleBundle(parser, keyName, bundle);
                        else
                            throw new NotSerializableException();
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void flattenBundle(Bundle bundle, XmlSerializer serializer) throws IOException {
        Set<String> keys = bundle.keySet();

        for (String key : keys) {
            Object o = bundle.get(key);
            if (o.getClass().isArray()) {
                if (o.getClass().getComponentType() == String.class) {
                    writeStringArray(key, bundle, serializer);
                } else if (o.getClass().getComponentType() == int.class) {
                    writeIntegerArray(key, bundle, serializer);
                } else if (o.getClass().getComponentType() == Boolean.class) {
                    writeBooleanArray(key, bundle, serializer);
                } else if (o.getClass().getComponentType() == Float.class) {
                    writeFloatArray(key, bundle, serializer);
                } else if (o.getClass().getComponentType() == Double.class) {
                    writeDoubleArray(key, bundle, serializer);
                } else {
                    throw new NotSerializableException();
                }
            } else if (o.getClass() == String.class) {
                writeString(key, bundle, serializer);
            } else if (o.getClass() == Integer.class) {
                writeInteger(key, bundle, serializer);
            } else if (o.getClass() == Boolean.class) {
                writeBoolean(key, bundle, serializer);
            } else if (o.getClass() == Float.class) {
                writeFloat(key, bundle, serializer);
            } else if (o.getClass() == Double.class) {
                writeDouble(key, bundle, serializer);
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

    private void writeString(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        String value = bundle.getString(key);
        startKeyTag(key, "string", serializer);
        serializer.startTag("", "value");
        serializer.text(value);
        serializer.endTag("", "value");
        endKeyTag(serializer);
    }

    private void writeInteger(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        int value = bundle.getInt(key);
        startKeyTag(key, "int", serializer);
        serializer.startTag("", "value");
        String string = new String();
        string += value;
        serializer.text(string);
        serializer.endTag("", "value");
        endKeyTag(serializer);
    }

    private void writeBoolean(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        boolean value = bundle.getBoolean(key);
        startKeyTag(key, "boolean", serializer);
        serializer.startTag("", "value");
        String string = new String();
        string += value;
        serializer.text(string);
        serializer.endTag("", "value");
        serializer.endTag("", "key");
    }

    private void writeFloat(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        float value = bundle.getFloat(key);
        startKeyTag(key, "float", serializer);
        serializer.startTag("", "value");
        String string = new String();
        string += value;
        serializer.text(string);
        serializer.endTag("", "value");
        serializer.endTag("", "key");
    }

    private void writeDouble(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        double value = bundle.getDouble(key);
        startKeyTag(key, "double", serializer);
        serializer.startTag("", "value");
        String string = new String();
        string += value;
        serializer.text(string);
        serializer.endTag("", "value");
        serializer.endTag("", "key");
    }

    private void writeStringArray(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        String[] array = bundle.getStringArray(key);
        startKeyTag(key, "string_array", serializer);
        for (String value : array) {
            serializer.startTag("", "value");
            serializer.text(value);
            serializer.endTag("", "value");
        }
        serializer.endTag("", "key");
    }

    private void writeIntegerArray(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        int[] array = bundle.getIntArray(key);
        startKeyTag(key, "int_array", serializer);
        for (int value : array) {
            serializer.startTag("", "value");
            String string = new String();
            string += value;
            serializer.text(string);
            serializer.endTag("", "value");
        }
        serializer.endTag("", "key");
    }

    private void writeBooleanArray(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        boolean[] array = bundle.getBooleanArray(key);
        startKeyTag(key, "boolean_array", serializer);
        for (boolean value : array) {
            serializer.startTag("", "value");
            String string = new String();
            string += value;
            serializer.text(string);
            serializer.endTag("", "value");
        }
        serializer.endTag("", "key");
    }

    private void writeFloatArray(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        float[] array = bundle.getFloatArray(key);
        startKeyTag(key, "float_array", serializer);
        for (float value : array) {
            serializer.startTag("", "value");
            String string = new String();
            string += value;
            serializer.text(string);
            serializer.endTag("", "value");
        }
        serializer.endTag("", "key");
    }

    private void writeDoubleArray(String key, Bundle bundle, XmlSerializer serializer) throws IOException {
        double[] array = bundle.getDoubleArray(key);
        startKeyTag(key, "double_array", serializer);
        for (double value : array) {
            serializer.startTag("", "value");
            String string = new String();
            string += value;
            serializer.text(string);
            serializer.endTag("", "value");
        }
        serializer.endTag("", "key");
    }

    private void handleStringValue(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        String value = parser.getText();
                        bundle.putString(key, value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }

    private void handleIntegerValue(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        String value = parser.getText();
                        bundle.putInt(key, Integer.parseInt(value));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }

    private void handleBooleanValue(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        String value = parser.getText();
                        bundle.putBoolean(key, Boolean.parseBoolean(value));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }

    private void handleFloatValue(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        String value = parser.getText();
                        bundle.putFloat(key, Float.parseFloat(value));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }


    private void handleDoubleValue(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        String value = parser.getText();
                        bundle.putDouble(key, Double.parseDouble(value));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    return;
            }
            tag = parser.next();
        }
    }

    private void handleStringArray(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<String> array = new ArrayList<String>();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        inValueTag = true;
                        String value = parser.getText();
                        array.add(value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (inValueTag)
                        inValueTag = false;
                    else {
                        bundle.putStringArray(key, array.toArray(new String[array.size()]));
                        return;
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void handleIntegerArray(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<Integer> array = new ArrayList<Integer>();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        inValueTag = true;
                        String stringValue = parser.getText();
                        int value = Integer.parseInt(stringValue);
                        array.add(value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (inValueTag)
                        inValueTag = false;
                    else {
                        int[] a = new int[array.size()];
                        for (int i = 0; i < array.size(); i++)
                            a[i] = array.get(i);
                        bundle.putIntArray(key, a);
                        return;
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void handleBooleanArray(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<Boolean> array = new ArrayList<Boolean>();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        inValueTag = true;
                        String stringValue = parser.getText();
                        boolean value = Boolean.parseBoolean(stringValue);
                        array.add(value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (inValueTag)
                        inValueTag = false;
                    else {
                        boolean[] a = new boolean[array.size()];
                        for (int i = 0; i < array.size(); i++)
                            a[i] = array.get(i);
                        bundle.putBooleanArray(key, a);
                        return;
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void handleFloatArray(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<Float> array = new ArrayList<Float>();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        inValueTag = true;
                        String stringValue = parser.getText();
                        float value = Float.parseFloat(stringValue);
                        array.add(value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (inValueTag)
                        inValueTag = false;
                    else {
                        float[] a = new float[array.size()];
                        for (int i = 0; i < array.size(); i++)
                            a[i] = array.get(i);
                        bundle.putFloatArray(key, a);
                        return;
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void handleDoubleArray(XmlPullParser parser, String key, Bundle bundle) throws IOException, XmlPullParserException {
        int tag = parser.next();
        boolean inValueTag = false;
        List<Double> array = new ArrayList<Double>();
        while (tag != XmlPullParser.END_DOCUMENT){
            switch (tag){
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
                    if (tagName == "value") {
                        inValueTag = true;
                        String stringValue = parser.getText();
                        double value = Double.parseDouble(stringValue);
                        array.add(value);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (inValueTag)
                        inValueTag = false;
                    else {
                        double[] a = new double[array.size()];
                        for (int i = 0; i < array.size(); i++)
                            a[i] = array.get(i);
                        bundle.putDoubleArray(key, a);
                        return;
                    }
                    break;
            }
            tag = parser.next();
        }
    }

    private void handleBundle(XmlPullParser parser, String keyName, Bundle bundle) throws IOException, XmlPullParserException {
        Bundle subBundle = new Bundle();
        unflattenBundle(parser, subBundle);
        bundle.putBundle("keyName", subBundle);
    }

}
