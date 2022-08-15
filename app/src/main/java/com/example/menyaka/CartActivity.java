package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.CartAdapter;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends AppCompatActivity {

    //for displaying products in the cart for checkout
    private ArrayList<CartItems> cartItemsList;
    private CartAdapter cartAdapter;
    private RecyclerView cartRecycler;

    private ImageView cart_close;
    private TextView totalPayment, cartHeader;
    private FirebaseUser user;
    private DatabaseReference cartRef;

    private String storeID, myStoreName, key;
    int totalBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_layout);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("MyTotalAmount"));

        Intent intent = getIntent();
        storeID = intent.getExtras().getString("storeId");

        cart_close = findViewById(R.id.cartBackBTN);
        totalPayment = findViewById(R.id.totalPrice);
        cartHeader = findViewById(R.id.cartHeader);
        cartRecycler = findViewById(R.id.cart_items_Recycler);

        cartItemsList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        key = user.getUid() + storeID;
        cartRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(key);

        cartRecycler.setHasFixedSize(true);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(CartActivity.this, cartItemsList);
        cartRecycler.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        cartAdapter.notifyDataSetChanged();

        getStoreDetails();
        getCartItems();

        findViewById(R.id.shopCategoryBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CartActivity.this, ShopCategoryActivity.class);
                intent.putExtra("storeId", storeID);
                startActivity(intent);
            }
        });

        cartHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CartActivity.this, "You will be able to access " + myStoreName, Toast.LENGTH_SHORT).show();
            }
        });

        cart_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.placeOrderBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartItemsList.isEmpty()){
                    Toast.makeText(CartActivity.this, "The shopping cart of " + myStoreName + " is empty, can not process empty shopping basket", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    builder.setTitle("PAYMENT")
                            .setMessage("You Are Now Starting Your Secure Payment Process")
                            .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateCartDetails();
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
            }
        });
    }

    private void updateCartDetails() {
        HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("totalPrice", String.valueOf(totalBill));
        cartRef.updateChildren(cartMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent intent = new Intent(CartActivity.this, DeliveryOptionsActivity.class);
                        intent.putExtra("cartID", key);
                        intent.putExtra("buyMethod", "Cart Buy");
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CartActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStoreDetails() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Retails");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if(retail.getRetail_id().equals(storeID))
                        myStoreName = retail.getRetailName();
                    cartHeader.setText("My " + myStoreName + " Shopping Basket");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCartItems() {
        cartRef.child("CartProducts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            CartItems items = ds.getValue(CartItems.class);
                            cartItemsList.add(items);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            totalBill = intent.getIntExtra("totalAmount", 0);
            DecimalFormat format = new DecimalFormat("##.00");
            totalPayment.setText("Total: M " + format.format(totalBill));
        }
    };
}