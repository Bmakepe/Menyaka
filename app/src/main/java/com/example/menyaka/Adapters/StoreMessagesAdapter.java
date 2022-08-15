package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.MessageActivity;
import com.example.menyaka.Models.Chat;
import com.example.menyaka.R;
import com.example.menyaka.ShopActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class StoreMessagesAdapter extends RecyclerView.Adapter<StoreMessagesAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_AUDIO_LEFT = 2;
    public static final int MSG_TYPE_AUDIO_RIGHT = 3;

    private final Context context;
    private final List<Chat> chats;
    private final String imageUrl;
    private String audioUrl;

    private FirebaseUser firebaseUser;

    public StoreMessagesAdapter(Context context, List<Chat> chats, String imageUrl) {
        this.context = context;
        this.chats = chats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.msg_item_right, parent, false);
            return new StoreMessagesAdapter.ViewHolder(view);
        }else if(viewType == MSG_TYPE_AUDIO_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_right, parent, false);
            return new StoreMessagesAdapter.ViewHolder(view);
        }else if(viewType == MSG_TYPE_AUDIO_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_left, parent, false);
            return new StoreMessagesAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.msg_item_left, parent, false);
            return new StoreMessagesAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StoreMessagesAdapter.ViewHolder holder, int position) {

        Chat chat = chats.get(position);
        String msgType = "audio";
        if (msgType.equals(chat.getMsg_type())){
            holder.voicePlayerView.setAudio(chat.getMessage());
        }else {

            holder.showMessages.setText(chat.getMessage());
        }
        String getTimestamp = chat.getTimestamp();
        //String userId = chat.getSender();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        try {
            calendar.setTimeInMillis(Long.parseLong(getTimestamp));
            String presentTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
            holder.timestamp.setText(presentTime);
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        Picasso.get().load(imageUrl).into(holder.chatPic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ShopActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView chatPic;
        ImageView msgImage, imagePick;
        TextView showMessages, timestamp;
        VoicePlayerView voicePlayerView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.msg_pic);
            msgImage = itemView.findViewById(R.id.msg_img);
            showMessages = itemView.findViewById(R.id.showMsg);
            timestamp = itemView.findViewById(R.id.msg_time);
            imagePick = itemView.findViewById(R.id.showImg);
            voicePlayerView = itemView.findViewById(R.id.showAudioMsg);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chats.get(position).getSender().equals(firebaseUser.getUid())){
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
        }
    }
}
