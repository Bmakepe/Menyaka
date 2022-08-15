package com.example.menyaka.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Sales;
import com.example.menyaka.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PointsDetailAdapter extends RecyclerView.Adapter<PointsDetailAdapter.ViewHolder> {

    private List<Sales> sales;
    private Context context;

    FirebaseUser firebaseUser;

    public PointsDetailAdapter(List<Sales> transactions, Context context){
        this.sales = transactions;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
