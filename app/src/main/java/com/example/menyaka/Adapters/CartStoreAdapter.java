package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.CartActivity;
import com.example.menyaka.DeliveryOptionsActivity;
import com.example.menyaka.MainActivity;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CartStoreAdapter extends RecyclerView.Adapter<CartStoreAdapter.ViewHolder> {

    Context context;
    FirebaseUser firebaseUser;
    ProductImageAdapter adapter;

    //for getting cart items
    ArrayList<CartItems> cartProducts;

    ArrayList<String> cartKeys, myCartStoreIDs, cartProductsIDs;
    DatabaseReference productRef, retailRef;

    String cartKey, cartStoreID, firebaseKey;

    public CartStoreAdapter(Context context, ArrayList<String> cartKeys, ArrayList<String> myCartStoreIDs) {
        this.context = context;
        this.cartKeys = cartKeys;
        this.myCartStoreIDs = myCartStoreIDs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_store_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        cartKey = cartKeys.get(position);
        cartStoreID = myCartStoreIDs.get(position);

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseKey = firebaseUser + myCartStoreIDs.get(position);

        cartProducts = new ArrayList<>();
        cartProductsIDs = new ArrayList<>();
        getCartProducts(position, holder);
        getStoreDetails(cartStoreID, holder);
        RatingsCalculator.totalRating(myCartStoreIDs.get(position), holder.cartStoreRating);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CartActivity.class);
                intent.putExtra("storeId", myCartStoreIDs.get(position));
                context.startActivity(intent);
            }
        });

        holder.cartStoreName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent storeIntent = new Intent(context, ShopDetailsActivity.class);
                storeIntent.putExtra("storeId", myCartStoreIDs.get(position));
                context.startActivity(storeIntent);
            }
        });

    }

    private void getStoreDetails(String cartStoreID, ViewHolder holder) {
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(cartStoreID)){
                        holder.cartStoreName.setText(retail.getRetailName());

                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.cartStoreLogo);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCartProducts(int position, ViewHolder holder) {

        final DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts")
                .child(cartKeys.get(position)).child("CartProducts");

        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    cartProducts.clear();
                    cartProductsIDs.clear();

                    holder.storeItems.setText(snapshot.getChildrenCount() + " items");

                }else{
                    Toast.makeText(context, "Could not retrieve your cart items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return cartKeys.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView cartStoreLogo;
        TextView cartStoreName, storeItems, viewCartBTN;
        RatingBar cartStoreRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cartStoreLogo = itemView.findViewById(R.id.cart_store_logo);
            cartStoreName = itemView.findViewById(R.id.cartStoreName);
            storeItems = itemView.findViewById(R.id.cartItemCount);
            cartStoreRating = itemView.findViewById(R.id.cartStoreRating);
            viewCartBTN = itemView.findViewById(R.id.viewCartBTN);
        }
    }
}
