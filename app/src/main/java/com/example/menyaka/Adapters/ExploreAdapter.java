package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.AgentProfileActivity;
import com.example.menyaka.HotDealActivity;
import com.example.menyaka.HotDealDetailActivity;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.PostDetailActivity;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    Context context;
    ArrayList<String> exploreItems;

    DatabaseReference userRef, productsRef, storeRef, postsRef, agentsRef, hotDealsRef;

    public ExploreAdapter(Context context, ArrayList<String> exploreItems) {
        this.context = context;
        this.exploreItems = exploreItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.explore_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(exploreItems.get(position));
        getItemDetails(holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userRef = FirebaseDatabase.getInstance().getReference("Users").child(exploreItems.get(position));
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            Intent profileIntent = new Intent(context, ViewProfileActivity.class);
                            profileIntent.putExtra("userID", exploreItems.get(position));
                            context.startActivity(profileIntent);

                        }else{

                            productsRef = FirebaseDatabase.getInstance().getReference("Products").child(exploreItems.get(position));
                            productsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()){

                                        Intent productIntent = new Intent(context, ProductDetailActivity.class);
                                        productIntent.putExtra("productID", exploreItems.get(position));
                                        context.startActivity(productIntent);

                                    }else{

                                        storeRef = FirebaseDatabase.getInstance().getReference("Retails").child(exploreItems.get(position));
                                        storeRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()){
                                                     Intent storeIntent = new Intent(context, ShopDetailsActivity.class);
                                                     storeIntent.putExtra("storeId", exploreItems.get(position));
                                                     context.startActivity(storeIntent);

                                                }else{

                                                    postsRef = FirebaseDatabase.getInstance().getReference("Moments").child(exploreItems.get(position));
                                                    postsRef.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            if (snapshot.exists()){

                                                                addView(exploreItems.get(position));

                                                                Intent postIntent = new Intent(context, MomentActivity.class);
                                                                postIntent.putExtra("momentId", exploreItems.get(position));
                                                                context.startActivity(postIntent);

                                                            }else{

                                                                agentsRef = FirebaseDatabase.getInstance().getReference("ShippingAgents").child(exploreItems.get(position));
                                                                agentsRef.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.exists()){
                                                                            Intent agentIntent = new Intent(context, AgentProfileActivity.class);
                                                                            agentIntent.putExtra("agentID", exploreItems.get(position));
                                                                            context.startActivity(agentIntent);

                                                                        }else{

                                                                            hotDealsRef = FirebaseDatabase.getInstance().getReference("HotDeals");
                                                                            hotDealsRef.addValueEventListener(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                                                        hotDealsRef.child(Objects.requireNonNull(ds.getKey())).child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                if (snapshot.exists()){

                                                                                                    Intent hotDealDetailActivity = new Intent(context, HotDealDetailActivity.class);
                                                                                                    hotDealDetailActivity.putExtra("hotDeal_id", exploreItems.get(position));
                                                                                                    context.startActivity(hotDealDetailActivity);

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
                                                                });                                                            }
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void getItemDetails(ViewHolder holder, int position) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    try{
                        assert user != null;
                        //holder.exploreImageArea.setVisibility(View.VISIBLE);
                        Picasso.get().load(user.getImageUrl()).into(holder.exploreImage);
                        holder.imageLoader.setVisibility(View.GONE);
                        holder.itemCategory.setImageResource(R.drawable.ic_person_black_24dp);
                    }catch (NullPointerException ignored){}

                }else{

                    productsRef = FirebaseDatabase.getInstance().getReference("Products").child(exploreItems.get(position));
                    productsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()){
                                Product product = snapshot.getValue(Product.class);
                                try{
                                    assert product != null;

                                    Picasso.get().load(product.getProductImg()).into(holder.exploreImage);
                                    holder.imageLoader.setVisibility(View.GONE);
                                    holder.itemCategory.setImageResource(R.drawable.ic_baseline_products_24);
                                }catch (NullPointerException ignored){}

                            }else{

                                storeRef = FirebaseDatabase.getInstance().getReference("Retails").child(exploreItems.get(position));
                                storeRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists()){
                                            Retail retail = snapshot.getValue(Retail.class);
                                            try{
                                                assert retail != null;

                                                Picasso.get().load(retail.getImageUrl()).into(holder.exploreImage);
                                                holder.imageLoader.setVisibility(View.GONE);
                                                holder.itemCategory.setImageResource(R.drawable.ic_store_dark);
                                            }catch (NullPointerException ignored){}

                                        }else{

                                            postsRef = FirebaseDatabase.getInstance().getReference("Moments").child(exploreItems.get(position));
                                            postsRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    if (snapshot.exists()){
                                                        Moment moment = snapshot.getValue(Moment.class);

                                                        assert moment != null;

                                                        if (moment.getPostType().equals("imagePost")){

                                                            try {

                                                                Picasso.get().load(moment.getImageUrl()).into(holder.exploreImage);
                                                                holder.imageLoader.setVisibility(View.GONE);
                                                                holder.itemCategory.setImageResource(R.drawable.ic_baseline_image_24);
                                                            } catch (NullPointerException ignored) {}

                                                        }

                                                    }else{

                                                        agentsRef = FirebaseDatabase.getInstance().getReference("ShippingAgents").child(exploreItems.get(position));
                                                        agentsRef.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()){
                                                                    ShippingAgents agents = snapshot.getValue(ShippingAgents.class);

                                                                    try{
                                                                        assert agents != null;

                                                                        Picasso.get().load(agents.getCompanyLogo()).into(holder.exploreImage);
                                                                        holder.imageLoader.setVisibility(View.GONE);
                                                                        holder.itemCategory.setImageResource(R.drawable.ic_baseline_local_shipping_24);
                                                                    }catch (NullPointerException ignored){}

                                                                }else{

                                                                    hotDealsRef = FirebaseDatabase.getInstance().getReference("HotDeals");
                                                                    hotDealsRef.addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                                                hotDealsRef.child(Objects.requireNonNull(ds.getKey())).child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        if (snapshot.exists()){
                                                                                            HotDeal hotDeal = snapshot.getValue(HotDeal.class);

                                                                                            try{
                                                                                                assert hotDeal != null;

                                                                                                Picasso.get().load(hotDeal.getItemUrl()).into(holder.exploreImage);
                                                                                                holder.imageLoader.setVisibility(View.GONE);
                                                                                                holder.itemCategory.setImageResource(R.drawable.ic_baseline_local_fire_department_24);
                                                                                            }catch (NullPointerException ignored){}
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addView(String postID) {
        FirebaseDatabase.getInstance().getReference("MenyakaViews")
                .child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    @Override
    public int getItemCount() {
        return exploreItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView exploreImage, itemCategory;
        ProgressBar imageLoader;
        RelativeLayout exploreImageArea;
        ConstraintLayout exploreVideoArea;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exploreImage = itemView.findViewById(R.id.exploreImage);
            itemCategory = itemView.findViewById(R.id.itemCategory);
            imageLoader = itemView.findViewById(R.id.exploreImageLoader);
            exploreImageArea = itemView.findViewById(R.id.exploreImageArea);
            exploreVideoArea = itemView.findViewById(R.id.exploreVideoArea);

        }
    }
}
