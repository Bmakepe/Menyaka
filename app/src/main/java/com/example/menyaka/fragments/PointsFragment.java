package com.example.menyaka.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.PointsAdapter;
import com.example.menyaka.Models.Sales;
import com.example.menyaka.R;
import com.example.menyaka.ShopActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PointsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private PointsAdapter pointsAdapter;
    private DatabaseReference reference;

    private List<Sales> mySales;

    public PointsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_points, container,false);

        recyclerView = view.findViewById(R.id.recyclerView_points);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "The story has appeared", Toast.LENGTH_LONG).show();
            }
        });
        //String id = storeAdapter.getItemId();

        reference = FirebaseDatabase.getInstance().getReference("transactions");
        return view;
    }

    public void onStart(){
        super.onStart();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mySales = new ArrayList<>();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Sales sales = snapshot.getValue(Sales.class);
                    mySales.add(sales);
                }

                pointsAdapter = new PointsAdapter(mySales,getActivity());
                recyclerView.setAdapter(pointsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        //pointsAdapter = new PointsAdapter(transactions, getActivity());
        //recyclerView.setAdapter(pointsAdapter);
    }
}
