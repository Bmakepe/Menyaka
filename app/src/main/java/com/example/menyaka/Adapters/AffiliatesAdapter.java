package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.MessageActivity;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
import com.example.menyaka.ProfileActivity;
import com.example.menyaka.R;
import com.example.menyaka.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AffiliatesAdapter extends RecyclerView.Adapter<AffiliatesAdapter.ViewHolder> {
    Context context;
    ArrayList<String> affiliates;

    DatabaseReference agentsRef, userRef, retailRef;

    public AffiliatesAdapter(Context context, ArrayList<String> affiliates) {
        this.context = context;
        this.affiliates = affiliates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String userid = affiliates.get(position);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        agentsRef = FirebaseDatabase.getInstance().getReference("ShippingAgents");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getId().equals(affiliates.get(position))){
                        holder.userName.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageUrl()).into(holder.userProPic);
                        }catch (NullPointerException ignored){}
                    }else{
                        retailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()){
                                    Retail retail = data.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(affiliates.get(position))){
                                        holder.userName.setText(retail.getRetailName());

                                        try{
                                            Picasso.get().load(retail.getImageUrl()).into(holder.userProPic);
                                        }catch (NullPointerException ignored){}
                                    }else{
                                        agentsRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    ShippingAgents agents = ds.getValue(ShippingAgents.class);

                                                    assert agents != null;
                                                    if (agents.getCompanyID().equals(affiliates.get(position))){
                                                        holder.userName.setText(agents.getShippingName());

                                                        try{
                                                            Picasso.get().load(agents.getCompanyLogo()).into(holder.userProPic);
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

        holder.followBTN.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    Intent intent1 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent1);
                }else{
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra("receiverID", userid);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return affiliates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userProPic;
        TextView userName, followBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProPic = itemView.findViewById(R.id.contactProPic);
            userName = itemView.findViewById(R.id.contactName);
            followBTN = itemView.findViewById(R.id.followBTN);

        }
    }
}
