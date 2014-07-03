package nz.ac.auckland.lablet.views.plotview;

public interface IYAxis {
    /**
     * Gets the view relativePosition of the axis top.
     * @return axis top
     */
    public float getAxisTopOffset();
    /**
     * Gets the view relativePosition of the axis bottom measured from the view bottom.
     * @return axis bottom
     */
    public float getAxisBottomOffset();

    /**
     * Set the number of relevant digits.
     *
     * For example, a value of 3 and a top of 9999.3324 and a bottom of 9999.3325 would give results in a displayed
     * range of 9999.32400 to 9999.32500.
     *
     * @param digits
     */
    public void setRelevantLabelDigits(int digits);
    public void setDataRange(float bottom, float top);

    public float optimalWidthForHeight(float height);
    public void setLabel(String label);
    public void setUnit(String unit);
}
