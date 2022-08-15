package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.menyaka.Models.User;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewAddressActivity extends AppCompatActivity implements LocationListener {

    EditText myName, myNumber, houseAddress, roadDetails, district, neighbourhood, zipCode;

    RelativeLayout saveAddressBTN;

    FirebaseUser firebaseUser;

    CircleImageView addressPic;

    ImageView locationBTN;

    public static final int LOCATION_REQUEST_CODE = 100;

    private String[] locationPermission;

    private LocationManager locationManager;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_address_layout);

        myName = findViewById(R.id.myNameET);
        myNumber = findViewById(R.id.myNumberET);
        houseAddress = findViewById(R.id.houseAddressET);
        roadDetails = findViewById(R.id.roadDetailsET);
        district = findViewById(R.id.districtET);
        neighbourhood = findViewById(R.id.neighbourhoodET);
        zipCode = findViewById(R.id.zipCode);
        saveAddressBTN = findViewById(R.id.saveAddressBTN);
        addressPic = findViewById(R.id.newAddressProPic);
        locationBTN = findViewById(R.id.pickLocationBTN);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMyDetails();

        locationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });

        saveAddressBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mobile = myNumber.getText().toString().trim();

                if (myName.getText().toString().isEmpty()) {
                    myName.setError("Enter a valid name");
                    myName.requestFocus();
                } else if (myNumber.getText().toString().isEmpty() || mobile.length() < 8 || mobile.length() > 20) {
                    myNumber.setError("Enter a valid number");
                    myNumber.requestFocus();
                } else if (houseAddress.getText().toString().isEmpty()) {
                    houseAddress.setError("Enter a house address");
                    houseAddress.requestFocus();
                } else if (roadDetails.getText().toString().isEmpty()) {
                    roadDetails.setError("Enter a your main street address");
                    roadDetails.requestFocus();
                } else if (district.getText().toString().isEmpty()) {
                    district.setError("Enter a district");
                    district.requestFocus();
                } else if (zipCode.getText().toString().isEmpty()) {
                    zipCode.setError("Enter a valid Zip Code");
                    zipCode.requestFocus();
                } else if (neighbourhood.getText().toString().isEmpty()) {
                    neighbourhood.setError("Enter your neighbourhood here");
                    neighbourhood.requestFocus();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewAddressActivity.this);
                    builder.setTitle("Address")
                            .setMessage("Would you like to make this your default address")
                            .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    uploadAddress();
                                    finish();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            uploadAddress();
                            finish();
                        }
                    }).show();
                }
            }
        });

        findViewById(R.id.newAddressBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
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

            neighbourhood.setText(address);
            district.setText(city);
            houseAddress.setText(country);
            roadDetails.setText(state);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getMyDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if(user.getId().equals(firebaseUser.getUid())){
                        myName.setText(user.getUsername());
                        myNumber.setText(firebaseUser.getPhoneNumber());

                        try{
                            Picasso.get().load(user.getImageUrl()).into(addressPic);
                        }catch (NullPointerException ignored){}

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadAddress() {
        final DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
        String key = addressRef.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("addressID", key);
        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("deliveryName", myName.getText().toString());
        hashMap.put("deliveryNumber", myNumber.getText().toString());
        hashMap.put("deliveryHouse", houseAddress.getText().toString());
        hashMap.put("deliveryRoad", roadDetails.getText().toString());
        hashMap.put("deliveryNeighbourHood", neighbourhood.getText().toString());
        hashMap.put("deliveryDistrict", district.getText().toString());
        hashMap.put("deliveryZipCode", zipCode.getText().toString());
        hashMap.put("defaultAddress", "secondary");

        assert key != null;
        addressRef.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(NewAddressActivity.this, "Added Address Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewAddressActivity.this, "Could not update your address", Toast.LENGTH_SHORT).show();
            }
        });

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
        //gps/location disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }else{
                        Toast.makeText(this, "You need location permission enabled", Toast.LENGTH_SHORT).show();
                    }
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}