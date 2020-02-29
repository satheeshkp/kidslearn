package com.kidslearn.kidslearn.colortracker;


import android.app.ActivityManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kidslearn.kidslearn.R;
import com.kidslearn.kidslearn.shape.OverlayView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * the main activity - entry to the application
 */
public class ColorTrackerActivity extends AppCompatActivity implements CvCameraViewListener2 {
    /**
     * class name for debugging with logcat
     */
    private static final String TAG = com.kidslearn.kidslearn.shape.ShapeDetectionActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    /**
     * the camera view
     */
    private CameraBridgeViewBase mOpenCvCameraView;
    /**
     * for displaying Toast info messages
     */
    private Toast toast;
    /**
     * responsible for displaying images on top of the camera picture
     */
    private OverlayView overlayView;
    /**
     * whether or not to log the memory usage per frame
     */
    private static final boolean LOG_MEM_USAGE = true;
    /**
     * detect only red objects
     */
    private static final boolean DETECT_RED_OBJECTS_ONLY = true;
    /**
     * the lower red HSV range (lower limit)
     */
    private static final Scalar HSV_LOW_RED1 = new Scalar(0, 100, 100);
    /**
     * the lower red HSV range (upper limit)
     */
    private static final Scalar HSV_LOW_RED2 = new Scalar(10, 255, 255);
    /**
     * the upper red HSV range (lower limit)
     */
    private static final Scalar HSV_HIGH_RED1 = new Scalar(160, 100, 100);
    /**
     * the upper red HSV range (upper limit)
     */
    private static final Scalar HSV_HIGH_RED2 = new Scalar(179, 255, 255);
    /**
     * definition of RGB red
     */
    private static final Scalar RGB_RED = new Scalar(255, 0, 0);
    /**
     * frame size width
     */
    private static final int FRAME_SIZE_WIDTH = 640;
    /**
     * frame size height
     */
    private static final int FRAME_SIZE_HEIGHT = 480;
    /**
     * whether or not to use a fixed frame size -> results usually in higher FPS
     * 640 x 480
     */
    private static final boolean FIXED_FRAME_SIZE = true;
    /**
     * whether or not to use the database to display
     * an image on top of the camera
     * when false the objects are labeled with writing
     */


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorTrackerActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    TextToSpeech textToSpeech;
    String detectedShape="";

    private String[] getRequiredPermissions() {
        AppCompatActivity activity = this;
        try {
            PackageInfo info =
                    activity
                            .getPackageManager()
                            .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);


        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_my);
        // get the OverlayView responsible for displaying images on top of the camera
        overlayView = (OverlayView) findViewById(R.id.overlay_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        // Michael Troger
        if (FIXED_FRAME_SIZE) {
            mOpenCvCameraView.setMaxFrameSize(FRAME_SIZE_WIDTH, FRAME_SIZE_HEIGHT);
        }
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

//        mi = new ActivityManager.MemoryInfo();
//        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        if(toast != null)
            toast.cancel();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        time=System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void onCameraViewStarted(int width, int height) {}

    public void onCameraViewStopped() {}

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat dst = inputFrame.rgba();
        dst= detectColor(dst);

        // return the matrix / image to show on the screen
        return dst;

    }


    Mat detectColorMulti(Mat srcImg) {
        Mat blurImg = new Mat();
        Mat hsvImage = new Mat();
        Mat color_range_red = new Mat();
        Mat color_range_green = new Mat();
        Mat color_range = new Mat();

        //bluring image to filter noises
        Imgproc.GaussianBlur(srcImg, blurImg, new Size(5,5),0);

        //converting blured image from BGR to HSV
        Imgproc.cvtColor(blurImg, hsvImage, Imgproc.COLOR_BGR2HSV);

        //filtering red and green pixels based on given opencv HSV color range
        Core.inRange(hsvImage, new Scalar(0,50,50), new Scalar(5,255,255), color_range_red);
        Core.inRange(hsvImage, new Scalar(40,50,50), new Scalar(50,255,255), color_range_green);

        //applying bitwise or to detect both red and green color.
        Core.bitwise_or(color_range_red,color_range_green,color_range);

        return color_range;
    }

    Mat detectColor(Mat srcImg) {
//        Mat orginal = srcImg.clone();
        try {
            Mat blurImg = new Mat();
            Mat hsvImage = new Mat();
            Mat color_range = new Mat();

            Mat rgbFrame=new Mat();

            Imgproc.cvtColor(srcImg, rgbFrame, Imgproc.COLOR_RGBA2RGB); // Cant't convert directly rgba->hsv


            //bluring image to filter small noises.
            Imgproc.GaussianBlur(rgbFrame, blurImg, new Size(5, 5), 0);

            //converting blured image from BGR to HSV
            Imgproc.cvtColor(blurImg, hsvImage, Imgproc.COLOR_RGB2HSV);


//            Scalar[] rgbMin={new Scalar(0, 50, 50),new Scalar(40, 50,50),new Scalar(110, 50, 50)};
//            Scalar[] rgbMax={new Scalar(5, 255, 255),new Scalar(89, 255, 255),new Scalar(128, 255, 255)};

            for (int i=0;i<3;i++){
                //filtering pixels based on given HSV color range
                Core.inRange(hsvImage, new Scalar(40, 50,50), new Scalar(89, 255, 255), color_range);
//                Core.inRange(hsvImage,rgbMin[i] , rgbMax[i], color_range);

                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

                Imgproc.findContours(
                        color_range,
                        contours,
                        new Mat(),
                        Imgproc.RETR_TREE,
                        Imgproc.CHAIN_APPROX_SIMPLE
                );


                // loop over all found contours
                for (MatOfPoint cnt : contours) {
                    Rect rect = Imgproc.boundingRect(cnt);
                    if (rect.area() > 500) {
                        Imgproc.rectangle(srcImg, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 3);

                    }
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }

        return srcImg;
    }



    /**
     * Helper function to find a cosine of angle between vectors
     * from pt0->pt1 and pt0->pt2
     */
    private static double angle(Point pt1, Point pt2, Point pt0)
    {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt(
                (dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10
        );
    }

    /**
     * display a label in the center of the given contur (in the given image)
     * @param im the image to which the label is applied
     * @param label the label / text to display
     * @param contour the contour to which the label should apply
     */
    private void setLabel(Mat im, String label, MatOfPoint contour) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 3;//0.4;
        int thickness = 3;//1;
        int[] baseline = new int[1];

        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);

        Point pt = new Point(
                r.x + ((r.width - text.width) / 2),
                r.y + ((r.height + text.height) /2)
        );
        /*
        Imgproc.rectangle(
                im,
                new Point(r.x + 0, r.y + baseline[0]),
                new Point(r.x + text.width, r.y -text.height),
                new Scalar(255,255,255),
                -1
                );
        */

        Imgproc.putText(im, label, pt, fontface, scale, RGB_RED, thickness);

    }

    /**
     * makes an logcat/console output with the string detected
     * displays also a TOAST message and finally sends the command to the overlay
     * @param content the content of the detected barcode
     */
    private void doSomethingWithContent(String content) {
        Log.d(TAG, "content: " + content); // for debugging in console

        final String command = content;

        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                overlayView.changeCanvas(command);
            }
        });
    }


    long time=0;

    private void speak(String item){

        long timeDiff=System.currentTimeMillis()-time;

        if(timeDiff>3000){
            String data = item;
            Log.i("TTS", "button clicked: " + data);
            int speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null);

            if (speechStatus == TextToSpeech.ERROR) {
                Log.e("TTS", "Error in converting Text to Speech!");
            }
            time=System.currentTimeMillis();
        }
    }

}
