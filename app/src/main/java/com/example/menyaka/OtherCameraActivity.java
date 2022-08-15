package com.example.menyaka;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Utils.TesterActivity;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

public class OtherCameraActivity extends AppCompatActivity {

    CameraView cameraView;
    ImageView conditionIcon, flipCamera, flashBTN, photoButton;
    private TextView camCondition;
    byte[] imageByte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta_style);

        cameraView = findViewById(R.id.cameraKit);
        conditionIcon = findViewById(R.id.conditionIcon);
        photoButton = findViewById(R.id.takePic);
        flipCamera = findViewById(R.id.flipCamera);
        flashBTN = findViewById(R.id.switchFlashButton);
        camCondition = findViewById(R.id.camTypeBTN);

        flipCamera.setTag("back_facing");
        flashBTN.setTag("flash_off");
        photoButton.setTag("PHOTO");

        cameraView.setFocus(CameraKit.Constants.FOCUS_TAP);

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened() {
                super.onCameraOpened();
            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                imageByte = jpeg;
                //Bitmap result = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                getByteImage(jpeg);

                Intent camIntent = new Intent(OtherCameraActivity.this, NyakallaActivity.class);
                startActivity(camIntent);
            }

            @Override
            public void onPictureTaken(YuvImage yuv) {
                super.onPictureTaken(yuv);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(photoButton.getTag().equals("PHOTO")){

                    cameraView.captureImage();
                    //startActivity(new Intent(OtherCameraActivity.this, NyakallaActivity.class));

                }else if (photoButton.getTag().equals("VIDEO")){
                    Toast.makeText(OtherCameraActivity.this, "You will be able to start recording your video", Toast.LENGTH_SHORT).show();
                }
            }
        });

        camCondition.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if(camCondition.getText().equals("VIDEO")){

                    camCondition.setText("PHOTO");
                    conditionIcon.setImageResource(R.drawable.ic_baseline_camera_24);
                    photoButton.setImageResource(R.drawable.record_circle);
                    photoButton.setTag("VIDEO");

                }else if (camCondition.getText().equals("PHOTO")){

                    camCondition.setText("VIDEO");
                    conditionIcon.setImageResource(R.drawable.record_circle);
                    photoButton.setImageResource(R.drawable.ic_baseline_camera_24);
                    photoButton.setTag("PHOTO");

                }
            }
        });

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flipCamera.getTag().equals("front_facing")){

                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    flipCamera.setTag("back_facing");
                    flashBTN.setVisibility(View.VISIBLE);

                }else if (flipCamera.getTag().equals("back_facing")){

                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    flipCamera.setTag("front_facing");
                    flashBTN.setVisibility(View.INVISIBLE);

                }
            }
        });

        flashBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flashBTN.getTag().equals("flash_off")){

                    flashBTN.setTag("flash_auto");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_off_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);

                }else if (flashBTN.getTag().equals("flash_auto")){

                    flashBTN.setTag("flash_on");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_auto_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);

                }else if(flashBTN.getTag().equals("flash_on")){

                    flashBTN.setTag("flash_torch");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_on_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_ON);

                }else if(flashBTN.getTag().equals("flash_torch")){

                    flashBTN.setTag("flash_off");
                    flashBTN.setImageResource(R.drawable.ic_baseline_highlight_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                }
            }
        });

        findViewById(R.id.cameraBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.skipCamBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OtherCameraActivity.this, NyakallaActivity.class));
            }
        });

        findViewById(R.id.filterBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OtherCameraActivity.this, TesterActivity.class));
            }
        });
    }

    public byte[] getByteImage(byte[] jpeg) {
        return jpeg;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}