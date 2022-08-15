package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Utils.RatingsCalculator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AgentProfileActivity extends AppCompatActivity {



    CircleImageView shipperLogo;
    ImageView backBTN, followIconBTN;
    TextView shipperLocation, shipperName, followersCounter, sendMessageBTN, followAgentBTN;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    String companyID;

    //for displaying reviews
    private RateReviewAdapter ratingsAdapter;
    private List<RatingAndReview> ratingsList;
    ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5;
    RatingBar ratingSection, agentRating;
    TextView ratingNumber, totalRatings;

    RecyclerView agentReviewRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        shipperLogo = findViewById(R.id.shipperLogo);
        backBTN = findViewById(R.id.shipperBackBTN);
        shipperName = findViewById(R.id.shipperName);
        shipperLocation = findViewById(R.id.shipperLocation);
        sendMessageBTN = findViewById(R.id.agentSendMessageBTN);
        agentReviewRecycler = findViewById(R.id.agent_comments_recycler);
        agentRating = findViewById(R.id.agent_Rating);
        followAgentBTN = findViewById(R.id.followAgentBTN);
        followersCounter = findViewById(R.id.agentFollowers);
        followIconBTN = findViewById(R.id.followAgentIcon);

        ratingSection = findViewById(R.id.agent_rating_section);
        ratingNumber = findViewById(R.id.agent_average_rating);
        totalRatings = findViewById(R.id.agent_number_of_ratings);

        progressBar1 = findViewById(R.id.agent_progress_1);
        progressBar2 = findViewById(R.id.agent_progress_2);
        progressBar3 = findViewById(R.id.agent_progress_3);
        progressBar4 = findViewById(R.id.agent_progress_4);
        progressBar5 = findViewById(R.id.agent_progress_5);

        Intent intent = getIntent();
        companyID = intent.getStringExtra("agentID");

        checkFollow(followIconBTN);
        getFollowData();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ShippingAgents");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ShippingAgents agents = ds.getValue(ShippingAgents.class);
                    if(agents.getCompanyID().equals(companyID)){
                        shipperName.setText(agents.getShippingName());
                        shipperLocation.setText(agents.getShipperLocation());
                        if (agents.getVerified()){
                            findViewById(R.id.shipperVerified).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(R.id.shipperVerified).setVisibility(View.GONE);
                        }
                        try{
                            Picasso.get().load(agents.getCompanyLogo()).into(shipperLogo);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        agentReviewRecycler.setHasFixedSize(true);
        agentReviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingsList = new ArrayList<>();
        ratingsAdapter = new RateReviewAdapter(this, ratingsList);
        agentReviewRecycler.setAdapter(ratingsAdapter);

        readRatingAndReviews();
        RatingsCalculator.ratingHeader(companyID, agentRating, totalRatings);
        RatingsCalculator.totalRating(companyID, ratingSection);
        RatingsCalculator.setRating(companyID, ratingNumber);

        RatingsCalculator.ratingBars(companyID,5, progressBar5);
        RatingsCalculator.ratingBars(companyID,4, progressBar4);
        RatingsCalculator.ratingBars(companyID,3, progressBar3);
        RatingsCalculator.ratingBars(companyID,2, progressBar2);
        RatingsCalculator.ratingBars(companyID,1, progressBar1);

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        followAgentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(followIconBTN.getTag().equals("Follow")){

                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(companyID).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(companyID)
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotifications();
                }else{
                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(companyID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(companyID)
                            .child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Notifications").child(companyID)
                            .child(firebaseUser.getUid()).removeValue();

                }
            }
        });

        sendMessageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent = new Intent(AgentProfileActivity.this, MessageActivity.class);
                messageIntent.putExtra("receiverID", companyID);
                startActivity(messageIntent);
            }
        });

        findViewById(R.id.newAgentReview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AgentProfileActivity.this, ReviewActivity.class);
                intent.putExtra("storeID", companyID);
                intent.putExtra("exit", "finish");
                startActivity(intent);

            }
        });

        findViewById(R.id.moreAgentReviews).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AgentProfileActivity.this, "You will be able to load more reviews", Toast.LENGTH_SHORT).show();
            }
        });

        /*findViewById(R.id.agentFollowersList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AgentProfileActivity.this, InteractionsActivity.class);
                intent.putExtra("Interaction", "Followers");
                intent.putExtra("postID", companyID);
                startActivity(intent);

            }
        });*/
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(companyID);
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
                Toast.makeText(AgentProfileActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFollowData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(companyID).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersCounter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers
    }

    private void checkFollow(ImageView followBTN) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("MyStores").child(firebaseUser.getUid());
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(companyID).exists()){
                    //executes if following agent
                    followAgentBTN.setText("Following");
                    followBTN.setImageResource(R.drawable.ic_person_black_24dp);
                    followBTN.setTag("Following");
                }else{
                    followAgentBTN.setText("Follow");
                    followBTN.setImageResource(R.drawable.ic_baseline_person_add_alt_1_24);
                    followBTN.setTag("Follow");
                    //executes if you are not following the agent
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readRatingAndReviews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ShopRatings").child(companyID);

        reference.limitToLast(3).addValueEventListener(new ValueEventListener() {
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