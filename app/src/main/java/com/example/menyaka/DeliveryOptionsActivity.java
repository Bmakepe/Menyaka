package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.AddressAdapter;
import com.example.menyaka.Adapters.ShippingAgentsAdapter;
import com.example.menyaka.Models.Address;
import com.example.menyaka.Models.ShippingAgents;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DeliveryOptionsActivity extends AppCompatActivity {

    Dialog moreAgentsDialog;
    CardView addressArea, deliveryOptions;
    TextView myDefaultAddress;

    FirebaseUser firebaseUser;
    DatabaseReference agentsRef, cartRef;

    //for checking delivery method
    RadioGroup deliveryMethod;
    String preferredDeliveryMethod, cartID, buyMethod, deliveryAddress;

    //for displaying shipping agents
    ArrayList<ShippingAgents> agents;
    RecyclerView agentsRecycler;
    ShippingAgentsAdapter agentsAdapter;

    public boolean shipperChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        addressArea = findViewById(R.id.addressArea);
        deliveryOptions = findViewById(R.id.deliveryOptions);
        myDefaultAddress = findViewById(R.id.my_default_address);
        deliveryMethod = findViewById(R.id.deliveryMethods);
        agentsRecycler = findViewById(R.id.agentsRecycler);

        Intent intent = getIntent();
        cartID = intent.getExtras().getString("cartID");
        buyMethod = intent.getExtras().getString("buyMethod");

        cartRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts")
                .child(cartID);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getDefaultAddress();
        iniAgentsDialog();

        deliveryMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                switch(checkedID){
                    case R.id.pickInStore:
                        deliveryOptions.setVisibility(View.GONE);
                        addressArea.setVisibility(View.GONE);
                        preferredDeliveryMethod = ((RadioButton)findViewById(deliveryMethod.getCheckedRadioButtonId())).getText().toString();
                        break;

                    case R.id.addressDelivery:
                        deliveryOptions.setVisibility(View.VISIBLE);
                        addressArea.setVisibility(View.VISIBLE);
                        preferredDeliveryMethod = ((RadioButton)findViewById(deliveryMethod.getCheckedRadioButtonId())).getText().toString();
                        break;

                    case R.id.differentAddress:
                        Toast.makeText(DeliveryOptionsActivity.this, "You will be able to put in different address", Toast.LENGTH_SHORT).show();
                        preferredDeliveryMethod = ((RadioButton)findViewById(deliveryMethod.getCheckedRadioButtonId())).getText().toString();
                        break;

                    default:
                        Toast.makeText(DeliveryOptionsActivity.this, "Please select a delivery method", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getDeliveryStores();

        findViewById(R.id.moreAgents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DeliveryOptionsActivity.this, "List of Agents will open from this click", Toast.LENGTH_SHORT).show();
                moreAgentsDialog.show();
            }
        });

        findViewById(R.id.pick_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DeliveryOptionsActivity.this, AddressActivity.class));
            }
        });

        findViewById(R.id.goToConfirmation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(preferredDeliveryMethod)){
                    Toast.makeText(DeliveryOptionsActivity.this, "Please select a delivery method", Toast.LENGTH_SHORT).show();
                }else{
                    updateDelivery();
                }
            }
        });

        findViewById(R.id.confirmationBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buyMethod.equals("Cart Buy"))
                    finish();
                else{
                    removeBuyNowProduct();
                }
            }
        });
    }

    private void removeBuyNowProduct() {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    snapshot.getRef().removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    });
                }else{
                    Toast.makeText(DeliveryOptionsActivity.this, "Could not delete", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateDelivery() {

        HashMap<String, Object> addressMap = new HashMap<>();

        switch (preferredDeliveryMethod){
            case "Pick Up In Store":
                addressMap.put("deliveryAddress", preferredDeliveryMethod);
                break;

            case "Deliver To Default Address":
                addressMap.put("deliveryAddress", deliveryAddress);
                break;

            case "Deliver To Different Address":
                addressMap.put("deliveryAddress", deliveryAddress);
                break;

            default:
                Toast.makeText(this, "Unknown delivery method", Toast.LENGTH_SHORT).show();

        }

        cartRef.updateChildren(addressMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent intent = new Intent(DeliveryOptionsActivity.this, OrderSummaryActivity.class);
                        intent.putExtra("cartID", cartID);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DeliveryOptionsActivity.this, "Could not update address details", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getDeliveryStores() {

        //for displaying the agents
        agentsRecycler.setHasFixedSize(true);
        agentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        agents = new ArrayList<>();
        agentsRef = FirebaseDatabase.getInstance().getReference("ShippingAgents");
        agentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                agents.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ShippingAgents allAgents = ds.getValue(ShippingAgents.class);
                    agents.add(allAgents);
                }
                agentsAdapter = new ShippingAgentsAdapter(DeliveryOptionsActivity.this, agents);
                agentsRecycler.setAdapter(agentsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void iniAgentsDialog() {
        moreAgentsDialog = new Dialog(DeliveryOptionsActivity.this);
        moreAgentsDialog.setContentView(R.layout.recycler_view_layout);
        moreAgentsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        moreAgentsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        moreAgentsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        RecyclerView allAgentsRecycler = moreAgentsDialog.findViewById(R.id.recycler_View);

        TextView header = moreAgentsDialog.findViewById(R.id.recyclerHeader);
        header.setText("Shipping Agents");

        //for displaying the agents
        allAgentsRecycler.setHasFixedSize(true);
        allAgentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        agents = new ArrayList<>();
        agentsRef = FirebaseDatabase.getInstance().getReference("ShippingAgents");
        agentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                agents.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ShippingAgents allAgents = ds.getValue(ShippingAgents.class);
                    agents.add(allAgents);
                }
                agentsAdapter = new ShippingAgentsAdapter(DeliveryOptionsActivity.this, agents);
                allAgentsRecycler.setAdapter(agentsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        moreAgentsDialog.findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreAgentsDialog.dismiss();
            }
        });

    }

    private void getDefaultAddress() {
        final DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Address address = ds.getValue(Address.class);
                    assert address != null;
                    if(address.getUserID().equals(firebaseUser.getUid())) {
                        if (address.getDefaultAddress().equals("default")) {
                            deliveryAddress = ds.getKey();
                            myDefaultAddress.setText(address.getDeliveryHouse() + ", " + address.getDeliveryRoad() + ", " + address.getDeliveryNeighbourHood()
                                    + ", " + address.getDeliveryDistrict() + ", " + address.getDeliveryZipCode());
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}