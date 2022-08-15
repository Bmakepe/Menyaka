package com.example.menyaka.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.ChatlistAdapter;
import com.example.menyaka.Adapters.UserAdapter;
import com.example.menyaka.AffliatesActivity;
import com.example.menyaka.Models.Chat;
import com.example.menyaka.Models.ChatList;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.example.menyaka.ViewProfileActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListFragment extends Fragment {

    private RecyclerView chatlistRecycler;
    private List<ChatList> chatlist;

    private DatabaseReference chatListReference;
    private FirebaseUser currentUser;
    private ChatlistAdapter chatlistAdapter;

    public ChatListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chatlist, container,false);

        chatlistRecycler = view.findViewById(R.id.chatlist_recycler);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatListReference = FirebaseDatabase.getInstance().getReference("MessageList")
                .child(currentUser.getUid());

        getChatList();

        chatlistRecycler.setHasFixedSize(true);
        chatlistRecycler.hasFixedSize();
        chatlistRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatlistAdapter = new ChatlistAdapter(getContext(), chatlist);
        chatlistRecycler.setAdapter(chatlistAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    ChatList friendChats = chatlist.get(viewHolder.getAdapterPosition());

                    int position = viewHolder.getAdapterPosition();
                    chatlist.remove(position);
                    chatlistAdapter.notifyItemRemoved(position);

                    Snackbar.make(chatlistRecycler, "Restore Delete", Snackbar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chatlist.add(position, friendChats);
                            chatlistAdapter.notifyItemInserted(position);
                        }
                    }).show();
                } else {
                    Toast.makeText(getContext(), "Unknown swipe selection", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(chatlistRecycler);
        
        view.findViewById(R.id.showContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AffliatesActivity.class));
            }
        });

        return view;
    }

    private void getChatList() {

        chatlist = new ArrayList<>();
        chatListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlist.clear();
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        ChatList chats = ds.getValue(ChatList.class);
                        chatlist.add(chats);
                    }

                    //set Last message
                    for(int i = 0; i < chatlist.size(); i++){
                        lastMessage(chatlist.get(i).getId());
                    }

                }else{
                    //chatListLoader.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "You have no active chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                String lastTimeStamp = "default";

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if(chat == null){
                        continue;
                    }

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();

                    if(sender == null || receiver == null){
                        continue;
                    }

                    if(chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        theLastMessage = chat.getMessage();
                        lastTimeStamp = chat.getTimestamp();
                        /*try{
                            if(chat.getMsg_type().equals("image")){
                                theLastMessage = "Sent a photo";
                            }else{
                                theLastMessage = chat.getMessage();
                            }
                            lastTimeStamp = chat.getTimestamp();
                        }catch (NullPointerException ignored){}*/
                    }
                }

                chatlistAdapter.setLastMessageMap(userId, theLastMessage);
                chatlistAdapter.setLastTimeStamp(userId, lastTimeStamp);

                chatlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
