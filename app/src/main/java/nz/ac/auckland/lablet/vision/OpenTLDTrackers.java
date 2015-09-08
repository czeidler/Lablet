package nz.ac.auckland.lablet.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.vision.tld.Tld;

/**
 * Created by Jamie on 8/09/2015.
 */
public class OpenTLDTrackers {

    private static String TAG = "OpenTLDTrackers";

    static {
        if(!OpenCVLoader.initDebug())
        {
            Log.i(TAG, "OpenCV initialisation failed");
        }
        else
        {
            Log.i(TAG, "OpenCV initialisation succeeded");
        }
    }



    Rect roiRect;

    private Mat currentGray = new Mat();
    private Mat lastGray = new Mat();
    private Tld tld = null;
    private Tld.ProcessFrameStruct processFrameStruct = null;
    private Properties tldProperties = new Properties();;

    private static final Size WORKING_FRAME_SIZE = new Size(144, 80);

    public OpenTLDTrackers(Context context)
    {
        InputStream propsIS = null;
        try{
            propsIS = context.getResources().openRawResource(R.raw.parameters);
            tldProperties = new Properties();
            tldProperties.load(propsIS);
        } catch (IOException e) {
            Log.e(TAG, "Can't load properties", e);
        }finally{
            if(propsIS != null){
                try {
                    propsIS.close();
                } catch (IOException e) {
                    Log.e(TAG, "Can't close props", e);
                }
            }
        }
    }

    public void setROI(Bitmap bmp, int x, int y, int width, int height)
    {
        Mat inputFrame = getResizedMat(bmp, WORKING_FRAME_SIZE);
        Size ratio = getResizeRatio(bmp, WORKING_FRAME_SIZE);
        roiRect =  new Rect(x, y, width, height);

        Imgproc.cvtColor(inputFrame, lastGray, Imgproc.COLOR_RGB2GRAY);
        tld = new Tld(tldProperties);
        Rect scaledDownTrackedBox = scaleDown(roiRect, ratio);
        Log.i(TAG, "Working Ratio: " + ratio + " / Tracking Box: " + roiRect + " / Scaled down to: " + scaledDownTrackedBox);
        tld.init(lastGray, scaledDownTrackedBox);
    }

    public Rect findObject(Bitmap bmp)
    {
        if(isROISet()) {
            Mat inputFrame = getResizedMat(bmp, WORKING_FRAME_SIZE);
            Size ratio = getResizeRatio(bmp, WORKING_FRAME_SIZE);

            Imgproc.cvtColor(inputFrame, currentGray, Imgproc.COLOR_RGB2GRAY);

            processFrameStruct = tld.processFrame(lastGray, currentGray);
            currentGray.copyTo(lastGray);

            Rect result = scaleUp(processFrameStruct.currentBBox, ratio);
            Log.i(TAG, "Found object: " + result.toString());

            return result;
        }

        return null;
    }

    public boolean isROISet()
    {
        return roiRect != null;
    }

    public Mat getResizedMat(Bitmap bmp, Size size)
    {
        Mat inputFrame = new Mat();
        Utils.bitmapToMat(bmp, inputFrame);

        Mat resized = new Mat();
        Imgproc.resize(inputFrame, resized, size);

        return resized;
    }

    public Size getResizeRatio(Bitmap bmp, Size size)
    {
        return new Size(bmp.getWidth() / size.width, bmp.getHeight() / size.height);
    }

    private static Point scaleUp(Point point, Size scale){
        if(point == null || scale == null) return null;
        return new Point(point.x * scale.width, point.y * scale.height);
    }

    private static Point scaleDown(Point point, Size scale){
        if(point == null || scale == null) return null;
        return new Point(point.x / scale.width, point.y / scale.height);
    }

    private static Rect scaleUp(Rect rect, Size scale) {
        if(rect == null || scale == null) return null;
        return new Rect(scaleUp(rect.tl(), scale), scaleUp(rect.br(), scale));
    }

    private static Rect scaleDown(Rect rect, Size scale) {
        if(rect == null || scale == null) return null;
        return new Rect(scaleDown(rect.tl(), scale), scaleDown(rect.br(), scale));
    }

}
