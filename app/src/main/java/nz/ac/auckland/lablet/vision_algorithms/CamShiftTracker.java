package nz.ac.auckland.lablet.vision_algorithms;

import android.graphics.Bitmap;

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

/**
 * Created by Jamie on 27/07/2015.
 */

public class CamShiftTracker {

    private Mat roiHist = new Mat();
    private Rect roiWindow;
    private TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);

   /**
    * Sets the region of interest for the CamShiftTracker.
    *
    */

    public void setROI(Bitmap bmp, int x, int y, int width, int height)
    {
        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);

        //Get region of interest and convert to HSV color space
        Mat roi = inputFrame.submat(x, x+width, y, y+height);
        Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2HSV);

        //Calculate HSV histogram for region of interest
        ArrayList<Mat> images = new ArrayList<Mat>();
        images.add(roi);
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), roiHist, new MatOfInt(16), new MatOfFloat(0, 180));
        Core.normalize(roiHist, roiHist, 0, 255, Core.NORM_MINMAX);
        roiWindow = new Rect(x, y, width, height);
    }

    /**
     * Finds an object in a bitmap frame. The object being searched for is detected based on
     * a pre-specified region of interest (setROI).
     *
     * Returns a RotatedRect that specifies the location of the object.
     *
     * Important: setROI must be called before this method is used, otherwise an IllegalStateException
     * will be thrown.
     */

    public RotatedRect findObject(Bitmap bmp)
    {
        if(roiWindow != null) {
            //Get current Mat frame and convert to HSV colour space
            Mat inputFrame = new Mat();
            Utils.bitmapToMat(bmp, inputFrame);
            Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_BGR2HSV);

            //Meanshift
            ArrayList<Mat> images = new ArrayList<Mat>();
            images.add(inputFrame);
            Mat output = new Mat();
            Imgproc.calcBackProject(images, new MatOfInt(0), roiHist, output, new MatOfFloat(0, 180), 1);

            //Camshift
            return Video.CamShift(output, roiWindow, termCriteria);
        }
        else
        {
            throw new IllegalStateException("CamShiftTracker: Please set a region of interest with the setROI method");
        }
    }

}
