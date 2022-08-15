package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.OrdersConfirmAdapter;
import com.example.menyaka.Adapters.ProDetailsConfirmationAdapter;
import com.example.menyaka.Models.Address;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Orders;
import com.example.menyaka.Models.Retail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

public class OrderReviewActivity extends AppCompatActivity {

    TextView method, recipientName, defaultAddress, recipientContacts, orderReview;
    ImageView continueShopping;
    RecyclerView productsRecycler;

    //for displaying the order details
    TextView receiptID, orderItems, deliveryCost, subTotal, orderTotal, finishBTN, storeName;
    DecimalFormat format;

    FirebaseUser user;

    CardView cardView;

    String cartID, storeID, store_name;

    DatabaseReference ordersReference;

    OrdersConfirmAdapter adapter;
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
        finishBTN = findViewById(R.id.summaryContinue);
        cardView = findViewById(R.id.shoppingProgress);
        orderReview = findViewById(R.id.orderHeading);
        storeName = findViewById(R.id.storeName);
        productsRecycler = findViewById(R.id.confirmedProductsRecycler);

        receiptID = findViewById(R.id.receiptID);
        orderItems = findViewById(R.id.totalOrderItems);
        deliveryCost = findViewById(R.id.shippingCosts);
        subTotal = findViewById(R.id.subTotal);
        orderTotal = findViewById(R.id.totalOrderPrice);

        Intent intent = getIntent();
        cartID = intent.getExtras().getString("cartID");

        productsRecycler = findViewById(R.id.confirmedProductsRecycler);
        productsRecycler.setHasFixedSize(true);
        productsRecycler.setLayoutManager(new LinearLayoutManager(this));

        productsItems = new ArrayList<>();

        cardView.setVisibility(View.GONE);
        finishBTN.setText("FINISH");
        orderReview.setText("Order Review");
        findViewById(R.id.summaryBackBTN).setVisibility(View.GONE);
        findViewById(R.id.continueShopping).setVisibility(View.GONE);

        Toast.makeText(this, "This cart id is " + cartID, Toast.LENGTH_SHORT).show();

        user =  FirebaseAuth.getInstance().getCurrentUser();
        ordersReference = FirebaseDatabase.getInstance().getReference("Orders");

        getOrderDetails();

        finishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderReviewActivity.this);
                builder.setTitle("Rating")
                        .setMessage("Do you wish to rate the service you have received from " + store_name)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent intent1 = new Intent(OrderReviewActivity.this, ReviewActivity.class);
                                intent1.putExtra("exit", "return");
                                intent1.putExtra("storeID", storeID);
                                startActivity(intent1);

                            }
                        }).setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(OrderReviewActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                }).show();
            }
        });

        findViewById(R.id.viewMoreProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OrderReviewActivity.this, "View More", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOrderDetails() {
        ordersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Orders orders = ds.getValue(Orders.class);

                    assert orders != null;

                    if (orders.getReceiptID().equals(cartID)){
                        receiptID.setText(orders.getReceiptID());
                        deliveryCost.setText(orders.getDeliveryCharges());
                        subTotal.setText(orders.getSubTotal());
                        orderTotal.setText(String.valueOf(orders.getTotalPrice()));

                        recipientName.setText(orders.getDeliveryAddress());

                        ordersReference.child(cartID).child("Products")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            productsItems.clear();
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                CartItems items = ds.getValue(CartItems.class);
                                                productsItems.add(items);
                                            }

                                            adapter = new OrdersConfirmAdapter (OrderReviewActivity.this, productsItems, cartID);
                                            productsRecycler.setAdapter(adapter);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        final DatabaseReference retailRef = FirebaseDatabase.getInstance().getReference("Retails");
                        retailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Retail retail = ds.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(orders.getStoreID())){
                                       storeName.setText(retail.getRetailName());

                                        storeID = retail.getRetail_id();
                                        store_name = retail.getRetailName();

                                        if (orders.getDeliveryAddress().equals("Pick Up In Store")){
                                            recipientName.setText(retail.getRetailName());
                                            recipientContacts.setText("Store Contacts will go here");
                                            defaultAddress.setText("Store Address will go here");
                                        }else{
                                            final DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
                                            addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        Address address = ds.getValue(Address.class);

                                                        assert address != null;
                                                        if (address.getAddressID().equals(orders.getDeliveryAddress())){
                                                            recipientName.setText(address.getDeliveryName());
                                                            recipientContacts.setText(address.getDeliveryNumber());
                                                            defaultAddress.setText(address.getDeliveryHouse() + ", " + address.getDeliveryRoad() + ", " + address.getDeliveryNeighbourHood()
                                                                    + ", " + address.getDeliveryDistrict() + ", " + address.getDeliveryZipCode());
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }


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

    private void getOrderProducts() {

    }
}