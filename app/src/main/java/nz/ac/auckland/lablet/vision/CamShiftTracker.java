/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 */
package nz.ac.auckland.lablet.vision;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
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


/**
 * Object tracker using the cam shift algorithm.
 */
public class CamShiftTracker {
    private static String TAG = CamShiftTracker.class.getName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV initialisation failed");
        } else {
            Log.i(TAG, "OpenCV initialisation succeeded");
        }
    }

    private TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 1);
    private MatOfInt histSize = new MatOfInt(16);
    private MatOfFloat ranges = new MatOfFloat(0, 180);
    private Rect trackWindow;
    private Scalar hsvMin;
    private Scalar hsvMax;
    private int colourRange = 9;
    public static final int KMEANS_IMG_SIZE = 100;

    Mat hsv, hue, mask, hist, backproj, bgr;
    Size size;

    /**
     * Gets the location of an object in a frame. Assumes you have called setRegionOfInterest,
     * which informs CamShiftTracker which object to track.
     *
     * @param frame The frame to search for the object in.
     * @return The location and bounds of the object, represented by a Rect.
     */
    public Rect getObjectLocation(Bitmap frame) {
        Mat image = new Mat();
        Utils.bitmapToMat(frame, image);

//        Mat out = new Mat(image.rows(), image.cols(), image.type());
//        image.convertTo(out, -1, 2.0, 2.0);
//        image = out;

        toHsv(image, hsvMin, hsvMax);

        ArrayList<Mat> hsvs = new ArrayList<>();
        hsvs.add(hsv);

        Imgproc.calcBackProject(hsvs, new MatOfInt(0), hist, backproj, ranges, 1);
        Core.bitwise_and(backproj, mask, backproj);

        try {
            Rect tempTrackWindow = trackWindow.clone();
            RotatedRect result = Video.CamShift(backproj, trackWindow, termCriteria);

            if (result.size.equals(new Size(0, 0)) && result.angle == 0 && result.center.equals(new Point(0, 0))) {
                trackWindow = tempTrackWindow;
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Shit went down: ", e);
            return null;
        }

        return trackWindow.clone();
    }

    /**
     * Internally sets the region of interest (ROI) to track.
     * Only needs to be set once, unless the region of interest changes.
     *
     * @param frame The frame to extract the ROI from.
     * @param x The x coordinate of the ROI (top left).
     * @param y The y coordinate of the ROI (top left).
     * @param width The width of the ROI.
     * @param height The height of the ROI.
     */
    public void setRegionOfInterest(Bitmap frame, int x, int y, int width, int height) {
        size = new Size(frame.getWidth(), frame.getHeight());
        hsv = new Mat(size, CvType.CV_8UC3);
        hue = new Mat(size, CvType.CV_8UC3);
        mask = new Mat(size, CvType.CV_8UC3);
        hist = new Mat(size, CvType.CV_8UC3);
        bgr = new Mat();
        backproj = new Mat();

        Mat image = new Mat();
        Utils.bitmapToMat(frame, image);

//        Mat out = new Mat(image.rows(), image.cols(), image.type());
//        image.convertTo(out, -1, 2.0, 2.0);
//        image = out;

        trackWindow = new Rect(x, y, width, height);

        Mat bgrRoi = image.submat(trackWindow);

        Pair<Scalar, Scalar> minMaxHsv = getMinMaxHsv(bgrRoi, 2);
        hsvMin = minMaxHsv.first;
        hsvMax = minMaxHsv.second;

        toHsv(image, hsvMin, hsvMax);

        Mat hsvRoi = hsv.submat(trackWindow);
        Mat maskRoi = mask.submat(trackWindow);

        ArrayList<Mat> hsvRois = new ArrayList<>();
        hsvRois.add(hsvRoi);
        Imgproc.calcHist(hsvRois, new MatOfInt(0), maskRoi, hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
    }

    /**
     * Finds the dominant colour in an image, and returns two values in HSV colour space to represent similar colours,
     * e.g. so you can keep all colours similar to the dominant colour.
     *
     * How the algorithm works:
     *
     * 1. Scale the frame down so that algorithm doesn't take too long.
     * 2. Segment the frame into different colours (number of colours determined by k)
     * 3. Find dominant cluster (largest area) and get its central colour point.
     * 4. Get range (min max) to represent similar colours.
     *
     * @param bgr The input frame, in BGR colour space.
     * @param k The number of segments to use (2 works well).
     * @return The min and max HSV colour values, which represent the colours similar to the dominant colour.
     */
    private Pair<Scalar, Scalar> getMinMaxHsv(Mat bgr, int k) {
        //Convert to HSV
        Mat input = new Mat();
        Imgproc.cvtColor(bgr, input, Imgproc.COLOR_BGR2BGRA, 3);

        //Scale image
        Size bgrSize = bgr.size();
        Size newSize = new Size();

        if (bgrSize.width > CamShiftTracker.KMEANS_IMG_SIZE || bgrSize.height > CamShiftTracker.KMEANS_IMG_SIZE) {

            if (bgrSize.width > bgrSize.height) {
                newSize.width = CamShiftTracker.KMEANS_IMG_SIZE;
                newSize.height = CamShiftTracker.KMEANS_IMG_SIZE / bgrSize.width * bgrSize.height;
            } else {
                newSize.width = CamShiftTracker.KMEANS_IMG_SIZE / bgrSize.height * bgrSize.width;
                newSize.height = CamShiftTracker.KMEANS_IMG_SIZE;
            }

            Imgproc.resize(input, input, newSize);
        }

        //Image quantization using k-means, see here for details of k-means algorithm: http://bit.ly/1JIvrlB
        Mat clusterData = new Mat();

        Mat reshaped = input.reshape(1, input.rows() * input.cols());
        reshaped.convertTo(clusterData, CvType.CV_32F, 1.0 / 255.0);
        Mat labels = new Mat();
        Mat centres = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 50, 1);
        Core.kmeans(clusterData, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centres);

        //Get num hits for each category
        int[] counts = new int[k];

        for (int i = 0; i < labels.rows(); i++) {
            int label = (int) labels.get(i, 0)[0];
            counts[label] += 1;
        }

        //Get cluster index with maximum number of members
        int maxCluster = 0;
        int index = -1;

        for (int i = 0; i < counts.length; i++) {
            int value = counts[i];

            if (value > maxCluster) {
                maxCluster = value;
                index = i;
            }
        }

        //Get cluster centre point hsv
        int r = (int) (centres.get(index, 2)[0] * 255.0);
        int g = (int) (centres.get(index, 1)[0] * 255.0);
        int b = (int) (centres.get(index, 0)[0] * 255.0);
        int sum = (r + g + b) / 3;

        //Get colour range
        Scalar min;
        Scalar max;

        int rg = Math.abs(r - g);
        int gb = Math.abs(g - b);
        int rb = Math.abs(r - b);
        int maxDiff = Math.max(Math.max(rg, gb), rb);

        if (maxDiff < 35 && sum > 120) { //white
            min = new Scalar(0, 0, 0);
            max = new Scalar(180, 40, 255);
        } else if (sum < 50 && maxDiff < 35) { //black
            min = new Scalar(0, 0, 0);
            max = new Scalar(180, 255, 40);
        } else {
            Mat bgrColour = new Mat(1, 1, CvType.CV_8UC3, new Scalar(r, g, b));
            Mat hsvColour = new Mat();

            Imgproc.cvtColor(bgrColour, hsvColour, Imgproc.COLOR_BGR2HSV, 3);
            double[] hsv = hsvColour.get(0, 0);

            int addition = 0;
            int minHue = (int) hsv[0] - colourRange;
            if (minHue < 0) {
                addition = Math.abs(minHue);
            }

            int maxHue = (int) hsv[0] + colourRange;

            min = new Scalar(Math.max(minHue, 0), 60, Math.max(35, hsv[2] - 30));
            max = new Scalar(Math.min(maxHue + addition, 180), 255, 255);
        }

        return new Pair<>(min, max);
    }

    //TODO: convert to local variables
    private void toHsv(Mat image, Scalar hsvMin, Scalar hsvMax) {
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV, 3);

        Core.inRange(hsv, hsvMin, hsvMax, mask);

        Mat output = new Mat();
        Imgproc.cvtColor(mask, output, Imgproc.COLOR_GRAY2BGR, 0);
        Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2RGBA, 0);
    }

    /**
     * Saves a Mat based image to /sdcard/ for debugging.
     *
     * @param frame The frame to save.
     * @param name The name of the file (without a file type).
     */
    public void saveFrame(Mat frame, String name)
    {
        Bitmap bmp = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bmp);
        this.saveFrame(bmp, name);
    }

    /**
     * Saves a Bitmap based image to /sdcard/ for debugging.
     *
     * @param frame The frame to save.
     * @param name The name of the file (without a file type).
     */
    public void saveFrame(Bitmap frame, String name) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/" + name + ".png");
            frame.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
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
}
