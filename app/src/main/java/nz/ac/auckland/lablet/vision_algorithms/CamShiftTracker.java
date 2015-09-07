package nz.ac.auckland.lablet.vision_algorithms;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jamie on 27/07/2015.
 */

public class CamShiftTracker {

    private static String TAG = "CamShiftTracker";

    static {
        if(!OpenCVLoader.initDebug())
        {
            Log.i("CamShiftTracker", "OpenCV initialisation failed");
        }
        else
        {
            Log.i("CamShiftTracker", "OpenCV initialisation succeeded");
        }
    }

    private Mat prob;
    private Mat mask;
    private Mat hist;
    private ArrayList<Mat> hues = new ArrayList<>();


    private MatOfFloat ranges = new MatOfFloat(0f, 256f);
    private MatOfInt histSize = new MatOfInt(25);
    private TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);

    private int range;
    private int vMax = 0;
    private int vMin = 0;
    private int sMin = 0;

    Rect roiRect;
    Rect prevRect;

    public CamShiftTracker() {

    }

    public boolean isROISet()
    {
        return roiRect != null;
    }

    /**
    * Sets the region of interest for the CamShiftTracker.
    *
    */

    public void setROI(Bitmap bmp, int x, int y, int width, int height)
    {
        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);

        Rect roiRect =  new Rect(x, y, width, height);

        mask = new Mat(inputFrame.size(), CvType.CV_8UC1);
        prob = new Mat(inputFrame.size(),CvType.CV_8UC1);

        updateHueImage(inputFrame);

        Mat mask = new Mat(inputFrame.size(), CvType.CV_8UC1);// inputFrame.submat(x, x+width, y, y+height);
        mask.submat(roiRect);

        List<Mat> images = Arrays.asList(hues.get(0).submat(roiRect));
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist);

        prevRect = roiRect;
        this.roiRect = roiRect;
    }

    /**
     * Finds an object in a bitmap frameId. The object being searched for is detected based on
     * a pre-specified region of interest (setROI).
     *
     * Returns a RotatedRect that specifies the location of the object.
     *
     * Important: setROI must be called before this method is used, otherwise an IllegalStateException
     * will be thrown.
     */

    private void updateHueImage(Mat input)
    {
        Mat bgr = new Mat(input.size(), CvType.CV_8UC3);
        Mat hsv = new Mat(input.size(), CvType.CV_8UC3);
        Mat hue = new Mat(input.size(), CvType.CV_8UC1);
        ArrayList<Mat> hsvs = new ArrayList<>();

        Imgproc.cvtColor(input, bgr, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsv, new Scalar(0, sMin, Math.min(vMin, vMax)), new Scalar(180, 256, Math.max(vMin, vMax)), mask);

        hues.clear();
        hsvs.add(hsv);
        hues.add(hue);

        MatOfInt from_to = new MatOfInt(0,0);
        Core.mixChannels(hsvs, hues, from_to);
    }

    public RotatedRect findObject(Bitmap bmp)
    {
        if(isROISet()) {
            //Get current Mat frameId and convert to HSV colour space
            Mat inputFrame = new Mat();
            Utils.bitmapToMat(bmp, inputFrame);

            updateHueImage(inputFrame);

            Imgproc.calcBackProject(hues, new MatOfInt(0), hist, prob, ranges, 255);
            Core.bitwise_and(prob, mask, prob, new Mat());

            RotatedRect result = Video.CamShift(prob, prevRect, termCriteria);
            prevRect = result.boundingRect();
            return result;
        }

        Log.e(TAG, "please set region of interest before calling findObject");

        return null;
    }

    public int getvMax() {
        return vMax;
    }

    public void setvMax(int vMax) {
        this.vMax = vMax;
    }

    public int getvMin() {
        return vMin;
    }

    public void setvMin(int vMin) {
        this.vMin = vMin;
    }

    public int getsMin() {
        return sMin;
    }

    public void setsMin(int sMin) {
        this.sMin = sMin;
    }
}
