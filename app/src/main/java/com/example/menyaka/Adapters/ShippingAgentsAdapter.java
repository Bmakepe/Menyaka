package com.example.menyaka.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.AgentProfileActivity;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShippingAgentsAdapter extends RecyclerView.Adapter<ShippingAgentsAdapter.ViewHolder> {

    Context context;
    ArrayList<ShippingAgents> shippingAgents;
    int mCheckedPosition = -1;

    Dialog shipperProfileDialog;

    public ShippingAgentsAdapter(Context context, ArrayList<ShippingAgents> shippingAgents) {
        this.context = context;
        this.shippingAgents = shippingAgents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shipping_agent_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAgents agents = shippingAgents.get(position);

        holder.shippingName.setText(agents.getShippingName());
        holder.shippingName.setChecked(position == mCheckedPosition);
        holder.shippingName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position == mCheckedPosition){
                    holder.shippingName.setChecked(false);
                    mCheckedPosition = -1;
                }else{
                    mCheckedPosition = position;
                    notifyDataSetChanged();
                }
            }
        });

        holder.viewShipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent agentIntent = new Intent(context, AgentProfileActivity.class);
                agentIntent.putExtra("agentID", agents.getCompanyID());
                context.startActivity(agentIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return shippingAgents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RadioButton shippingName;
        RatingBar shipperRating;
        ImageView viewShipper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shippingName = itemView.findViewById(R.id.shippingName);
            shipperRating = itemView.findViewById(R.id.shipperRating);
            viewShipper = itemView.findViewById(R.id.viewShipper);
        }
    }
}
