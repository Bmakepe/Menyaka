package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.CartAdapter;
import com.example.menyaka.Adapters.StoreMessagesAdapter;
import com.example.menyaka.Common.MediaAdapter;
import com.example.menyaka.Models.Chat;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.MyStore.ShopProductsFragment;
import com.example.menyaka.fragments.ExploreTabFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    ImageView close, shopImg, cartBtn, mediaRecycler;
    ImageView msg_close,msg_send,audio_rec,gallery;
    TextView shopName, myShopPoints, retailStoreName;
    FirebaseUser firebaseUser;
    Button messageBtn;
    Dialog messageDialog, cartDialog;
    DatabaseReference reference;
    String enableBooking, Uid, shopId,storeImage;
    EditText shop_msg;

    private StorageReference storageReference;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MediaRecorder mediaRecorder;
    private String fileName = null;
    private static final String LOG_TAG = "Record_log";

    StoreMessagesAdapter messageAdapter;
    MediaAdapter mediaAdapter;
    List<Chat> chatList, mediaList;

    //for displaying products in the cart for checkout
    ArrayList<Product> cartProducts;
    ArrayList<String> cartList;
    CartAdapter cartAdapter;
    RecyclerView cartRecycler;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;

    private String checker = "",myUri = "";
    Uri fileUri;
    private String [] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        close = findViewById(R.id.backShop);
        shopImg = findViewById(R.id.storeImage);
        retailStoreName = findViewById(R.id.retailStoreName);
        myShopPoints = findViewById(R.id.showPoints);
        messageBtn = findViewById(R.id.msg_btn);
        cartBtn = findViewById(R.id.shop_cart);
        mediaRecycler = findViewById(R.id.mediaRecycler);
        //user_image = findViewById(R.id.image_profile);

        Uid = FirebaseAuth.getInstance().getUid();
        progressBar = new ProgressBar(this);

        iniMessageDialog();
        iniCartDialog();

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageDialog.show();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartDialog.show();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        shopId = getIntent().getExtras().getString("storeId");

        DatabaseReference retailRef = FirebaseDatabase.getInstance().getReference("Retails").child(shopId);
        retailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Retail retail = snapshot.getValue(Retail.class);

                retailStoreName.setText(retail.getRetailName());

                try{
                    Picasso.get().load(retail.getImageUrl()).into(shopImg);
                }catch (NullPointerException ignored){}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //String enableBooking = getIntent().getExtras().getString("Booking");

        //Toast.makeText(this, "your store is " + shopId, Toast.LENGTH_SHORT).show();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdpter viewPagerAdpter = new ViewPagerAdpter(getSupportFragmentManager());

        viewPagerAdpter.addFragment(new ShopProductsFragment(), "Products");

        viewPagerAdpter.addFragment(new ExploreTabFragment(), "Reviews");

        /*if(enableBooking.equals("on")){
            viewPagerAdpter.addFragment(new ShopChatsFragment(), "Bookings");
        }*/

        viewPager.setAdapter(viewPagerAdpter);
        tabLayout.setupWithViewPager(viewPager);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void iniMessageDialog() {
        messageDialog = new Dialog(ShopActivity.this);
        messageDialog.setContentView(R.layout.shop_chat);
        messageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        messageDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        messageDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        mediaList = new ArrayList<>();

        recyclerView = messageDialog.findViewById(R.id.shopChatsRecycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();

        msg_close = messageDialog.findViewById(R.id.messBackBTN);
        gallery = messageDialog.findViewById(R.id.gallery_btn);
        //pickedImg = messageDialog.findViewById(R.id.showImg);
        shopName = messageDialog.findViewById(R.id.shop_name_msg);
        msg_send = messageDialog.findViewById(R.id.msg_post);
        audio_rec = messageDialog.findViewById(R.id.rec_audio);
        shop_msg = messageDialog.findViewById(R.id.add_message);

        final String storeName = getIntent().getExtras().getString("myStoreName");
        shopName.setText(storeName);

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/Menyaka_audio.3gp";

        audio_rec.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    startRecording();
                    Toast.makeText(getApplicationContext(), "Recording Started...", Toast.LENGTH_SHORT).show();
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    stopRecording();
                    Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

        msg_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageDialog.dismiss();
            }
        });

        shop_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>0){
                    audio_rec.setVisibility(View.GONE);
                    msg_send.setVisibility(View.VISIBLE);
                }else {
                    audio_rec.setVisibility(View.VISIBLE);
                    msg_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(Uid) && chat.getSender().equals(shopId) ||
                            chat.getReceiver().equals(shopId) && chat.getSender().equals(Uid)){
                        chatList.add(chat);
                    }

                    messageAdapter = new StoreMessagesAdapter(getApplicationContext(), chatList, storeImage);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //readMessages(Uid, shopId, storeImage);

        //handle Gallery button
        gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //check runtime Manifest.permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        //permission not granted, request it.
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permission
                        requestPermissions(permissions, PERMISSION_CODE);
                    }else {
                        //permission already granted
                        pickImageFromGallery();
                    }
                }else {
                    //System os less than marshmallow
                    pickImageFromGallery();
                }
            }
        });


        msg_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = shop_msg.getText().toString();
                if (!myUri.equals(null)){
                    uploadImageMsg(myUri, msg);
                }else if (!msg.equals("")){
                    sendMessage(firebaseUser.getUid(), shopId, msg);
                }else {
                    Toast.makeText(getApplicationContext(), "Can not send empty text", Toast.LENGTH_SHORT).show();
                }
                shop_msg.setText("");
            }
        });

    }

    private void iniCartDialog() {
        cartDialog = new Dialog(ShopActivity.this);
        cartDialog.setContentView(R.layout.cart_layout);
        cartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        cartDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        cartDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        ImageView cart_close;

        cart_close = cartDialog.findViewById(R.id.cartBackBTN);
        cartRecycler = cartDialog.findViewById(R.id.cart_items_Recycler);
        cartRecycler.setHasFixedSize(true);
        cartRecycler.setLayoutManager(new LinearLayoutManager(ShopActivity.this));

        cartList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CartItems");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    cartList.add(ds.getKey());
                }
                getCartProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cart_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartDialog.dismiss();
            }
        });


    }

    private void getCartProducts() {
        cartProducts = new ArrayList<>();

        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartProducts.clear();

                for(DataSnapshot ds : snapshot.getChildren()){

                    Product product = ds.getValue(Product.class);

                    for(String ID : cartList){
                        if(product.getProductId().equals(ID))
                            cartProducts.add(product);
                    }
                }

                cartAdapter = new CartAdapter(ShopActivity.this, cartProducts);
                cartRecycler.setAdapter(cartAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    int totalUploadMedia = 0;
    ArrayList<String> mediaIdList = new ArrayList<>();
    private void sendMessage(final String sender, final String receiver, String message){


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages");
        reference.keepSynced(true);
        String messageId = reference.push().getKey();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("msg_type", "text");
        hashMap.put("isSeen", false);
        hashMap.put("attended", false);
        hashMap.put("timestamp", timestamp);

        if (!mediaUriList.isEmpty()){
            for (String mediaUri : mediaUriList){
                String mediaId = reference.child("media").push().getKey();
                mediaIdList.add(mediaId);
                StorageReference mediaReference = storageReference.child("MediaMessages").child(firebaseUser.getUid());

                UploadTask uploadTask = mediaReference.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mediaReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                hashMap.put("/media/"+ mediaIdList.get(totalUploadMedia) + "/", uri.toString());

                                totalUploadMedia++;
                                if (totalUploadMedia == mediaUriList.size()){
                                    uplaodMediaMsg(reference.child(messageId),hashMap);
                                }
                            }
                        });
                    }
                });
            }
        }

        reference.push().setValue(hashMap);

        final DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("MessageList")
                .child(sender).child(receiver);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    senderReference.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference("MessageList")
                .child(receiver)
                .child(sender);
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    receiverReference.child("id").setValue(sender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uplaodMediaMsg(DatabaseReference newMessageDb, HashMap newMsgMap){
        newMessageDb.updateChildren(newMsgMap);
        shop_msg.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        shop_msg.setHint("Write your message");
        mediaAdapter.notifyDataSetChanged();
    }

    private void startRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        uploadVoiceMsg();
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImageMsg(String image, String message) {

        progressBar.setMax(100);
        progressBar.setProgress(10);
        progressBar.setVisibility(View.VISIBLE);

        StorageReference imageReference = storageReference.child("ImageMessages").child(System.currentTimeMillis()
                + ".jpg");
        Uri uri = Uri.fromFile(new File(image));

        imageReference.putFile(uri).continueWithTask(task -> {
            if(!task.isSuccessful()){

            }
            return imageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downloadUri = task.getResult();
                String imageUrl = downloadUri.toString();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.keepSynced(true);

                String timestamp = String.valueOf(System.currentTimeMillis());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Uid);
                hashMap.put("receiver", shopId);
                hashMap.put("message", message);
                hashMap.put("media", imageUrl);
                hashMap.put("msg_type", "audio");
                hashMap.put("isSeen", false);
                hashMap.put("attended", false);
                hashMap.put("timestamp", timestamp);

                reference.child("Messages").push().setValue(hashMap);

                //String msg = message;

                final DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("MessageList")
                        .child(Uid).child(shopId);
                senderReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            senderReference.child("id").setValue(Uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference("MessageList")
                        .child(shopId)
                        .child(Uid);
                receiverReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            receiverReference.child("id").setValue(shopId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(ShopActivity.this,"", Toast.LENGTH_SHORT).show());
    }

    private void uploadVoiceMsg() {

        progressBar.setMax(100);
        progressBar.setProgress(10);
        progressBar.setVisibility(View.VISIBLE);

        StorageReference voiceReference = storageReference.child("VoiceMessages").child(System.currentTimeMillis()
                + ".3gp");
        Uri uri = Uri.fromFile(new File(fileName));

        voiceReference.putFile(uri).continueWithTask(task -> {
            if(!task.isSuccessful()){

            }
            return voiceReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downloadUri = task.getResult();
                String audioUrl = downloadUri.toString();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.keepSynced(true);

                String timestamp = String.valueOf(System.currentTimeMillis());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Uid);
                hashMap.put("receiver", shopId);
                hashMap.put("message", audioUrl);
                hashMap.put("msg_type", "audio");
                hashMap.put("isSeen", false);
                hashMap.put("attended", false);
                hashMap.put("timestamp", timestamp);

                reference.child("Messages").push().setValue(hashMap);

                //String msg = message;

                final DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("MessageList")
                        .child(Uid).child(shopId);
                senderReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            senderReference.child("id").setValue(Uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference("MessageList")
                        .child(shopId)
                        .child(Uid);
                receiverReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            receiverReference.child("id").setValue(shopId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(ShopActivity.this,"", Toast.LENGTH_SHORT).show());
    }

    private void readMessages(final String myUserId, final String userId, final String imageUrl){
        chatList = new ArrayList<>();
        Toast.makeText(getApplicationContext(), "Read Messages Function", Toast.LENGTH_SHORT).show();

        reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(myUserId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myUserId)){
                        chatList.add(chat);
                    }

                    messageAdapter = new StoreMessagesAdapter(getApplicationContext(), chatList, imageUrl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList = new ArrayList<>();

    private void pickImageFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        shop_msg.setHint("add media caption...");
        audio_rec.setVisibility(View.GONE);
        msg_send.setVisibility(View.VISIBLE);
        startActivityForResult(Intent.createChooser(intent,"Select Picture(s)"),IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CODE: {
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else {
                    Toast.makeText(this, "Permission denied..", Toast.LENGTH_SHORT).show();
                }
            }
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null) {

            fileUri = data.getData();
            myUri = data.getData().toString();
            //mediaRecycler.setImageURI(data.getData());
            mediaRecycler.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Image Selected Successfully", Toast.LENGTH_SHORT).show();
            /*if (!checker.equals("image")){
                if (data.getClipData() == null){
                    mediaUriList.add(data.getData().toString());
                }else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++){
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                mediaAdapter.notifyDataSetChanged();

            }else if (!checker.equals("pdf")){
                Toast.makeText(this, "Pdf file Selected Successfully", Toast.LENGTH_SHORT).show();
            }else if (!checker.equals("docx")){
                Toast.makeText(this, "Word file Selected Successfully", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Nothing Selected, try again!!!", Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    /*private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(user_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    class ViewPagerAdpter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> title;


        ViewPagerAdpter(FragmentManager fragmentManager){
            super(fragmentManager);

            this.fragments = new ArrayList<>();
            this.title = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        public int getCount(){
            return fragments.size();
        }

        public  void addFragment(Fragment fragment, String titles){
            fragments.add(fragment);
            title.add(titles);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return title.get(position);
        }
    }

}
