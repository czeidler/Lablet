package nz.ac.auckland.lablet.vision_algorithms;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jamie on 27/07/2015.
 */

public class CamShiftTracker {

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

    private Mat roiHist;
    //private Rect roiWindow;
    private TermCriteria termCriteria;
    private HashMap<Integer, RotatedRect> output = new HashMap<Integer, RotatedRect>();
    private HashMap<Integer, Rect> rois = new HashMap<Integer, Rect>();
    Integer currentRoiFrame = null;

    public CamShiftTracker() {
        roiHist = new Mat();
        termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);
    }

    public boolean isROISet()
    {
        return this.currentRoiFrame != null;
    }


   /**
    * Sets the region of interest for the CamShiftTracker.
    *
    */

    public void setROI(int frameId, Bitmap bmp, int x, int y, int width, int height)
    {
        this.currentRoiFrame = frameId;

        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);

        //Get region of interest and convert to HSV color space
        Rect rect = new Rect(x, y, width, height);
        Mat roi = new Mat(inputFrame, rect);// inputFrame.submat(x, x+width, y, y+height);

        Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2HSV);

        //Calculate HSV histogram for region of interest
        ArrayList<Mat> images = new ArrayList<Mat>();
        images.add(roi);
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), roiHist, new MatOfInt(16), new MatOfFloat(0, 180));
        Core.normalize(roiHist, roiHist, 0, 255, Core.NORM_MINMAX);

        Rect roiRect = new Rect(x, y, width, height);
        rois.put(frameId, roiRect);
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

    public void findObject(int frameId, Bitmap bmp)
    {
        //Get initial seed window
        int previousFrame = frameId-1;
        RotatedRect previousResult = this.output.get(previousFrame);
        Rect seedWindow = new Rect();

        if(previousResult != null)
        {
            seedWindow = previousResult.boundingRect();
        }
        else if(previousFrame == this.currentRoiFrame)
        {
            seedWindow = this.rois.get(this.currentRoiFrame);
        }

        if(this.currentRoiFrame == null)
        {
            throw new IllegalStateException("CamShiftTracker: Please set a region of interest with the setROI method");
        }
        else if(previousResult == null)
        {
            throw new IllegalStateException("CamShiftTracker: Please set a region of interest or search for the object in the previous frameId");
        }

        //Get current Mat frameId and convert to HSV colour space
        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);
        Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_BGR2HSV);

        //Meanshift
        ArrayList<Mat> images = new ArrayList<Mat>();
        images.add(inputFrame);
        Mat output = new Mat();
        Imgproc.calcBackProject(images, new MatOfInt(0), roiHist, output, new MatOfFloat(0, 180), 1);

        RotatedRect result = Video.CamShift(output, seedWindow, termCriteria); //Camshift todo:  check if back project was successful
        this.output.put(frameId, result);
    }

    /**
     *
     * @return RotatedRect. Returns null if no object found.
     */

    public RotatedRect getOutput(int frameId)
    {
        return this.output.get(frameId);
    }

}
