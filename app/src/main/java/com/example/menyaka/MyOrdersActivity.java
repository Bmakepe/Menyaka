package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.OrdersAdapter;
import com.example.menyaka.Models.Orders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MyOrdersActivity extends AppCompatActivity {

    ArrayList<Orders> orders;

    RecyclerView ordersRecycler;
    TextView header;
    ImageView backBTN;

    DatabaseReference ordersRef;
    FirebaseUser firebaseUser;

    OrdersAdapter ordersAdapter;

    LinearLayout orderDetails, moreOrderDetails;

    TextView activeOrders, pendingOrders, completedOrders;
    int totalOrders = 0, ordersPending = 0, ordersCompleted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        ordersRecycler = findViewById(R.id.recycler_View);
        header = findViewById(R.id.recyclerHeader);
        backBTN = findViewById(R.id.recyclerBackBTN);
        orderDetails = findViewById(R.id.orderDetails);
        moreOrderDetails = findViewById(R.id.moreOrderDetails);
        activeOrders = findViewById(R.id.activeOrders);
        pendingOrders = findViewById(R.id.pendingOrders);
        completedOrders = findViewById(R.id.completedOrders);

        orderDetails.setVisibility(View.VISIBLE);
        moreOrderDetails.setVisibility(View.VISIBLE);
        findViewById(R.id.lineView).setVisibility(View.VISIBLE);

        header.setText("My Orders");

        orders = new ArrayList<>();

        ordersRecycler.hasFixedSize();
        ordersRecycler.setHasFixedSize(true);
        ordersRecycler.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        getAllOrders();
        getOrderNumbers();

        findViewById(R.id.expensesArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyOrdersActivity.this, "You will be able to view your expenditure history", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.rewardsArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyOrdersActivity.this, "You will view your rewards", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.productsArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyOrdersActivity.this, "You will view products", Toast.LENGTH_SHORT).show();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getOrderNumbers() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Orders orders = ds.getValue(Orders.class);

                    assert orders != null;
                    if (orders.getUserID().equals(firebaseUser.getUid())){
                        totalOrders++;
                        if (orders.getStatus().equals("pending") || orders.getStatus().equals("now preparing"))
                            ordersPending++;
                        else if (orders.getStatus().equals("completed") || orders.getStatus().equals("delivered"))
                            ordersCompleted++;
                    }

                    activeOrders.setText(totalOrders + "");
                    pendingOrders.setText(ordersPending + "");
                    completedOrders.setText(ordersCompleted + "");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Orders myOrders = ds.getValue(Orders.class);

                    assert myOrders != null;
                    if (myOrders.getUserID().equals(firebaseUser.getUid()))
                        orders.add(myOrders);
                }

                ordersAdapter = new OrdersAdapter(orders, MyOrdersActivity.this);
                Collections.reverse(orders);
                ordersRecycler.setAdapter(ordersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}