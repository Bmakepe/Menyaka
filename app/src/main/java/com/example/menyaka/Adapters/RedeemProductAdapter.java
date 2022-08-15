package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Product;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RedeemProductAdapter extends RecyclerView.Adapter<RedeemProductAdapter.ViewHolder> {

    Context context;
    ArrayList<Product> productList;

    FirebaseUser firebaseUser;;

    public RedeemProductAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.redeem_product_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        try{
            Picasso.get().load(product.getProductImg()).into(holder.redeemProductPic);
        }catch (NullPointerException ignored){}

        holder.productName.setText(product.getProductName());
        holder.productDescription.setText(product.getProductCategory());
        holder.productPrice.setText("M " + product.getPrice());

        holder.redeemBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You will be able to redeem this product", Toast.LENGTH_SHORT).show();
            }
        });

        holder.redeemProductPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", product.getProductId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView redeemProductPic;
        ProgressBar imageLoader;
        TextView productName, productPrice, productDescription;
        Button redeemBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            redeemProductPic = itemView.findViewById(R.id.redeemProductImage);
            imageLoader = itemView.findViewById(R.id.redeemImageLoader);
            productName = itemView.findViewById(R.id.redeem_product_name);
            productPrice = itemView.findViewById(R.id.redeem_product_price);
            productDescription = itemView.findViewById(R.id.redeemProductDesc);
            redeemBTN = itemView.findViewById(R.id.redeemProductBTN);

        }
    }
}
