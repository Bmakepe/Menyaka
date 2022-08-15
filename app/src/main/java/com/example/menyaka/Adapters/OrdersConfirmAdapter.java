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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.ProductDetailActivity;
import com.example.menyaka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class OrdersConfirmAdapter extends RecyclerView.Adapter<OrdersConfirmAdapter.ViewHolder> {
    Context context;
    ArrayList<CartItems> items;

    int totalAmount = 0;

    String cartID;

    DatabaseReference ordersRef, returnsReference;


    public OrdersConfirmAdapter(Context context, ArrayList<CartItems> items, String cartID) {
        this.context = context;
        this.items = items;
        this.cartID = cartID;
    }

    public OrdersConfirmAdapter(Context context, ArrayList<CartItems> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItems cartItems = items.get(position);

        //getProductDetails(cartItems.getProductID(), holder);
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders").child(cartID);
        returnsReference = FirebaseDatabase.getInstance().getReference("Returns");
        getProductDetails(cartItems.getProductID(), holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(context, holder.itemView, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Product");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Request Return");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case 0:

                                Intent intent = new Intent(context, ProductDetailActivity.class);
                                intent.putExtra("productID", cartItems.getProductID());
                                context.startActivity(intent);
                                break;

                            case 1:
                                requestReturn(cartItems);
                                break;

                            default:
                                Toast.makeText(context, "Unknown Selection", Toast.LENGTH_SHORT).show();

                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });
    }

    private void requestReturn(CartItems cartItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Returns")
                .setMessage("Are you sure you want to return this product?")
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String returnID = returnsReference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("returnID", returnID);
                        hashMap.put("storeID", cartItems.getStoreID());
                        hashMap.put("productID", cartItems.getProductID());
                        hashMap.put("userID", cartItems.getUserID());
                        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                        assert returnID != null;
                        returnsReference.child(returnID).setValue(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        HashMap<String, Object> orderMap = new HashMap<>();
                                        orderMap.put("status", "return requested");

                                        ordersRef.child("Products").child(cartItems.getProductID()).updateChildren(orderMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(context, "Your Request Has Been Sent!!!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void getProductDetails(String productID, ViewHolder holder) {
        ordersRef.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    CartItems items = ds.getValue(CartItems.class);

                    assert items != null;
                    if (items.getProductID().equals(productID)){

                        holder.productQuantity.setText("Quantity: " + items.getOrderQuantity() + " items");
                        holder.transaction_status.setText(items.getStatus() + "...");

                        final DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
                        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Product product = ds.getValue(Product.class);

                                    assert product != null;
                                    if (product.getProductId().equals(items.getProductID())){

                                        holder.productName.setText(product.getProductName());

                                        String price = product.getPrice();
                                        holder.productPrice.setText("Price: M " + price);

                                        double total = Double.parseDouble(items.getOrderQuantity()) * Double.parseDouble(price);
                                        DecimalFormat format = new DecimalFormat("##.00");
                                        holder.totalPrice.setText("Total: M " + format.format(total));

                                        totalAmount = totalAmount + (int)total;
                                        Intent intent = new Intent("FinalPaymentDue");
                                        intent.putExtra("finalPaymentDue", totalAmount);

                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                        try{
                                            Picasso.get().load(product.getProductImg()).into(holder.productIMG);
                                        }catch (NullPointerException ignored){}

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("Retails");
                        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Retail retail = ds.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(items.getStoreID()))
                                        holder.productRetailer.setText(retail.getRetailName());
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productPrice, productRetailer, productQuantity, totalPrice, transaction_status;
        ImageView productIMG;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.order_tv_ProductName);
            productPrice = itemView.findViewById(R.id.order_tvPPrice);
            productRetailer = itemView.findViewById(R.id.order_tv_xx_retailer_name);
            productQuantity = itemView.findViewById(R.id.order_tvPQuantity);
            productIMG = itemView.findViewById(R.id.order_iv_product_image);
            totalPrice = itemView.findViewById(R.id.order_totalPrice);
            transaction_status = itemView.findViewById(R.id.order_transaction_status);
        }
    }
}
