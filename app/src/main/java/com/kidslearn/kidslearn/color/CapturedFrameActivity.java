package com.kidslearn.kidslearn.color;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kidslearn.kidslearn.R;

public class CapturedFrameActivity extends Activity
{
	int flag = 0; // flag = 0 --> don't save image; flag = 1 --> save image

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_captured_frame);

		// ImageView with the captured image
		ImageView iv1 = (ImageView)findViewById(R.id.img_original);
		iv1.setImageBitmap(CamMainActivity.bitmap);

		// ImageView with the color mask
		ImageView iv2 = (ImageView)findViewById(R.id.img_filtered);
		iv2.setImageBitmap(CamMainActivity.bitmapMask);
	}

	/*** method for the button "save image" and "continue without saving" ***/
	public void setSaveFrame(View view)
	{
		flag = 1;
		callReturnActivity();
	}

	public void setDiscardFrame(View view)
	{
		flag = 0;
		callReturnActivity();
	}

	public void callReturnActivity()
	{
		Intent intent = new Intent();
		intent.putExtra(CamMainActivity.MESSAGE_FLAG, flag);
		setResult(RESULT_OK, intent);
		finish(); // exit current activity
	}
}
