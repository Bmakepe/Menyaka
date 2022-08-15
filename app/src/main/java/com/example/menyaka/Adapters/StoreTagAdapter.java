package com.example.menyaka.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoreTagAdapter extends RecyclerView.Adapter<StoreTagAdapter.ViewHolder> {

    Context context;
    List<MyStore> tagStoreList;
    List<MyStore> taggedStores;

    public StoreTagAdapter(Context context, List<MyStore> tagStoreList) {
        this.context = context;
        this.tagStoreList = tagStoreList;
    }

    public StoreTagAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagging_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyStore retail = tagStoreList.get(position);
        taggedStores = new ArrayList<>();

        holder.taggedName.setText(retail.getRetailName());

        holder.selection.setTag("Untagged");

        try{
            Picasso.get().load(retail.getImageUrl()).into(holder.taggedProPic);
        }catch (NullPointerException ignored){}

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!holder.selection.getTag().equals("Untagged")){
                    holder.selection.setTag("Untagged");
                    holder.selection.setVisibility(View.GONE);
                    taggedStores.remove(retail);
                }else{
                    holder.selection.setTag("Tagged");
                    holder.selection.setColorFilter(R.color.colorPrimaryDark);
                    holder.selection.setVisibility(View.VISIBLE);
                    taggedStores.add(retail);
                }

                Toast.makeText(context, "You will be able to tag " + retail.getRetailName() + " and the array list contains " + taggedStores.size() + " stores", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return tagStoreList.size();
    }

    public void getTags(List<MyStore> taggedItems) {
        taggedItems = taggedStores;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView taggedProPic;
        TextView taggedName;
        ImageView selection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taggedProPic = itemView.findViewById(R.id.taggedProPic);
            taggedName = itemView.findViewById(R.id.taggedName);
            selection = itemView.findViewById(R.id.taggingCondition);
        }
    }
}
