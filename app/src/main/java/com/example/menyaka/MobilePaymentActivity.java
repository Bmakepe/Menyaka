package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Merchants;
import com.example.menyaka.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MobilePaymentActivity extends AppCompatActivity {

    String network, cartID, storeID;

    ImageView paymentLogo;
    TextView merchantName,  merchantNumber, processPayment;
    EditText numberArea;

    DatabaseReference reference, merchantRef, cartReference;
    FirebaseUser firebaseUser;

    private static final int REQUEST_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_payment_layout);

        Intent intent = getIntent();
        network = intent.getExtras().getString("network");
        cartID = intent.getExtras().getString("cartID");

        numberArea = findViewById(R.id.phoneNumberArea);
        paymentLogo = findViewById(R.id.paymentLogo);
        merchantName = findViewById(R.id.merchantName);
        merchantNumber = findViewById(R.id.merchantNumber);
        processPayment = findViewById(R.id.processPayment);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(cartID);

        getOrderDetails();
        getMerchantDetails();
        displayAppropriatePic();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if(user.getId().equals(firebaseUser.getUid())){
                        numberArea.setText(user.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        processPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MobilePaymentActivity.this);
                builder.setTitle("PAYMENT")
                        .setMessage("I hereby, declare that the number entered is mine")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialUSSD(network);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        findViewById(R.id.mobileBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.copyMerchant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MobilePaymentActivity.this, "This merchant number will be copied to your clipboard", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getOrderDetails() {
        cartReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CartItems items = snapshot.getValue(CartItems.class);
                    assert items != null;
                    storeID = items.getStoreID();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAppropriatePic() {
        if(network.equals("Mpesa"))
            paymentLogo.setImageResource(R.drawable.mpesa_logo);
        else if (network.equals("EcoCash"))
            paymentLogo.setImageResource(R.drawable.eco_cash_logo);
    }

    private void getMerchantDetails() {
        merchantRef = FirebaseDatabase.getInstance().getReference("MerchantDetails");
        merchantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Merchants merchants = ds.getValue(Merchants.class);
                    assert merchants != null;
                    if(merchants.getShopID().equals(storeID))
                        if (merchants.getMerchantType().equals(network)){
                            merchantName.setText(merchants.getMerchantName());
                            merchantNumber.setText(merchants.getMerchantNumber());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dialUSSD(String network) {

        if(ContextCompat.checkSelfPermission(MobilePaymentActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MobilePaymentActivity.this,
                    new String [] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);

        } else if (network.equals("EcoCash")){

            String encodedHash = Uri.encode("#");
            String mNumber = "*100";

            String R_Text = mNumber + encodedHash;
            String dial = "tel:"+R_Text;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            readUSSDConfirmation();

        }else if(network.equals("Mpesa")){

            String encodedHash = Uri.encode("#");
            String mNumber = "*200";

            String R_Text = mNumber + encodedHash;
            String dial = "tel:"+R_Text;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            readUSSDConfirmation();

        }else{
            Toast.makeText(this, "Unable to process payment", Toast.LENGTH_SHORT).show();
        }

    }

    private void readUSSDConfirmation() {
        Intent intent1 = new Intent(MobilePaymentActivity.this, OrderReviewActivity.class);
        intent1.putExtra("cartID", cartID);
        startActivity(intent1);
    }

}