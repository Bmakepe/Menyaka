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

import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StoreInteractionAdapter extends RecyclerView.Adapter<StoreInteractionAdapter.ViewHolder> {
    ArrayList<Retail> retailList;
    Context context;

    DatabaseReference retailRef;
    FirebaseUser firebaseUser;

    UniversalFunctions universalFunctions;

    public StoreInteractionAdapter(ArrayList<Retail> retailList, Context context) {
        this.retailList = retailList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_interaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Retail retail = retailList.get(position);
        retailRef = FirebaseDatabase.getInstance().getReference("Retails");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        universalFunctions = new UniversalFunctions(context);

        getStoreDetails(holder, retail);
        isStoreFavourite(retail.getRetail_id(), holder);

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.followBTN.getText().toString().equals("Add Store")){

                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(retail.getRetail_id()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(retail.getRetail_id())
                            .child(firebaseUser.getUid()).setValue(true);
                    universalFunctions.addFollowNotifications(retail.getRetail_id());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("MyStores").child(firebaseUser.getUid())
                            .child(retail.getRetail_id()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("MyCustomers").child(retail.getRetail_id())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", retail.getRetail_id());
                context.startActivity(shopIntent);
            }
        });
    }

    private void isStoreFavourite(String retail_id, ViewHolder holder) {
        DatabaseReference myStoreRef = FirebaseDatabase.getInstance().getReference()
                .child("MyStores").child(firebaseUser.getUid());

        myStoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(retail_id).exists()){
                    holder.followBTN.setText("Remove");
                }else{
                    holder.followBTN.setText("Add Store");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoreDetails(ViewHolder holder, Retail retail) {
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail1 = ds.getValue(Retail.class);

                    if (retail1.getRetail_id().equals(retail.getRetail_id())){
                        holder.userName.setText(retail.getRetailName());

                        try{
                            Picasso.get().load(retail1.getImageUrl()).into(holder.userProPic);
                        }catch (NullPointerException ignored){}
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
        return retailList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, followBTN;
        ImageView userProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.interactionUserUsername);
            followBTN = itemView.findViewById(R.id.interactionFollowBTN);
            userProPic = itemView.findViewById(R.id.interactionUserImage);
        }
    }
}
