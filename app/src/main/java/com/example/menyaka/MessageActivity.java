package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordPermissionHandler;
import com.devlomi.record_view.RecordView;
import com.example.menyaka.Adapters.MessageAdapter;
import com.example.menyaka.Models.Chat;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity implements LocationListener {

    CircleImageView receiverPicture;
    TextView receiverName;
    ImageView menuBTN;
    EditText messageED;
    ImageButton sendBTN;

    String receiverID, hisImage, audioPath;

    DatabaseReference receiverRef, messageRef;
    StorageReference audioReference;
    FirebaseUser user;

    String contactType, fileName, msg;

    List<Chat> chatList;
    MessageAdapter messageAdapter;

    RecyclerView chatRecycler;

    public static final int LOCATION_REQUEST_CODE = 100;
    public static final int AUDIO_REQUEST_CODE = 200;
    public static final int SELECT_IMAGE = 300;

    private String[] locationPermission;
    private String[] audioPermission;

    private LocationManager locationManager;
    double latitude, longitude;
    private int FASTEST_INTERVAL =  1000;
    private Location currentLocation = null;
    private long locationUpdatedAt = Long.MIN_VALUE;

    //for voice notes
    RecordView recordView;
    RecordButton recordButton;
    MediaRecorder mediaRecorder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        receiverPicture = findViewById(R.id.messReceiverProPic);
        receiverName = findViewById(R.id.messReceiverName);
        menuBTN = findViewById(R.id.msgMenuBTN);
        messageED = findViewById(R.id.add_message);
        sendBTN = findViewById(R.id.msg_post);
        chatRecycler = findViewById(R.id.chatsRecycler);

        recordButton = findViewById(R.id.recordButton);
        recordView = findViewById(R.id.recordView);

        //recordButton.setListenForRecord(false);
        recordView.setCancelBounds(8);
        recordView.setSlideToCancelText("Slide to cancel");
        recordButton.setRecordView(recordView);
        recordButton.setListenForRecord(false);

        Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiverID");

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/Menyaka_audio.3gp";

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        audioPermission = new String[]{Manifest.permission.RECORD_AUDIO};

        audioReference = FirebaseStorage.getInstance().getReference();
        receiverRef = FirebaseDatabase.getInstance().getReference("Users");
        user = FirebaseAuth.getInstance().getCurrentUser();

        getReceiverDetails();
        readMessages();

        chatRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setHasFixedSize(true);

        messageED.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length()>0){
                    recordButton.setVisibility(View.GONE);
                    sendBTN.setVisibility(View.VISIBLE);
                }else {
                    recordButton.setVisibility(View.VISIBLE);
                    sendBTN.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAudioPermission()){
                    recordButton.setListenForRecord(true);
                }else{
                    requestAudioPermission();
                }
            }
        });

        recordView.setOnRecordListener(new OnRecordListener(){
            @Override
            public void onStart() {
                //Toast.makeText(MessageActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onStart");
                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                messageED.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {

                try {
                    //on swipe to cancel
                    Log.d("RecordView", "onCancel");

                    mediaRecorder.reset();
                    mediaRecorder.release();

                    File file = new File(audioPath);

                    if (file.exists()) {
                        file.delete();
                    }
                }catch (NullPointerException ignored){}

                recordView.setVisibility(View.GONE);
                messageED.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {

                try {
                    //stop recording
                    Log.d("RecordView", "onFinish");

                    mediaRecorder.stop();
                    mediaRecorder.release();
                }catch (IllegalArgumentException ignored){}

                recordView.setVisibility(View.GONE);
                messageED.setVisibility(View.VISIBLE);

                sendMessage("audio");

                //sendRecordingMessage(audioPath);
            }

            @Override
            public void onLessThanSecond() {
                //when the record time is less than 1 second
                Log.d("RecordView" , "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);

                if (file.exists()){
                    file.delete();
                }

                recordView.setVisibility(View.GONE);
                messageED.setVisibility(View.VISIBLE);
            }
        });

        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessageActivity.this, "Record Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basked Animation Finished");
            }
        });

        recordView.setRecordPermissionHandler(new RecordPermissionHandler() {
            @Override
            public boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return true;
                }

                boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                if (recordPermissionAvailable) {
                    return true;
                }


                ActivityCompat.
                        requestPermissions(MessageActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                0);

                return false;

            }
        });

        receiverName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (contactType.equals("person")){
                    intent = new Intent(MessageActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userID", receiverID);
                }else if (contactType.equals("retail")){
                    intent = new Intent(MessageActivity.this, ShopDetailsActivity.class);
                    intent.putExtra("storeId", receiverID);
                }else{
                    intent = new Intent(MessageActivity.this, AgentProfileActivity.class);
                    intent.putExtra("agentID", receiverID);
                }
                startActivity(intent);
            }
        });

        findViewById(R.id.messBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        menuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(MessageActivity.this, menuBTN, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");
                popupMenu.getMenu().add(Menu.NONE, 1,0,"Send Alert");
                popupMenu.getMenu().add(Menu.NONE, 2,0,"Send Files");
                popupMenu.getMenu().add(Menu.NONE, 3,0,"Send Location");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == 0){

                            Intent intent;
                            if (contactType.equals("person")){
                                intent = new Intent(MessageActivity.this, ViewProfileActivity.class);
                                intent.putExtra("userID", receiverID);
                            }else if (contactType.equals("retail")){
                                intent = new Intent(MessageActivity.this, ShopDetailsActivity.class);
                                intent.putExtra("storeId", receiverID);
                            }else{
                                intent = new Intent(MessageActivity.this, AgentProfileActivity.class);
                                intent.putExtra("agentID", receiverID);
                            }
                            startActivity(intent);

                        }else if(id == 1){

                            sendMessage("alert");

                        }else if(id == 2){
                            Toast.makeText(MessageActivity.this, "Send Media to your friend", Toast.LENGTH_SHORT).show();
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_GET_CONTENT);
                            shareIntent.setType("*/*");
                            startActivity(shareIntent);
                        }else if (id == 3){
                            if (checkLocationPermission()){
                                detectLocation(); 
                            }else{
                                requestLocationPermission(); 
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msg = messageED.getText().toString().trim();

                if(TextUtils.isEmpty(msg))
                    messageED.setError("Can not send empty message");
                else
                    sendMessage("noAudio");
            }
        });

        findViewById(R.id.gallery_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(MessageActivity.this, findViewById(R.id.gallery_btn), Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0,0,"Take Picture");
                popupMenu.getMenu().add(Menu.NONE, 1,0,"Pick From Gallery");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == 0){
                            startActivity(new Intent(MessageActivity.this, OtherCameraActivity.class));
                        }else if(id == 1){Intent intent = new Intent();
                            Toast.makeText(MessageActivity.this, "The Gallery will open from here", Toast.LENGTH_SHORT).show();

                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        /*contactImg = findViewById(R.id.profileCIV);
        contactName = findViewById(R.id.hisNameTV);
        chatBack = findViewById(R.id.chatBackBTN);
        recyclerView = findViewById(R.id.chatRecyclerView);
        chatMsg = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBTN);

        intent = getIntent();
        final String senderId = intent.getStringExtra("receiverID");

        //iniProfileDialog(senderId, senderName);

        contactList = new ArrayList<>();
        //contacts = new ContactList(contactList, MessageActivity.class);

        //matchContactDetails(senderId);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert senderId != null;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(senderId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("username").getValue();
                displayImage = "" + snapshot.child("imageUrl").getValue();

                contactName.setText(senderName);

                Picasso.get().load(displayImage).into(contactImg);

                readMessages(firebaseUser.getUid(), senderId, displayImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //try{
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msg = chatMsg.getText().toString();
                    if (!msg.equals("")){
                        sendMessage(firebaseUser.getUid(), senderId, msg);
                    }else {
                        Toast.makeText(MessageActivity.this, "Can not send empty text", Toast.LENGTH_SHORT).show();
                    }
                    chatMsg.setText("");
                }
            });
        //}catch (NullPointerException e){
         //   Toast.makeText(MessageActivity.this, "Message Button has a problem", Toast.LENGTH_SHORT).show();
        //}

        findViewById(R.id.chatBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.chatDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileDialog.show();
            }
        });*/
    }

    private void sendMessage(String choice){

        if (choice.equals("audio")) {
            StorageReference voiceReference = audioReference.child("VoiceMessages").child(System.currentTimeMillis()
                    + ".3gp");
            Uri uri = Uri.fromFile(new File(audioPath));

            voiceReference.putFile(uri).continueWithTask(task -> {
                if(!task.isSuccessful()){

                }
                return voiceReference.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String audioUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.keepSynced(true);

                        String timestamp = String.valueOf(System.currentTimeMillis());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", user.getUid());
                        hashMap.put("receiver", receiverID);
                        hashMap.put("msg_type", "audio");
                        hashMap.put("message", audioUrl);
                        hashMap.put("isSeen", false);
                        hashMap.put("timestamp", timestamp);

                        reference.child("Messages").push().setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MessageActivity.this, "Voice Note Send Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this, "Could not send voice note", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else{

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.keepSynced(true);

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", user.getUid());
            hashMap.put("receiver", receiverID);

            switch (choice) {
                case "location":

                    hashMap.put("message", "Location");
                    hashMap.put("msg_type", "location");
                    hashMap.put("latitude", latitude);
                    hashMap.put("longitude", longitude);

                    break;
                case "alert":

                    hashMap.put("message", "Haibo!!!");
                    hashMap.put("msg_type", "text");

                    break;
                case "noAudio":

                    hashMap.put("message", msg);
                    hashMap.put("msg_type", "text");

                    break;
            }

            hashMap.put("isSeen", false);
            hashMap.put("timestamp", timestamp);

            reference.child("Messages").push().setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            messageED.setText(null);
                            messageED.setHint("Write your message");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //String msg = message;
        final DatabaseReference senderReference = FirebaseDatabase.getInstance()
                .getReference("MessageList")
                .child(user.getUid())
                .child(receiverID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    senderReference.child("id").setValue(receiverID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance()
                .getReference("MessageList")
                .child(receiverID)
                .child(user.getUid());
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    receiverReference.child("id").setValue(user.getUid());
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, audioPermission, AUDIO_REQUEST_CODE);
    }

    private void setUpRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Menyaka/Media/Recording");

        if(!file.exists()){
            file.mkdirs();
        }

        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void getReceiverDetails() {

        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if(user.getId().equals(receiverID)){

                        receiverName.setText(user.getUsername());
                        hisImage = user.getImageUrl();

                        try {
                            Picasso.get().load(user.getImageUrl()).into(receiverPicture);
                        }catch (NullPointerException ignored){}

                        contactType = "person";

                    }else{
                        final DatabaseReference retailRef = FirebaseDatabase.getInstance().getReference("Retails");
                        retailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot data : snapshot.getChildren()){
                                    Retail retail = data.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(receiverID)){

                                        receiverName.setText(retail.getRetailName());
                                        hisImage = retail.getImageUrl();

                                        try {
                                            Picasso.get().load(retail.getImageUrl()).into(receiverPicture);
                                        }catch (NullPointerException ignored){}

                                        contactType = "retail";

                                    }else{
                                        final DatabaseReference agentReference = FirebaseDatabase.getInstance().getReference("ShippingAgents");
                                        agentReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    ShippingAgents agents = ds.getValue(ShippingAgents.class);

                                                    if (agents.getCompanyID().equals(receiverID)){
                                                        receiverName.setText(agents.getShippingName());

                                                        hisImage = agents.getCompanyLogo();

                                                        try{
                                                            Picasso.get().load(agents.getCompanyLogo()).into(receiverPicture);
                                                        }catch (NullPointerException ignored){}

                                                        contactType = "agent";
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
                                Toast.makeText(MessageActivity.this, "Could not retrieve receiver ID", Toast.LENGTH_SHORT).show();
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

    private void readMessages(){
        chatList = new ArrayList<>();

        messageRef = FirebaseDatabase.getInstance().getReference("Messages");
        messageRef.keepSynced(true);
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(user.getUid()) && chat.getSender().equals(receiverID) ||
                            chat.getReceiver().equals(receiverID) && chat.getSender().equals(user.getUid())){
                        chatList.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, chatList, hisImage);
                    messageAdapter.notifyDataSetChanged();
                    chatRecycler.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        boolean updateLocationReport = false;
        if(currentLocation == null){
            currentLocation = location;
            locationUpdatedAt = System.currentTimeMillis();
            updateLocationReport = true;
        } else {
            long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt);
            if (secondsElapsed >= TimeUnit.MILLISECONDS.toSeconds(FASTEST_INTERVAL)){
                // check location accuracy here
                currentLocation = location;
                locationUpdatedAt = System.currentTimeMillis();
                updateLocationReport = true;
            }
        }

        if(updateLocationReport){
            //  send your location to server

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            sendMessage("location");

        }

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps/location disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        detectLocation();
                    }else{
                        Toast.makeText(this, "You need location permission enabled", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case SELECT_IMAGE:


        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}