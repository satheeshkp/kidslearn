package com.kidslearn.kidslearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kidslearn.kidslearn.classifier.ClassifierActivity;
import com.kidslearn.kidslearn.color.CamMainActivity;
import com.kidslearn.kidslearn.colortracker.ColorTrackerActivity;
import com.kidslearn.kidslearn.invisible.InvisibleActivity;
import com.kidslearn.kidslearn.numberrecog.MainActivity;
import com.kidslearn.kidslearn.shape.ShapeDetectionActivity;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {


    Button btShapeDetection,btColorDetection,btColorTracker;
    Button btNumberRecog;
    Button btClassifier;
    Button btInvisible;
    Button btMotionDetection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        btShapeDetection=findViewById(R.id.bt_shape_detection);
        btColorDetection=findViewById(R.id.bt_color_detection);
        btColorTracker=findViewById(R.id.bt_color_tracker);
        btNumberRecog=findViewById(R.id.bt_number_recog);
        btClassifier=findViewById(R.id.bt_classifier);
        btInvisible=findViewById(R.id.bt_invisible);
        btMotionDetection=findViewById(R.id.bt_motiondetect);
        btShapeDetection.setOnClickListener(this);
        btColorDetection.setOnClickListener(this);
        btColorTracker.setOnClickListener(this);
        btNumberRecog.setOnClickListener(this);
        btClassifier.setOnClickListener(this);
        btInvisible.setOnClickListener(this);
        btMotionDetection.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v.getId()==btShapeDetection.getId()){
            Intent intent=new Intent(this, ShapeDetectionActivity.class);
            startActivity(intent);
        }
        else if(v.getId()==btColorTracker.getId()){
            Intent intent=new Intent(this, ColorTrackerActivity.class);
            startActivity(intent);
        }else if(v.getId()==btNumberRecog.getId()){
            Intent intent=new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if(v.getId()==btClassifier.getId()){
            Intent intent=new Intent(this, ClassifierActivity.class);
            startActivity(intent);
        }
        else if(v.getId()==btInvisible.getId()){
            Intent intent=new Intent(this, InvisibleActivity.class);
            startActivity(intent);
        }
        else if(v.getId()==btMotionDetection.getId()){
            Intent intent=new Intent(this, com.kidslearn.kidslearn.motiondetect.MainActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent=new Intent(this, CamMainActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
