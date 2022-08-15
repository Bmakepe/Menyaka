package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.CategoryStockActivity;
import com.example.menyaka.Models.Product;
import com.example.menyaka.R;
import com.example.menyaka.SubCategoriesActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class ShopCategoryAdapter extends RecyclerView.Adapter<ShopCategoryAdapter.ViewHolder> {

    Context context;
    ArrayList<String> storeID, category, productImages;
    DatabaseReference productRef;

    public ShopCategoryAdapter(Context context, ArrayList<String> storeID, ArrayList<String> category) {
        this.context = context;
        this.storeID = storeID;
        this.category = category;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_category_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        productRef = FirebaseDatabase.getInstance().getReference("Products");
        productImages = new ArrayList<>();

        holder.categoryName.setText(category.get(position));

        getItemBackgroundImage(holder, category.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, SubCategoriesActivity.class);
                intent.putExtra("storeID", storeID.get(position));
                intent.putExtra("storeCategory", category.get(position));
                context.startActivity(intent);
            }
        });
    }

    private void getItemBackgroundImage(ViewHolder holder, String categoryName) {

        Random r = new Random();

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productImages.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);

                    //for (String ID : category){
                        if (product.getProductCategory().equals(categoryName)){
                            productImages.add(product.getProductImg());
                        }
                   // }
                }

                if (!productImages.isEmpty()){
                    try{
                        Picasso.get().load(productImages.get(r.nextInt(Math.abs(productImages.size())))).into(holder.background_pic);
                    }catch (NullPointerException ignored){}
                }else{
                    Toast.makeText(context, "There are no product images loaded", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return category.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        ImageView background_pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.categoryName);
            background_pic = itemView.findViewById(R.id.background_pic);

        }
    }
}
