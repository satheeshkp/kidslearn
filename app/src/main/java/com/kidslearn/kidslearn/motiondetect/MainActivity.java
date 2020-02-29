package com.kidslearn.kidslearn.motiondetect;

/*******************************************************************************************
 * Modifed by Satheesh kumar
 * 29/02/2020
 * 
 * This is a modified version of the Camera Preview sample app provided by OpenCV. The
 * BackgroundSubtractorMOG algorithm was used to identify objects in motion from the videos
 * and draws overlays over them. The ideal is to have good contours drawn around the objects
 * that are moving. 
 * 
 * 
 *******************************************************************************************/

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.kidslearn.kidslearn.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private CameraBridgeViewBase mOpenCvCameraView;
	private BackgroundSubtractorMOG2 sub;
	private Mat mGray;
	private Mat mRgb;
	private Mat mFGMask;
	private List<MatOfPoint> contours;
	private double lRate = 0.5;
	
//	private SeekBar sb;
	
	// Initialization required by apps using OpenCV Manager
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) 
	{
		@Override
		public void onManagerConnected(int status) 
		{
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

	public MainActivity() {
		//Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_my);
		//sets up camera
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

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
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) 
	{
		//creates a new BackgroundSubtractorMOG class with the arguments
		sub=Video.createBackgroundSubtractorMOG2();
//		sub = new BackgroundSubtractorMOG2(3, 4, 0.8, 0.5);
		
		//creates matrices to hold the different frames
		mRgb = new Mat();
		mFGMask = new Mat();
		mGray = new Mat();
		
		//arraylist to hold individual contours
		contours = new ArrayList<MatOfPoint>();

	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) 
	{   
		contours.clear();
		//gray frame because it requires less resource to process
		mGray = inputFrame.gray(); 
		
		//this function converts the gray frame into the correct RGB format for the BackgroundSubtractorMOG apply function
		Imgproc.cvtColor(mGray, mRgb, Imgproc.COLOR_GRAY2RGB); 
		
		//apply detects objects moving and produces a foreground mask
		//the lRate updates dynamically dependent upon seekbar changes
		sub.apply(mRgb, mFGMask, lRate); 

		//erode and dilate are used  to remove noise from the foreground mask
		Imgproc.erode(mFGMask, mFGMask, new Mat());
		Imgproc.dilate(mFGMask, mFGMask, new Mat());
		
		//drawing contours around the objects by first called findContours and then calling drawContours
		//RETR_EXTERNAL retrieves only external contours
		//CHAIN_APPROX_NONE detects all pixels for each contour
		Imgproc.findContours(mFGMask, contours, new Mat(), Imgproc.RETR_EXTERNAL , Imgproc.CHAIN_APPROX_NONE);
		
		//draws all the contours in red with thickness of 2
		Imgproc.drawContours(mRgb, contours, -1, new Scalar(255, 0, 0), 2);
		
		return mRgb;
	}
}
