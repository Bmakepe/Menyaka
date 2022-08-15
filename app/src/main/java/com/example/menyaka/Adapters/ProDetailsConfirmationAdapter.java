package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProDetailsConfirmationAdapter extends RecyclerView.Adapter<ProDetailsConfirmationAdapter.ViewHolder>{
    Context context;
    ArrayList<CartItems> items;
    String cartID;

    int totalAmount = 0;

    DatabaseReference cartReference;

    public ProDetailsConfirmationAdapter(Context context, ArrayList<CartItems> items, String cartID) {
        this.context = context;
        this.items = items;
        this.cartID = cartID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pro_details_confirmation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItems cartItems = items.get(position);

        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts")
                .child(cartID).child("CartProducts");

        getCartProductDetails(holder, cartItems.getProductID());

        holder.cancelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeBasketItem(cartItems.getProductID());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", cartItems.getProductID());
                context.startActivity(intent);
            }
        });
    }

    private void getCartProductDetails(ViewHolder holder, String productID) {
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    CartItems items = ds.getValue(CartItems.class);
                    if (items.getProductID().equals(productID)){

                        holder.productQuantity.setText("Quantity: " + items.getOrderQuantity() +" items");

                        final DatabaseReference productReference = FirebaseDatabase.getInstance().getReference("Products");
                        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()){
                                    Product product = data.getValue(Product.class);
                                    if (product.getProductId().equals(items.getProductID())){

                                        holder.productName.setText(product.getProductName());

                                        String price = product.getPrice();
                                        holder.productPrice.setText("Price: M " + price);

                                        double total = Double.parseDouble(items.getOrderQuantity()) * Double.parseDouble(price);
                                        DecimalFormat format = new DecimalFormat("##.00");
                                        holder.totalPrice.setText("Total: M " + format.format(total));

                                        totalAmount = totalAmount + (int)total;
                                        Intent intent = new Intent("FinalPaymentDue");
                                        intent.putExtra("finalPaymentDue", totalAmount);

                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                        try{
                                            Picasso.get().load(product.getProductImg()).into(holder.productIMG);
                                        }catch (NullPointerException ignored){}
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Retails");
                        storeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    Retail retail = ds.getValue(Retail.class);
                                    assert retail != null;
                                    if(retail.getRetail_id().equals(items.getStoreID()))
                                        holder.productRetailer.setText(retail.getRetailName());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeBasketItem(String productID) {
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CartItems item = ds.getValue(CartItems.class);

                        assert item != null;
                        if (item.getProductID().equals(productID)){
                            ds.getRef().removeValue();
                            Toast.makeText(context, "Cart Item Has Been Removed Successfully", Toast.LENGTH_SHORT).show();
                        }
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
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productPrice, productRetailer, productQuantity, totalPrice;
        ImageView productIMG, cancelItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.tv_ProductName);
            productPrice = itemView.findViewById(R.id.tvPPrice);
            productRetailer = itemView.findViewById(R.id.tv_xx_retailer_name);
            productQuantity = itemView.findViewById(R.id.tvPQuantity);
            productIMG = itemView.findViewById(R.id.iv_product_image);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cancelItem = itemView.findViewById(R.id.cancelItem);
        }
    }
}
