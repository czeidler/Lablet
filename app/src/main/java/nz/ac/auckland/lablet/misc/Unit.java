/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import android.support.annotation.Nullable;

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
    private String baseUnit = "";
    private String name = "";
    private List<Prefix> prefixes;

    public Unit(String baseUnit) {
        setBaseUnit(baseUnit, true);
    }

    public Unit(String baseUnit, int baseExponent) {
        setBaseUnit(baseUnit, true);
        setBaseExponent(baseExponent);
    }

    public Unit(String baseUnit, boolean derivePrefixesFromUnit) {
        setBaseUnit(baseUnit, derivePrefixesFromUnit);
    }

    /**
     * Sets the base exponent.
     *
     * The base exponent is the exponent of the associated data. For example, if the data is given in milli meter the
     * base exponent is -3.
     *
     * @param baseExponent
     */
    public void setBaseExponent(int baseExponent) {
        this.baseExponent = baseExponent;
        notifyBaseExponentChanged();
    }

    public int getBaseExponent() {
        return baseExponent;
    }

    /**
     * Sets the base unit.
     *
     * The base unit is the unit without prefix. For example, "s" for seconds.
     *
     * If derivePrefixesFromUnit is true a fitting set of prefixes is searched.
     *
     * @param baseUnit for example "m" for meter
     * @param derivePrefixesFromUnit determines if the prefixes should be derived from the base unit.
     */
    public void setBaseUnit(String baseUnit, boolean derivePrefixesFromUnit) {
        this.baseUnit = baseUnit;

        if (derivePrefixesFromUnit) {
            if (baseUnit.equals("m")) {
                setPrefixes(createMeterPrefixes());
            } else if (baseUnit.equals("s")) {
                setPrefixes(createSecondPrefixes());
            } else
                setPrefixes(createDefaultPrefixes());
        }
    }

    public String getBaseUnit() {
        return baseUnit;
    }

    /**
     * Sets the name of the unit.
     *
     * This can be an informal description of the unit. For example, "height" for the base unit "m".
     *
     * @param name the name of the unit.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public float transformToPrefix(float value, Prefix prefix) {
        return value * (float)Math.pow(10, getBaseExponent() - prefix.exponent);
    }

    public String getPrefix() {
        return getPrefixFor(getBaseExponent()).prefix;
    }

    /**
     * Returns a string of the base exponent prefix + the base unit.
     *
     * @return base exponent prefix + base unit.
     */
    public String getTotalUnit() {
        Prefix prefix = getPrefixFor(getBaseExponent());
        return prefix.prefix + getBaseUnit();
    }

    /**
     * Returns a string of the given prefix + the base unit. For example, km (kilo + meter).
     *
     * If prefix is null {link getTotalUnit} is called.
     *
     * @param prefix the prefix that should be used. Can be null.
     * @return prefix + base unit.
     */
    public String getTotalUnit(@Nullable Prefix prefix) {
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

    /**
     * Sets the prefixes that are valid for a base unit.
     *
     * @param prefixes list of prefixes.
     */
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

