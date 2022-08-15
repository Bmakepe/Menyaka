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
import com.example.menyaka.Models.Contact;
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

public class TaggingAdapter extends RecyclerView.Adapter<TaggingAdapter.ViewHolder>{

    private List<Contact> contactList;
    private Context context;
    private FirebaseUser firebaseUser;

    public TaggingAdapter(List<Contact> contactList, Context context) {
        this.contactList = contactList;
        this.context = context;
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

        final Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getUsername());
        holder.followerId = contact.getId();

        publisherInfo(holder.displayImage, holder.contactName, contact.getId());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ImageActivity = new Intent(context, FullScreenImageActivity.class);
                    int position = getAdapterPosition();

                    //ImageActivity.putExtra("contactImage", contactList.get(position).getImageUrl());
                    ImageActivity.putExtra("contactName", contactList.get(position).getUsername());
                    ImageActivity.putExtra("contactId", contactList.get(position).getId());

                    context.startActivity(ImageActivity);
                }
            });

        }
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final String id){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                assert contact != null;

                if(contact.getImageUrl().equals("")){
                    image_profile.setImageResource(R.drawable.profile_png_1114185);
                }else {
                    Glide.with(context).load(contact.getImageUrl()).into(image_profile);
                }
                username.setText(contact.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
