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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    Context context;
    ArrayList<String> productList;
    //ArrayList<String> storeIDs;

    public ProductImageAdapter(Context context, ArrayList<String> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_image_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //Toast.makeText(context, productList.get(position) + " items have been added, and the total number is " + productList.size(), Toast.LENGTH_SHORT).show();

        getProductDetails(holder, position);
        RatingsCalculator.totalRating(productList.get(position), holder.ratingBar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", productList.get(position));
                context.startActivity(intent);
            }
        });

    }

    private void getShopDetails(ViewHolder holder, String storeID) {
        final DatabaseReference retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail =  ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(storeID)){
                        holder.shopName.setText(retail.getRetailName());

                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.shopLogo);
                        }catch (NullPointerException ignored){}

                        //Toast.makeText(context, retail.getRetailName() + " has " + productList.size() + " products in its cart", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getProductDetails(ViewHolder holder, int position) {

        final DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product myProduct = ds.getValue(Product.class);
                    assert myProduct != null;

                    for (String ID : productList) {
                        if (myProduct.getProductId().equals(ID)) {
                            //if (myProduct.getStoreId().equals(storeIDs.get(position))) {
                                holder.productName.setText(myProduct.getProductName());

                                try {
                                    Picasso.get().load(myProduct.getProductImg()).into(holder.productImage);
                                } catch (NullPointerException ignored) {}
                           // }
                            getShopDetails(holder, myProduct.getStoreId());
                        }
                    }

                    /*DataSnapshot data = ds.child("CartProducts");
                    Product myProduct = data.getValue(Product.class);

                    assert myProduct != null;

                    for (String ID : productList) {
                        if (myProduct.getProductId().equals(ID)) {
                            holder.productName.setText(myProduct.getProductName());

                            try {
                                Picasso.get().load(myProduct.getProductImg()).into(holder.productImage);
                            } catch (NullPointerException ignored) {
                            }

                            getShopDetails(holder, myProduct.getStoreId());
                        }
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product myProduct = ds.getValue(Product.class);
                    assert myProduct != null;

                    for (String ID : productList) {
                        if (myProduct.getProductId().equals(ID)) {
                            if (myProduct.getStoreId().equals(storeIDs.get(position))) {
                                holder.productName.setText(myProduct.getProductName());

                                try {
                                    Picasso.get().load(myProduct.getProductImg()).into(holder.productImage);
                                } catch (NullPointerException ignored) {
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView shopLogo;
        ImageView productImage;
        ProgressBar productLoader;
        TextView shopName, productName;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shopLogo = itemView.findViewById(R.id.product_shop_logo);
            productImage = itemView.findViewById(R.id.product_image);
            productLoader = itemView.findViewById(R.id.productLoader);
            shopName = itemView.findViewById(R.id.product_store_name);
            productName = itemView.findViewById(R.id.product_item_name);
            ratingBar = itemView.findViewById(R.id.product_rating_area);
        }
    }
}
