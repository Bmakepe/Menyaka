package com.example.menyaka.Share;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.example.menyaka.R;
import com.example.menyaka.Utils.CheckPermissions;
import com.example.menyaka.Utils.DirectoryScanner;
import com.example.menyaka.Utils.SectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MenyakaActivity extends AppCompatActivity {
    private static final String TAG = "MenyakaActivity";
    public TabLayout tabLayout;
    private Context context;

    private final int requestCode = 3;
    private final String[] permissionList = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menyaka);

        context = MenyakaActivity.this;

        if(Build.VERSION.SDK_INT >= 23&&!CheckPermissions.hasPermissions(context,permissionList)) {

            //Toast.makeText(mContext, "Please give the required permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,permissionList, requestCode);
        }else {
            //Loading all image and video directories
            new DirectoryScanner().execute(Environment.getExternalStorageDirectory().getAbsolutePath());
            setupViewPager();
            Log.d(TAG,"Scanning Files");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new GalleryFragment()); //index 0
        adapter.addFragment(new PhotoFragment());  //index 1
        //adapter.addFragment(new VideoFragment()); //index 2

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText("GALLERY");
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("PHOTO");
        //Objects.requireNonNull(tabLayout.getTabAt(2)).setText("VIDEO");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case 3 :
                if(CheckPermissions.hasPermissions(context,permissionList)){
                    //Loading all image and video directories
                    new DirectoryScanner().execute(Environment.getExternalStorageDirectory().getAbsolutePath());
                    setupViewPager();
                    Log.d(TAG,"Scanning Files");
                }else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }
}