package nz.ac.auckland.lablet.vision;

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
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);

    private int vMax = 0;
    private int vMin = 0;
    private int sMin = 0;

    private MatOfInt histSize = new MatOfInt(16);
    private MatOfFloat ranges = new MatOfFloat(0, 180);
    private Rect trackWindow;
    private Scalar hsvMin;
    private Scalar hsvMax;
    private int colourRange = 30;


    Mat hsv, hue, mask, hist, backproj, bgr;// = Mat::zeros(200, 320, CV_8UC3), backproj;
    Size size;

    public CamShiftTracker() {
    }

    public boolean isROISet()
    {
        return trackWindow != null;
    }

    /**
    * Sets the region of interest for the CamShiftTracker.
    *
    */

    public void setROI(Bitmap bmp, int x, int y, int width, int height)
    {
        size = new Size(bmp.getWidth(), bmp.getHeight());
        hsv = new Mat(size, CvType.CV_8UC3);
        hue = new Mat(size, CvType.CV_8UC3);
        mask = new Mat(size, CvType.CV_8UC3);
        hist = new Mat(size, CvType.CV_8UC3);
        bgr = new Mat();
        backproj = new Mat();

        Mat image = new Mat();
        Utils.bitmapToMat(bmp, image);
        //this.saveFrame(image, "roi_raw_frame");

        trackWindow = new Rect(x, y, width, height);

        Mat bgrRoi = image.submat(trackWindow);

        Scalar domColour = getDominantHsv(bgrRoi, 5);

        int minHue = (int)domColour.val[0] - colourRange;
        if(minHue < 0)
        {
            minHue = 180 - minHue;
        }

        //white:
        //hsvMin = new Scalar(minHue, Math.max(domColour.val[1] - 50, 0), Math.max(domColour.val[2]- 50, 0));
        //hsvMax = new Scalar((domColour.val[0] + colourRange) % 180, Math.min(domColour.val[1]+ 50, 255), Math.min(domColour.val[2]+ 50, 255));

        hsvMin = domColour;// new Scalar(minHue, Math.max(domColour.val[1] - 50, 0), Math.max(domColour.val[2]- 50, 0));
        hsvMax = domColour;// new Scalar((domColour.val[0] + colourRange) % 180, Math.min(domColour.val[1]+ 50, 255), Math.min(domColour.val[2]+ 50, 255));

        toHsv(image, hsvMin, hsvMax);

//        getDominantColour(bgrRoi);
//        kmeansCluster(bgrRoi);

        Mat hsvRoi = hsv.submat(trackWindow);
        //this.saveFrame(hsvRoi, "roi_hue_submat");

        Mat maskRoi = mask.submat(trackWindow);
        //this.saveFrame(maskRoi, "roi_mask_submat");

        ArrayList<Mat> hsvRois = new ArrayList<>();
        hsvRois.add(hsvRoi); //List<Mat> images, MatOfInt channels, Mat mask, Mat hist, MatOfInt histSize, MatOfFloat ranges, boolean accumulate
        Imgproc.calcHist(hsvRois, new MatOfInt(0), maskRoi, hist, histSize, ranges);
        //this.saveFrame(hist, "roi_hist");
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
        //hist.reshape(-1);
        //this.saveFrame(hist, "roi_hist_norm");
    }

