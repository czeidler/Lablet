package nz.ac.auckland.lablet.vision;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import nz.ac.auckland.lablet.camera.CalibrationVideoTimeData;
import nz.ac.auckland.lablet.experiment.FrameDataModel;


public class VideoPlayer {

    public interface IListener {
        void onFinished();
    }

    private IListener listener;
    private boolean isPlaying = false;
    private FrameDataModel frameDataModel;
    private CalibrationVideoTimeData timeData;

    public VideoPlayer(FrameDataModel frameDataModel, CalibrationVideoTimeData timeData) {
        this.frameDataModel = frameDataModel;
        this.timeData = timeData;
    }

    public void play()
    {
        isPlaying = true;
        new BackgroundTask().execute();
    }

    public void stop()
    {
        isPlaying = false;
    }

    public void setListener(IListener listener)
    {
        this.listener = listener;
    }

    class BackgroundTask extends AsyncTask<Void, Integer, Void>
    {
        protected Void doInBackground(Void[] objects) {
            int startFrame = frameDataModel.getCurrentFrame();
            int endFrame = timeData.getClosestFrame(timeData.getAnalysisVideoEnd());

            for(int i = startFrame + 1; i <= endFrame && isPlaying; i++)
            {
                final int frameX = i;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        frameDataModel.setCurrentFrame(frameX);
                    }
                });

                SystemClock.sleep(300); //TODO: allow playback speed to be changed
            }

            return null;
        }

        protected void onProgressUpdate(Integer... frame) {

        }

        protected void onPostExecute(Void result) {
            if(listener != null)
            {
                listener.onFinished();
            }
        }
    }

}
