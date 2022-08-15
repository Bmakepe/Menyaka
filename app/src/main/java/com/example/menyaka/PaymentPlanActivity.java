package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.Orders;
import com.example.menyaka.Models.Retail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PaymentPlanActivity extends AppCompatActivity {

    TextView totalItemsCount, subTotalPrice, deliveryChargesTV, finalPayment, orderID, storeNameTV;

    RadioGroup paymentMethod;

    FirebaseUser user;
    DatabaseReference cartReference, retailReference, ordersReference;

    DecimalFormat format;
    boolean isMethodChecked = false;

    String cartID, patala, preferredPayment, receiptID,
            totalItems, subTotal, storeID, address, productKey;

    double totalPrice;

    ArrayList<CartItems> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_layout);

        totalItemsCount = findViewById(R.id.final_payment_item_count);
        subTotalPrice = findViewById(R.id.final_payment_price);
        deliveryChargesTV = findViewById(R.id.deliveryCharges);
        finalPayment = findViewById(R.id.final_payment_amount_payable);
        orderID = findViewById(R.id.final_payment_order_id);
        paymentMethod = findViewById(R.id.paymentMethod);
        storeNameTV = findViewById(R.id.storeNameTV);

        format = new DecimalFormat("##.00");

        Intent intent = getIntent();
        cartID = intent.getExtras().getString("cartID");

        cartItems = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(cartID);
        retailReference = FirebaseDatabase.getInstance().getReference("Retails");
        ordersReference = FirebaseDatabase.getInstance().getReference("Orders");

        getOrderDetails();
        getPaymentMethod();

        Toast.makeText(this, "The cart ID is " + cartID, Toast.LENGTH_SHORT).show();


        findViewById(R.id.paymentBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.payment_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            if(isMethodChecked == false)
                Toast.makeText(PaymentPlanActivity.this, "Please Select a Payment Method", Toast.LENGTH_SHORT).show();
            else
                checkPaymentMethod();

            }
        });
    }

    private void getPaymentMethod() {

        paymentMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                switch(checkedID){
                    case R.id.cashBTN:
                        preferredPayment = ((RadioButton)findViewById(paymentMethod.getCheckedRadioButtonId())).getText().toString();
                        patala = "Cash";
                        isMethodChecked = true;
                        break;

                    case R.id.visaBTN:
                        preferredPayment = ((RadioButton)findViewById(paymentMethod.getCheckedRadioButtonId())).getText().toString();
                        patala = "Bank";
                        isMethodChecked = true;
                        break;

                    case  R.id.mpesaBTN:
                        preferredPayment = ((RadioButton)findViewById(paymentMethod.getCheckedRadioButtonId())).getText().toString();
                        patala = "Mobile Money";
                        isMethodChecked = true;
                        break;

                    default:
                        Toast.makeText(PaymentPlanActivity.this, "Please select a payment method correctly", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void checkPaymentMethod() {
        switch (patala) {
            case "Cash":
                placeOrder();
                break;

            case "Bank":
                Intent intent1 = new Intent(PaymentPlanActivity.this, CardPaymentActivity.class);
                //intent1.putExtra("finalPrice", String.valueOf(finalPayment));
                startActivity(intent1);
                break;

            case "Mobile Money":
                showMoreOptions();
                break;

            default:
                Toast.makeText(this, "Unknown payment selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void placeOrder() {

        final String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("receiptID", receiptID);
        hashMap.put("userID", user.getUid());
        hashMap.put("totalItems", totalItems);
        hashMap.put("deliveryCharges", "20.00");
        hashMap.put("subTotal", subTotal);
        hashMap.put("totalPrice", totalPrice);
        hashMap.put("status", "pending");
        hashMap.put("storeID", storeID);
        hashMap.put("timeStamp", timestamp);
        hashMap.put("deliveryAddress", address);
        hashMap.put("paymentMethod", patala);

        ordersReference.child(receiptID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        for (int i = 0; i < cartItems.size(); i++){
                            productKey = ordersReference.push().getKey();
                            assert productKey != null;
                            ordersReference.child(receiptID).child("Products").child(productKey).setValue(cartItems.get(i))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(PaymentPlanActivity.this, "Could not place products", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        removeCartItem();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PaymentPlanActivity.this, "Did not place the order properly", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeCartItem() {

        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Intent intent = new Intent(PaymentPlanActivity.this, OrderReviewActivity.class);
                                    intent.putExtra("cartID", receiptID);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PaymentPlanActivity.this, "Could not remove cart item", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {

        String[] options = {"Mpesa", "EcoCash"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                for(int j = 0; j < options.length; j++){
                    if(j == i){
                        Intent intent = new Intent(PaymentPlanActivity.this, MobilePaymentActivity.class);
                        intent.putExtra("network", options[i]);
                        intent.putExtra("cartID", cartID);
                        startActivity(intent);
                    }
                }
            }
        });
        builder.create().show();
    }

    private void getOrderDetails() {
        cartReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CartItems items = snapshot.getValue(CartItems.class);

                    assert items != null;
                    receiptID = items.getReceiptID();
                    subTotal = items.getTotalPrice();
                    //totalPrice = Integer.parseInt(subTotal) + 20;
                    totalPrice = Double.parseDouble(subTotal) + 20;
                    storeID = items.getStoreID();
                    address = items.getDeliveryAddress();

                    orderID.setText(receiptID);
                    subTotalPrice.setText("M " + subTotal);
                    deliveryChargesTV.setText("M 20.00");

                    cartReference.child("CartProducts")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        totalItems = String.valueOf(snapshot.getChildrenCount());
                                        totalItemsCount.setText(totalItems + " items");

                                        cartItems.clear();
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            CartItems items1 = ds.getValue(CartItems.class);

                                            Toast.makeText(PaymentPlanActivity.this, "this product has been added " + items1, Toast.LENGTH_SHORT).show();
                                            cartItems.add(items1);

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    retailReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                Retail retail = ds.getValue(Retail.class);

                                assert retail != null;
                                if (retail.getRetail_id().equals(items.getStoreID())){
                                    storeNameTV.setText(retail.getRetailName());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}