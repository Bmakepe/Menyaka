package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewActivity extends AppCompatActivity {

    //for rating the store
    ImageView ratingBackBTN;
    CircleImageView ratingProPic;
    TextView ratingHeader, postRatingBTN, rateCounter;
    EditText ratingET;
    RatingBar ratingBar;
    float rateValue;

    String storeId, exitType;

    DatabaseReference storeRef, productRef, agentRef, hotDealsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_review_layout);

        ratingBackBTN = findViewById(R.id.ratingBackBTN);
        ratingProPic = findViewById(R.id.ratingProPic);
        ratingHeader = findViewById(R.id.ratingHeader);
        postRatingBTN = findViewById(R.id.postRatingFAB);
        ratingET = findViewById(R.id.ratingET);
        rateCounter = findViewById(R.id.rateCounter);
        ratingBar = findViewById(R.id.shopRating);

        Intent intent = getIntent();
        storeId = intent.getExtras().getString("storeID");
        exitType = intent.getExtras().getString("exit");

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        agentRef = FirebaseDatabase.getInstance().getReference("ShippingAgents");
        hotDealsRef = FirebaseDatabase.getInstance().getReference("HotDeals");

        getRatersDetails();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                rateValue = ratingBar.getRating();

                if(rateValue <= 1 && rateValue > 0){
                    //rateCounter.setText("Very Bad " + rateValue + "/5");
                    ratingET.setHint("What was very bad about the experience");
                }
                else if(rateValue <= 2 && rateValue > 1){
                    //rateCounter.setText("Bad " + rateValue + "/5");
                    ratingET.setHint("What was bad about the experience");
                }
                else if(rateValue <= 3 && rateValue > 2){
                    //rateCounter.setText("Good " + rateValue + "/5");
                    ratingET.setHint("What was good about the experience");
                }
                else if(rateValue <= 4 && rateValue > 3){
                    //rateCounter.setText("Very Good " + rateValue + "/5");
                    ratingET.setHint("What was very good about the experience");
                }
                else if(rateValue <= 5 && rateValue > 4){
                    //rateCounter.setText("Excellent " + rateValue + "/5");
                    ratingET.setHint("What was excellent about the experience");
                }

            }
        });

        postRatingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String reviewCaption = ratingET.getText().toString().trim();

                if(TextUtils.isEmpty(reviewCaption)){
                    ratingET.setError("Write Review Here");
                    ratingET.requestFocus();
                }else if(rateValue == 0){
                    Toast.makeText(ReviewActivity.this, "Please rate properly", Toast.LENGTH_SHORT).show();
                }else{
                    uploadRating(reviewCaption, ratingET, rateValue);
                }

            }
        });

        ratingBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getRatersDetails() {

        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if(retail.getRetail_id().equals(storeId)){

                        ratingHeader.setText("Rate and Review " + retail.getRetailName());
                        try{
                            Picasso.get().load(retail.getImageUrl()).into(ratingProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.profile_png_1114185).into(ratingProPic);
                        }
                    }else{
                        getProductDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getProductDetails() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    if(product.getProductId().equals(storeId)) {
                        ratingHeader.setText("Rate and Review " + product.getProductName());
                        try{
                            Picasso.get().load(product.getProductImg()).into(ratingProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.profile_png_1114185).into(ratingProPic);
                        }
                    }else{
                        getAgentDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAgentDetails() {
        agentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ShippingAgents agents = ds.getValue(ShippingAgents.class);

                    if (agents.getCompanyID().equals(storeId)) {
                        ratingHeader.setText("Rate and Review " + agents.getShippingName());
                        try {
                            Picasso.get().load(agents.getCompanyLogo()).into(ratingProPic);
                        } catch (NullPointerException e) {
                            Picasso.get().load(R.drawable.profile_png_1114185).into(ratingProPic);
                        }
                    }else{
                        getHotDealDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHotDealDetails() {
        hotDealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    hotDealsRef.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dss : snapshot.getChildren()){
                                HotDeal hotDeal = dss.getValue(HotDeal.class);

                                if (hotDeal.getHotDeal_id().equals(storeId)){
                                    storeRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dss1 : snapshot.getChildren()){
                                                Retail retail = dss1.getValue(Retail.class);

                                                if (retail.getRetail_id().equals(hotDeal.getStoreId())){
                                                    ratingHeader.setText("Rate and Review " + retail.getRetailName());

                                                    try {
                                                        Picasso.get().load(retail.getImageUrl()).into(ratingProPic);
                                                    }catch (NullPointerException ignored){}
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadRating(String reviewCaption, EditText ratingET, float rateValue) {
        DatabaseReference reviewReference = FirebaseDatabase.getInstance().getReference("ShopRatings");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String reviewID = reviewReference.push().getKey();

        final String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", reviewCaption);
        hashMap.put("reviewID", reviewID);
        hashMap.put("publisherId", user.getUid());
        hashMap.put("timestamp", timestamp);
        hashMap.put("rating", rateValue);

        reviewReference.child(storeId).child(reviewID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ratingET.setText("");

                        if(exitType.equals("return")) {
                            startActivity(new Intent(ReviewActivity.this, MainActivity.class));
                        }else if (exitType.equals("finish")) {
                            finish();
                        }else {
                            Toast.makeText(ReviewActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}