package com.example.menyaka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.menyaka.Adapters.PointsDetailAdapter;
import com.example.menyaka.Models.Sales;

import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    ImageView close;
    RecyclerView recyclerView;
    PointsDetailAdapter pointsDetailAdapter;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    private List<Sales> transactions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        recyclerView = findViewById(R.id.recyclerView_itemDetail);
        close = findViewById(R.id.backShop);

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setLayoutManager(layoutManager);

        pointsDetailAdapter = new PointsDetailAdapter(transactions,getApplicationContext());
        recyclerView.setAdapter(pointsDetailAdapter);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
