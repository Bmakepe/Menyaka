package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SharePostActivity extends AppCompatActivity implements LocationListener {

    CircleImageView myProfilePic, hisProfilePic;
    TextView shareBTN, checkInBTN, hisNameTV, hisLocationTV, hisCaptionTV, hisPostDate, myLocationCheckIn, audiencePicker;
    ImageView hisPostImage, galleryBTN, picToUpload;
    EditText myCaptionET;
    CardView picArea, uploadImageCV;
    LinearLayout locationArea;

    DatabaseReference momentRef, userRef, retailRef;
    FirebaseUser firebaseUser;

    String sharePostID, sharePostTimeStamp, myLocation, privacyProtection = "Public", userID, myCaption;

    GetTimeAgo getTimeAgo;

    UniversalFunctions universalFunctions;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    public static final int STORAGE_REQUEST_CODE = 100;
    public static final int IMAGE_PICK_GALLERY_CODE = 200;
    public static final int LOCATION_REQUEST_CODE = 300;

    String[] storagePermissions;
    private String[] locationPermission;

    private LocationManager locationManager;
    private double latitude, longitude;

    Uri image_uri;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_post);

        myProfilePic = findViewById(R.id.share_userProPic);
        hisProfilePic = findViewById(R.id.share_image_user);
        shareBTN = findViewById(R.id.postShareBTN);
        checkInBTN = findViewById(R.id.share_tag_location);
        hisNameTV = findViewById(R.id.share_username);
        hisPostDate = findViewById(R.id.share_postDate);
        hisLocationTV = findViewById(R.id.share_postCheckIn);
        hisCaptionTV = findViewById(R.id.share_post_desc);
        hisPostImage = findViewById(R.id.share_postImage);
        galleryBTN = findViewById(R.id.share_galleryBTN);
        myCaptionET = findViewById(R.id.shareCaption);
        picArea = findViewById(R.id.share_postPicArea);
        locationArea = findViewById(R.id.share_postLocationArea);
        myLocationCheckIn = findViewById(R.id.share_locationCheckIn);
        picToUpload = findViewById(R.id.share_preview_image);
        audiencePicker = findViewById(R.id.share_audience_picker);
        uploadImageCV = findViewById(R.id.image_post_to_be_shared);

        Intent intent = getIntent();
        sharePostID = intent.getStringExtra("postID");
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(SharePostActivity.this);

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        momentRef = FirebaseDatabase.getInstance().getReference("Moments");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMyDetails();
        getSharedPostDetails();

        hisProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userID.equals(firebaseUser.getUid())){
                    startActivity(new Intent(SharePostActivity.this, ProfileActivity.class));
                }else {
                    Intent intent = new Intent(SharePostActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                }
            }
        });

        hisPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SharePostActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", sharePostID);
                startActivity(intent);
            }
        });

        findViewById(R.id.post_share_back_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SharePostActivity.this, "You will be able to share this share", Toast.LENGTH_SHORT).show();

                myCaption = myCaptionET.getText().toString().trim();
                pd = new ProgressDialog(SharePostActivity.this);
                pd.setMessage("Loading...");

                if (TextUtils.isEmpty(myCaption)){
                    myCaptionET.setError("Whats Up?");
                    myCaptionET.requestFocus();
                }else{
                    pd.show();
                    uploadSharedPost(myCaption, "noImage");
                }
            }
        });

        galleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    PopupMenu popupMenu = new PopupMenu(SharePostActivity.this, galleryBTN, Gravity.START);

                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Photo Gallery");
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Camera");

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();

                            switch(id){
                                case 0:
                                    pickFromGallery();
                                    break;

                                case 1:
                                    startActivity(new Intent(SharePostActivity.this, OtherCameraActivity.class));
                                    break;

                                default:
                                    Toast.makeText(SharePostActivity.this, "Illegal Selection", Toast.LENGTH_SHORT).show();
                            }

                            return false;
                        }
                    });

                    popupMenu.show();
                }

            }
        });

        findViewById(R.id.share_store_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });

        findViewById(R.id.share_audience_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(SharePostActivity.this, audiencePicker, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Public");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Only To My Followers");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id){
                            case 0:
                                privacyProtection = "Public";
                                audiencePicker.setText(privacyProtection);
                                break;

                            case 1:
                                privacyProtection = "Private";
                                audiencePicker.setText("Only To My Followers");
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        findViewById(R.id.share_editImageBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SharePostActivity.this, "You will be able to edit this image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadSharedPost(String myCaption, String imageUri) {
        final String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> sharedMap = new HashMap<>();
        String postID = momentRef.push().getKey();

        sharedMap.put("momentId", postID);
        sharedMap.put("moment_desc", myCaption);
        sharedMap.put("username", firebaseUser.getUid());
        sharedMap.put("imageUrl", imageUri);
        sharedMap.put("videoUrl", "noVideo");
        sharedMap.put("timestamp", timestamp);
        sharedMap.put("privacy", privacyProtection);
        sharedMap.put("postType", "sharedTextPost");
        sharedMap.put("sharedPost", sharePostID);

        if (!TextUtils.isEmpty(myLocationCheckIn.getText().toString())) {
            sharedMap.put("latitude", latitude);
            sharedMap.put("longitude", longitude);
        }

        momentRef.child(postID).setValue(sharedMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        myCaptionET.setText("");
                        pd.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SharePostActivity.this, "Could not upload shared post", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...Detecting Your Location", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            myLocation = address + ", " + city + ", " + state + ", " + country;

            myLocationCheckIn.setText(myLocation);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void pickFromGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void getSharedPostDetails() {
        momentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    if (moment.getMomentId().equals(sharePostID)){

                        userID = moment.getUsername();

                        switch (moment.getPostType()){
                            case "textPost":
                                picArea.setVisibility(View.GONE);
                                break;

                            case "imagePost":
                                hisPostImage.setVisibility(View.VISIBLE);
                                try{
                                    Picasso.get().load(moment.getImageUrl()).into(hisPostImage);
                                }catch (NullPointerException ignored){}
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown Post Type Identified", Toast.LENGTH_SHORT).show();
                                break;
                        }

                        try{
                            sharePostTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(moment.getTimestamp()), SharePostActivity.this);
                            hisPostDate.setText(sharePostTimeStamp);
                        }catch (NumberFormatException ignored){}

                        if (moment.getMoment_desc().equals("")){
                            hisCaptionTV.setVisibility(View.GONE);
                        }else{
                            hisCaptionTV.setVisibility(View.VISIBLE);
                            hisCaptionTV.setText(moment.getMoment_desc());
                        }

                        try{
                            universalFunctions.findAddress(moment, hisLocationTV, locationArea);
                        }catch (Exception ignored){}

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dss : snapshot.getChildren()){
                                    User user = dss.getValue(User.class);

                                    if (user.getId().equals(moment.getUsername())){
                                        try{
                                            Picasso.get().load(user.getImageUrl()).into(hisProfilePic);
                                        }catch (NullPointerException ignored){}

                                        hisNameTV.setText(user.getUsername());
                                    }else{
                                        retailRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Retail retail = ds.getValue(Retail.class);

                                                    if (retail.getRetail_id().equals(moment.getUsername())){

                                                        try{
                                                            Picasso.get().load(retail.getImageUrl()).into(hisProfilePic);
                                                        }catch (NullPointerException ignored){}

                                                        hisNameTV.setText(retail.getRetailName());
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMyDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getId().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageUrl()).into(myProfilePic);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "You need to enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }else if (requestCode == LOCATION_REQUEST_CODE){
            if (grantResults.length > 0){
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (locationAccepted){
                    detectLocation();
                }else{
                    Toast.makeText(this, "You need location permission enabled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                myCaptionET.setText(result.get(0));
            }
        }else if (requestCode == IMAGE_PICK_GALLERY_CODE){
            if (resultCode == RESULT_OK){
                image_uri = data.getData();
                picToUpload.setVisibility(View.VISIBLE);
                uploadImageCV.setVisibility(View.VISIBLE);
                picToUpload.setImageURI(image_uri);
            }
        }

        /*switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    caption.setText(result.get(0));
                }
                break;

            case IMAGE_PICK_GALLERY_CODE:
                if (resultCode == RESULT_OK){
                    image_uri = data.getData();
                    picToUpload.setVisibility(View.VISIBLE);
                    picToUpload.setImageURI(image_uri);
                }
                break;

            default:
                Toast.makeText(this, "An Error Has Occurred...", Toast.LENGTH_SHORT).show();
        }*/

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}