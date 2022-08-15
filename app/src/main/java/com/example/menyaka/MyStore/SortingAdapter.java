package com.example.menyaka.MyStore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Adapters.ProductAdapter;
import com.example.menyaka.Models.Product;
import com.example.menyaka.R;

import java.util.List;

public class SortingAdapter extends RecyclerView.Adapter<SortingAdapter.ViewHolder>{

    private Context context;
    private List<Product> productList;

    public SortingAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sort_tab, parent, false);

        return new SortingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_product;
        public TextView productCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productCategory = itemView.findViewById(R.id.product_name);

        }
    }
}
