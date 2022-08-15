package com.example.menyaka.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.PhoneContacts;
import com.example.menyaka.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhoneContactsAdapter extends RecyclerView.Adapter<PhoneContactsAdapter.ViewHolder> {

    List<PhoneContacts> contactsList;
    Context context;

    public PhoneContactsAdapter(List<PhoneContacts> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.phone_contact_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final String name = contactsList.get(position).getName();
        final String number = contactsList.get(position).getPhoneNumber();
        final String userid = contactsList.get(position).getUserId();

        holder.contactName.setText(name);
        holder.contactNumber.setText(number);
        holder.inviteBTN.setVisibility(View.VISIBLE);
        holder.inviteBTN.setText("INVITE");

        holder.inviteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, name + " will be invited to join Menyaka soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (contactsList == null){
            return 0;
        }
        return contactsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber, inviteBTN;
        CircleImageView contactProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            inviteBTN = itemView.findViewById(R.id.invitationBTN);
        }
    }
}
