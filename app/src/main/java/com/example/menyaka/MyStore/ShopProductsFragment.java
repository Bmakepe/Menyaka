package com.example.menyaka.MyStore;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.ProductAdapter;
import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Models.Product;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopProductsFragment extends Fragment {

    RecyclerView productRecycler;
    ArrayList<Product> productList;
    ProductItemAdapter productAdapter;

    DatabaseReference reference;

    String shopId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_products, container, false);

        shopId = getActivity().getIntent().getExtras().getString("storeId");
        Toast.makeText(getActivity(), "The store opened is " + shopId + " successfully", Toast.LENGTH_SHORT).show();

        productRecycler = view.findViewById(R.id.product_recycler);
        productRecycler.setHasFixedSize(true);
        productRecycler.hasFixedSize();
        productRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Products");
        //Query query = reference.child("storeId").equalTo(shopId);
        Query query = reference.orderByChild("storeId").equalTo(shopId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    productList.add(product);
                }
                Toast.makeText(getActivity(), "this shop has " + productList.size() + " products", Toast.LENGTH_SHORT).show();
                productAdapter = new ProductItemAdapter(getActivity(), productList);
                productRecycler.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}