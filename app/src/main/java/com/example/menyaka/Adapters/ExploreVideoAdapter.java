package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.example.menyaka.Utils.UniversalFunctions;
import com.example.menyaka.VideoMomentActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExploreVideoAdapter extends RecyclerView.Adapter<ExploreVideoAdapter.ViewHolder> {

    Context context;
    ArrayList<Moment> moments;

    DatabaseReference userRef, storeRef;
    UniversalFunctions universalFunctions;

    public ExploreVideoAdapter(Context context, ArrayList<Moment> moments) {
        this.context = context;
        this.moments = moments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.explore_videos, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Moment moment = moments.get(position);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        universalFunctions = new UniversalFunctions(context);

        getVideoOwnerDetails(moment, holder);
        universalFunctions.checkVideoViewCount(holder.videoViews, moment);

        holder.exploreVideoView.setVideoURI(Uri.parse(moment.getVideoUrl()));
        holder.exploreVideoView.setOnPreparedListener(mediaPlayer -> {

            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setLooping(true);

        });

        holder.exploreVideoView.start();
        holder.videoLoader.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoMomentActivity.class);
                intent.putExtra("moment", moment.getMomentId());
                context.startActivity(intent);
            }
        });
    }

    private void getVideoOwnerDetails(Moment moment, ViewHolder holder) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getId().equals(moment.getUsername()))
                        try{
                            Picasso.get().load(user.getImageUrl()).into(holder.videoOwnPic);
                        }catch (NullPointerException ignored){}
                    else
                        storeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                               for (DataSnapshot ds : snapshot.getChildren()){
                                   Retail retail = ds.getValue(Retail.class);

                                   if (retail.getRetail_id().equals(moment.getUsername()))
                                       try{
                                           Picasso.get().load(retail.getImageUrl()).into(holder.videoOwnPic);
                                       }catch (NullPointerException ignored){}
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

    @Override
    public int getItemCount() {
        return moments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        VideoView exploreVideoView;
        TextView videoViews;
        CircleImageView videoOwnPic;
        ProgressBar videoLoader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exploreVideoView = itemView.findViewById(R.id.exploreVideoView);
            videoViews = itemView.findViewById(R.id.videoViewsCount);
            videoOwnPic = itemView.findViewById(R.id.videoOwnerPic);
            videoLoader = itemView.findViewById(R.id.videoItemLoader);
        }
    }
}
