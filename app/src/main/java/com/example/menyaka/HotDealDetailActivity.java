package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.menyaka.Adapters.HotDealAdapter;
import com.example.menyaka.Adapters.PromoAdapter;
import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HotDealDetailActivity extends AppCompatActivity {

    private PromoAdapter promoAdapter;
    private List<Product> productList;
    private ArrayList<String> id_list;

    ImageView close, itemImage, morePromoItems, moreRatings, moreHotDeals;
    CircleImageView shopImg;
    TextView shopName, itemPriceBefore, itemName, storeLocation, itemPriceAfter, discount,
            discount_rate, startDate, endDate, addToCartBTN, buyNowBTN;
    RecyclerView ratingsRecycler, hotDealsRecycler;
    ViewPager2 promoItemsPager;

    FirebaseUser firebaseUser;
    DatabaseReference hotDealRef, storeRef;

    String hotDealID, storeID;
    UniversalFunctions universalFunctions;

    //for displaying reviews
    private RateReviewAdapter ratingsAdapter;
    private List<RatingAndReview> ratingsList;
    ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5;
    RatingBar ratingSection;
    TextView ratingNumber, totalRatings;

    //for retrieving hot deals in the activity
    private HotDealAdapter hotDealAdapter;
    private List<HotDeal> hotDealList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_deal_detail);

        //declaring all image views in the xml
        close = findViewById(R.id.backShop);
        itemImage = findViewById(R.id.itemImg);
        shopImg = findViewById(R.id.shop_img);
        morePromoItems = findViewById(R.id.moreItemsBTN);
        moreRatings = findViewById(R.id.more_reviewsBTN);
        moreHotDeals = findViewById(R.id.more_hotDealsBTN);

        //declaring all text views in the xml
        shopName = findViewById(R.id.shop_name);
        storeLocation = findViewById(R.id.shop_location);
        itemName = findViewById(R.id.hotdeal_name);
        itemPriceBefore = findViewById(R.id.original_price_info);
        itemPriceAfter = findViewById(R.id.dicount_price);
        discount_rate = findViewById(R.id.discount_rate);
        discount = findViewById(R.id.discount);
        startDate = findViewById(R.id.promoStartDate);
        endDate = findViewById(R.id.promoDuration);
        addToCartBTN = findViewById(R.id.hotDeal_AddToCart);
        buyNowBTN = findViewById(R.id.hotDeal_buy_now);

        //declaring recyclerviews and viewpager
        promoItemsPager = findViewById(R.id.promotionProductsViewPager);
        hotDealsRecycler = findViewById(R.id.hot_deals_recycler);
        ratingsRecycler = findViewById(R.id.hot_deal_rating_recycler);

        //for ratings
        ratingSection = findViewById(R.id.hot_deal_rating_section);
        ratingNumber = findViewById(R.id.hot_deal_average_rating);
        totalRatings = findViewById(R.id.hot_deal_number_of_ratings);

        progressBar1 = findViewById(R.id.hot_deal_progress_1);
        progressBar2 = findViewById(R.id.hot_deal_progress_2);
        progressBar3 = findViewById(R.id.hot_deal_progress_3);
        progressBar4 = findViewById(R.id.hot_deal_progress_4);
        progressBar5 = findViewById(R.id.hot_deal_progress_5);

        hotDealID = getIntent().getExtras().getString("hotDeal_id");
        universalFunctions = new UniversalFunctions(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hotDealRef = FirebaseDatabase.getInstance().getReference("HotDeals");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");

        ratingsRecycler.setHasFixedSize(true);
        ratingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingsList = new ArrayList<>();
        ratingsAdapter = new RateReviewAdapter(this, ratingsList);
        ratingsRecycler.setAdapter(ratingsAdapter);

        universalFunctions.readRatingAndReviews(hotDealID, ratingsList, ratingsAdapter);
        RatingsCalculator.ratingHeader(hotDealID, ratingSection, totalRatings);
        RatingsCalculator.totalRating(hotDealID, ratingSection);
        RatingsCalculator.setRating(hotDealID, ratingNumber);

        RatingsCalculator.ratingBars(hotDealID,5, progressBar5);
        RatingsCalculator.ratingBars(hotDealID,4, progressBar4);
        RatingsCalculator.ratingBars(hotDealID,3, progressBar3);
        RatingsCalculator.ratingBars(hotDealID,2, progressBar2);
        RatingsCalculator.ratingBars(hotDealID,1, progressBar1);

        getHotDealInfo();
        getHotDealList();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addToCartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HotDealDetailActivity.this, "this product will be added to cart", Toast.LENGTH_SHORT).show();
            }
        });

        buyNowBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HotDealDetailActivity.this, "You will be able to buy this product", Toast.LENGTH_SHORT).show();
            }
        });

        moreHotDeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HotDealDetailActivity.this, HotDealActivity.class);
                intent.putExtra("listStyle", "unfilteredList");
                startActivity(intent);
            }
        });

        morePromoItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HotDealDetailActivity.this, "You will be able to view more promo items", Toast.LENGTH_SHORT).show();
            }
        });

        moreRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HotDealDetailActivity.this, MoreReviewsActivity.class);
                intent.putExtra("storeId", hotDealID);
                startActivity(intent);
            }
        });

        shopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shopIntent = new Intent(HotDealDetailActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", storeID);
                startActivity(shopIntent);
            }
        });

        shopImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shopIntent = new Intent(HotDealDetailActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", storeID);
                startActivity(shopIntent);

            }
        });

    }

    private void getHotDealList() {
        hotDealList = new ArrayList<>();
        hotDealsRecycler.hasFixedSize();
        hotDealsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        hotDealRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotDealList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    hotDealRef.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dss : snapshot.getChildren()){
                                HotDeal hotDeal = dss.getValue(HotDeal.class);
                                hotDealList.add(hotDeal);

                            }
                            hotDealAdapter = new HotDealAdapter(hotDealList, HotDealDetailActivity.this);
                            hotDealsRecycler.setAdapter(hotDealAdapter);
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

    private void getHotDealInfo() {
        hotDealRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){

                    hotDealRef.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot dss : snapshot.getChildren()) {

                                HotDeal hotDeal = dss.getValue(HotDeal.class);

                                if (hotDeal.getHotDeal_id().equals(hotDealID)) {

                                    itemName.setText(hotDeal.getItemName());
                                    endDate.setText("Valid Until: " + hotDeal.getDealEnd());
                                    discount.setText("M " + hotDeal.getDiscount());
                                    itemPriceBefore.setText("M " + hotDeal.getItemPrice());
                                    //itemPriceAfter.setText(String.format("M%d", hotDeal.getItemPrice() - hotDeal.getDiscount()));

                                    try {
                                        Picasso.get().load(hotDeal.getItemUrl()).into(itemImage);
                                    } catch (NullPointerException ignored) {
                                    }

                                    storeRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                Retail retail = ds.getValue(Retail.class);

                                                if (retail.getRetail_id().equals(hotDeal.getStoreId())) {

                                                    storeID = retail.getRetail_id();
                                                    shopName.setText(retail.getRetailName());
                                                    storeLocation.setText("Ha Mopenyaki, Roma 180, Maseru 100, Lesotho");

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
