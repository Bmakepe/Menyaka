package com.example.menyaka.Share;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.menyaka.Adapters.TagStoreAdapter;
import com.example.menyaka.Adapters.TaggingAdapter;
import com.example.menyaka.Models.Contact;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.MomentActivity;
import com.example.menyaka.R;
import com.example.menyaka.Utils.VideoProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FullScreenImageActivity extends AppCompatActivity {

    ImageView backBTN, menuBTN, fullScreenPic;
    TextView header;

    DatabaseReference storeRef, userRef, momentRef;

    String picID, displayedImage;

    /*Bitmap rotateBitmap;
    RecyclerView recyclerView;
    List<Contact> contactList;
    List<Retail> retailList;
    List<String> userId;
    TaggingAdapter taggingAdapter;
    TagStoreAdapter tagStoreAdapter;

    EditText caption;
    TextView next, personTag, storeTag, postAudience;
    String Uid;
    ImageView back, imageView, exit;

    Dialog taggingDialog, storeDialog;*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_show_image);
        setContentView(R.layout.full_screen_pic_display);

        backBTN = findViewById(R.id.fullScreenBackBTN);
        menuBTN = findViewById(R.id.fullScreenMenu);
        fullScreenPic = findViewById(R.id.fullScreenPicImage);
        header = findViewById(R.id.fullScreenTitle);

        Intent intent = getIntent();
        picID = intent.getStringExtra("pictureID");

        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        momentRef = FirebaseDatabase.getInstance().getReference("Moments");

        getMedia();

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        menuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FullScreenImageActivity.this, "Menu Button", Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(FullScreenImageActivity.this, menuBTN, Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Save Image");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        if (id == 0){
                            Toast.makeText(FullScreenImageActivity.this, "you will be able to save this picture", Toast.LENGTH_SHORT).show();
                            downloadFile();

                        }else{
                            Toast.makeText(FullScreenImageActivity.this, "Unknown selection made", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        /*Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        byte[] bytes = bundle.getByteArray("capture");

        if(bytes!=null){
            imageView = findViewById(R.id.capturedImage);

            Bitmap decodeBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            rotateBitmap = rotate(decodeBitmap);

            imageView.setImageBitmap(rotateBitmap);
        }

        next = findViewById(R.id.photo_next);
        exit = findViewById(R.id.cancel_menu);
        back = findViewById(R.id.backToPhoto);
        caption = findViewById(R.id.photo_caption);
        personTag = findViewById(R.id.tag_people);
        postAudience = findViewById(R.id.audience);
        storeTag = findViewById(R.id.tag_store);

        Uid = FirebaseAuth.getInstance().getUid();
        iniMessageDialog();
        iniStoreDialog();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToMenyaka();
            }
        });

        personTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*getSupportFragmentManager().beginTransaction().replace(R.id.show_image_header,
                        new MessagesFragment()).commit();
                taggingDialog.show();
            }
        });

        storeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeDialog.show();
            }
        });

        postAudience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popUpView = layoutInflater.inflate(R.layout.privacy_menu, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;

                final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(view, Gravity.CENTER_VERTICAL, 0, 0);

                /*exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                        return;
                    }
                });

                popUpView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        popupWindow.dismiss();
                        return false;
                    }
                });
            }
        });*/
    }

    private void getMedia() {

        momentRef.child(picID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Moment moment = snapshot.getValue(Moment.class);
                    assert moment != null;

                    if ("imagePost".equals(moment.getPostType())) {
                        fullScreenPic.setVisibility(View.VISIBLE);
                        getMomentPic(moment);
                    } else {
                        Toast.makeText(FullScreenImageActivity.this, "Could not retrieve the media", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    userRef.child(picID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                User user = snapshot.getValue(User.class);
                                assert user != null;

                                fullScreenPic.setVisibility(View.VISIBLE);
                                getUserProPic(user);

                            }else{
                                storeRef.child(picID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){

                                            Retail retail = snapshot.getValue(Retail.class);
                                            assert retail != null;

                                            fullScreenPic.setVisibility(View.VISIBLE);
                                            getRetailIcon(retail);

                                        }else{
                                            Toast.makeText(FullScreenImageActivity.this, "Picture does not exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRetailIcon(Retail retail) {
        if (retail.getRetail_id().equals(picID)) {
            header.setText(retail.getRetailName());
            try {
                Picasso.get().load(retail.getImageUrl()).into(fullScreenPic);
                findViewById(R.id.fullScreenLoader).setVisibility(View.GONE);
            } catch (NullPointerException ignored) {
            }
        }
    }

    private void getUserProPic(User user) {
        if (user.getId().equals(picID)) {
            header.setText(user.getUsername());
            displayedImage = user.getImageUrl();
            try {
                Picasso.get().load(user.getImageUrl()).into(fullScreenPic);
                findViewById(R.id.fullScreenLoader).setVisibility(View.GONE);
            } catch (NullPointerException ignored) {}
        }
    }

    private void getMomentPic(Moment moment) {
        if (moment.getMomentId().equals(picID)) {
            displayedImage = moment.getImageUrl();
            try {
                Picasso.get().load(moment.getImageUrl()).into(fullScreenPic);
                findViewById(R.id.fullScreenLoader).setVisibility(View.GONE);
            } catch (NullPointerException ignored) {}

        }
    }

    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(displayedImage);
        final String timestamp = String.valueOf(System.currentTimeMillis());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Downloading file");
        pd.setMessage("Downloading, Please Wait...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();

        final File rootPath = new File(Environment.getExternalStorageDirectory(), "MENYAKA");

        if (!rootPath.exists()){
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, "MENYAKA_" + timestamp +".jpg");

        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ", ";local item created created " + localFile.toString());

                /*if(!isVisible()){
                    return;
                }*/

                if (localFile.canRead()){
                    pd.dismiss();
                }

                Toast.makeText(FullScreenImageActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void saveToMenyaka() {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Menyaka")
                .child(Uid);

        final String key = reference.push().getKey();

        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("memories").child(key);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotateBitmap.compress(Bitmap.CompressFormat.JPEG,20, baos);
        byte[] dataToUpload = baos.toByteArray();

        UploadTask uploadTask = filepath.putBytes(dataToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri imageUrl = taskSnapshot.getUploadSessionUri();

                Long currentTimestamp = System.currentTimeMillis();
                Long endTimestamp = currentTimestamp + (24*60*60*1000);

                Map<String, Object> mapToUpload = new HashMap<>();
                assert imageUrl != null;
                mapToUpload.put("imageUrl", imageUrl.toString());
                mapToUpload.put("timestamp_start", currentTimestamp);
                mapToUpload.put("timestamp_end", endTimestamp);
                mapToUpload.put("moment_desc", caption.getText().toString());

                reference.child(key).setValue(mapToUpload);

                finish();
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                return;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void iniMessageDialog() {
        taggingDialog = new Dialog(FullScreenImageActivity.this);
        taggingDialog.setContentView(R.layout.followers);
        Objects.requireNonNull(taggingDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        taggingDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        taggingDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        recyclerView = taggingDialog.findViewById(R.id.followers_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        contactList = new ArrayList<>();
        userId = new ArrayList<>();
        taggingAdapter = new TaggingAdapter(contactList, this);
        recyclerView.setAdapter(taggingAdapter);

        ImageView exit_tag = taggingDialog.findViewById(R.id.tag_back);

        exit_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taggingDialog.dismiss();
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(Uid).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userId.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    userId.add(dataSnapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void iniStoreDialog() {
        storeDialog = new Dialog(FullScreenImageActivity.this);
        storeDialog.setContentView(R.layout.store_tag);
        Objects.requireNonNull(storeDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        storeDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        storeDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        recyclerView = storeDialog.findViewById(R.id.stores_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        retailList = new ArrayList<>();
        userId = new ArrayList<>();
        tagStoreAdapter = new TagStoreAdapter(this, retailList);
        recyclerView.setAdapter(tagStoreAdapter);

        ImageView exit_store = storeDialog.findViewById(R.id.tag_store_exit);

        exit_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeDialog.dismiss();
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Retails");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retailList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Retail retail = dataSnapshot.getValue(Retail.class);
                    retailList.add(retail);
                }
                tagStoreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    for (String id : userId){
                        assert contact != null;
                        if (contact.getId().equals(id)){
                            contactList.add(contact);
                        }
                    }
                }
                taggingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private Bitmap rotate(Bitmap decodeBitmap) {
        int W = decodeBitmap.getWidth();
        int H = decodeBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodeBitmap, 0, 0 ,W, H, matrix, true);
    }*/
}