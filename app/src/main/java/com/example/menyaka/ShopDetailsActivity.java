package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.CartAdapter;
import com.example.menyaka.Adapters.HisTabAdapter;
import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Adapters.StoreTabAdapter;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Points;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.fragments.HisPostsFragment;
import com.example.menyaka.fragments.HisVideosFragment;
import com.example.menyaka.fragments.ShopProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ShopDetailsActivity extends AppCompatActivity {

    ImageView shopImg, product_img, verifiedStore;
    TextView shopName, shopDescription, location, SpecificArea, headerRatings;
    TextView addStoreBtn;
    String storeId, storeCategory, myStoreName;
    RatingBar ratingHeader;

    FirebaseUser firebaseUser;
    DatabaseReference retailRef, loyaltyRef;

    Boolean subscription = false;

    TabLayout storeTabLayout;
    ViewPager storeViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        storeId = getIntent().getExtras().getString("storeId");

        Bundle bundle = new Bundle();
        bundle.putString("hisUserID", storeId);

        HisPostsFragment postsFragment = new HisPostsFragment();
        postsFragment.setArguments(bundle);

        ShopProfileFragment profileFragment = new ShopProfileFragment();
        profileFragment.setArguments(bundle);

        HisVideosFragment videosFragment = new HisVideosFragment();
        videosFragment.setArguments(bundle);

        shopImg = findViewById(R.id.storeImage);
        product_img = findViewById(R.id.product_img);
        shopName = findViewById(R.id.retailStoreName);
        location = findViewById(R.id.location_area);
        shopDescription = findViewById(R.id.shop_desc);
        SpecificArea = findViewById(R.id.location_town);
        verifiedStore = findViewById(R.id.verifiedStore);
        storeTabLayout = findViewById(R.id.storeTabLayout);
        storeViewPager = findViewById(R.id.storeViewPager);
        ratingHeader = findViewById(R.id.shopRatingBar);
        headerRatings = findViewById(R.id.number_of_ratings1);

        addStoreBtn = findViewById(R.id.addToStore);

        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        isFavourite(storeId, addStoreBtn);
        checkSubscription(storeId);
        getStoreDetails();

        RatingsCalculator.storeRatingHeader(storeId, ratingHeader, headerRatings);

        StoreTabAdapter tabAdapter = new StoreTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new ShopProfileFragment(), "Profile", storeId);
        tabAdapter.addFragment(new HisPostsFragment(),"Posts", storeId);
        tabAdapter.addFragment(new HisVideosFragment(),"Videos", storeId);
        storeViewPager.setAdapter(tabAdapter);
        storeTabLayout.setupWithViewPager(storeViewPager);

        addStoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (addStoreBtn.getText().toString().equals("Add to favourites")){

                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(storeId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(storeId)
                            .child(firebaseUser.getUid()).setValue(true);
                }else if (addStoreBtn.getText().toString().equals("Message")){

                    Intent intent = new Intent(ShopDetailsActivity.this, MessageActivity.class);
                    intent.putExtra("receiverID", storeId);
                    startActivity(intent);
                }else{
                    Toast.makeText(ShopDetailsActivity.this, "Uncivilized Click", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.browseStoreCategories).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ShopDetailsActivity.this, ShopCategoryActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        findViewById(R.id.shop_rating_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.viewCart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, CartActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        shopImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", storeId);
                startActivity(intent);
            }
        });

        findViewById(R.id.shopMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(ShopDetailsActivity.this, findViewById(R.id.shopMenu), Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Policies");

                if (subscription)
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Unsubscribe Loyalty");
                else
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Subscribe Loyalty");

                if(addStoreBtn.getText().toString().equals("Message")) {

                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Remove Favourite");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        if (id == 0) {
                            Toast.makeText(ShopDetailsActivity.this, "You will be able to view the companies policies", Toast.LENGTH_SHORT).show();
                        } else if (id == 1) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                            if (subscription){

                                builder.setTitle("Loyalty Subscriptions")
                                    .setMessage("Are you sure you want to unsubscribe to " + myStoreName + " loyalty program?" )
                                    .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            loyaltyRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(firebaseUser.getUid());
                                            loyaltyRef.child(storeId).removeValue();
                                            Toast.makeText(ShopDetailsActivity.this, "Unsubscribed successfully", Toast.LENGTH_SHORT).show();

                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    }
                                }).show();

                            }else{
                                    builder.setTitle("Loyalty Subscriptions")
                                        .setMessage("Are you sure you want to subscribe to " + myStoreName + " loyalty program?" )
                                        .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                subscribe();
                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                            }

                        } else if (id == 2){

                            FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                                    .child(storeId).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(storeId)
                                    .child(firebaseUser.getUid()).removeValue();

                        }else{
                            Toast.makeText(ShopDetailsActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });

    }

    private void getStoreDetails() {
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(storeId)) {
                        shopName.setText(retail.getRetailName());
                        myStoreName = retail.getRetailName();

                        storeCategory = retail.getCategory();

                        if (retail.isVerified())
                            verifiedStore.setVisibility(View.VISIBLE);
                        else
                            verifiedStore.setVisibility(View.GONE);

                        try {
                            Picasso.get().load(retail.getImageUrl()).into(shopImg);
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void subscribe() {
        loyaltyRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints");
        String pointsKey = loyaltyRef.push().getKey();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pointsID", pointsKey);
        hashMap.put("pointsNo", "0");
        hashMap.put("shopID", storeId);
        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("subscriptionDate", timestamp);
        hashMap.put("expiryDate", "");

        loyaltyRef.child(firebaseUser.getUid()).child(storeId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShopDetailsActivity.this, "Subscribed Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShopDetailsActivity.this, "Could not subscribe", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkSubscription(String retail_ID) {
        loyaltyRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(firebaseUser.getUid());
        loyaltyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                subscription = snapshot.child(retail_ID).exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isFavourite(final String retail_id, final TextView button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("MyStores").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(retail_id).exists()){
                    button.setText("Message");
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_message_24, 0, 0, 0);
                } else {
                    button.setText("Add to favourites");
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}