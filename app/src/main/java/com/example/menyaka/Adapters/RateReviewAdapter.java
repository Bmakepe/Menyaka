package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
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
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.User;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.GetTimeAgo;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RateReviewAdapter extends RecyclerView.Adapter<RateReviewAdapter.ViewHolder> {

    private Context context;
    private List<RatingAndReview> reviewList;
    private int recyclerViewItems;

    String shopID;

    GetTimeAgo timeAgo;

    FirebaseUser firebaseUser;
    DatabaseReference userRef;

    public RateReviewAdapter(Context context, List<RatingAndReview> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    public RateReviewAdapter(Context context, List<RatingAndReview> reviewList, String shopID) {
        this.context = context;
        this.reviewList = reviewList;
        this.shopID = shopID;
    }

    /*public RateReviewAdapter(Context context, List<RatingAndReview> reviewList, int recyclerViewItems) {
        this.context = context;
        this.reviewList = reviewList;
        this.recyclerViewItems = recyclerViewItems;
    }

    public RateReviewAdapter(Context context, List<RatingAndReview> reviewList, String shopID, int recyclerViewItems) {
        this.context = context;
        this.reviewList = reviewList;
        this.shopID = shopID;
        this.recyclerViewItems = recyclerViewItems;
    }*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rate_and_review, parent, false);

        return new RateReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateReviewAdapter.ViewHolder holder, int position) {

        RatingAndReview review = reviewList.get(position);
        timeAgo = new GetTimeAgo();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        holder.userReview.setText(review.getComment());
        holder.userRating.setRating(review.getRating());
        //RatingsCalculator.totalRating(storeId, holder.userRating);

        try {
            getUserInfo(holder.userImg, holder.ratingUsername, review.getPublisherId());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        try{
            String timeStamp = timeAgo.getTimeAgo(Long.parseLong(review.getTimestamp()), context);
            holder.reviewTimestamp.setText(timeStamp);
        }catch (NumberFormatException ignored){}

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(review.getPublisherId().equals(firebaseUser.getUid())){

                    PopupMenu popupMenu = new PopupMenu(context, holder.itemView, Gravity.END);

                    popupMenu.getMenu().add(Menu.NONE, 0,0,"View My Profile");
                    popupMenu.getMenu().add(Menu.NONE, 1,0,"Delete Review");

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
                                            .setMessage("Are you sure to delete this review")
                                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ShopRatings");
                                                    ref.child(shopID).child(review.getReviewID()).removeValue();

                                                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();

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
                    intent.putExtra("userID", review.getPublisherId());
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        /*if(recyclerViewItems != 0){
            return recyclerViewItems;
        }else{
            return reviewList.size();
        }*/
        return reviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userImg;
        public TextView ratingUsername, userReview, reviewTimestamp;
        public RatingBar userRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImg = itemView.findViewById(R.id.rating_userImg);
            ratingUsername = itemView.findViewById(R.id.rating_username);
            userReview = itemView.findViewById(R.id.user_review);
            userRating = itemView.findViewById(R.id.user_rating);
            reviewTimestamp = itemView.findViewById(R.id.reviewTimeStamp);
        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherId){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if (user.getId().equals(publisherId)) {
                        Glide.with(context).load(user.getImageUrl()).into(imageView);
                        username.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
