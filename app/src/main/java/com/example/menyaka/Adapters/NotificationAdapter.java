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

import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Notification;
import com.example.menyaka.Models.User;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    ArrayList<Notification> notifications;

    GetTimeAgo getTimeAgo;

    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        getTimeAgo = new GetTimeAgo();
        final String nTimeStamp = notifications.get(position).getTimeStamp();

        holder.text.setText(notification.getText());

        getUserinfo(holder.image_profile, holder.username, notification.getUserid());

        String postTime = getTimeAgo.getTimeAgo(Long.parseLong(nTimeStamp), context);
        holder.timeStamp.setText(" - " + postTime);

        if(notification.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostid());
        }else{
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.isIspost()){
                    Intent intent = new Intent(context, MomentActivity.class);
                    intent.putExtra("momentId", notification.getPostid());

                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("userID", notification.getUserid());
                    context.startActivity(intent);
                }
                Toast.makeText(context, "This Notification will redirect correctly ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPostImage(ImageView post_image, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Moments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Moment post = dataSnapshot.getValue(Moment.class);
                try{
                    Picasso.get().load(post.getImageUrl()).into(post_image);
                }catch (NullPointerException ignore){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserinfo(ImageView image_profile, TextView username, String uid) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("id").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);

                        try {
                            Picasso.get().load(user.getImageUrl()).into(image_profile);
                        } catch (NullPointerException ignored) {}

                        username.setText(user.getUsername());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image_profile, post_image;
        public TextView username, text, timeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.notiProIMG);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.notiUsername);
            text = itemView.findViewById(R.id.notiComment);
            timeStamp = itemView.findViewById(R.id.notiTimeStamp);
        }
    }
}
