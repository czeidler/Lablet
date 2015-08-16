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

import java.io.FileOutputStream;
import java.io.IOException;
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
    private Rect roiWindow;
    private TermCriteria termCriteria;
    private HashMap<Integer, RotatedRect> rotatedRectangles = new HashMap<Integer, RotatedRect>();

    public CamShiftTracker() {
        roiHist = new Mat();
        termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);
    }

    public boolean isROISet()
    {
        return roiWindow != null;
    }

    public void reset()
    {
        roiWindow = null;
    }

   /**
    * Sets the region of interest for the CamShiftTracker.
    *
    */

    public void setROI(Bitmap bmp, int x, int y, int width, int height)
    {
        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);

        //Get region of interest and convert to HSV color space
        Rect rect = new Rect(x, y, width, height);
        Mat roi = new Mat(inputFrame, rect);// inputFrame.submat(x, x+width, y, y+height);
        //this.saveROI(roi);

        //Mat roi =
        Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2HSV);

        //Calculate HSV histogram for region of interest
        ArrayList<Mat> images = new ArrayList<Mat>();
        images.add(roi);
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), roiHist, new MatOfInt(16), new MatOfFloat(0, 180));
        Core.normalize(roiHist, roiHist, 0, 255, Core.NORM_MINMAX);
        roiWindow = new Rect(x, y, width, height);
    }

    public void saveROI(Mat roi)
    {
        Bitmap bmp = Bitmap.createBitmap(roi.width(), roi.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(roi, bmp);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/roi.png");
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public boolean findObject(int frameNum, Bitmap bmp)
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

            RotatedRect result = Video.CamShift(output, roiWindow, termCriteria); //Camshift todo:  check if back project was successful
            this.rotatedRectangles.put(frameNum, result);

            return true;
        }
        else
        {
            throw new IllegalStateException("CamShiftTracker: Please set a region of interest with the setROI method");
        }
    }

    /**
     *
     * @return RotatedRect. Returns null if no object found.
     */

    public RotatedRect getRotatedRect(int frameNum)
    {
        return this.rotatedRectangles.get(frameNum);
    }

}
