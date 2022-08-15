package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaggedItemAdapter extends RecyclerView.Adapter<TaggedItemAdapter.ViewHolder> {

    Context context;
    List<String> taggedItems;

    DatabaseReference userRef, retailRef;
    FirebaseUser firebaseUser;

    public TaggedItemAdapter(Context context, List<String> taggedItems) {
        this.context = context;
        this.taggedItems = taggedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagged_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String identity = taggedItems.get(position);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getItemDetails(holder, identity);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            if (user.getId().equals(identity)){

                                Toast.makeText(context, user.getUsername() + " will be tagged in this post.", Toast.LENGTH_SHORT).show();

                            }else{
                                retailRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot data : snapshot.getChildren()){
                                            Retail retail = data.getValue(Retail.class);

                                            if (retail.getRetail_id().equals(identity)){

                                                Toast.makeText(context, retail.getRetailName()  + " will be tagged in this post", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void getItemDetails(ViewHolder holder, String identity) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getId().equals(identity)){
                        holder.taggedName.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageUrl()).into(holder.taggedIcon);
                        }catch (NullPointerException ignored){}
                    }else{
                        retailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()){
                                    Retail retail = data.getValue(Retail.class);

                                    if (retail.getRetail_id().equals(identity)){
                                        holder.taggedName.setText(retail.getRetailName());

                                        try{
                                            Picasso.get().load(retail.getImageUrl()).into(holder.taggedIcon);
                                        }catch (NullPointerException ignored){}
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return taggedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView taggedIcon;
        TextView taggedName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taggedIcon = itemView.findViewById(R.id.tagIcon);
            taggedName = itemView.findViewById(R.id.tagged_name);
        }
    }
}