//    private void getDominantColour(Mat bgr)
//    {
//        Mat hsv = new Mat();
//        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV, 3);
//
//        ArrayList<Mat> hsvRois = new ArrayList<>();
//        hsvRois.add(hsv);
//
//        Mat hist = new Mat();
//        int size = 180;
//        Imgproc.calcHist(hsvRois, new MatOfInt(0), new Mat(), hist, new MatOfInt(size), new MatOfFloat(0, 180));
//
//        for (int i = 0; i < size; i++) {
//            double[] histValues = hist.get(i, 0);
//            for (int j = 0; j < histValues.length; j++) {
//                Log.d(TAG, "count=" + histValues[j] + " hue=" + i * (180/size));
//            }
//        }
//
//    }

    private Scalar getDominantHsv(Mat bgr, int k)
    {
        //Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV, 3);

        //Image quantization using k-means: https://github.com/abidrahmank/OpenCV2-Python-Tutorials/blob/master/source/py_tutorials/py_ml/py_kmeans/py_kmeans_opencv/py_kmeans_opencv.rst
        Mat clusterData = new Mat();
        hsv.reshape(1, hsv.rows() * hsv.cols()).convertTo(clusterData, CvType.CV_32F, 1.0 / 255.0);
        Mat labels = new Mat();
        Mat centres = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Core.kmeans(clusterData, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centres);

        //Get num hits for each category
        int[] counts = new int[k];

        for (int i = 0; i < labels.rows(); i++) {
            int label = (int)labels.get(i, 0)[0];
            counts[label] += 1;
        }

        //Get cluster index with maximum number of members
        int max = 0;
        int index = -1;

        for (int i = 0; i < counts.length; i++)
        {
            int value = counts[i];

            if(value > max)
            {
                max = value;
                index = i;
            }
        }

        //Get cluster centre point hsv
        int h = (int)(centres.get(index, 0)[0] * 255.0);
        int s = (int)(centres.get(index, 1)[0] * 255.0);
        int v = (int)(centres.get(index, 2)[0] * 255.0);

        Log.d(TAG, "h=" + h + " s=" + s + " v=" + v);



        for (int i = 0; i < centres.rows(); i++) {
            for (int j = 0; j < centres.cols(); j++) {
                double[] centreData = centres.get(i, j);
                Log.d(TAG, "i=" + i + " j=" + j + " centres=" + centreData[0] * 255);
            }
        }

        return new Scalar(h, s, v);





    }

    private void toHsv(Mat image, Scalar hsvMin, Scalar hsvMax)
    {
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV, 3);
        //this.saveFrame(hsv, "hsv");

        //Scalar hsvMin = new Scalar(106, 60, 90);
        //Scalar hsvMax = new Scalar(124,255,255);
        //Core.inRange(hsv, new Scalar(0, sMin, Math.min(vMin, vMax)), new Scalar(180, 255, Math.max(vMin, vMax)), mask);
        Core.inRange(hsv, hsvMin, hsvMax, mask);

        Mat output = new Mat();
        Imgproc.cvtColor(mask, output, Imgproc.COLOR_GRAY2BGR, 0);
        Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2RGBA, 0);
        this.saveFrame(output, "mask");

//        MatOfInt fromTo = new MatOfInt(0, 0);
//        hue.create(size, hsv.depth());
//
//        ArrayList<Mat> hues = new ArrayList<>();
//        ArrayList<Mat> hsvs = new ArrayList<>();
//        hues.add(hue);
//        hsvs.add(hsv);
//
//
//
//        Core.mixChannels(hsvs, hues, fromTo);
//        hue = hues.get(0);
//
//        this.saveFrame(hue, "hue");
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

    public Rect findObject(Bitmap bmp)
    {
        if(isROISet()) {

            Mat image = new Mat();
            Utils.bitmapToMat(bmp, image);

            toHsv(image, hsvMin, hsvMax);

            ArrayList<Mat> hsvs = new ArrayList<>();
            hsvs.add(hsv);

            Imgproc.calcBackProject(hsvs, new MatOfInt(0), hist, backproj, ranges, 1);
            //this.saveFrame(backproj, "backproj");
            Core.bitwise_and(backproj, mask, backproj);
            //this.saveFrame(backproj, "backproj_bitwise_and");

            RotatedRect result = Video.CamShift(backproj, trackWindow, termCriteria);

            Log.i(TAG, "window: " + trackWindow.toString());

            return trackWindow;
        }

        Log.e(TAG, "please set region of interest before calling findObject");

        return null;
    }

    public void saveFrame(Mat mat, String name)
    {
        Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);// .createBitmap();
        Utils.matToBitmap(mat, bmp);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/" + name + ".png");
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
