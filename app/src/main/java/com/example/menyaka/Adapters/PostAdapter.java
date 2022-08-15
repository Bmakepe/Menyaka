package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menyaka.InteractionsActivity;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.example.menyaka.SharePostActivity;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.UniversalFunctions;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Moment> momentItems;
    private Context context;

    private FirebaseUser firebaseUser;
    DatabaseReference userRef, retailRef, momentRef;
    String userProPic, postTimeStamp, postOwner;

    /*public static final int ITEM_POST = 0;
    public static final int ITEM_SPONSOR = 1;*/

    public static final int TEXT_POST_ITEM = 100;
    public static final int IMAGE_POST_ITEM = 200;
    public static final int SHARED_TEXT_POST_ITEM = 300;

    GetTimeAgo getTimeAgo;

    boolean isFollowing = false;

    String postType;

    int shareCount = 0;

    UniversalFunctions universalFunctions;

    public PostAdapter(List<Moment> momentItems, Context context) {
        this.momentItems = momentItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType){
            case TEXT_POST_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                return new ViewHolder(view);
            case IMAGE_POST_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_post_layout, parent, false);
                return new ViewHolder(view);
            case SHARED_TEXT_POST_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_text_post_item, parent, false);
                return new ViewHolder(view);

            default:
                throw new IllegalStateException("Unexpected value" + viewType);
        }

        /*View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        return new ViewHolder(v);*/

        /*if (viewType == ITEM_POST) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
            return new ViewHolder(v);
        }else if(viewType == ITEM_SPONSOR) {
            View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.promo_item, parent, false);
            return new ViewHolder(v1);
        }else
            return null;*/
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        momentRef = FirebaseDatabase.getInstance().getReference("Moments");
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);

        Moment listItem = momentItems.get(position);

        getPostDetails(listItem, holder);
        publisherInfo(holder.image_profile, holder.username, listItem.getUsername());
        universalFunctions.isLiked(listItem.getMomentId(), holder.like);
        universalFunctions.nrLikes(holder.likes, listItem.getMomentId());
        universalFunctions.getComments(listItem.getMomentId(), holder.comments);
        //seenNumber(listItem.getMomentId());
        universalFunctions.isSaved(listItem.getMomentId(), holder.bookmarkBTN);
        checkFollow(listItem.getUsername());
        getSharesCount(listItem, holder);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                            .child(firebaseUser.getUid()).setValue(true);
                    if(!listItem.getUsername().equals(firebaseUser.getUid()))
                        universalFunctions.addLikesNotification(listItem.getMomentId(), listItem.getUsername());
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                            .child(firebaseUser.getUid()).removeValue();
                    //removeLikeNotification(listItem.getMomentId(), listItem.getUsername());
                }
            }
        });

        holder.itemView.findViewById(R.id.shareArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(context, SharePostActivity.class);
                shareIntent.putExtra("postID", listItem.getMomentId());
                context.startActivity(shareIntent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //addView(listItem.getMomentId());

                universalFunctions.addView(listItem.getMomentId());

                Intent momentActivity = new Intent(context, MomentActivity.class);
                momentActivity.putExtra("momentId", listItem.getMomentId());
                context.startActivity(momentActivity);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                universalFunctions.profileClickOptions(listItem);
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                universalFunctions.profileClickOptions(listItem);
            }
        });

        holder.itemView.findViewById(R.id.likesArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.likes.getText().toString().equals("Like")){
                    Toast.makeText(context, "No Likes", Toast.LENGTH_SHORT).show();
                }else {
                    Intent likesIntent = new Intent(context, InteractionsActivity.class);
                    likesIntent.putExtra("Interaction", "Likes");
                    likesIntent.putExtra("postID", listItem.getMomentId());
                    context.startActivity(likesIntent);
                }

            }
        });

        holder.itemView.findViewById(R.id.postPicArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("pictureID", listItem.getMomentId());
                context.startActivity(intent);
            }
        });

        holder.postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.postMenuBTN, Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");

                if (!listItem.getUsername().equals(firebaseUser.getUid())){
                    //popupMenu.getMenu().add(Menu.NONE,3,0,"Add to favourites");

                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                User user = ds.getValue(User.class);

                                if (user.getId().equals(listItem.getUsername())){
                                    String username = user.getUsername();
                                    if (isFollowing)
                                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Unfollow " + username);
                                    else
                                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Follow " + username);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else{
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Delete Post");
                    popupMenu.getMenu().add(Menu.NONE, 3,0,"Post Activity");
                    popupMenu.getMenu().add(Menu.NONE, 4,0,"Promote Post");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        switch (id){
                            case 0:
                                universalFunctions.profileClickOptions(listItem);
                                break;
                            case 1:
                                if (isFollowing){
                                    //unfollow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(listItem.getUsername()).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                            .child("followers").child(firebaseUser.getUid()).removeValue();
                                }else{
                                    //follow the user
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                            .child("following").child(listItem.getUsername()).setValue(true);
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                                    universalFunctions.addFollowNotifications(listItem.getUsername());

                                }
                                break;
                            case 2:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Are you sure to delete this post")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                universalFunctions.beginDelete(listItem.getMomentId(), listItem.getImageUrl());

                                            }
                                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                                break;
                            case 3:
                                Intent viewsIntent = new Intent(context, InteractionsActivity.class);
                                viewsIntent.putExtra("Interaction", "Views");
                                viewsIntent.putExtra("postID", listItem.getMomentId());
                                context.startActivity(viewsIntent);
                                break;

                            case 4:
                                Toast.makeText(context, "you will promote this post", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value: " + id);
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.bookmarkBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(listItem.getMomentId()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(listItem.getMomentId()).removeValue();
                }
            }
        });

        holder.itemView.findViewById(R.id.postLocationArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + listItem.getLatitude() + "," + listItem.getLongitude();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                context.startActivity(intent);
            }
        });

        try{
            holder.sharedPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    universalFunctions.addView(listItem.getSharedPost());

                    Intent momentActivity = new Intent(context, MomentActivity.class);
                    momentActivity.putExtra("momentId", listItem.getSharedPost());
                    context.startActivity(momentActivity);

                }
            });
        }catch (NullPointerException ignored){}

        /*int viewType = getItemViewType(position);

        switch(viewType){
            case ITEM_POST:
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                getTimeAgo = new GetTimeAgo();

                Moment listItem = momentItems.get(position);

                try {
                    postTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(listItem.getTimestamp()), context);
                    holder.date.setText(postTimeStamp);
                }catch (NumberFormatException ignored){}

                if (listItem.getMoment_desc().equals("")){
                    holder.momentDesc.setVisibility(View.GONE);
                }else {
                    holder.momentDesc.setVisibility(View.VISIBLE);
                    holder.momentDesc.setText(listItem.getMoment_desc());
                }

                if(listItem.getImageUrl().equals("noImage"))
                    holder.postPicArea.setVisibility(View.GONE);
                else {
                    try {
                        Picasso.get().load(listItem.getImageUrl()).into(holder.momentPic);
                    } catch (NullPointerException ignored) {}
                }

                if(!listItem.getUsername().equals(firebaseUser.getUid())){
                    holder.itemView.findViewById(R.id.postViews).setVisibility(View.GONE);
                }

                publisherInfo(holder.image_profile, holder.username, listItem.getUsername());
                checkFollow(holder.followBTN, listItem.getUsername());
                isLiked(listItem.getMomentId(), holder.like);
                nrLikes(holder.likes, listItem.getMomentId());
                getComments(listItem.getMomentId(), holder.comments);
                seenNumber(listItem.getMomentId(), holder.views);
                iniLikesDialog(holder, listItem.getMomentId());
                iniViewersDialog(holder, listItem.getMomentId());

                holder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(holder.like.getTag().equals("like")){
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                                    .child(firebaseUser.getUid()).setValue(true);
                            addNotification(listItem.getMomentId(), listItem.getUsername());
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(listItem.getMomentId())
                                    .child(firebaseUser.getUid()).removeValue();
                        }
                    }
                });

                if(listItem.getUsername().equals(firebaseUser.getUid())){
                    holder.followBTN.setVisibility(View.GONE);
                }else {
                    holder.followBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //try{
                            if(holder.followBTN.getText().toString().equals("Follow")){
                                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                        .child("following").child(listItem.getUsername()).setValue(true);
                                FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                        .child("followers").child(firebaseUser.getUid()).setValue(true);

                            }else{
                                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                        .child("following").child(listItem.getUsername()).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("Follow").child(listItem.getUsername())
                                        .child("followers").child(firebaseUser.getUid()).removeValue();
                            }
                            //}catch (NullPointerException ignored){}
                            Toast.makeText(context, "FollowBTN", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        addView(listItem.getMomentId());

                        Intent momentActivity = new Intent(context, MomentActivity.class);
                        momentActivity.putExtra("momentId", listItem.getMomentId());
                        context.startActivity(momentActivity);
                    }
                });

                holder.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(listItem.getUsername().equals(firebaseUser.getUid())){
                            context.startActivity(new Intent(context, ProfileActivity.class));
                        }else {
                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("userID", listItem.getUsername());
                            context.startActivity(intent);
                        }
                    }
                });

                holder.image_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(listItem.getUsername().equals(firebaseUser.getUid())){
                            context.startActivity(new Intent(context, ProfileActivity.class));
                        }else {
                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("userID", listItem.getUsername());
                            context.startActivity(intent);
                        }
                    }
                });

                holder.likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.likesDialog.show();
                    }
                });

                holder.views.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.viewersDialog.show();
                    }
                });

                holder.momentPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("pictureID", listItem.getMomentId());
                        context.startActivity(intent);
                    }
                });

                break;

            case ITEM_SPONSOR:
/*
                HotDeal deals = hotDeals.get(position);

                holder.productName.setText(deals.getItemName());
                holder.shopName.setText(deals.getStoreName());
                holder.newPrice.setText("M " + deals.getItemPrice());
                holder.expiryDate.setText(deals.getDealEnd());

                try{
                    Picasso.get().load(deals.getItemUrl()).into(holder.productImage);
                }catch (NullPointerException ignored){}

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "You have clicked on this ad", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "This ad will be added to your cart trolley", Toast.LENGTH_SHORT).show();
                    }
                });

        }*/
    }

    private void getSharesCount(Moment listItem, ViewHolder holder) {
        momentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    try {
                        if (moment.getSharedPost().equals(listItem.getMomentId())) {
                            shareCount++;
                        }
                        if (shareCount == 0)
                            holder.shareCounter.setText("Shares");
                        else if (shareCount == 1)
                            holder.shareCounter.setText(shareCount + " Share");
                        else
                            holder.shareCounter.setText(shareCount + " Shares");
                    }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostDetails(Moment listItem, ViewHolder holder) {

        postType = listItem.getPostType();

        switch (listItem.getPostType()) {
            case "textPost":
                //for posts with no video and images
                holder.postPicArea.setVisibility(View.GONE);
                break;

            case "imagePost":
                //for posts with images
                holder.momentPic.setVisibility(View.VISIBLE);
                try {
                    Picasso.get().load(listItem.getImageUrl()).into(holder.momentPic);
                } catch (NullPointerException ignored) {}
                break;

            default:
                Toast.makeText(context, "Unknown Post Type Identified", Toast.LENGTH_SHORT).show();
                break;
        }

        try{
            getSharedPost(holder, listItem);
        }catch (NullPointerException ignored){}

        try {
            postTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(listItem.getTimestamp()), context);
            holder.date.setText(postTimeStamp);
        }catch (NumberFormatException ignored){}

        if (listItem.getMoment_desc().equals("")){
            holder.momentDesc.setVisibility(View.GONE);
        }else {
            holder.momentDesc.setVisibility(View.VISIBLE);
            holder.momentDesc.setText(listItem.getMoment_desc());
        }

        try{

            universalFunctions.findAddress(listItem, holder.postCheckIn, holder.locationArea);

        }catch (Exception ignored){}

        try{
            if (listItem.getPrivacy().equals("Private")){
                holder.itemView.findViewById(R.id.shareArea).setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}

    }

    private void getSharedPost(ViewHolder holder, Moment listItem) {
        momentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    if (moment.getMomentId().equals(listItem.getSharedPost())){
                        holder.sharedPostDesc.setText(moment.getMoment_desc());

                        try{
                            holder.sharedPostImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(moment.getImageUrl()).into(holder.sharedPostImage);
                        }catch (NullPointerException ignored){}

                        try{
                            String shared_timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(moment.getTimestamp()), context);
                            holder.sharedPostDate.setText(shared_timeStamp);
                        }catch (NumberFormatException ignored){}

                        try{
                            universalFunctions.findAddress(moment, holder.sharedPostCheckIn, holder.sharedLocationArea);
                        }catch (NullPointerException ignored){}

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    User user = ds.getValue(User.class);

                                    if (user.getId().equals(moment.getUsername())){
                                        holder.sharedPostUsername.setText(user.getUsername());

                                        try{
                                            Picasso.get().load(user.getImageUrl()).into(holder.sharedPostProPic);
                                        }catch (NullPointerException ignored){}
                                    }else{
                                        retailRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Retail retail = ds.getValue(Retail.class);

                                                    if (retail.getRetail_id().equals(moment.getUsername())){
                                                        holder.sharedPostUsername.setText(retail.getRetailName());

                                                        try{
                                                            Picasso.get().load(retail.getImageUrl()).into(holder.sharedPostProPic);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow(String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
            .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                isFollowing = dataSnapshot.child(username).exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final String id){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;

                    if (user.getId().equals(id)){

                        userProPic = user.getImageUrl();

                        if(user.getImageUrl().equals("")){
                            image_profile.setImageResource(R.drawable.profile_png_1114185);
                        }else {
                            Picasso.get().load(user.getImageUrl()).into(image_profile);
                            //Glide.with(context).load(user.getImageUrl()).into(image_profile);
                        }
                        postOwner = user.getUsername();
                        username.setText(user.getUsername());
                    }else{
                        retailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Retail retail = ds.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(id)){

                                        userProPic = retail.getImageUrl();

                                        if(retail.getImageUrl().equals("")){
                                            image_profile.setImageResource(R.drawable.profile_png_1114185);
                                        }else {
                                            Picasso.get().load(userProPic).into(image_profile);
                                            //Glide.with(context).load(userProPic).into(image_profile);
                                        }
                                        postOwner = retail.getRetailName();
                                        username.setText(postOwner);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount(){
        try{
            return momentItems.size();
        }catch (NullPointerException e){
            e.printStackTrace();
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //for regular posts
        public ImageView momentPic, like, image_profile, comment, postMenuBTN, bookmarkBTN;
        public TextView comments, date, username, momentDesc, likes, postCheckIn, shareCounter;
        ProgressBar progressBar;
        CardView postPicArea;
        LinearLayout locationArea;

        //for sponsored ads
        ImageView productImage, addToCart;
        TextView shopName, productName, oldPrice, newPrice, expiryDate;

        //for shared posts
        public CircleImageView sharedPostProPic;
        public TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc;
        public ProgressBar sharedProgressBar;
        public LinearLayout sharedLocationArea;
        public ImageView sharedPostImage;
        public CardView sharedPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //for regular posts
            momentPic = itemView.findViewById(R.id.postImage);
            progressBar = itemView.findViewById(R.id.progress_load_media);
            momentDesc = itemView.findViewById(R.id.desc);
            like = itemView.findViewById(R.id.post_likes);
            image_profile = itemView.findViewById(R.id.moment_image_user);
            username = itemView.findViewById(R.id.moment_username);
            date = itemView.findViewById(R.id.postDate);
            likes =itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.post_comments);
            comment = itemView.findViewById(R.id.comment);
            postPicArea = itemView.findViewById(R.id.postPicArea);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            bookmarkBTN = itemView.findViewById(R.id.bookmarkBTN);
            postCheckIn = itemView.findViewById(R.id.postCheckIn);
            locationArea = itemView.findViewById(R.id.postLocationArea);
            shareCounter = itemView.findViewById(R.id.shareCounter);

            //for sponsored ads
            productImage = itemView.findViewById(R.id.item_img);
            addToCart = itemView.findViewById(R.id.postAddToCard);
            shopName = itemView.findViewById(R.id.sponsorShop);
            productName = itemView.findViewById(R.id.item_name);
            oldPrice = itemView.findViewById(R.id.original_price);
            newPrice = itemView.findViewById(R.id.discounted_price);
            expiryDate = itemView.findViewById(R.id.expiryDate);

            //for shared Posts
            sharedPostProPic = itemView.findViewById(R.id.shared_image_user);
            sharedPostUsername = itemView.findViewById(R.id.shared_username);
            sharedPostDate = itemView.findViewById(R.id.shared_postDate);
            sharedPostCheckIn = itemView.findViewById(R.id.shared_postCheckIn);
            sharedPostDesc = itemView.findViewById(R.id.shared_post_desc);
            sharedProgressBar = itemView.findViewById(R.id.shared_progress_load_media);
            sharedPostImage = itemView.findViewById(R.id.shared_postImage);
            sharedLocationArea = itemView.findViewById(R.id.sharedLocationArea);
            sharedPost = itemView.findViewById(R.id.shared_post_item);

        }

    }

    /*@Override
    public int getItemViewType(int position) {
        if(position% HomeFragment.ITEM_BEFORE_SPONSOR == 0)
            return ITEM_SPONSOR;
        else
            return ITEM_POST;

    }*/

    @Override
    public int getItemViewType(int position) {

        switch (momentItems.get(position).getPostType()){

            case "textPost":
                return TEXT_POST_ITEM;

            case "imagePost":
                return IMAGE_POST_ITEM;

            case "sharedTextPost":
                return SHARED_TEXT_POST_ITEM;

            default:
                throw new IllegalStateException("Unexpected value" + momentItems.get(position).getPostType());

        }
    }
}
