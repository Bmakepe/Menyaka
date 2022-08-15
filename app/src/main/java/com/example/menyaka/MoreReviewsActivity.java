package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreReviewsActivity extends AppCompatActivity {

    private List<RatingAndReview> ratingsList;
    private RateReviewAdapter ratingsAdapter;

    ImageView back;
    TextView storeName;
    String storeId, store_name;

    FloatingActionButton reviewBTN;

    DatabaseReference storeRef, productRef, hotDealRef;

    UniversalFunctions universalFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        Intent intent = getIntent();
        storeId = intent.getExtras().getString("storeId");
        universalFunctions = new UniversalFunctions(this);

        RecyclerView ratingRecycler = findViewById(R.id.recycler_View);
        ratingRecycler.setHasFixedSize(true);
        ratingRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingsList = new ArrayList<>();
        ratingsAdapter = new RateReviewAdapter(this, ratingsList, storeId);
        ratingRecycler.setAdapter(ratingsAdapter);

        back = findViewById(R.id.recyclerBackBTN);
        storeName = findViewById(R.id.recyclerHeader);
        reviewBTN = findViewById(R.id.reviewBTN);
        reviewBTN.setVisibility(View.VISIBLE);

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        hotDealRef = FirebaseDatabase.getInstance().getReference("HotDeals");

        getStoreDetails();
        //universalFunctions.readRatingAndReviews(storeId, ratingsList, ratingsAdapter);
        readRatingAndReviews();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        reviewBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MoreReviewsActivity.this, ReviewActivity.class);
                intent.putExtra("storeID", storeId);
                intent.putExtra("exit", "finish");
                startActivity(intent);
            }
        });

    }

    private void getStoreDetails() {
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    if(retail.getRetail_id().equals(storeId)) {
                        store_name = retail.getRetailName();
                        storeName.setText(store_name + " Reviews");
                    }else{
                        productRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    Product product = ds.getValue(Product.class);

                                    if(product.getProductId().equals(storeId)){
                                        storeName.setText(product.getProductName() + " Reviews");
                                    }else{
                                        hotDealRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){

                                                    hotDealRef.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot dss : snapshot.getChildren()){

                                                                HotDeal hotDeal = dss.getValue(HotDeal.class);

                                                                if (hotDeal.getHotDeal_id().equals(storeId))
                                                                    storeRef.addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            for (DataSnapshot dss : snapshot.getChildren()) {
                                                                                Retail retail1 = dss.getValue(Retail.class);

                                                                                if (retail1.getRetail_id().equals(hotDeal.getStoreId()))
                                                                                    storeName.setText(retail1.getRetailName() + " Reviews");
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

    private void readRatingAndReviews(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingsList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    RatingAndReview reviews = dataSnapshot.getValue(RatingAndReview.class);
                    ratingsList.add(reviews);
                }

                Collections.reverse(ratingsList);
                ratingsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}