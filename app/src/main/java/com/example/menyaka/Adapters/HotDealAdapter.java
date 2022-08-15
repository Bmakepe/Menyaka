package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.menyaka.HotDealDetailActivity;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.fragments.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HotDealAdapter extends RecyclerView.Adapter<HotDealAdapter.ViewHolder>{

    private List<HotDeal> hotDeals;
    private Context context;

    FirebaseUser firebaseUser;

    /*public static final int ITEM_HOT_DEAL = 0;
    public static final int ITEM_SPONSOR = 1;*/

    public HotDealAdapter(List<HotDeal> hotDeals, Context context) {
        this.hotDeals = hotDeals;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hot_deal_item, parent, false);
        return new ViewHolder(v);

        /*if (viewType == ITEM_HOT_DEAL) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hot_deal_item, parent, false);
            return new ViewHolder(v);
        }else if(viewType == ITEM_SPONSOR) {
            View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.sponsored_hot_deal_layout, parent, false);
            return new ViewHolder(v1);
        }else
            return null;*/

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        HotDeal list_item = hotDeals.get(position);

        holder.item_name.setText(list_item.getItemName());
        holder.discount_price.setText("Now M " + list_item.getDiscount());
        holder.storeId = list_item.getStoreId();
        getShopInfo(holder.shop_img, holder.storeName, holder.storeId);
        RatingsCalculator.totalRating(list_item.getHotDeal_id(), holder.hotDealRating);

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

        /*int viewType = getItemViewType(position);

        switch (viewType){
            case ITEM_HOT_DEAL:
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                HotDeal list_item = hotDeals.get(position);
                ViewHolder viewHolder = holder;

                viewHolder.item_name.setText(list_item.getItemName());
                viewHolder.discount_price.setText("Now M " + list_item.getDiscount());
                viewHolder.storeId = list_item.getStoreId();
                getShopInfo(viewHolder.shop_img, viewHolder.storeName, viewHolder.storeId);

                try{
                    Picasso.get().load(list_item.getItemUrl()).into(holder.deal_image);
                    holder.loader.setVisibility(View.GONE);
                }catch (NullPointerException e){
                    holder.loader.setVisibility(View.GONE);
                }

                viewHolder.deal_image.setColorFilter(ContextCompat.getColor(
                        context,
                        R.color.black_overlay),
                        PorterDuff.Mode.SRC_OVER
                );

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent hotDealDetailActivity = new Intent(context, HotDealDetailActivity.class);

                        hotDealDetailActivity.putExtra("deal_img", hotDeals.get(position).getItemUrl());
                        hotDealDetailActivity.putExtra("item_price", hotDeals.get(position).getItemPrice());
                        hotDealDetailActivity.putExtra("store_name", hotDeals.get(position).getStoreName());
                        hotDealDetailActivity.putExtra("shop_id", hotDeals.get(position).getStoreId());
                        hotDealDetailActivity.putExtra("item_name", hotDeals.get(position).getItemName());
                        hotDealDetailActivity.putExtra("deal_id", hotDeals.get(position).getHotDeal_id());

                        context.startActivity(hotDealDetailActivity);
                    }
                });
                break;

            case ITEM_SPONSOR:

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "You have clicked on the sponsored hot deal ad", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "You will add this product to your cart", Toast.LENGTH_SHORT).show();
                    }
                });

        }*/

    }

    @Override
    public int getItemCount() {

        return hotDeals.size();

        /*try{
            return hotDeals.size();
        }catch (NullPointerException e){
            e.printStackTrace();
            return 0;
        }*/
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //for normal hot deals
        TextView storeName;
        ImageView deal_image,shop_img;
        TextView item_name;
        TextView discount_price;
        String storeId;
        ProgressBar loader;
        RatingBar hotDealRating;

        //for sponsored hot deals
        ImageView addToCart;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shop_img = itemView.findViewById(R.id.shop_img);
            storeName = itemView.findViewById(R.id.date);
            deal_image = itemView.findViewById(R.id.deal_img);
            item_name = itemView.findViewById(R.id.item_name);
            discount_price = itemView.findViewById(R.id.discount);
            loader = itemView.findViewById(R.id.hotDealLoader);
            hotDealRating = itemView.findViewById(R.id.hotDealRating);

            addToCart = itemView.findViewById(R.id.sponsorAddToCart);
        }
    }

    /*@Override
    public int getItemViewType(int position) {
        if(position% HomeFragment.ITEM_BEFORE_SPONSOR == 0)
            return ITEM_SPONSOR;
        else
            return ITEM_HOT_DEAL;
    }*/

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
}
