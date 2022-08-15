package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.ProductListAdapter;
import com.example.menyaka.Adapters.RedeemProductAdapter;
import com.example.menyaka.Models.Points;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Share.FullScreenImageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RedeemPointsActivity extends AppCompatActivity {

    ImageView backBTN, storeVerification;
    CircleImageView shopLogo;
    Button messageBTN;
    TextView shopName, header, timeStamp, expiryDate;
    RecyclerView productsRecycler;

    String pointsID, storeID, storeName;

    DatabaseReference reference, storeRef, productsRef, loyaltyRef;
    FirebaseUser user;

    ArrayList<Product> productList;

    RedeemProductAdapter listAdapter;

    Boolean subscription = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loyalty);

        Intent intent =  getIntent();
        pointsID = intent.getExtras().getString("pointsID");

        backBTN = findViewById(R.id.redeemBackBTN);
        shopLogo = findViewById(R.id.redeemPicture);
        messageBTN = findViewById(R.id.redeemSendMessageBTN);
        shopName = findViewById(R.id.redeemProfileName);
        header = findViewById(R.id.redeemHeader);
        productsRecycler = findViewById(R.id.redeemProductsRecycler);
        timeStamp = findViewById(R.id.enrollmentDate);
        expiryDate = findViewById(R.id.pointsExpiryDate);
        storeVerification = findViewById(R.id.storeVerification);

        productList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(user.getUid());
        productsRef = FirebaseDatabase.getInstance().getReference("Products");

        getDetails();
        getStoreProducts();

        productsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productsRecycler.setLayoutManager(layoutManager);

        messageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RedeemPointsActivity.this, MessageActivity.class);
                intent.putExtra("receiverID", storeID);
                startActivity(intent);
            }
        });

        findViewById(R.id.pointsProfileMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(RedeemPointsActivity.this, findViewById(R.id.pointsProfileMenu), Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "View " + storeName + " Profile ");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Redeem Points ");
                if (subscription)
                    popupMenu.getMenu().add(Menu.NONE, 2, 0, "Unsubscribe loyalty program");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        if (id == 0) {

                            Intent shopIntent = new Intent(RedeemPointsActivity.this, ShopDetailsActivity.class);
                            shopIntent.putExtra("storeId", storeID);
                            startActivity(shopIntent);

                        } else if (id == 1) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(RedeemPointsActivity.this);
                            builder.setTitle("Points Redemption")
                                    .setMessage("Are you sure you want to redeem your " + storeName + " loyalty program points?" )
                                    .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            Toast.makeText(RedeemPointsActivity.this, "You will be able to redeem your points", Toast.LENGTH_SHORT).show();

                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();

                        } else if (id == 2){

                            AlertDialog.Builder builder = new AlertDialog.Builder(RedeemPointsActivity.this);
                            builder.setTitle("Subscription")
                                    .setMessage("Are you sure you want to unsubscribe to " + storeName + " loyalty program?" )
                                    .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            loyaltyRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(user.getUid());
                                            loyaltyRef.child(storeID).removeValue();
                                            finish();
                                            Toast.makeText(RedeemPointsActivity.this, "Unsubscribed successfully", Toast.LENGTH_SHORT).show();

                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();

                        }else {
                            Toast.makeText(RedeemPointsActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }

        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        shopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(RedeemPointsActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", storeID);
                startActivity(shopIntent);

            }
        });

        shopLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RedeemPointsActivity.this, FullScreenImageActivity.class);
                intent.putExtra("pictureID", storeID);
                startActivity(intent);
            }
        });

    }

    private void checkSubscription(String storeID) {
        loyaltyRef = FirebaseDatabase.getInstance().getReference("MenyakaPoints").child(user.getUid());
        loyaltyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                subscription = snapshot.child(storeID).exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDetails() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Points myPoints = ds.getValue(Points.class);

                        assert myPoints != null;
                        if (myPoints.getPointsID().equals(pointsID)){
                            storeID = myPoints.getShopID();
                            header.setText(myPoints.getPointsNo() + " Total Points Accumulated");

                            try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(Long.parseLong(myPoints.getSubscriptionDate()));
                                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                timeStamp.setText("Enrollment Date: " + pTime);
                            }catch (NumberFormatException ignored){}//for converting timestamp

                            try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(Long.parseLong(myPoints.getExpiryDate()));
                                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                expiryDate.setText("Expiry Date: " + pTime);
                            }catch (NumberFormatException ignored){}//for converting timestamp
                            
                            checkSubscription(storeID);

                            storeRef = FirebaseDatabase.getInstance().getReference("Retails");
                            storeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        Retail retail = ds.getValue(Retail.class);

                                        assert retail != null;
                                        if (retail.getRetail_id().equals(myPoints.getShopID())){
                                            shopName.setText(retail.getRetailName());
                                            storeName = retail.getRetailName();

                                            if(retail.isVerified())
                                                storeVerification.setVisibility(View.VISIBLE);

                                            try{
                                                Picasso.get().load(retail.getImageUrl()).into(shopLogo);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoreProducts() {

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);

                    assert product != null;
                    if (product.getStoreId().equals(storeID))
                        productList.add(product);
                }

                Collections.shuffle(productList);
                listAdapter = new RedeemProductAdapter(RedeemPointsActivity.this, productList);
                productsRecycler.setAdapter(listAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}