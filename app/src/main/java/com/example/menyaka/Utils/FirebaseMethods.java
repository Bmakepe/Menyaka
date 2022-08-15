package com.example.menyaka.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.menyaka.MainActivity;
import com.example.menyaka.Models.Photo;
import com.example.menyaka.Models.User;
import com.example.menyaka.Models.Video;
import com.example.menyaka.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";
    private Activity mActivity;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private long mediaCount = 0;

    public FirebaseMethods(Activity activity) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        myRef = database.getReference();
        mStorageRef = mStorage.getReference();
        mActivity = activity;
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    private void sendVerificationEmail(final Boolean isRequired){

        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"Verification code sent");
                        Toast.makeText(mActivity, "Verification email sent, please check your inbox", Toast.LENGTH_SHORT).show();

                        if(isRequired) {
                            mAuth.signOut();
                            mActivity.finish();
                        }
                    }else {
                        Toast.makeText(mActivity, "Couldn't send verification email !!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void reAuthenticateUser(final String newEmail, String password){

        try {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(mAuth.getCurrentUser().getEmail(), password);

            // Prompt the user to re-provide their sign-in credentials
            Objects.requireNonNull(mAuth.getCurrentUser()).reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "User re-authenticated.");
                                sendVerificationEmail(false);
                                updateEmail(newEmail);
                            }else {
                                Log.d(TAG, "User re-authentication failed.");
                                Toast.makeText(mActivity, "Failed!! Your password doesn't match!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateEmail(final String newEmail){

        FirebaseUser user = mAuth.getCurrentUser();

        Objects.requireNonNull(user).updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            myRef.child(mActivity.getString(R.string.users_node))
                                    .child(userID)
                                    .child(mActivity.getString(R.string.emailField)).setValue(newEmail);
                            Toast.makeText(mActivity, "Email updated", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User email address updated.");
                        }else {
                            Toast.makeText(mActivity, "Failed!!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void updateProfilePhoto(String photoUri){

        //In user_account_settings node
        myRef.child(mActivity.getString(R.string.user_account_settings_node))
                .child(userID)
                .child(mActivity.getString(R.string.profilePhotoField)).setValue(photoUri);
    }


    public long getImageCount(DataSnapshot dataSnapshot) {
        Log.d(TAG,"image_count: "+dataSnapshot.getChildrenCount());
        return dataSnapshot.getChildrenCount();
    }


    public long getVideoCount(DataSnapshot dataSnapshot) {

        Log.d(TAG,"video_count: "+dataSnapshot.getChildrenCount());
        return dataSnapshot.getChildrenCount();
    }

    public void uploadNewPhoto(final String caption, long count, String imageUrl, final ProgressBar progressBar, final TextView uploadProgress, boolean isProfilePhoto){

        final String FIREBASE_IMAGE_STORAGE = "memories/users/";
        FileCompressor compressor = new FileCompressor(mActivity);
        final StorageReference storageReference;

        //If it is not a profile photo
        if(!isProfilePhoto){
            storageReference =  mStorageRef.child(FIREBASE_IMAGE_STORAGE+userID+"/photo"+(count+1));
            final UploadTask uploadTask = storageReference.putFile(Uri.fromFile(compressor.compressImage(imageUrl)));

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        addPhotoToDatabase(caption,downloadUri.toString());
                        progressBar.setVisibility(View.GONE);
                        uploadProgress.setVisibility(View.GONE);
                        mActivity.finish();
                        mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                        Toast.makeText(mActivity, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        uploadProgress.setVisibility(View.GONE);
                        Toast.makeText(mActivity, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Tracking progress
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    long uploadPercentage = (taskSnapshot.getBytesTransferred()*100)/taskSnapshot.getTotalByteCount();
                    uploadProgress.setText(String.valueOf("Uploading "+uploadPercentage+"%"));
                }
            });
        //If it is a profile photo
        }else {
            storageReference =  mStorageRef.child(FIREBASE_IMAGE_STORAGE+userID+"/profile_photo");
            UploadTask uploadTask = storageReference.putFile(Uri.fromFile(compressor.compressImage(imageUrl)));

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        updateProfilePhoto(downloadUri.toString());
                        mActivity.finish();
                        Toast.makeText(mActivity, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mActivity, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }





    public void uploadNewVideo(final String caption, final long count, final String videoUrl, final ProgressBar progressBar, final TextView uploadProgress){

        final String FIREBASE_VIDEO_STORAGE = "videos/users/";
        final StorageReference storageReference;

            storageReference =  mStorageRef.child(FIREBASE_VIDEO_STORAGE+userID+"/video"+(count+1));
            UploadTask uploadTask = storageReference.putFile(Uri.fromFile(new File(videoUrl)));

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        addVideoToDatabase(caption,downloadUri.toString());
                        progressBar.setVisibility(View.GONE);
                        uploadProgress.setVisibility(View.GONE);
                        mActivity.finish();
                        Toast.makeText(mActivity, "Video uploaded successfully", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        uploadProgress.setVisibility(View.GONE);
                        Toast.makeText(mActivity, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //Tracking progress
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    long uploadPercentage = (taskSnapshot.getBytesTransferred()*100)/taskSnapshot.getTotalByteCount();
                    uploadProgress.setText(String.valueOf("Uploading "+uploadPercentage+"%"));
                }
            });
    }

    private void addPhotoToDatabase(String caption, String imageUrl){

        String photoId = myRef.push().getKey();
        String dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        String tags = StringManipulation.getTags(caption);
        Photo photo = new Photo(caption,imageUrl,dateAdded,photoId,tags,userID);
        myRef.child(mActivity.getString(R.string.user_photos_node)).child(userID).child(photoId).setValue(photo);
        myRef.child(mActivity.getString(R.string.photos_node)).child(photoId).setValue(photo);
    }


    private void addVideoToDatabase(String caption, String videoUrl){

        String videoId = myRef.push().getKey();
        String dateAdded = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        String tags = StringManipulation.getTags(caption);
        Video video = new Video(caption,videoUrl,dateAdded,videoId,tags,userID);
        myRef.child(mActivity.getString(R.string.user_videos_node)).child(userID).child(videoId).setValue(video);
        myRef.child(mActivity.getString(R.string.videos_node)).child(videoId).setValue(video);
    }

}
