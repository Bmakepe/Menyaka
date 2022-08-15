package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.ProDetailsConfirmationAdapter;
import com.example.menyaka.Models.Address;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class OrderSummaryActivity extends AppCompatActivity {

    TextView method, recipientName, defaultAddress, recipientContacts, orderReview;
    ImageView continueShopping;

    //for displaying products to be paid for
    RecyclerView productsRecycler;

    //for displaying the order details
    TextView receiptID, orderItems, deliveryCost, subTotal, orderTotal, storeName;
    DecimalFormat format; 

    FirebaseUser firebaseUser;

    String cartID, orderID, storeID;

    DatabaseReference cartReference, retailRef;

    ProDetailsConfirmationAdapter adapter;
    ArrayList<CartItems> productsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        format = new DecimalFormat("##.00");

        method = findViewById(R.id.preferredMethod);
        recipientName = findViewById(R.id.recipientName);
        defaultAddress = findViewById(R.id.finalDeliveryAddress);
        recipientContacts = findViewById(R.id.recipientContacts);
        continueShopping = findViewById(R.id.continueShopping);
        orderReview = findViewById(R.id.orderHeading);

        storeName = findViewById(R.id.storeName);
        receiptID = findViewById(R.id.receiptID);
        orderItems = findViewById(R.id.totalOrderItems);
        deliveryCost = findViewById(R.id.shippingCosts);
        subTotal = findViewById(R.id.subTotal);
        orderTotal = findViewById(R.id.totalOrderPrice);

        productsRecycler = findViewById(R.id.confirmedProductsRecycler);
        productsRecycler.setHasFixedSize(true);
        productsRecycler.setLayoutManager(new LinearLayoutManager(this));

        orderReview.setText("Shopping and Delivery Confirmation");

        Intent intent = getIntent();
        cartID = intent.getExtras().getString("cartID");

        firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(cartID);
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");

        productsItems = new ArrayList<>();

        getShippingDetails();
        getCartProducts();
        getOrderDetails();

        continueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderSummaryActivity.this, ShopCategoryActivity.class);
                intent.putExtra("storeId", storeID);
                startActivity(intent);
            }
        });

        findViewById(R.id.summaryBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.viewMoreProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OrderSummaryActivity.this, "View More", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.summaryContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
                builder.setTitle("PAYMENT")
                        .setMessage("Continue to process payment")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //startActivity(new Intent(OrderSummaryActivity.this, PaymentPlanActivity.class));
                                updateOrderDetails();
                            }
                        }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

    }

    private void updateOrderDetails() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("receiptID", orderID);

        cartReference.updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(OrderSummaryActivity.this, PaymentPlanActivity.class);
                        intent.putExtra("cartID", cartID);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderSummaryActivity.this, "Unable to update receipt details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOrderDetails() {
        orderID = cartReference.push().getKey();
        receiptID.setText(orderID);

        deliveryCost.setText("M 20.00");

        cartReference.child("CartProducts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            orderItems.setText(snapshot.getChildrenCount() + " items");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void getCartProducts() {
        cartReference.child("CartProducts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            productsItems.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                CartItems items = ds.getValue(CartItems.class);
                                productsItems.add(items);
                            }
                            adapter = new ProDetailsConfirmationAdapter(OrderSummaryActivity.this, productsItems, cartID);
                            productsRecycler.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(OrderSummaryActivity.this, "Cart Items do not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getShippingDetails() {
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CartItems items = snapshot.getValue(CartItems.class);
                    assert items != null;
                    storeID = items.getStoreID();

                    if (items.getDeliveryAddress().equals("Pick Up In Store")){
                        getStoreDetails(storeID);
                    }else{
                        getUserDetails(items.getDeliveryAddress());
                    }

                    getStore(items.getStoreID());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getStore(String storeID) {
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    if (retail.getRetail_id().equals(storeID)){
                        storeName.setText(retail.getRetailName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUserDetails(String deliveryAddress) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Address address = ds.getValue(Address.class);

                    assert address != null;
                    if (address.getAddressID().equals(deliveryAddress)){

                        recipientName.setText(address.getDeliveryName());
                        defaultAddress.setText(address.getDeliveryHouse() + ", " + address.getDeliveryRoad() + ", " + address.getDeliveryNeighbourHood()
                                + ", " + address.getDeliveryDistrict() + ", " + address.getDeliveryZipCode());
                        recipientContacts.setText(address.getDeliveryNumber());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoreDetails(String storeID) {
        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(storeID)){

                        recipientName.setText(retail.getRetailName());
                        defaultAddress.setText("Store Address Will go here");
                        recipientContacts.setText("Store Contacts will go here");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}