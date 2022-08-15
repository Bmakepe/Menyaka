package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menyaka.HotDealDetailActivity;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.Sales;
import com.example.menyaka.R;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HotDealListAdapter extends RecyclerView.Adapter<HotDealListAdapter.ViewHolder> {

    private List<HotDeal> hotDeals;
    private Context context;

    FirebaseUser firebaseUser;

    public HotDealListAdapter(List<HotDeal> hotDeals, Context context) {
        this.hotDeals = hotDeals;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.sales, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        HotDeal list_item = hotDeals.get(position);

        holder.item_name.setText(list_item.getItemName());
        holder.discount_price.setText("Now M " + list_item.getDiscount());
        getShopInfo(holder.shop_img, holder.storeName, list_item.getStoreId());
        RatingsCalculator.totalRating(list_item.getHotDeal_id(), holder.dealRating);

        try{
            Picasso.get().load(list_item.getItemUrl()).into(holder.deal_image);
            holder.loader.setVisibility(View.GONE);
        }catch (NullPointerException e){
            holder.loader.setVisibility(View.GONE);
        }

        holder.deal_image.setColorFilter(ContextCompat.getColor(
                context,
                R.color.black_overlay),
                PorterDuff.Mode.SRC_OVER
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent hotDealDetailActivity = new Intent(context, HotDealDetailActivity.class);
                hotDealDetailActivity.putExtra("hotDeal_id", list_item.getHotDeal_id());
                context.startActivity(hotDealDetailActivity);
            }
        });
    }

    private void getShopInfo(final ImageView imageView, final TextView username, String publisherId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Retails").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Retail retail = dataSnapshot.getValue(Retail.class);
                assert retail != null;
                try {
                    Glide.with(context).load(retail.getImageUrl()).into(imageView);
                    username.setText(retail.getRetailName());
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return hotDeals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //for normal hot deals
        TextView storeName;
        ImageView deal_image,shop_img;
        TextView item_name;
        TextView discount_price;
        ProgressBar loader;
        RatingBar dealRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shop_img = itemView.findViewById(R.id.hot_deal_shop_img);
            storeName = itemView.findViewById(R.id.store_name_hotDeal_);
            deal_image = itemView.findViewById(R.id.hot_deal_img);
            item_name = itemView.findViewById(R.id.hot_deal_item_name);
            discount_price = itemView.findViewById(R.id.hot_deal_discount_amount);
            loader = itemView.findViewById(R.id.hotDeal_Loader);
            dealRating = itemView.findViewById(R.id.hotDealItemRating);

        }
    }

}
