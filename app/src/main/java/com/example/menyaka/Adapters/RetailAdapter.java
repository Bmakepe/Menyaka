package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.ShopActivity;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.RatingsCalculator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RetailAdapter extends RecyclerView.Adapter<RetailAdapter.ViewHolder> {

    Context context;
    ArrayList<MyStore> shopList;

    public RetailAdapter(Context context, ArrayList<MyStore> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyStore retail = shopList.get(position);

        holder.retailName.setText(retail.getRetailName());
        try{
            Picasso.get().load(retail.getImageUrl()).into(holder.retailLogo);
        }catch (NullPointerException ignored){}

        RatingsCalculator.totalRating(retail.getRetail_id(), holder.businessRating);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", retail.getRetail_id());
                context.startActivity(shopIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView retailLogo;
        TextView retailName;
        //ProgressBar loader;
        RatingBar businessRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            retailLogo = itemView.findViewById(R.id.shopLogo);
            retailName = itemView.findViewById(R.id.shop_name_);
            businessRating = itemView.findViewById(R.id.ratingBar);
        }
    }
}
