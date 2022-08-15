package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.DeliveryOptionsActivity;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> {

    Context context;
    ArrayList<Product> wishedProducts;
    FirebaseUser firebaseUser;

    DatabaseReference cartReference;
    String timestamp, purchaseMethod;

    public WishListAdapter(Context context, ArrayList<Product> wishedProducts) {
        this.context = context;
        this.wishedProducts = wishedProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wish_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Product product = wishedProducts.get(position);
        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("M " + product.getPrice());

        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts");
        timestamp = String.valueOf(System.currentTimeMillis());

        try{
            Picasso.get().load(product.getProductImg()).into(holder.productImage);
            holder.imageLoader.setVisibility(View.GONE);
        }catch (NullPointerException ignored){}

        getShopName(product.getStoreId(), holder);
        checkWish(product.getProductId(), holder);

        holder.wishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.wishBTN.getTag().equals("noWish")){
                    FirebaseDatabase.getInstance().getReference().child("WishList").child(product.getProductId())
                            .child(firebaseUser.getUid()).setValue(true);
                    Toast.makeText(context, "Added to wish list successfully", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseDatabase.getInstance().getReference().child("WishList").child(product.getProductId())
                            .child(firebaseUser.getUid()).removeValue();
                    Toast.makeText(context, "Removed from the wish list successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productID", product.getProductId());
                context.startActivity(intent);
            }
        });

        holder.buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("PAYMENT")
                        .setMessage("You Are Now Starting Your Secure Payment Process")
                        .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                purchaseMethod = "Buy Now";
                                addToBuyNowBasket(purchaseMethod, product);
                                //addToShoppingBasket(product);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
    }

    private void addToBuyNowBasket(String purchaseMethod, Product product) {

        String buyKey = cartReference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("storeID", product.getStoreId());
        hashMap.put("status", "pending...");
        hashMap.put("purchaseMethod", purchaseMethod);
        hashMap.put("timestamp", timestamp);
        hashMap.put("totalPrice", String.valueOf(product.getPrice()));

        assert buyKey != null;
        cartReference.child(buyKey).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if ("Buy Now".equals(purchaseMethod)) {
                                buyNow(buyKey, product);
                            } else {
                                Toast.makeText(context, "Unable to buy at the moment", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(context, "Cannot Process Transaction", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error adding transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buyNow(String buyKey, Product product) {
        final DatabaseReference refCart = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(buyKey);

        HashMap<String, Object> productsMap = new HashMap<>();
        productsMap.put("productID", product.getProductId());
        productsMap.put("orderQuantity", "1");
        productsMap.put("productSize", product.getProductSize());
        productsMap.put("productPrice", product.getPrice());
        productsMap.put("storeID", product.getStoreId());
        productsMap.put("status", "pending");

        refCart.child("CartProducts").child(product.getProductId()).setValue(productsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(context, DeliveryOptionsActivity.class);
                        intent.putExtra("cartID", buyKey);
                        intent.putExtra("buyMethod", purchaseMethod);
                        context.startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Unable to add product to cart", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addToShoppingBasket(Product product) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CartItems");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productID", product.getProductId());
        hashMap.put("orderNo", key);
        hashMap.put("userID", user.getUid());
        hashMap.put("storeID", product.getStoreId());
        hashMap.put("orderQuantity", "1");
        hashMap.put("productSize", "2 kg");
        hashMap.put("purchaseMethod", "Buy Now");
        hashMap.put("status", "pending...");

        reference.child(key).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if("Buy Now".equals("Buy Now")){
                            Intent intent = new Intent(context, DeliveryOptionsActivity.class);
                            intent.putExtra("storeID", product.getStoreId());
                            intent.putExtra("productID", product.getProductId());
                            intent.putExtra("buyMethod", "Buy Now");
                            context.startActivity(intent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "failed to add item to cart basket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkWish(String productID, ViewHolder holder) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("WishList").child(productID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assert user != null;
                if(snapshot.child(user.getUid()).exists()){
                    holder.wishBTN.setTag("wished");
                    holder.wishBTN.setImageResource(R.drawable.ic_baseline_favorite_24);
                }else{
                    holder.wishBTN.setTag("noWish");
                    holder.wishBTN.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getShopName(String storeId, ViewHolder holder) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Retails").child(storeId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Retail retail = snapshot.getValue(Retail.class);

                holder.shopName.setText(retail.getRetailName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return wishedProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView productName, shopName, productPrice;
        ImageView productImage;
        ImageButton wishBTN;
        ProgressBar imageLoader;
        Button buyNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.wish_item_pic);
            wishBTN = itemView.findViewById(R.id.wish_list_btn);
            buyNow = itemView.findViewById(R.id.wishlist_buy_now_btn);

            productName = itemView.findViewById(R.id.wish_product_name);
            shopName = itemView.findViewById(R.id.wish_shop_name);
            productPrice = itemView.findViewById(R.id.wish_product_price);
            imageLoader = itemView.findViewById(R.id.wishlistImageLoader);

        }
    }
}
