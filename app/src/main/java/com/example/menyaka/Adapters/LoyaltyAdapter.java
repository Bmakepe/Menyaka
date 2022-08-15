package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.MessageActivity;
import com.example.menyaka.Models.Points;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.RedeemPointsActivity;
import com.example.menyaka.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoyaltyAdapter extends RecyclerView.Adapter<LoyaltyAdapter.ViewHolder> {

    Context context;
    ArrayList<Points> loyaltyStores;
    DatabaseReference loyaltyRef, storesRef;

    public LoyaltyAdapter(Context context, ArrayList<Points> loyaltyStores) {
        this.context = context;
        this.loyaltyStores = loyaltyStores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.points_rating, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Points myPoints = loyaltyStores.get(position);

        holder.pointsNo.setText(myPoints.getPointsNo() + " Points Accumulated");

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(Long.parseLong(myPoints.getExpiryDate()));
            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
            holder.timeStamp.setText("Expiry Date: " + pTime);
        }catch (NumberFormatException ignored){}//for converting timestamp

        getInfo(holder, myPoints.getShopID());

        holder.messageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("receiverID", myPoints.getShopID());
                context.startActivity(intent);
            }
        });

        holder.shopLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", myPoints.getShopID());
                context.startActivity(shopIntent);
            }
        });

        holder.viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, RedeemPointsActivity.class);
                intent.putExtra("pointsID", myPoints.getPointsID());
                context.startActivity(intent);
            }
        });
    }

    private void getInfo(ViewHolder holder, String shopID) {

        storesRef = FirebaseDatabase.getInstance().getReference("Retails");
        storesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()){
                    Retail retail = data.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(shopID)) {
                        holder.shopName.setText(retail.getRetailName());
                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.shopLogo);
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
        return loyaltyStores.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView  messageBTN;
        CircleImageView shopLogo;
        TextView shopName, pointsNo, timeStamp, viewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shopLogo = itemView.findViewById(R.id.points_storeImage);
            messageBTN = itemView.findViewById(R.id.messageBox);
            shopName = itemView.findViewById(R.id.pointHeader);
            pointsNo = itemView.findViewById(R.id.pointsNO);
            timeStamp = itemView.findViewById(R.id.points_date);
            viewDetails = itemView.findViewById(R.id.viewPointDetails);
        }
    }
}
