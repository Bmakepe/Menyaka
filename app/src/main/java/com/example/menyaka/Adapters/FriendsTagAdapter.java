package com.example.menyaka.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsTagAdapter extends RecyclerView.Adapter<FriendsTagAdapter.ViewHolder> {

    Context context;
    List<User> userList;
    private ArrayList<User> taggedFriends;

    public FriendsTagAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagging_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        taggedFriends = new ArrayList<>();

        holder.selection.setTag("Untagged");

        holder.taggedName.setText(user.getUsername());
        try{
            Picasso.get().load(user.getImageUrl()).into(holder.taggedProPic);
        }catch (NullPointerException ignored){}

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "You will be able to tag " + user.getUsername(), Toast.LENGTH_SHORT).show();

                if(holder.selection.getTag().equals("Untagged")){
                    holder.selection.setTag("Tagged");
                    holder.selection.setColorFilter(R.color.colorPrimaryDark);

                    taggedFriends.add(user);

                }else{
                    holder.selection.setTag("Untagged");
                    holder.selection.setColorFilter(R.color.colorGold);

                    taggedFriends.remove(user);

                }

                Toast.makeText(context, "You have tagged "+ user.getUsername() + " and the array contains " + taggedFriends.size() + " elements.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView taggedProPic;
        TextView taggedName;
        ImageView selection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taggedProPic = itemView.findViewById(R.id.taggedProPic);
            taggedName = itemView.findViewById(R.id.taggedName);
            selection = itemView.findViewById(R.id.taggingCondition);
        }
    }
}
