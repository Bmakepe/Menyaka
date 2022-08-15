package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Orders;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.OrderDetailsActivity;
import com.example.menyaka.R;
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

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    ArrayList<Orders> orders;
    Context context;

    public OrdersAdapter(ArrayList<Orders> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_status_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders myOrder = orders.get(position);

        holder.orderNumber.setText(myOrder.getReceiptID());
        holder.orderStatus.setText(myOrder.getStatus() + "...");
        holder.productsNumber.setText(myOrder.getTotalItems() + " Items");
        holder.totalPrice.setText("Total: M " + myOrder.getTotalPrice() + ".00" );

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(Long.parseLong(myOrder.getTimeStamp()));
            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
            holder.timeStamp.setText(pTime);
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        getStoreDetails(myOrder.getStoreID(), holder);
        prepareOrderProgress(myOrder, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, OrderDetailsActivity.class);
                myIntent.putExtra("orderID", myOrder.getReceiptID());
                context.startActivity(myIntent);
            }
        });

        holder.shopPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", myOrder.getStoreID());
                context.startActivity(shopIntent);
            }
        });

        holder.shopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", myOrder.getStoreID());
                context.startActivity(shopIntent);
            }
        });
    }

    private void prepareOrderProgress(Orders myOrder, ViewHolder holder) {

    }

    private void getStoreDetails(String storeID, ViewHolder holder) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Retails");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    if(retail.getRetail_id().equals(storeID)){
                        holder.shopName.setText(retail.getRetailName());

                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.shopPic);
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
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView shopPic;
        TextView shopName, orderNumber, orderStatus, timeStamp, productsNumber, totalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shopPic = itemView.findViewById(R.id.current_order_image);
            shopName = itemView.findViewById(R.id.OrderShopName);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderStatus = itemView.findViewById(R.id.tv_current_order_status);
            timeStamp = itemView.findViewById(R.id.OrderTimeStamp);
            productsNumber = itemView.findViewById(R.id.order_item_count);
            totalPrice = itemView.findViewById(R.id.order_product_price);
        }
    }
}
