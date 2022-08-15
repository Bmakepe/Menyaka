package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.DeliveryOptionsActivity;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ViewHolder> {

    Context context;
    ArrayList<Product> productList;

    FirebaseUser firebaseUser;

    public ProductItemAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        try{
            Picasso.get().load(product.getProductImg()).into(holder.productImage);
        }catch (NullPointerException ignored){}

        holder.productName.setText(product.getProductName());
        holder.productDescription.setText(product.getProductCategory());
        holder.productPrice.setText("M " + product.getPrice());

        RatingsCalculator.ratingHeader(product.getProductId(), holder.product_rating, holder.proRatingCounter);
        getStoreDetails(holder, product);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", product.getProductId());
                context.startActivity(intent);
            }
        });

    }

    private void getStoreDetails(ViewHolder holder, Product product) {
        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    if (retail.getRetail_id().equals(product.getStoreId())){
                        holder.productStore.setText(retail.getRetailName());

                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.productShopLogo);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productDescription, proRatingCounter, productStore;
        RatingBar product_rating;
        CircleImageView productShopLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.productImage);
            productStore = itemView.findViewById(R.id.productStore);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            product_rating = itemView.findViewById(R.id.product_rating);
            proRatingCounter = itemView.findViewById(R.id.proRatingCounter);
            productShopLogo = itemView.findViewById(R.id.product_logo_Store);
        }
    }
}
