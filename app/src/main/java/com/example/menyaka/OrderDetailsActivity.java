package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.OrdersConfirmAdapter;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Orders;
import com.example.menyaka.Models.Retail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrderDetailsActivity extends AppCompatActivity {

    TextView orderID, orderProgress, shopName, timeStamp, totalPrice, orderCounter;
    ImageView backBTN, optionsMenu;
    CircleImageView shopLogo;
    RecyclerView ordersRecycler;

    ArrayList<CartItems> cartItems;
    ArrayList<String> productsList;
    OrdersConfirmAdapter adapter;

    String receiptID, storeID, store_name;

    DatabaseReference ordersRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details_activity);

        Intent intent = getIntent();
        receiptID = intent.getExtras().getString("orderID");

        orderID = findViewById(R.id.orderDetailsOrderID);
        orderProgress = findViewById(R.id.myOrderStatus);
        shopLogo = findViewById(R.id.orderDetailsShopLogo);
        backBTN = findViewById(R.id.myOrdersBackBTN);
        ordersRecycler = findViewById(R.id.orderDetailsRecycler);
        optionsMenu = findViewById(R.id.orderDetailsMenu);
        shopName = findViewById(R.id.orderDetailsShopName);
        timeStamp = findViewById(R.id.myOrderTimeStamp);
        totalPrice = findViewById(R.id.my_order_product_price);
        orderCounter = findViewById(R.id.my_order_item_count);

        user = FirebaseAuth.getInstance().getCurrentUser();
        //cartRef = FirebaseDatabase.getInstance().getReference("CartItems");
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        cartItems = new ArrayList<>();
        productsList = new ArrayList<>();

        ordersRecycler.setHasFixedSize(true);
        ordersRecycler.hasFixedSize();
        ordersRecycler.setLayoutManager(new LinearLayoutManager(OrderDetailsActivity.this));

        getOrderProducts();
        showOrderDetails();

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });

        shopLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(OrderDetailsActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", storeID);
                startActivity(shopIntent);
            }
        });

        shopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(OrderDetailsActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", storeID);
                startActivity(shopIntent);
            }
        });

    }

    private void showOrderDetails() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Orders orders = ds.getValue(Orders.class);

                    if(orders.getReceiptID().equals(receiptID))
                        if(orders.getUserID().equals(user.getUid())){
                            storeID = orders.getStoreID();
                            orderID.setText("Receipt No: " + orders.getReceiptID());
                            orderProgress.setText(orders.getStatus() + "...");
                            totalPrice.setText("Total Price: M " + orders.getTotalPrice() + ".00");
                            orderCounter.setText(orders.getTotalItems() + " Items");

                            try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(Long.parseLong(orders.getTimeStamp()));
                                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                timeStamp.setText(pTime);
                            }catch (NumberFormatException n){
                                Toast.makeText(OrderDetailsActivity.this, "Could not format time", Toast.LENGTH_SHORT).show();
                            }//for converting timestamp

                            final DatabaseReference picRef = FirebaseDatabase.getInstance().getReference("Retails");
                            picRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Retail retail = ds.getValue(Retail.class);

                                        if(orders.getStoreID().equals(retail.getRetail_id())) {
                                            shopName.setText(retail.getRetailName());
                                            store_name = retail.getRetailName();

                                            try {
                                                Picasso.get().load(retail.getImageUrl()).into(shopLogo);
                                            } catch (NullPointerException ignored) {}
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

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(OrderDetailsActivity.this, optionsMenu, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"View " + store_name + " Profile");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Order Details");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Delivery Details");
        popupMenu.getMenu().add(Menu.NONE, 3,0,"Download Receipt");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if(id == 0){
                    Intent shopIntent = new Intent(OrderDetailsActivity.this, ShopDetailsActivity.class);
                    shopIntent.putExtra("storeId", storeID);
                    startActivity(shopIntent);
                }else if(id == 1){
                    Toast.makeText(OrderDetailsActivity.this, "You will view order details", Toast.LENGTH_SHORT).show();
                }else if(id == 2){
                    Toast.makeText(OrderDetailsActivity.this, "You will view and edit delivery details", Toast.LENGTH_SHORT).show();
                }else if (id == 3){
                    Toast.makeText(OrderDetailsActivity.this, "You will be able to download the order receipt", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void getOrderProducts() {
        ordersRef.child(receiptID).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productsList.clear();

                if (snapshot.exists()){
                    for (DataSnapshot data : snapshot.getChildren()){
                        CartItems items = data.getValue(CartItems.class);
                        cartItems.add(items);
                    }

                    adapter = new OrdersConfirmAdapter(OrderDetailsActivity.this, cartItems, receiptID);
                    ordersRecycler.setAdapter(adapter);
                }else{
                    Toast.makeText(OrderDetailsActivity.this, "Can not retrieve order products", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}