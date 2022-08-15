package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.DeliveryOptionsActivity;
import com.example.menyaka.Models.Address;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    Context context;
    ArrayList<Address> addresses;
    Dialog editAddressDialog;

    String addressID;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    public AddressAdapter(Context context, ArrayList<Address> addresses) {
        this.context = context;
        this.addresses = addresses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.address_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        reference = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Address address = addresses.get(position);

        holder.deliveryName.setText(address.getDeliveryName());
        holder.deliveryNumber.setText(address.getDeliveryNumber());

        holder.deliveryAddress.setText(address.getDeliveryHouse() + ", " + address.getDeliveryRoad() + ", " + address.getDeliveryNeighbourHood()
                + ", " + address.getDeliveryDistrict() + ", " + address.getDeliveryZipCode());
        
        if(address.getDefaultAddress().equals("default")){
            holder.defaultNumber.setText("Default Address");
            holder.defaultNumber.setTypeface(Typeface.DEFAULT_BOLD);
        }else if(address.getDefaultAddress().equals("secondary")){
            holder.defaultNumber.setText("Set As Default Address");
        }

        iniEditAddressDialog(holder);
        
        holder.deleteAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");

                //delete btn
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAddress(position);
                    }
                });
                //cancel delete btn
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.itemView, address.getAddressID());
            }
        });
    }

    private void iniEditAddressDialog(ViewHolder holder) {
        editAddressDialog = new Dialog(context);
        editAddressDialog.setContentView(R.layout.new_address_layout);
        editAddressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        editAddressDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        editAddressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        //for editing the address in a dialog
        holder.myName = editAddressDialog.findViewById(R.id.myNameET);
        holder.myNumber = editAddressDialog.findViewById(R.id.myNumberET);
        holder.houseAddress = editAddressDialog.findViewById(R.id.houseAddressET);
        holder.roadDetails = editAddressDialog.findViewById(R.id.roadDetailsET);
        holder.district = editAddressDialog.findViewById(R.id.districtET);
        holder.neighbourhood = editAddressDialog.findViewById(R.id.neighbourhoodET);
        holder.zipCode = editAddressDialog.findViewById(R.id.zipCode);
        holder.saveAddressBTN = editAddressDialog.findViewById(R.id.saveAddressBTN);
        holder.updateAddress = editAddressDialog.findViewById(R.id.summaryContinue);
        holder.dialogHeader = editAddressDialog.findViewById(R.id.addressHeader);
        holder.dialogBackBTN = editAddressDialog.findViewById(R.id.newAddressBackBTN);

        holder.dialogHeader.setText("Update Address");
        holder.updateAddress.setText("UPDATE");

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DeliveryAddress");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Address address = ds.getValue(Address.class);
                    if(address.getUserID().equals(fUser.getUid())){

                        holder.myName.setText(address.getDeliveryName());
                        holder.myNumber.setText(address.getDeliveryNumber());
                        holder.houseAddress.setText(address.getDeliveryHouse());
                        holder.roadDetails.setText(address.getDeliveryRoad());
                        holder.district.setText(address.getDeliveryDistrict());
                        holder.neighbourhood.setText(address.getDeliveryNeighbourHood());
                        holder.zipCode.setText(address.getDeliveryZipCode());

                        addressID = address.getAddressID();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.saveAddressBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You will be able to update the address", Toast.LENGTH_SHORT).show();

                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("deliveryName", holder.myName.getText().toString());
                hashMap.put("deliveryNumber", holder.myNumber.getText().toString());
                hashMap.put("deliveryHouse", holder.houseAddress.getText().toString());
                hashMap.put("deliveryRoad", holder.roadDetails.getText().toString());
                hashMap.put("deliveryNeighbourHood", holder.neighbourhood.getText().toString());
                hashMap.put("deliveryDistrict", holder.district.getText().toString());
                hashMap.put("deliveryZipCode", holder.zipCode.getText().toString());

                reference.child(addressID).updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                editAddressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Update Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.dialogBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editAddressDialog.dismiss();
            }
        });
    }

    private void showMoreOptions(View itemView, String addressID) {
        PopupMenu menu = new PopupMenu(context, itemView, Gravity.END);

        menu.getMenu().add(Menu.NONE, 0,0,"Select Address");
        menu.getMenu().add(Menu.NONE, 1,0,"Make Default Address");
        menu.getMenu().add(Menu.NONE, 2,0,"Edit Address");

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 0:
                        Toast.makeText(context, "You will be able to select this address", Toast.LENGTH_SHORT).show();
                    case 1:

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){

                                    Address defaultAddress = ds.getValue(Address.class);

                                    if(defaultAddress.getUserID().equals(firebaseUser.getUid()))

                                        if(defaultAddress.getDefaultAddress().equals("default")){

                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("defaultAddress", "secondary");

                                            reference.child(defaultAddress.getAddressID()).updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            Toast.makeText(context, "Successfully removed default address", Toast.LENGTH_SHORT).show();

                                                            HashMap<String, Object> defaultMap = new HashMap<>();
                                                            defaultMap.put("defaultAddress", "default");

                                                            reference.child(addressID).updateChildren(defaultMap)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(context, "Default address updated successfully", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, "Could not update default address", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Could not remove default address", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }else{

                                            HashMap<String, Object> defaultMap = new HashMap<>();
                                            defaultMap.put("defaultAddress", "default");

                                            reference.child(addressID).updateChildren(defaultMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(context, "Default address updated successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Could not update default address", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;
                    case 2:
                        editAddressDialog.show();
                        Toast.makeText(context, "Edit Address", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        menu.show();
    }

    private void deleteAddress(int position) {
        String addressID = addresses.get(position).getAddressID();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DeliveryAddress");

        Query query = reference.orderByChild("addressID").equalTo(addressID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Address address = ds.getValue(Address.class);
                    if(address.getAddressID().equals(addressID))
                        ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //for displaying the address
        TextView deliveryName, deliveryNumber, deliveryAddress, defaultNumber;
        ImageView deleteAddress;

        //for editing the address in a dialog
        EditText myName, myNumber, houseAddress, roadDetails, district, neighbourhood, zipCode;
        TextView updateAddress, dialogHeader;
        ImageView dialogBackBTN;
        RelativeLayout saveAddressBTN;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //for displaying the address
            deliveryName = itemView.findViewById(R.id.deliveryName);
            deliveryNumber = itemView.findViewById(R.id.deliveryNumber);
            deliveryAddress = itemView.findViewById(R.id.deliveryAddress);
            deleteAddress = itemView.findViewById(R.id.deleteAddress);
            defaultNumber = itemView.findViewById(R.id.defaultNumber);

        }
    }
}
