package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    ArrayList<CartItems> cartItemsList;
    FirebaseUser firebaseUser;
    String price, key;
    int totalAmount = 0;

    public CartAdapter(Context context, ArrayList<CartItems> cartItemsList) {
        this.context = context;
        this.cartItemsList = cartItemsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItems product = cartItemsList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;
        key = firebaseUser.getUid() + product.getStoreID();

        getProductByID(product.getProductID(), holder);
        RatingsCalculator.totalRating(product.getProductID(), holder.ratingBar);
        //RatingsCalculator.setRating(product.getProductID(), holder.ratingCounter);
        RatingsCalculator.ratingHeader(product.getProductID(), holder.ratingBar, holder.ratingCounter);

        holder.orderQuantity.setText(product.getOrderQuantity());

        holder.removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeBasketItem(product.getProductID());
            }
        });

        holder.removeQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int q = Integer.parseInt(holder.orderQuantity.getText().toString());
                int qn = q - 1;
                holder.orderQuantity.setText(String.valueOf(qn));
                getFinalPrice(price, holder);

            }
        });

        holder.addQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Incremented", Toast.LENGTH_SHORT).show();
                try {
                    int q = Integer.parseInt(holder.orderQuantity.getText().toString());
                    q = q + 1;
                    holder.orderQuantity.setText(String.valueOf(q));
                    getFinalPrice(price, holder);

                }catch (NumberFormatException ignored){}
            }
        });

        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Product " + product.getProductID() + " has been clicked", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", product.getProductID());
                context.startActivity(intent);
                Toast.makeText(context, "Product " + product.getProductID() + " has been clicked", Toast.LENGTH_SHORT).show();
            }
        });

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", product.getProductID());
                context.startActivity(intent);
                Toast.makeText(context, "Product " + product.getProductID() + " has been clicked", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void removeBasketItem(String productID) {

        final DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts")
                .child(key).child("CartProducts");
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CartItems items = ds.getValue(CartItems.class);

                        assert items != null;
                        if (items.getProductID().equals(productID)){
                            ds.getRef().removeValue();
                            Toast.makeText(context, "Product has been removed from the cart", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CartItems");
        Query query = reference.orderByChild("productID").equalTo(productID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    private void getProductByID(String productID, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);

                    assert product != null;
                    if(product.getProductId().equals(productID)) {

                        holder.productName.setText(product.getProductName());
                        holder.productPrice.setText("Unit Price: M " + product.getPrice());
                        price = product.getPrice();
                        getFinalPrice(product.getPrice(), holder);

                        try {
                            Picasso.get().load(product.getProductImg()).into(holder.productImage);
                        } catch (NullPointerException ignored) {}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFinalPrice(String price, ViewHolder holder) {
        try {
            double initialPrice = Double.parseDouble(price);
            double quantity = Double.parseDouble(holder.orderQuantity.getText().toString());

            double finalPrice = initialPrice * quantity;
            DecimalFormat format = new DecimalFormat("##.00");

            holder.priceToPay.setText("M " + format.format(finalPrice));

            totalAmount = totalAmount + (int)finalPrice;
            Intent intent = new Intent("MyTotalAmount");
            intent.putExtra("totalAmount", totalAmount);

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        }catch (NumberFormatException ignored){}
    }

    @Override
    public int getItemCount() {
        return cartItemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productPrice, orderQuantity, priceToPay, ratingCounter;
        ImageView addQuantity, removeQuantity, removeItem, productImage;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.cart_item_pic);
            productName = itemView.findViewById(R.id.cart_product_name);
            productPrice = itemView.findViewById(R.id.cart_product_price);
            addQuantity = itemView.findViewById(R.id.addMoreProducts);
            removeQuantity = itemView.findViewById(R.id.lessMoreProducts);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            removeItem = itemView.findViewById(R.id.cancelOrder);
            priceToPay = itemView.findViewById(R.id.cart_price_to_pay);
            ratingCounter = itemView.findViewById(R.id.cart_rating_count);
            ratingBar = itemView.findViewById(R.id.cartProductRating);
        }
    }
}
