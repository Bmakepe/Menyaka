package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TagStoreAdapter extends RecyclerView.Adapter<TagStoreAdapter.ViewHolder>{

    private Context context;
    private List<Retail> retailList;
    private FirebaseUser firebaseUser;

    public TagStoreAdapter(Context context, List<Retail> retailList) {
        this.context = context;
        this.retailList = retailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Retail retail = retailList.get(position);
        holder.contactName.setText(retail.getRetailName());
        holder.followerId = retail.getRetail_id();

        storeInfo(holder.displayImage, holder.contactName, retail.getRetail_id());
    }

    @Override
    public int getItemCount() {
        return retailList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView contactName;
        String followerId;
        CircleImageView displayImage;
        ImageView close, imageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contact_name);
            displayImage = itemView.findViewById(R.id.contact_img);

            //close = itemView.findViewById(R.id.tag_back);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ImageActivity = new Intent(context, FullScreenImageActivity.class);
                    int position = getAdapterPosition();

                    //ImageActivity.putExtra("contactImage", contactList.get(position).getImageUrl());
                    ImageActivity.putExtra("contactName", retailList.get(position).getRetailName());
                    ImageActivity.putExtra("contactId", retailList.get(position).getRetail_id());

                    context.startActivity(ImageActivity);
                }
            });

        }
    }

    private void storeInfo(final ImageView image_profile, final TextView username, final String retail_id){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Retails").child(retail_id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Retail retail = dataSnapshot.getValue(Retail.class);
                assert retail != null;

                try{
                    if(retail.getImageUrl().equals("")){
                        image_profile.setImageResource(R.drawable.profile_png_1114185);
                    }else {
                        Glide.with(context).load(retail.getImageUrl()).into(image_profile);
                    }
                    username.setText(retail.getRetailName());
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
