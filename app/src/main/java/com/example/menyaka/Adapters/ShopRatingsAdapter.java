package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ShopRatingsAdapter extends RecyclerView.Adapter<ShopRatingsAdapter.ViewHolder>{

    private List<Retail> shopRatings;
    private Context context;

    FirebaseUser firebaseUser;

    public ShopRatingsAdapter(List<Retail> shopRatings, Context context){
        this.shopRatings = shopRatings;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_rating, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopRatingsAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Retail list_item = shopRatings.get(position);
        final ViewHolder viewHolder = holder;
        viewHolder.storeName.setText(list_item.getRetailName());
        viewHolder.btn_favourates.setVisibility(View.VISIBLE);
        //viewHolder.ratings.setNumStars(list_item.getRatingStatus());

        Glide.with(context).load(list_item.getImageUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        //viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        //viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.deal_image);
        isFavourite(list_item.getRetail_id(), viewHolder.btn_favourates);
        RatingsCalculator.totalRating(list_item.getRetail_id(), viewHolder.ratings);

        if(list_item.getRetail_id().equals(firebaseUser.getUid())){
            viewHolder.btn_favourates.setVisibility(View.GONE);
        }

        viewHolder.btn_favourates.setOnClickListener(new View.OnClickListener() {

            //DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MyStores").child(list_item.getRetail_id());

            @Override
            public void onClick(View view) {
                if (viewHolder.btn_favourates.getText().toString().equals("Add Store")){

                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(list_item.getRetail_id()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(list_item.getRetail_id())
                            .child(firebaseUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(list_item.getRetail_id()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(list_item.getRetail_id())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopRatings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeName;
        ImageView deal_image;
        RatingBar ratings;
        TextView btn_favourates;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storeName = itemView.findViewById(R.id.shopRatingName);
            deal_image = itemView.findViewById(R.id.storeRatingImage);
            ratings = itemView.findViewById(R.id.ratingBar);
            btn_favourates = itemView.findViewById(R.id.favourates);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent shopDetail = new Intent(context, ShopDetailsActivity.class);
                    int position = getAdapterPosition();

                    shopDetail.putExtra("retail_id", shopRatings.get(position).getRetail_id());
                    shopDetail.putExtra("storeImage", shopRatings.get(position).getImageUrl());
                    shopDetail.putExtra("storeName", shopRatings.get(position).getRetailName());
                    shopDetail.putExtra("storeDescription", shopRatings.get(position).getRetailDescription());
                    shopDetail.putExtra("storeCategory", shopRatings.get(position).getCategory());

                    context.startActivity(shopDetail);
                }
            });
        }
    }

    private void isFavourite(final String retail_id, final TextView button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("MyStores").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(retail_id).exists()){
                    button.setText(R.string.my_store);
                } else {
                    button.setText("Add Store");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
