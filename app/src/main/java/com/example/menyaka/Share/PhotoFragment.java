package com.example.menyaka.Share;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.TransactionTooLargeException;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.camerakit.type.CameraSize;
import com.example.menyaka.NyakallaActivity;
import com.example.menyaka.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpegkit.Jpeg;
import com.jpegkit.JpegImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PhotoFragment extends Fragment{

    private CameraKitView cameraView;

    private AppCompatTextView facingText;
    private AppCompatTextView flashText;
    private AppCompatTextView previewSizeText;
    private AppCompatTextView photoSizeText;

    private ImageView photoButton, flipCamera, flashBTN, conditionIcon;
    private TextView videoDuration, camCondition;

    private Button permissionsButton;

    private JpegImageView capturedImage;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        cameraView = view.findViewById(R.id.camera);
        flipCamera = view.findViewById(R.id.flipCamera);
        flashBTN = view.findViewById(R.id.switchFlashButton);
        videoDuration = view.findViewById(R.id.videoDuration);
        camCondition = view.findViewById(R.id.camTypeBTN);
        conditionIcon = view.findViewById(R.id.conditionIcon);

        facingText = view.findViewById(R.id.facingText);
        flashText = view.findViewById(R.id.flashText);
        previewSizeText = view.findViewById(R.id.previewSizeText);
        photoSizeText = view.findViewById(R.id.photoSizeText);

        photoButton = view.findViewById(R.id.photoButton);

        capturedImage = view.findViewById(R.id.capturedImage);

        permissionsButton = view.findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.requestPermissions(getActivity());
            }
        });

        photoButton.setTag("PHOTO");

        cameraView.setPermissionsListener(new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                permissionsButton.setVisibility(View.GONE);
            }

            @Override
            public void onPermissionsFailure() {
                permissionsButton.setVisibility(View.VISIBLE);
            }
        });

        cameraView.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {
                Log.v("CameraKitView", "CameraListener: onOpened()");
            }

            @Override
            public void onClosed() {
                Log.v("CameraKitView", "CameraListener: onClosed()");
            }

        });

        cameraView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                Log.v("CameraKitView", "PreviewListener: onStart()");
                updateInfoText();
            }

            @Override
            public void onStop() {
                Log.v("CameraKitView", "PreviewListener: onStop()");
            }
        });

        cameraView.setGestureListener(new CameraKitView.GestureListener() {
            @Override
            public void onTap(CameraKitView cameraKitView, float v, float v1) {

                cameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView view, final byte[] photo) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final Jpeg jpeg = new Jpeg(photo);
                                capturedImage.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        capturedImage.setJpeg(jpeg);
                                        //startActivity(new Intent(getActivity(), NyakallaActivity.class));
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }

            @Override
            public void onLongTap(CameraKitView cameraKitView, float v, float v1) {
                Toast.makeText(getActivity(), "this is long tap", Toast.LENGTH_SHORT).show();
                videoDuration.setVisibility(View.VISIBLE);

                cameraView.captureVideo(new CameraKitView.VideoCallback() {
                    @Override
                    public void onVideo(CameraKitView cameraKitView, Object o) {
                        cameraKitView.startVideo();
                        Toast.makeText(getActivity(), "Recording has started", Toast.LENGTH_SHORT).show();

                        cameraKitView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cameraKitView.stopVideo();
                                Toast.makeText(getActivity(), "Video Stopped", Toast.LENGTH_SHORT).show();
                            }
                        }, 2500);
                    }
                });

            }

            @Override
            public void onDoubleTap(CameraKitView cameraKitView, float v, float v1) {
                Toast.makeText(getActivity(), "this was a double tap", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPinch(CameraKitView cameraKitView, float v, float v1, float v2) {
                Toast.makeText(getActivity(), "This was pinched", Toast.LENGTH_SHORT).show();

            }
        });

        flipCamera.setTag("back_facing");
        flashBTN.setTag("flash_off");

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flipCamera.getTag().equals("front_facing")){

                    cameraView.setFacing(CameraKit.FACING_BACK);
                    flipCamera.setTag("back_facing");
                    flashBTN.setVisibility(View.VISIBLE);

                }else if (flipCamera.getTag().equals("back_facing")){

                    cameraView.setFacing(CameraKit.FACING_FRONT);
                    flipCamera.setTag("front_facing");
                    flashBTN.setVisibility(View.INVISIBLE);

                }
            }
        });

        flashBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flashBTN.getTag().equals("flash_off")){
                    if (cameraView.getFlash() != CameraKit.FLASH_OFF) {
                        cameraView.setFlash(CameraKit.FLASH_OFF);
                        updateInfoText();
                    }
                    flashBTN.setTag("flash_auto");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_off_24);

                }else if (flashBTN.getTag().equals("flash_auto")){
                    if (cameraView.getFlash() != CameraKit.FLASH_ON) {
                        cameraView.setFlash(CameraKit.FLASH_ON);
                        updateInfoText();
                    }
                    flashBTN.setTag("flash_off");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_auto_24);
                }
            }
        });

        view.findViewById(R.id.cameraBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        view.findViewById(R.id.goToGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        view.findViewById(R.id.camSettingsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Camera Settings will appear hear", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.filterBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "filters will appear hear", Toast.LENGTH_SHORT).show();
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photoButton.getTag().equals("PHOTO")){
                    cameraView.captureImage(new CameraKitView.ImageCallback() {
                        @Override
                        public void onImage(CameraKitView view, final byte[] photo) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final Jpeg jpeg = new Jpeg(photo);
                                    capturedImage.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            capturedImage.setJpeg(jpeg);
                                            startActivity(new Intent(getActivity(), NyakallaActivity.class));
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                    /*
                    cameraView.captureImage(new CameraKitView.ImageCallback() {
                        @Override
                        public void onImage(CameraKitView view, byte[] photo) {

                            byte[] imageByte = photo;

                            File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
                            try{
                                FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                                outputStream.write(imageByte);
                                outputStream.close();

                                Intent intent = new Intent(getActivity(), NyakallaActivity.class);
                                intent.putExtra("capturedImage", imageByte);
                                intent.putExtra("camWidth", cameraView.getWidth());
                                intent.putExtra("camHeight", cameraView.getHeight());
                                startActivity(intent);

                            }catch (java.io.IOException e){
                                e.printStackTrace();
                                Log.e("CKDemo", "Exception in photo callback");
                            }
                        }
                    });*/

                }else if (photoButton.getTag().equals("VIDEO")){
                    Toast.makeText(getActivity(), "You will be able to start recording your video", Toast.LENGTH_SHORT).show();
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void updateInfoText() {
        String facingValue = cameraView.getFacing() == CameraKit.FACING_BACK ? "BACK" : "FRONT";
        facingText.setText(Html.fromHtml("<b>Facing:</b> " + facingValue));

        String flashValue = "OFF";
        switch (cameraView.getFlash()) {
            case CameraKit.FLASH_OFF: {
                flashValue = "OFF";
                break;
            }

            case CameraKit.FLASH_ON: {
                flashValue = "ON";
                break;
            }

            case CameraKit.FLASH_AUTO: {
                flashValue = "AUTO";
                break;
            }

            case CameraKit.FLASH_TORCH: {
                flashValue = "TORCH";
                break;
            }
        }
        flashText.setText(Html.fromHtml("<b>Flash:</b> " + flashValue));

        CameraSize previewSize = cameraView.getPreviewResolution();
        if (previewSize != null) {
            previewSizeText.setText(Html.fromHtml(String.format("<b>Preview Resolution:</b> %d x %d", previewSize.getWidth(), previewSize.getHeight())));
        }

        CameraSize photoSize = cameraView.getPhotoResolution();
        if (photoSize != null) {
            photoSizeText.setText(Html.fromHtml(String.format("<b>Photo Resolution:</b> %d x %d", photoSize.getWidth(), photoSize.getHeight())));
        }
    }
}