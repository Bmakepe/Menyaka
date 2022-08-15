package com.example.menyaka.Share;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.menyaka.MobilePaymentActivity;
import com.example.menyaka.OtherCameraActivity;
import com.example.menyaka.PaymentPlanActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.DirectoryScanner;
import com.example.menyaka.Utils.FirebaseMethods;
import com.example.menyaka.Utils.GridViewAdapter;
import com.example.menyaka.Utils.MediaFilesScanner;
import com.example.menyaka.Utils.UniversalImageLoader;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class GalleryFragment extends Fragment {

    TextView nextScreen;
    ProgressBar progressBar;
    GridView gridView;
    ImageView camImage,close_gallery;
    ProgressDialog progressDialog;
    VideoView videoView;
    RelativeLayout rl;
    Spinner dirSpinner;

    private static final int NUM_GRID_COLUMNS = 4;
    String mPath;
    ArrayList<String> listPaths, dirPaths, directoryNames;
    GestureDetector gestureDetector;
    final String append = "file:/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallary,container,false);

        gridView = view.findViewById(R.id.galleryGrid);
        nextScreen = view.findViewById(R.id.gallery_next);
        progressBar = view.findViewById(R.id.galleryProgress);
        //progressBar.setVisibility(View.GONE);
        camImage = view.findViewById(R.id.camImage);
        videoView = view.findViewById(R.id.videoView);
        close_gallery = view.findViewById(R.id.gallery_close);
        dirSpinner = view.findViewById(R.id.spinnerGallery);
        rl = view.findViewById(R.id.gallery_rl);

        view.findViewById(R.id.gallery_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSpinner();
        hideImageView();

        //closing gallery
        close_gallery.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        //moving to next screen
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getActivity(), OtherCameraActivity.class));

                /*if(requireActivity().getIntent().getBooleanExtra("Upload Monyaka",false)) {

                    if(!MediaFilesScanner.isVideo(mPath)) {

                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.setTitle("Posting");
                        progressDialog.setMessage("Please wait");
                        progressDialog.show();
                        new FirebaseMethods(getActivity()).uploadNewPhoto(null, 0, mPath,null,null,true);
                    }else {
                        Toast.makeText(getContext(), "Videos are not allowed as profile!!", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Intent nextIntent = new Intent(getActivity(), NextScreenActivity.class);
                    nextIntent.putExtra("mPath", mPath);
                    startActivity(nextIntent);
                }*/
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupSpinner(){

        dirPaths = DirectoryScanner.getFileDirectories();
        directoryNames = DirectoryScanner.getDirectoryNames();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(requireActivity(),android.R.layout.simple_list_item_1,directoryNames);
        //arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dirSpinner.setAdapter(arrayAdapter);

        dirSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(listPaths !=null){ listPaths.clear();}

                //setting up gridView for selected directory
                setupGridView(dirPaths.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupGridView(final String selectedDirectory){

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);


        try {
            listPaths = new MediaFilesScanner(progressBar).execute(selectedDirectory).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        GridViewAdapter adapter = new GridViewAdapter(requireActivity(),R.layout.layout_grid_image_view,append, listPaths);
        gridView.setAdapter(adapter);

        if(listPaths !=null) {

            final int  position = 0;
            mPath = listPaths.get(position);
            if(MediaFilesScanner.isVideo(mPath)) { playVideo(mPath); }
            else { displayImage(mPath); }
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mPath = listPaths.get(position);
                if(MediaFilesScanner.isVideo(mPath)) { playVideo(mPath); }
                else { displayImage(mPath); }
            }
        });
    }

    private void playVideo(String path){

        camImage.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(path);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
    }

    private  void displayImage(String path){

        videoView.setVisibility(View.GONE);
        camImage.setVisibility(View.VISIBLE);
        //GlideImageLoader.loadImageWithTransition(getContext(), path,camImage);
        UniversalImageLoader.setImage(path, camImage, progressBar, append);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void hideImageView(){

        gestureDetector = new GestureDetector(getContext(),new com.example.menyaka.Utils.GestureDetector(rl,gridView));

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);
            }
        });
    }
}