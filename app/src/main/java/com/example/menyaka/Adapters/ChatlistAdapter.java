package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.MessageActivity;
import com.example.menyaka.Models.ChatList;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.example.menyaka.Utils.GetTimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.ViewHolder> {

    Context context;
    List<ChatList> allChats;

    DatabaseReference userReference, storeReference;

    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> lastTimeStampMap;

    GetTimeAgo timeAgo;

    public ChatlistAdapter(Context context, List<ChatList> allChats) {
        this.context = context;
        this.allChats = allChats;
        lastMessageMap = new HashMap<>();
        lastTimeStampMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storeReference = FirebaseDatabase.getInstance().getReference("Retails");

        ChatList chats = allChats.get(position);

        final String hisUid = chats.getId();

        timeAgo = new GetTimeAgo();

        String lastmessage = lastMessageMap.get(hisUid);
        String lastTime = lastTimeStampMap.get(hisUid);

        getUserDetails(hisUid, holder);

        if(lastmessage == null || lastmessage.equals("default")){
            holder.lastMessage.setVisibility(View.GONE);
        }else{
            holder.lastMessage.setVisibility(View.VISIBLE);
            holder.lastMessage.setText(lastmessage);

            String postTime = timeAgo.getTimeAgo(Long.parseLong(lastTime), context);
            holder.timestamp.setText("- " + postTime);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("receiverID", hisUid);
                context.startActivity(intent);
            }
        });

    }

    private void getUserDetails(String hisUid, ViewHolder holder) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        Query query = reference.orderByChild("id").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){

                        User user = ds.getValue(User.class);

                        assert user != null;
                        holder.contactName.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageUrl()).into(holder.profilePic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.profile_png_1114185).into(holder.profilePic);
                        }

                    }
                }else{
                    final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Retails");
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot data : snapshot.getChildren()){
                                Retail retail = data.getValue(Retail.class);
                                assert retail != null;
                                if (retail.getRetail_id().equals(hisUid)) {
                                    holder.contactName.setText(retail.getRetailName());

                                    try {
                                        Picasso.get().load(retail.getImageUrl()).into(holder.profilePic);
                                    } catch (NullPointerException e) {
                                        Picasso.get().load(R.drawable.profile_png_1114185).into(holder.profilePic);
                                    }
                                }else{
                                    final DatabaseReference agentReference = FirebaseDatabase.getInstance().getReference("ShippingAgents");
                                    agentReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                ShippingAgents agents = ds.getValue(ShippingAgents.class);

                                                if (agents.getCompanyID().equals(hisUid)){

                                                    holder.contactName.setText(agents.getShippingName());

                                                    try {
                                                        Picasso.get().load(agents.getCompanyLogo()).into(holder.profilePic);
                                                    } catch (NullPointerException e) {
                                                        Picasso.get().load(R.drawable.profile_png_1114185).into(holder.profilePic);
                                                    }

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

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void setLastMessageMap(String userid, String lastMessage){
        lastMessageMap.put(userid, lastMessage);
    }

    public void setLastTimeStamp(String userId, String lastTimeStamp) {

        lastTimeStampMap.put(userId, lastTimeStamp);
    }

    @Override
    public int getItemCount() {
        return allChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profilePic;
        TextView contactName, timestamp, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.contact_img);
            contactName = itemView.findViewById(R.id.contact_name);
            timestamp = itemView.findViewById(R.id.contact_date);
            lastMessage = itemView.findViewById(R.id.contact_message);
        }
    }
}
