package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileBuildActivity extends AppCompatActivity implements LocationListener {

    EditText nameTV, biographyTV, locationTV;
    CircleImageView profileImage;
    ImageView myLocationBTN, pickDateBTN;
    TextView userAge, proSetup_dob_TV;
    RelativeLayout picArea;
    TextInputLayout fullNameTIL, biographyTIL;
    RadioGroup userGender;
    RadioButton selectedGender;

    public static final int LOCATION_REQUEST_CODE = 100;

    private String[] locationPermission;

    private LocationManager locationManager;
    private double latitude, longitude;

    DatePickerDialog.OnDateSetListener dateSetListener;
    private static final String TAG = "ProfileBuildActivity";
    private int getYear;
    private String date, profPic;

    ProgressDialog pd;

    Uri proPicUri;
    String myUri = "";
    StorageTask uploadTask;


    DatabaseReference userRef;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_build);

        picArea = findViewById(R.id.proSetUpPicArea);
        profileImage = findViewById(R.id.proSetupProPic);
        nameTV = findViewById(R.id.proSetUp_FullName);
        biographyTV = findViewById(R.id.proSetUp_biography);
        locationTV = findViewById(R.id.proSetUp_userLocation);
        pickDateBTN = findViewById(R.id.proSetup_dateIcon);
        myLocationBTN = findViewById(R.id.proSetup_myLocationBTN);
        userAge = findViewById(R.id.proSetUp_date);
        myLocationBTN = findViewById(R.id.proSetup_myLocationBTN);
        userAge = findViewById(R.id.proSetUp_date);
        fullNameTIL = findViewById(R.id.fullNameTIL);
        biographyTIL = findViewById(R.id.biographyTIL);
        userGender = findViewById(R.id.gender_inputs);
        proSetup_dob_TV = findViewById(R.id.proSetUp_date);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("pro_pics");

        pd = new ProgressDialog(this);

        checkUserRegistry();

        pickDateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ProfileBuildActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: date:" + month +"/" + day + "/" + year);

                date = day + "-" + month + "-" + year;
                getYear = year;
                userAge.setText(date);
            }
        };

        myLocationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }

            }
        });

        picArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(ProfileBuildActivity.this);
            }
        });

        findViewById(R.id.profileBuildContinueBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validateName() | !validateGender()| !validateAge() | !validateBio()){
                    return;
                }else{
                    updateUserInfo();
                }

            }
        });

    }

    private void updateUserInfo() {
        pd.setMessage("Loading...");
        pd.show();

        selectedGender = findViewById(userGender.getCheckedRadioButtonId());
        String _gender = selectedGender.getText().toString();

        if (proPicUri != null){

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(proPicUri));

            uploadTask = fileReference.putFile(proPicUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        //StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(profPic);
                        //picRef.delete();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("username", fullNameTIL.getEditText().getText().toString().trim());
                        userMap.put("gender", _gender);
                        userMap.put("dateOfBirth", date);
                        userMap.put("imageUrl", myUri);
                        userMap.put("verified", false);
                        userMap.put("Terms", "Accepted");
                        userMap.put("latitude", latitude);
                        userMap.put("longitude", longitude);
                        userMap.put("id", firebaseUser.getUid());
                        userMap.put("biography", biographyTIL.getEditText().getText().toString().trim());
                        userMap.put("phoneNumber", firebaseUser.getPhoneNumber());

                        userRef.child(firebaseUser.getUid()).setValue(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileBuildActivity.this, "Successfully setup profile", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ProfileBuildActivity.this, ProfileInteractionsSetupActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileBuildActivity.this, "We are experiencing technical problem, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileBuildActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("username", fullNameTIL.getEditText().getText().toString().trim());
            hashMap.put("gender", _gender);
            hashMap.put("dateOfBirth", date);
            hashMap.put("imageUrl", "Default");
            hashMap.put("verified", false);
            hashMap.put("Terms", "Accepted");
            hashMap.put("id", firebaseUser.getUid());
            hashMap.put("latitude", latitude);
            hashMap.put("longitude", longitude);
            hashMap.put("biography", biographyTIL.getEditText().getText().toString().trim());
            hashMap.put("phoneNumber", firebaseUser.getPhoneNumber());

            userRef.child(firebaseUser.getUid()).setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            Toast.makeText(ProfileBuildActivity.this, "Successfully setup profile", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileBuildActivity.this, ProfileInteractionsSetupActivity.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileBuildActivity.this, "Error Setting Up Profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkUserRegistry() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getId().equals(firebaseUser.getUid())){

                        nameTV.setText(user.getUsername());
                        biographyTV.setText(user.getBiography());
                        proSetup_dob_TV.setText(user.getDateOfBirth());

                        //find address, country, state, city
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(ProfileBuildActivity.this, Locale.getDefault());

                        try{
                            addresses = geocoder.getFromLocation(user.getLatitude(), user.getLongitude(), 1);
                            String address = addresses.get(0).getAddressLine(0);//complete address
                            locationTV.setText(address);

                            profPic = user.getImageUrl();
                            Picasso.get().load(user.getImageUrl()).into(profileImage);
                        }catch (Exception ignored){}

                    }
                }
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

    private String getFileExtension(Uri uri){
        try {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(contentResolver.getType(uri));
        }catch (NullPointerException ignored){}
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            proPicUri = result.getUri();
            profileImage.setImageURI(proPicUri);

        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps/location disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();

    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            locationTV.setText(address);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Validations functions
    private boolean validateName() {
        String val = fullNameTIL.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            fullNameTIL.setError("Full Names are required");
            return false;
        } else {
            fullNameTIL.setError(null);
            fullNameTIL.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateBio() {
        String val = biographyTIL.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            biographyTIL.setError("Tell Us More About Yourself");
            return false;
        } else {
            biographyTIL.setError(null);
            biographyTIL.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateGender() {

        if (userGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validateAge() {

        int current = Calendar.getInstance().get(Calendar.YEAR);
        int userAge = getYear;
        int isAgeValid = current - userAge;

        if (isAgeValid < 8){
            Toast.makeText(this, "Age is not Appropriate", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
}