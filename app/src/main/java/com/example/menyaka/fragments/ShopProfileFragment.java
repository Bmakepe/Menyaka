package com.example.menyaka.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.menyaka.Adapters.HotDealAdapter;
import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.HotDealActivity;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.MoreBusinessActivity;
import com.example.menyaka.MoreProductActivity;
import com.example.menyaka.MoreReviewsActivity;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopProfileFragment extends Fragment {

    String storeId, storeCategory;

    //for displaying products ins this fragment
    private ProductItemAdapter productAdapter;
    private ArrayList<Product> productList;
    private RecyclerView productsRecycler;
    private TextView productsHeader;

    //for displaying rating and reviews
    private TextView ratingNumber,totalRatings;
    private RatingBar  ratingSection;
    private ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5;
    private RecyclerView ratingRecycler;
    private List<RatingAndReview> ratingsList;
    private RateReviewAdapter ratingsAdapter;

    //for displaying similar stores
    private MyStoreAdapter businessAdapter;
    private ArrayList<MyStore> businessList;
    RecyclerView businessRecycler;

    //for displaying store Hot Deals
    private RecyclerView hotDealsRecycler;
    private HotDealAdapter hotDealAdapter;
    private TextView moreHotDeals;
    private List<String> id_list;
    private ImageView moreDealsBTN;
    private List<HotDeal> hotDealList;

    //Other views in the fragment
    private ImageView moreBusinesses, moreReviews, moreProducts;


    private DatabaseReference productRef, ratingsRef, storeRef, hotDealsRef;

    UniversalFunctions universalFunctions;


    public ShopProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_profile, container, false);

        storeId = getArguments().getString("storeID");
        universalFunctions = new UniversalFunctions(getActivity());

        //declaring firebase
        productRef = FirebaseDatabase.getInstance().getReference("Products");
        ratingsRef = FirebaseDatabase.getInstance().getReference("ShopRatings");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        hotDealsRef = FirebaseDatabase.getInstance().getReference("HotDeals");

        //for displaying other views
        moreBusinesses = view.findViewById(R.id.all_businesses);
        moreProducts = view.findViewById(R.id.all_products);
        moreReviews = view.findViewById(R.id.all_reviews);

        //for hot deals in the store
        hotDealsRecycler = view.findViewById(R.id.hot_deals_recycler);
        moreHotDeals = view.findViewById(R.id.hotDealsTitle);
        moreDealsBTN = view.findViewById(R.id.all_hotDeals);

        //for handling products list
        productsRecycler = view.findViewById(R.id.products_recycler);
        productsHeader = view.findViewById(R.id.storeProductsHeader);

        productsRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        productsRecycler.setLayoutManager(linearLayoutManager);
        productList = new ArrayList<>();

        //for displaying ratings and reviews
        ratingSection = view.findViewById(R.id.rating_section);
        ratingNumber = view.findViewById(R.id.average_rating);
        totalRatings = view.findViewById(R.id.number_of_ratings);

        progressBar1 = view.findViewById(R.id.progress_1);
        progressBar2 = view.findViewById(R.id.progress_2);
        progressBar3 = view.findViewById(R.id.progress_3);
        progressBar4 = view.findViewById(R.id.progress_4);
        progressBar5 = view.findViewById(R.id.progress_5);

        ratingRecycler = view.findViewById(R.id.shop_comments_recycler);

        ratingRecycler.setHasFixedSize(true);
        ratingRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ratingsList = new ArrayList<>();
        ratingsAdapter = new RateReviewAdapter(getContext(), ratingsList, storeId);
        ratingRecycler.setAdapter(ratingsAdapter);

        //for retrieving similar stores
        businessRecycler = view.findViewById(R.id.business_recycler);
        businessRecycler.setHasFixedSize(true);
        businessRecycler.hasFixedSize();
        businessRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        businessList = new ArrayList<>();

        hotDealsRecycler.hasFixedSize();
        hotDealsRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        id_list = new ArrayList<>();
        hotDealList = new ArrayList<>();

        loadProducts();
        getStoreDetails();
        universalFunctions.readRatingAndReviews(storeId, ratingsList, ratingsAdapter);
        readSimilarBusinesses();
        loadHotDeals();

        //RatingsCalculator.storeRatingHeader(storeId, ratingHeader, totalRatings);
        //RatingsCalculator.storeRatingHeader(storeId, ratingHeader, headerRatings);
        RatingsCalculator.storeRatingHeader(storeId, ratingSection, totalRatings);
        RatingsCalculator.storeTotalRating(storeId, ratingSection);
        RatingsCalculator.storeSetRating(storeId, ratingNumber);

        RatingsCalculator.storeRatingBars(storeId,5, progressBar5);
        RatingsCalculator.storeRatingBars(storeId,4, progressBar4);
        RatingsCalculator.storeRatingBars(storeId,3, progressBar3);
        RatingsCalculator.storeRatingBars(storeId,2, progressBar2);
        RatingsCalculator.storeRatingBars(storeId,1, progressBar1);

        moreBusinesses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MoreBusinessActivity.class);
                intent.putExtra("storeId", storeId);
                intent.putExtra("storeCategory", storeCategory);
                intent.putExtra("style", "similarStores");
                startActivity(intent);
            }
        });

        moreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MoreProductActivity.class);
                intent.putExtra("storeId", storeId);
                intent.putExtra("style", "businessProducts");
                startActivity(intent);
            }
        });

        moreReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MoreReviewsActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        moreDealsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HotDealActivity.class);
                intent.putExtra("storeID", storeId);
                intent.putExtra("listStyle", "filteredList");
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadHotDeals() {
        hotDealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                id_list.clear();
                hotDealList.clear();
                for (DataSnapshot ds1 : snapshot.getChildren()){
                    if (storeId.equals(ds1.getKey())){
                        hotDealsRef.child(ds1.getKey())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            HotDeal hotDeal = dataSnapshot.getValue(HotDeal.class);
                                            hotDealList.add(hotDeal);
                                        }
                                        Collections.shuffle(hotDealList);
                                        hotDealAdapter = new HotDealAdapter(hotDealList, getActivity());
                                        hotDealsRecycler.setAdapter(hotDealAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        storeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Retail retail = ds.getValue(Retail.class);

                                    if (retail.getRetail_id().equals(ds1.getKey())){
                                        moreHotDeals.setText(retail.getRetailName() + " Hot Deals");
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

    private void readSimilarBusinesses(){

        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                businessList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MyStore retail = dataSnapshot.getValue(MyStore.class);
                    assert retail != null;
                    if(!retail.getRetail_id().equals(storeId))
                        if(retail.getCategory().equals(storeCategory))
                            businessList.add(retail);
                }

                businessAdapter = new MyStoreAdapter(businessList, getActivity());
                businessRecycler.setAdapter(businessAdapter);

                businessAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoreDetails() {
        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(storeId)){
                        productsHeader.setText(retail.getRetailName() + " Products");
                        storeCategory = retail.getCategory();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadProducts() {

        //Query query = productRef.orderByChild("storeId").equalTo(storeId);
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    if (product.getStoreId().equals(storeId))
                        productList.add(product);
                }

                productAdapter = new ProductItemAdapter(getActivity(), productList);
                productsRecycler.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readRatingAndReviews(){

        ratingsRef.child(storeId).limitToLast(5).addValueEventListener(new ValueEventListener() {
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