package com.example.menyaka.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Chat;
import com.example.menyaka.R;
import com.example.menyaka.Utils.GetTimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_AUDIO_LEFT = 2;
    public static final int MSG_TYPE_AUDIO_RIGHT = 3;
    public static final int MSG_TYPE_LOCATION_LEFT = 4;
    public static final int MSG_TYPE_LOCATION_RIGHT = 5;

    private Context context;
    private List<Chat> chats;
    private String imageUrl;

    private String audioUrl;

    private FirebaseUser firebaseUser;

    GetTimeAgo timeAgo;

    public MessageAdapter(Context context, List<Chat> chats, String imageUrl) {
        this.context = context;
        this.chats = chats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case MSG_TYPE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_AUDIO_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_left, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_AUDIO_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_right, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_LOCATION_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_location_left, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_LOCATION_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_location_right, parent, false);
                return new ViewHolder(view);

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chat chat = chats.get(position);
        timeAgo = new GetTimeAgo();

        getChatDetails(chat, holder, position);

        if (chat.getMsg_type().equals("location")){
            holder.locationBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String address = "https://maps.google.com/maps?saddr=" + chat.getLatitude() + "," + chat.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                    context.startActivity(intent);
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChatOptions(holder.itemView, chat.getSender(), position, chat);
            }
        });
    }

    private void showChatOptions(View itemView, String sender, int position, Chat chat) {

        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(sender.equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Delete");
        }
        if (chat.getMsg_type().equals("location"))
            popupMenu.getMenu().add(Menu.NONE, 4, 0, "View Location");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 0:
                        Toast.makeText(context, "Reply Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(context, "Copy Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(context, "Forward Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(position);
                            }
                        });
                        //cancel delete btn
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 4:
                        String address = "https://maps.google.com/maps?saddr=" + chat.getLatitude() + "," + chat.getLongitude();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                        context.startActivity(intent);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteMessage(int position) {
        String msgTimeStamp = chats.get(position).getTimestamp();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");

        Query query = reference.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(firebaseUser.getUid())) {
                        ds.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatDetails(Chat chat, ViewHolder holder, int position) {

        if (chat.getMsg_type().equals("audio")){
            holder.voicePlayerView.setAudio(chat.getMessage());
        }else if (chat.getMsg_type().equals("text")){
            holder.showMessages.setText(chat.getMessage());
        }

        String PostTime = timeAgo.getTimeAgo(Long.parseLong(chat.getTimestamp()), context);
        holder.timestamp.setText(PostTime);

        try{
            Picasso.get().load(imageUrl).into(holder.chatPic);
        }catch (NullPointerException e){
            Picasso.get().load(R.drawable.profile_png_1114185).into(holder.chatPic);
        }

        //setSeen
        if(position == chats.size() - 1){
            if(chats.get(position).isSeen()){
                holder.ticks.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
            }else{
                holder.ticks.setImageResource(R.drawable.ic_baseline_done_all_24);
            }
        }else{
            holder.ticks.setImageResource(R.drawable.ic_baseline_done_24);
            holder.ticks.setColorFilter(R.color.colorPrimary);
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView chatPic;
        TextView showMessages, timestamp;
        ImageView ticks;
        ImageView msgImage, imagePick;
        VoicePlayerView voicePlayerView;
        Button locationBTN;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timestamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.seenTicks);
            msgImage = itemView.findViewById(R.id.msg_img);
            imagePick = itemView.findViewById(R.id.showImg);
            voicePlayerView = itemView.findViewById(R.id.showAudioMsg);
            locationBTN = itemView.findViewById(R.id.viewLocation);

        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(chats.get(position).getSender().equals(firebaseUser.getUid())){
            if(chats.get(position).getMsg_type().equals("audio")){
                return MSG_TYPE_AUDIO_RIGHT;
            }else if (chats.get(position).getMsg_type().equals("location")){
                return MSG_TYPE_LOCATION_RIGHT;
            }else{
                return MSG_TYPE_RIGHT;
            }
        }else {
            if(chats.get(position).getMsg_type().equals("audio")){
                return MSG_TYPE_AUDIO_LEFT;
            }else if (chats.get(position).getMsg_type().equals("location")){
                return MSG_TYPE_LOCATION_LEFT;
            }else{
                return MSG_TYPE_LEFT;
            }
        }

        /*if(chats.get(position).getSender().equals(firebaseUser.getUid())){
            if(chats.get(position).getMsg_type().equals("audio")){
                return MSG_TYPE_AUDIO_RIGHT;
            }else {
                return MSG_TYPE_RIGHT;
            }
        }else {
            if(chats.get(position).getMsg_type().equals("audio")){
                return MSG_TYPE_AUDIO_LEFT;
            }else {
                return MSG_TYPE_LEFT;
            }
        }*/

        /*if(chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }*/
    }
}
