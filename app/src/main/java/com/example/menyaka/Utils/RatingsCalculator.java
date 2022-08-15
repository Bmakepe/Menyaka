package com.example.menyaka.Utils;

import android.app.Activity;
import android.os.Build;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.menyaka.Models.Retail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RatingsCalculator {

    //below will be used for product ratings

    public static void ratingHeader(final String storeId, final RatingBar avgRating, final TextView numOfRatings){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    avgRating.setRating(ratingAvg);
                    numOfRatings.setText(String.valueOf(ratingTotal + " Review(s)"));
                }else{
                    numOfRatings.setText("0 Reviews");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void totalRating(final String storeId, final RatingBar avgRating){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    avgRating.setRating(ratingAvg);
                    //Toast.makeText(mActivity, "Show Ratings"+avgRating, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void setRating(final String storeId, final TextView avgRating){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                    formatter.setMaximumFractionDigits(1);
                    formatter.setMinimumFractionDigits(1);
                    formatter.setRoundingMode(RoundingMode.HALF_EVEN);
                    String avarageRating = formatter.format(ratingAvg);

                    avgRating.setText(avarageRating);
                    //Toast.makeText(mActivity, "Show Ratings"+avgRating, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void ratingBars(String storeId, final int numRating, final ProgressBar progressBar){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int ratingTotal = 0;
                int progressCount = 0;
                int progressPercentage = 0, progress =0;

                ratingTotal = (int) snapshot.getChildrenCount();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try{
                        int ratingNumber = Integer.parseInt(dataSnapshot.child("rating").getValue().toString());
                        if (ratingNumber == numRating){
                            progressCount++;
                        }
                    }catch (NumberFormatException ignored){}

                }

                /*progress = ((progressCount/ratingTotal)*100);
                progressPercentage = progress * 100;
                progressBar.setProgress(progressPercentage);*/
                progressBar.setProgress(progressCount);
                progressBar.setMax(ratingTotal);

                //Toast.makeText(ShopDetailsActivity.this, "Total Ratings " + progressCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //below will be used for store ratings

    public static void storeRatingHeader(final String storeId, final RatingBar avgRating, final TextView numOfRatings){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    avgRating.setRating(ratingAvg);
                    numOfRatings.setText(String.valueOf(ratingTotal + " Review(s)"));
                }else{
                    numOfRatings.setText("0 Reviews");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void storeTotalRating(final String storeId, final RatingBar avgRating){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    avgRating.setRating(ratingAvg);
                    //Toast.makeText(mActivity, "Show Ratings"+avgRating, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void storeSetRating(final String storeId, final TextView avgRating){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float ratingSum = 0;
                int ratingTotal = 0;
                float ratingAvg = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ratingSum = ratingSum + Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                    ratingTotal++;
                }

                if(ratingTotal != 0){

                    ratingAvg = ratingSum/ratingTotal;
                    NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                    formatter.setMaximumFractionDigits(1);
                    formatter.setMinimumFractionDigits(1);
                    formatter.setRoundingMode(RoundingMode.HALF_EVEN);
                    String avarageRating = formatter.format(ratingAvg);

                    avgRating.setText(avarageRating);
                    //Toast.makeText(mActivity, "Show Ratings"+avgRating, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void storeRatingBars(String storeId, final int numRating, final ProgressBar progressBar){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int ratingTotal = 0;
                int progressCount = 0;
                int progressPercentage = 0, progress =0;

                ratingTotal = (int) snapshot.getChildrenCount();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try{
                        int ratingNumber = Integer.parseInt(dataSnapshot.child("rating").getValue().toString());
                        if (ratingNumber == numRating){
                            progressCount++;
                        }
                    }catch (NumberFormatException ignored){}

                }

                /*progress = ((progressCount/ratingTotal)*100);
                progressPercentage = progress * 100;
                progressBar.setProgress(progressPercentage);*/
                progressBar.setProgress(progressCount);
                progressBar.setMax(ratingTotal);

                //Toast.makeText(ShopDetailsActivity.this, "Total Ratings " + progressCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*public static void ratingBars(String storeId, final int numProgress, final ProgressBar progressBar){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(storeId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int ratingTotal = 0;
                int progressCount = 0;
                int progressPercentage = 0;

                ratingTotal = (int) snapshot.getChildrenCount();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    if (dataSnapshot.child("rating").getValue().equals(numProgress)){
                        progressCount++;
                    }
                }

                //progressPercentage = progressCount/ratingTotal*100;
                progressBar.setProgress(progressCount);
                progressBar.setMax(ratingTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/
}
