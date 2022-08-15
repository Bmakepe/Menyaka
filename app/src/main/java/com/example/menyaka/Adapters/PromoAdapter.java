package com.example.menyaka.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;

    Dialog showProduct;

    public PromoAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.promo_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = productList.get(position);
        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("Was : M"+product.getPrice());
        holder.productId = product.getProductId();

        productDetails(holder.productId);

        holder.product_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProduct.show();
            }
        });

        Glide.with(context).load(product.getProductImg())
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
                .into(holder.image_product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_product, product_view;
        public TextView productName, productPrice;
        String productId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_product = itemView.findViewById(R.id.item_img);
            product_view = itemView.findViewById(R.id.postAddToCard);
            productName = itemView.findViewById(R.id.item_name);
            productPrice = itemView.findViewById(R.id.original_price);

        }
    }

    private void getProductInfo(final ImageView imageView, final TextView username, String publisherId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").child(publisherId);

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

    private void productDetails(String id){
        showProduct = new Dialog(context);
        showProduct.setContentView(R.layout.show_product);
        showProduct.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        showProduct.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        ImageView close = showProduct.findViewById(R.id.close_product);
        ImageSlider slider = showProduct.findViewById(R.id.productImages);

        productImages(id,slider);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProduct.dismiss();
            }
        });

    }

    private void productImages(String product_id, final ImageSlider imageSlider){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("ProductImages").child(product_id);
        final List<SlideModel> slider = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    slider.add(new SlideModel(dataSnapshot.child("url").getValue().toString(), ScaleTypes.CENTER_INSIDE));
                    imageSlider.setImageList(slider, ScaleTypes.CENTER_INSIDE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
