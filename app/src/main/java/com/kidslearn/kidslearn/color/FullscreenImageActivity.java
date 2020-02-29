package com.kidslearn.kidslearn.color;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import com.kidslearn.kidslearn.R;

import java.io.File;
import java.io.FilenameFilter;

public class FullscreenImageActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		/* To show image in fullscreen, first  obtain the reference at the file path, then
		 * visualize the image file in the ImageView defined in activity_fullscreen_image.xml layout */

		// getting image position in the ImageAdapter
		Intent intent = getIntent();
		int position = intent.getIntExtra(GalleryActivity.MESSAGE_POSITION, -1);
		if(position != -1)
		{
			// filter for obtain only jpeg images from the predefined directory
			FilenameFilter fileNameFilter = new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					if(name.lastIndexOf('.') > 0)
					{
						int lastIndex = name.lastIndexOf('.'); // getting last index of character '.'
						String str = name.substring(lastIndex); // getting file extension
						if(str.equals(".jpg") || str.equals(".jpeg")){ return true; } // compare file extension with 'jpg'
					}
					return false;
				}
			};
			// Path and images list
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/ColorDetector");
			// check if path already exist or create it
			if(!path.isDirectory()) path.mkdirs();
			else
			{
				File[] images = path.listFiles(fileNameFilter);
				setContentView(R.layout.activity_fullscreen_image);

				// get the ImageView and associate the image with it
				ImageView iv = (ImageView)findViewById(R.id.img_full);
				iv.setImageURI(Uri.parse(images[position].getAbsolutePath()));
				iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		}
		// if enter in this else the passed position is NOT valid; show message error
		else
		{
			setContentView(R.layout.activity_fullscreen_image);
			Toast.makeText(this, getString(R.string.position_error), Toast.LENGTH_LONG).show();
		}
	}

}