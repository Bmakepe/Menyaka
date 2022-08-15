package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.R;
import com.example.menyaka.ShopActivity;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.fragments.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.snapshot.Index;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyStoreAdapter extends RecyclerView.Adapter<MyStoreAdapter.ViewHolder>{

    private List<MyStore> storeItems;
    private Context context;

    FirebaseUser firebaseUser;

    public MyStoreAdapter(List<MyStore> MyStore, Context context) {
        this.storeItems = MyStore;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyStoreAdapter.ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        MyStore listItem = storeItems.get(position);
        holder.loader.setVisibility(View.VISIBLE);

        try{
            Picasso.get().load(listItem.getImageUrl()).into(holder.myStoreLogo);
            holder.loader.setVisibility(View.GONE);
        }catch (NullPointerException e){
            Picasso.get().load(R.drawable.menyaka).into(holder.myStoreLogo);
            holder.loader.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", listItem.getRetail_id());
                context.startActivity(shopIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView myStoreLogo;
        ProgressBar loader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myStoreLogo = itemView.findViewById(R.id.myStoreLogo);
            loader = itemView.findViewById(R.id.shopLogoLoader);
        }
    }
}
