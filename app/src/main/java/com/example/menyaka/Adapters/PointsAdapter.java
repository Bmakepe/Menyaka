package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Sales;
import com.example.menyaka.PostDetailActivity;
import com.example.menyaka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private List<Sales> salesList;
    private Context context;

    private FirebaseUser firebaseUser;

    public PointsAdapter(List<Sales> sales, Context context){
        this.salesList = sales;
        this.context = context;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.points_rating, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Sales mySales = salesList.get(position);
        ViewHolder viewHolder = holder;

        viewHolder.msgName.setText(mySales.getRetailName());
        viewHolder.salesTotal.setText(mySales.getSalesPrice());

    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView msgName;
        TextView salesTotal;
        ImageView storePic;
        TextView storePoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            msgName = itemView.findViewById(R.id.pointHeader);
            salesTotal = itemView.findViewById(R.id.pointsNO);
            //storePic = itemView.findViewById(R.id.storeImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent PostDetailActivity = new Intent(context, PostDetailActivity.class);
                    //int position = getAdapterPosition();


                    context.startActivity(PostDetailActivity);
                }
            });
        }
    }
}
