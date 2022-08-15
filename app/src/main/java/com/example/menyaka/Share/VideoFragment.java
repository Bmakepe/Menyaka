package com.example.menyaka.Share;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Html;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.camerakit.type.CameraSize;
import com.example.menyaka.R;
import com.jpegkit.JpegImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static androidx.core.content.ContextCompat.getSystemService;

public class VideoFragment extends Fragment{

    ImageView btn_capture, flipCamera, flashBTN;
    CameraKitView videoCamKit;
    TextView videoDuration;

    private AppCompatTextView facingText;
    private AppCompatTextView flashText;
    private AppCompatTextView previewSizeText;
    private AppCompatTextView photoSizeText;

    private Button permissionsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        flipCamera = view.findViewById(R.id.flipCamera);
        flashBTN = view.findViewById(R.id.switchFlashButton);

        facingText = view.findViewById(R.id.facingText);
        flashText = view.findViewById(R.id.flashText);
        previewSizeText = view.findViewById(R.id.previewSizeText);
        photoSizeText = view.findViewById(R.id.photoSizeText);
        videoDuration = view.findViewById(R.id.videoDuration);

        btn_capture = view.findViewById(R.id.videoButton);
        videoCamKit = view.findViewById(R.id.videoCamera);

        permissionsButton = view.findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoCamKit.requestPermissions(getActivity());
            }
        });

        videoCamKit.setPermissionsListener(new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                permissionsButton.setVisibility(View.GONE);
            }

            @Override
            public void onPermissionsFailure() {
                permissionsButton.setVisibility(View.VISIBLE);
            }
        });

        videoCamKit.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {
                Log.v("CameraKitView", "CameraListener: onOpened()");
            }

            @Override
            public void onClosed() {
                Log.v("CameraKitView", "CameraListener: onClosed()");
            }
        });

        btn_capture.setOnClickListener(videoOnClickListener);

        videoCamKit.setPreviewListener(new CameraKitView.PreviewListener() {
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

        flipCamera.setTag("back_facing");
        flashBTN.setTag("flash_off");

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flipCamera.getTag().equals("front_facing")){

                    videoCamKit.setFacing(CameraKit.FACING_BACK);
                    flipCamera.setTag("back_facing");

                }else if (flipCamera.getTag().equals("back_facing")){

                    videoCamKit.setFacing(CameraKit.FACING_FRONT);
                    flipCamera.setTag("front_facing");

                }
            }
        });

        flashBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flashBTN.getTag().equals("flash_off")){
                    if (videoCamKit.getFlash() != CameraKit.FLASH_OFF) {
                        videoCamKit.setFlash(CameraKit.FLASH_OFF);
                        updateInfoText();
                    }
                    flashBTN.setTag("flash_auto");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_off_24);

                }else if (flashBTN.getTag().equals("flash_auto")){
                    if (videoCamKit.getFlash() != CameraKit.FLASH_ON) {
                        videoCamKit.setFlash(CameraKit.FLASH_ON);
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

        return view;
    }

    private void updateInfoText() {
        String facingValue = videoCamKit.getFacing() == CameraKit.FACING_BACK ? "BACK" : "FRONT";
        facingText.setText(Html.fromHtml("<b>Facing:</b> " + facingValue));

        String flashValue = "OFF";
        switch (videoCamKit.getFlash()) {
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

        CameraSize previewSize = videoCamKit.getPreviewResolution();
        if (previewSize != null) {
            previewSizeText.setText(Html.fromHtml(String.format("<b>Preview Resolution:</b> %d x %d", previewSize.getWidth(), previewSize.getHeight())));
        }

        CameraSize photoSize = videoCamKit.getPhotoResolution();
        if (photoSize != null) {
            photoSizeText.setText(Html.fromHtml(String.format("<b>Photo Resolution:</b> %d x %d", photoSize.getWidth(), photoSize.getHeight())));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        videoCamKit.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        videoCamKit.onResume();
    }

    @Override
    public void onPause() {
        videoCamKit.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        videoCamKit.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        videoCamKit.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnClickListener videoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            videoCamKit.captureVideo(new CameraKitView.VideoCallback() {
                @Override
                public void onVideo(CameraKitView cameraKitView, Object o) {
                    cameraKitView.startVideo();
                    videoDuration.setVisibility(View.VISIBLE);
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
    };

}