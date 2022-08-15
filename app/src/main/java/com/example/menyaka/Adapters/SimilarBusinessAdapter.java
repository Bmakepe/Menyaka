package com.example.menyaka.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.MyStore;

import java.util.ArrayList;

public class SimilarBusinessAdapter extends RecyclerView.Adapter<SimilarBusinessAdapter.ViewHolder> {
    Context context;
    ArrayList<MyStore> myStores;

    public SimilarBusinessAdapter(Context context, ArrayList<MyStore> myStores) {
        this.context = context;
        this.myStores = myStores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
