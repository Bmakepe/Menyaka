package com.example.menyaka.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Adapters.VideoAdapter;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Notification;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.ViewProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UniversalFunctions {

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference retailRef = FirebaseDatabase.getInstance().getReference("Retails");

    Context context;

    public UniversalFunctions(Context context) {
        this.context = context;
    }

    public UniversalFunctions() {
    }

    public void addView(String postID){
        FirebaseDatabase.getInstance().getReference("MenyakaViews")
                .child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    public void addFollowNotifications(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Following", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addLikesNotification(String momentId, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", momentId);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.child(key).setValue(hashMap);
    }

    public void addVideoNotification(String momentId, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your video");
        hashMap.put("postid", momentId);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.child(key).setValue(hashMap);
    }

    public void isSaved(String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_24);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeLikeNotification(String momentId, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Notification notification = ds.getValue(Notification.class);

                    if (notification.getPostid().equals(momentId)){
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

    public void beginDelete(String momentId, String imageUrl) {
        if(imageUrl.equals("noImage")){
            deleteWithoutImage(momentId);
        }else{
            deleteWithImage(momentId, imageUrl);
        }
    }

    public void deleteWithImage(String momentId, String imageUrl) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query fquery = FirebaseDatabase.getInstance().getReference("Moments").orderByChild("momentId").equalTo(momentId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    deletePostInteraction(momentId);
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteWithoutImage(String momentId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Moments").orderByChild("momentId").equalTo(momentId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    deletePostInteraction(momentId);
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deletePostInteraction(String momentId) {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(momentId);
        likesRef.removeValue();//remove likes interaction

        final DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(momentId);
        commentsRef.removeValue();//remove all comments

        final DatabaseReference viewsRef = FirebaseDatabase.getInstance().getReference("MenyakaViews").child(momentId);
        viewsRef.removeValue();//removes all views
    }

    public void isLiked(String momentId, final ImageView like){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(momentId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    like.setImageResource(R.drawable.ic_baseline_favorite_24);
                    like.setTag("liked");
                }else {
                    like.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    like.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void nrLikes(final TextView likes, String momentId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(momentId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    likes.setText("Like");
                }else if (dataSnapshot.getChildrenCount() == 1) {
                    likes.setText(dataSnapshot.getChildrenCount() + " Like");
                }else {
                    likes.setText(dataSnapshot.getChildrenCount() + " Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getComments(String momentId, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(momentId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0)
                    comments.setText("Comment");
                else if (dataSnapshot.getChildrenCount() == 1)
                    comments.setText(dataSnapshot.getChildrenCount() + " Comment");
                else
                    comments.setText(dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkVideoViewCount(TextView videoViews, Moment listItem) {
        DatabaseReference viewsReference = FirebaseDatabase.getInstance().getReference("MenyakaViews")
                .child(listItem.getMomentId());
        viewsReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() == 0) {
                            videoViews.setText("Views");
                        }else if (snapshot.getChildrenCount() == 1) {
                            videoViews.setText(snapshot.getChildrenCount() + " View");
                        }else {
                            videoViews.setText(snapshot.getChildrenCount() + " Views");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void findAddress(Moment listItem, TextView postCheckIn, LinearLayout locationArea) {
        //find address, country, state, city
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(listItem.getLatitude(), listItem.getLongitude(), 1);

            String address = addresses.get(0).getAddressLine(0);//complete address

            postCheckIn.setText(address);
            locationArea.setVisibility(View.VISIBLE);
        }catch (Exception ignored){}
    }

    public void profileClickOptions(Moment listItem) {

        if(listItem.getUsername().equals(firebaseUser.getUid())){
            context.startActivity(new Intent(context, ProfileActivity.class));
        }else{
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        assert user != null;
                        if (user.getId().equals(listItem.getUsername())){

                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("userID", listItem.getUsername());
                            context.startActivity(intent);

                        }else{
                            retailRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        Retail retail =  ds.getValue(Retail.class);

                                        assert retail != null;
                                        if (retail.getRetail_id().equals(listItem.getUsername())){

                                            Intent storeIntent = new Intent(context, ShopDetailsActivity.class);
                                            storeIntent.putExtra("storeId", listItem.getUsername());
                                            context.startActivity(storeIntent);

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

    public void readRatingAndReviews(String productID, List<RatingAndReview> ratingsList, RateReviewAdapter ratingsAdapter) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(productID);

        reference.limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingsList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    RatingAndReview reviews = dataSnapshot.getValue(RatingAndReview.class);
                    ratingsList.add(reviews);
                }

                Collections.reverse(ratingsList);
                ratingsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
