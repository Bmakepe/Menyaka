package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.menyaka.Adapters.TaggedItemAdapter;
import com.example.menyaka.Models.User;
import com.example.menyaka.Share.LocationActivity;
import com.example.menyaka.Share.TaggingActivity;
import com.example.menyaka.Utils.TesterActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NyakallaActivity extends AppCompatActivity implements LocationListener {

    ImageView voiceBTN, picToUpload, galleryBTN, usersTagBTN, storesTagBTN;
    EditText caption;
    LinearLayout tagStoreBTN, taggedPeople, taggedLocation, audienceSet;
    ProgressBar loader;
    String myCaption, myLocation, privacyProtection = "Public";
    CircleImageView userProPic;
    TextView audience_picker, locationCheckIn;
    CardView imageCardView, videoCardView;

    ProgressDialog pd;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    public static final int STORAGE_REQUEST_CODE = 100;
    public static final int IMAGE_PICK_GALLERY_CODE = 200;
    public static final int LOCATION_REQUEST_CODE = 300;
    private static final int PICK_VIDEO_REQUEST = 400;

    private String[] locationPermission;
    String[] storagePermissions;

    private LocationManager locationManager;
    private double latitude, longitude;

    Uri image_uri;
    String myUri = "";
    StorageTask uploadTask;

    StorageReference storageReference;
    DatabaseReference postReference, userReference, retailRef;
    FirebaseUser firebaseUser;

    ArrayList<String> taggedFriends;

    //for getting tagged stores
    RecyclerView storesRecycler;
    RecyclerView friendsRecycler;
    TaggedItemAdapter itemAdapter;

    //for video gallery
    private Uri videoURI;

    VideoView videoView;
    ConstraintLayout videoArea;
    private String mediaUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nyakalla);

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        voiceBTN = findViewById(R.id.speak_now);
        picToUpload = findViewById(R.id.preview_image);
        caption = findViewById(R.id.caption_text);
        taggedPeople = findViewById(R.id.people_tag);
        taggedLocation = findViewById(R.id.store_location);
        loader = findViewById(R.id.upload_progress);
        galleryBTN = findViewById(R.id.galleryBTN);
        userProPic = findViewById(R.id.userProPic);
        tagStoreBTN = findViewById(R.id.store_tag);
        audienceSet = findViewById(R.id.audience_set);
        audience_picker = findViewById(R.id.audience_picker);
        storesRecycler = findViewById(R.id.taggedStoresRecycler);
        friendsRecycler = findViewById(R.id.friendsRecycler);
        locationCheckIn = findViewById(R.id.locationCheckIn);
        videoView = findViewById(R.id.previewCameraSelectedVideo);
        videoArea = findViewById(R.id.videoArea);
        imageCardView = findViewById(R.id.imageCardView);
        videoCardView = findViewById(R.id.videoCardView);
        usersTagBTN = findViewById(R.id.friendsTagger);
        storesTagBTN = findViewById(R.id.storesTagger);

        Bundle intent = getIntent().getExtras();
        if(intent != null) {
            byte[] byteArray = intent.getByteArray("capturedImage");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            picToUpload.setVisibility(View.VISIBLE);
            picToUpload.setImageBitmap(bmp);
        } else{
            Toast.makeText(this, "You will not upload camera media post", Toast.LENGTH_SHORT).show();
        }

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("PostImages");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postReference = FirebaseDatabase.getInstance().getReference("Moments");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");

        getStores();
        getFriendsList();
        getUserDetails();

        galleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    PopupMenu popupMenu = new PopupMenu(NyakallaActivity.this, galleryBTN, Gravity.START);

                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "Photo Gallery");
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Video Gallery");
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Camera");

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();

                            switch (id){
                                case 0:
                                    pickFromGallery();
                                    break;

                                case 1:
                                    chooseVideo();
                                    break;

                                case 2:
                                    startActivity(new Intent(NyakallaActivity.this, OtherCameraActivity.class));
                                    break;

                                default:
                                    Toast.makeText(NyakallaActivity.this, "Illegal Selection", Toast.LENGTH_SHORT).show();

                            }

                            return false;
                        }
                    });

                    popupMenu.show();
                }
            }
        });

        voiceBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });

        findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myCaption = caption.getText().toString().trim();
                pd = new ProgressDialog(NyakallaActivity.this);
                pd.setMessage("Loading...");

                if(TextUtils.isEmpty(myCaption)){
                    caption.setError("Whats Up?");
                    caption.requestFocus();
                }else if (image_uri != null){
                    uploadPost(myCaption, String.valueOf(image_uri));
                    Toast.makeText(NyakallaActivity.this, "Uploading image post", Toast.LENGTH_SHORT).show();
                }else if (videoURI != null){
                    uploadVideo(myCaption);
                    Toast.makeText(NyakallaActivity.this, "Uploading video post", Toast.LENGTH_SHORT).show();
                }else{
                    uploadPost(myCaption, "noImage");
                    Toast.makeText(NyakallaActivity.this, "Uploading text only post", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.share_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        storesTagBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NyakallaActivity.this, TaggingActivity.class);
                intent1.putExtra("headerText", "storeTags");
                startActivity(intent1);
            }
        });

        usersTagBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NyakallaActivity.this, TaggingActivity.class);
                intent1.putExtra("headerText", "friendTags");
                startActivity(intent1);
            }
        });

        taggedLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }

                //startActivity(new Intent(NyakallaActivity.this, LocationActivity.class));
                //Toast.makeText(NyakallaActivity.this, "You will be able to show us your location", Toast.LENGTH_SHORT).show();
            }
        });

        audienceSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(NyakallaActivity.this, audienceSet, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Public");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Only To My Followers");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id){
                            case 0:
                                privacyProtection = "Public";
                                audience_picker.setText(privacyProtection);
                                break;

                            case 1:
                                privacyProtection = "Private";
                                audience_picker.setText("Only To My Followers");
                                break;

                            default:
                                Toast.makeText(NyakallaActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        findViewById(R.id.editImageBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NyakallaActivity.this, "You will be able to edit this image", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.editVideoBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NyakallaActivity.this, "You will be able to edit this video", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserDetails() {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if(user.getId().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageUrl()).into(userProPic);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadVideo(String myCaption) {

        pd.show();
        if(videoURI != null){

            final String timestamp = String.valueOf(System.currentTimeMillis());

            long systemMillis=System.currentTimeMillis();
            StorageReference ref= FirebaseStorage.getInstance().getReference()
                    .child("videos")
                    .child(systemMillis + "." + getFileExtension(videoURI));


            //UPLOADING TASK
            UploadTask uploadTask=ref.putFile(videoURI);

            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d("PROGRESS", "Upload is " + progress + "% done");
            })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(NyakallaActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NyakallaActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    });


            //RETREIVING THE DOWNLOAD URL

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    mediaUrl = downloadUri.toString();
                    Log.i("downloadTAG",mediaUrl);


                    HashMap<String, Object> videoMap = new HashMap<>();
                    String postID = postReference.push().getKey();

                    videoMap.put("momentId", postID);
                    videoMap.put("moment_desc", myCaption);
                    videoMap.put("username", firebaseUser.getUid());
                    videoMap.put("videoUrl", mediaUrl);
                    videoMap.put("imageUrl", "noImage");
                    videoMap.put("timestamp", timestamp);
                    videoMap.put("privacy", privacyProtection);
                    videoMap.put("postType", "videoPost");

                    if (!TextUtils.isEmpty(locationCheckIn.getText().toString())) {
                        videoMap.put("latitude", latitude);
                        videoMap.put("longitude", longitude);
                    }

                    postReference.child(postID).setValue(videoMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    caption.setText("");
                                    videoView.setVideoURI(null); 
                                    Toast.makeText(NyakallaActivity.this, "Done Posting Your Video", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(NyakallaActivity.this, MainActivity.class));

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                    /*postReference.child(postID).setValue(videoMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    caption.setText("");
                                    picToUpload.setImageURI(null);
                                    Toast.makeText(NyakallaActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(NyakallaActivity.this, MainActivity.class));

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });*/


                } else {
                    // Handle failures

                }
            });
        }
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void getFriendsList() {
        List<String> allFollowing = new ArrayList<>();
        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        final DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");
        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFollowing.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    allFollowing.add(ds.getKey());
                }
                itemAdapter = new TaggedItemAdapter(NyakallaActivity.this, allFollowing);
                friendsRecycler.setAdapter(itemAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStores() {
        List<String> myStores = new ArrayList<>();
        storesRecycler.hasFixedSize();
        storesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        final DatabaseReference myStoreRef = FirebaseDatabase.getInstance().getReference("MyStores").child(firebaseUser.getUid());
        myStoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myStores.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    myStores.add(ds.getKey());
                }
                itemAdapter = new TaggedItemAdapter(NyakallaActivity.this, myStores);
                storesRecycler.setAdapter(itemAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

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

            locationCheckIn.setText(myLocation);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /*private void getTaggedFriends() {

        if(taggedFriends != null){

            Toast.makeText(this, "You will be able to tag " + taggedFriends.size() + " friends on this posts", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You have no tagged friends", Toast.LENGTH_SHORT).show();
        }
        //taggedFriends = (ArrayList<String>) friendsIntent.getSerializableExtra("taggedFriends");

    }*/

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadPost(String myCaption, String imageUri) {
        pd.show();
        final String timestamp = String.valueOf(System.currentTimeMillis());

        if(!imageUri.equals("noImage")){

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(image_uri));

            uploadTask = fileReference.putFile(image_uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        HashMap<String, Object> hashMap = new HashMap<>();

                        String postID = postReference.push().getKey();

                        hashMap.put("momentId", postID);
                        hashMap.put("moment_desc", myCaption);
                        hashMap.put("username", firebaseUser.getUid());
                        hashMap.put("imageUrl", myUri);
                        hashMap.put("videoUrl", "noVideo");
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("privacy", privacyProtection);
                        hashMap.put("postType", "imagePost");


                        if (!TextUtils.isEmpty(locationCheckIn.getText().toString())) {
                            hashMap.put("latitude", latitude);
                            hashMap.put("longitude", longitude);
                        }

                        postReference.child(postID).setValue(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        caption.setText("");
                                        picToUpload.setImageURI(null);

                                        pd.dismiss();

                                        startActivity(new Intent(NyakallaActivity.this, MainActivity.class));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(NyakallaActivity.this, "Could not upload the post", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
            });
        }else{
            HashMap<String, Object> hashMap = new HashMap<>();

            String postID = postReference.push().getKey();

            hashMap.put("momentId", postID);
            hashMap.put("moment_desc", myCaption);
            hashMap.put("username", firebaseUser.getUid());
            hashMap.put("imageUrl", "noImage");
            hashMap.put("videoUrl", "noVideo");
            hashMap.put("timestamp", timestamp);
            hashMap.put("privacy", privacyProtection);
            hashMap.put("postType", "textPost");

            if (!TextUtils.isEmpty(locationCheckIn.getText().toString())) {
                hashMap.put("latitude", latitude);
                hashMap.put("longitude", longitude);
            }

            postReference.child(postID).setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            caption.setText("");
                            pd.dismiss();
                            finish();

                            //startActivity(new Intent(NyakallaActivity.this, MainActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NyakallaActivity.this, "Could not upload the post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Whats Up?");

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }else{
            Toast.makeText(this, "Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
        /*try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }catch(Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        //for getting tagged friends
        Intent friendsIntent = getIntent();
        taggedFriends = friendsIntent.getStringArrayListExtra("taggedFriends");

        if(taggedFriends != null){

            for(int i = 0; i < taggedFriends.size(); i++){
                Toast.makeText(this, "the id that is contained in here is " + taggedFriends.get(i), Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "You will be able to tag " + taggedFriends.size() + " friends on this posts", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You have no tagged friends", Toast.LENGTH_SHORT).show();
        }

        //getTaggedFriends();
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
                caption.setText(result.get(0));
            }
        }else if (requestCode == IMAGE_PICK_GALLERY_CODE){
            if (resultCode == RESULT_OK){
                image_uri = data.getData();
                picToUpload.setVisibility(View.VISIBLE);
                imageCardView.setVisibility(View.VISIBLE);
                picToUpload.setImageURI(image_uri);
            }
        }else if (requestCode==PICK_VIDEO_REQUEST && resultCode == -1 && data!=null && data.getData()!=null){
            videoURI=data.getData();

            //video set to video view in the next activity
            videoArea.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoCardView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(videoURI);
            videoView.setOnPreparedListener(mp -> {

                //scaling it to a correct size
                float videoRatio=mp.getVideoWidth()/(float)mp.getVideoHeight();
                float screenRatio=videoView.getWidth()/(float)videoView.getHeight();
                float scale=videoRatio/screenRatio;

                if(scale >=1f) videoView.setScaleX(scale);
                else videoView.setScaleY(1f/scale);

                mp.start();
                mp.setLooping(true);
            });
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

}