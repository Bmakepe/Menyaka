package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menyaka.CartActivity;
import com.example.menyaka.Common.Comment;
import com.example.menyaka.DeliveryOptionsActivity;
import com.example.menyaka.MainActivity;
import com.example.menyaka.Models.Notification;
import com.example.menyaka.Models.User;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private Context context;
    private List<Comment> comments;

    private FirebaseUser firebaseUser;
    String postID;

    GetTimeAgo timeAgo;

    public CommentAdapter(Context context, List<Comment> comments, String postID) {
        this.context = context;
        this.comments = comments;
        this.postID = postID;
    }

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);

        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        timeAgo = new GetTimeAgo();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = comments.get(position);

        holder.comment.setText(comment.getComment());
        try{
            String timeStamp = timeAgo.getTimeAgo(Long.parseLong(comment.getTimeStamp()), context);
            holder.commentTimeStamp.setText(timeStamp);
        }catch (NumberFormatException ignored){}

        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comment.getPublisher().equals(firebaseUser.getUid())){

                    PopupMenu popupMenu = new PopupMenu(context, holder.itemView, Gravity.END);

                    popupMenu.getMenu().add(Menu.NONE, 0,0,"View My Profile");
                    popupMenu.getMenu().add(Menu.NONE, 1,0,"Delete Comment");

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();

                            switch (id){
                                case 0:
                                    context.startActivity(new Intent(context, ProfileActivity.class));
                                    break;
                                case 1:

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Delete")
                                            .setMessage("Are you sure to delete this comment")
                                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments");
                                                    ref.child(postID).child(comment.getCommentID()).removeValue();
                                                    //removeCommentNotification(comment.getCommentID(), comment.getPublisher());

                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                                    break;

                                default:
                                    throw new IllegalStateException("Unexpected value: " + id);
                            }

                            return false;
                        }
                    });
                    popupMenu.show();
                }else {

                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("userID", comment.getPublisher());
                    context.startActivity(intent);
                }
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "You will be able to view comment owner profile picture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCommentNotification(String postId, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Notification notification = ds.getValue(Notification.class);

                    if (notification.getPostid().equals(postId)){
                        String key = notification.getNotificationID();
                        reference.child(key).removeValue();
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
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile;
        public TextView username, comment, commentTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            commentTimeStamp = itemView.findViewById(R.id.commentTimeStamp);

        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                Glide.with(context).load(user.getImageUrl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
