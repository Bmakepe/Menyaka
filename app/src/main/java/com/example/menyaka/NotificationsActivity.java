package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.NotificationAdapter;
import com.example.menyaka.Models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notiRecycler;
    NotificationAdapter notificationAdapter;
    ArrayList<Notification> notificationList;

    TextView header;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        notiRecycler = findViewById(R.id.recycler_View);
        header = findViewById(R.id.recyclerHeader);

        header.setText("Notifications");

        notificationList = new ArrayList<>();

        notiRecycler.hasFixedSize();
        notiRecycler.setLayoutManager(new LinearLayoutManager(NotificationsActivity.this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();

                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notification notiModel = snapshot.getValue(Notification.class);

                        if (!notiModel.getUserid().equals(firebaseUser.getUid()))
                            notificationList.add(notiModel);
                    }

                    notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notificationList);
                    notiRecycler.setAdapter(notificationAdapter);

                    Collections.reverse(notificationList);
                    notificationAdapter.notifyDataSetChanged();

                }else {
                    Toast.makeText(NotificationsActivity.this, "You have no notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}