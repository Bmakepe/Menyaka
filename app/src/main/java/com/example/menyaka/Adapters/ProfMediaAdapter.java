package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Moment;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfMediaAdapter extends RecyclerView.Adapter<ProfMediaAdapter.ViewHolder> {
    Context context;
    ArrayList<Moment> images;

    public ProfMediaAdapter(Context context, ArrayList<Moment> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Moment moment = images.get(position);

        try{
            Picasso.get().load(moment.getImageUrl()).into(holder.post_image);
            holder.image_loader.setVisibility(View.GONE);
        }catch (NullPointerException ignored){}

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MomentActivity.class);
                intent.putExtra("momentId", moment.getMomentId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView post_image;
        ProgressBar image_loader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            image_loader = itemView.findViewById(R.id.image_loader);

        }
    }
}
