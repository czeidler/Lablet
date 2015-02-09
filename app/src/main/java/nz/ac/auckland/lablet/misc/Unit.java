/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import java.util.ArrayList;
import java.util.List;


public class Unit extends WeakListenable<Unit.IListener> {
    public interface IListener {
        public void onBaseExponentChanged();
    }

    private void notifyBaseExponentChanged() {
        for (IListener listener : getListeners())
            listener.onBaseExponentChanged();
    }

    static public class Prefix {
        final public int exponent;
        final public String prefix;
        final public String name;

        public Prefix(int exponent, String prefix, String name) {
            this.exponent = exponent;
            this.prefix = prefix;
            this.name = name;
        }
    }

    private int baseExponent = 0;
    private String unit = "";
    private String name = "";
    private List<Prefix> prefixes;

    public Unit(String unit) {
        setBaseUnit(unit, true);
    }

    public Unit(String unit, int baseExponent) {
        setBaseUnit(unit, true);
        setBaseExponent(baseExponent);
    }

    public Unit(String unit, boolean derivePrefixesFromUnit) {
        setBaseUnit(unit, derivePrefixesFromUnit);
    }

    public void setBaseExponent(int baseExponent) {
        this.baseExponent = baseExponent;
        notifyBaseExponentChanged();
    }

    public int getBaseExponent() {
        return baseExponent;
    }

    public void setBaseUnit(String unit, boolean derivePrefixesFromUnit) {
        this.unit = unit;

        if (derivePrefixesFromUnit) {
            if (unit.equals("m")) {
                setPrefixes(createMeterPrefixes());
            } else if (unit.equals("s")) {
                setPrefixes(createSecondPrefixes());
            } else
                setPrefixes(createDefaultPrefixes());
        }
    }

    public String getBaseUnit() {
        return unit;
    }

    public String getName() {
        return name;
    }

    public float transformToPrefix(float value, Prefix prefix) {
        return value * (float)Math.pow(10, getBaseExponent() - prefix.exponent);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return getPrefixFor(getBaseExponent()).prefix;
    }

    public String getTotalUnit() {
        Prefix prefix = getPrefixFor(getBaseExponent());
        return prefix.prefix + getBaseUnit();
    }

    public String getTotalUnit(Prefix prefix) {
        if (prefix == null)
            return getTotalUnit();
        return prefix.prefix + getBaseUnit();
    }

    public Prefix getPrefixFor(int exponent) {
        for (Prefix prefix : prefixes) {
            if (exponent == prefix.exponent)
                return prefix;
        }
        return null;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    static final public int MICRO = -6;
    static final public int MILLI = -3;
    static final public int CENTI = -2;
    static final public int DECI = -1;
    static final public int KILO = 3;

    static public List<Prefix> createSecondPrefixes() {
        List<Prefix> list = new ArrayList<>();
        list.add(new Prefix(-9, "n", "nano"));
        list.add(new Prefix(MICRO, "\u00B5", "micro"));
        list.add(new Prefix(MILLI, "m", "milli"));
        list.add(new Prefix(0, "", ""));
        return list;
    }

    static public List<Prefix> createMeterPrefixes() {
        List<Prefix> list = new ArrayList<>();
        list.add(new Prefix(-24, "y", "yocto"));
        list.add(new Prefix(-21, "z", "zepto"));
        list.add(new Prefix(-18, "a", "atto"));
        list.add(new Prefix(-15, "f", "femto"));
        list.add(new Prefix(-12, "p", "pico"));
        list.add(new Prefix(-9, "n", "nano"));
        list.add(new Prefix(MICRO, "\u00B5", "micro"));
        list.add(new Prefix(MILLI, "m", "milli"));
        list.add(new Prefix(CENTI, "c", "centi"));
        list.add(new Prefix(DECI, "d", "deci"));
        list.add(new Prefix(0, "", ""));
        list.add(new Prefix(KILO, "k", "kilo"));
        return list;
    }

    static public List<Prefix> createDefaultPrefixes() {
        List<Prefix> list = new ArrayList<>();
        list.add(new Prefix(0, "", ""));
        return list;
    }

    static public List<Prefix> createGeneralPrefixes() {
        List<Prefix> list = new ArrayList<>();
        list.add(new Prefix(-24, "y", "yocto"));
        list.add(new Prefix(-21, "z", "zepto"));
        list.add(new Prefix(-18, "a", "atto"));
        list.add(new Prefix(-15, "f", "femto"));
        list.add(new Prefix(-12, "p", "pico"));
        list.add(new Prefix(-9, "n", "nano"));
        list.add(new Prefix(MICRO, "\u00B5", "micro"));
        list.add(new Prefix(MILLI, "m", "milli"));
        list.add(new Prefix(0, "", ""));
        list.add(new Prefix(KILO, "k", "kilo"));
        list.add(new Prefix(6, "M", "mega"));
        list.add(new Prefix(9, "G", "giga"));
        list.add(new Prefix(12, "T", "tera"));
        list.add(new Prefix(15, "P", "peta"));
        list.add(new Prefix(18, "E", "exa"));
        list.add(new Prefix(21, "Z", "zetta"));
        list.add(new Prefix(24, "Y", "yotta"));
        return list;
    }
}

