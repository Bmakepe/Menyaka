package com.example.menyaka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menyaka.VideoFullScreenActivity;
import com.example.menyaka.Models.BannerAds;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.R;
import com.example.menyaka.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BannerAdsAdapter extends RecyclerView.Adapter<BannerAdsAdapter.ViewHolder>{

    Context context;
    ArrayList<BannerAds> adsList;

    DatabaseReference adsReference, storeReference;

    public BannerAdsAdapter(Context context, ArrayList<BannerAds> adsList) {
        this.context = context;
        this.adsList = adsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_ads_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerAds ads = adsList.get(position);

        adsReference = FirebaseDatabase.getInstance().getReference("AdBanners");
        storeReference = FirebaseDatabase.getInstance().getReference("Retails");

        getAdDetails(ads, holder);
        getStoreDetails(ads, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ads.getAd_type().equals("mediaAd")){

                    Toast.makeText(context, "You will be able to view the media of this ad", Toast.LENGTH_SHORT).show();
                    Intent adVideoIntent = new Intent(context, VideoFullScreenActivity.class);
                    adVideoIntent.putExtra("videoURL_ID", ads.getBannerID());
                    context.startActivity(adVideoIntent);

                }else if (ads.getAd_type().equals("no media")) {

                    Intent shopIntent = new Intent(context, ShopDetailsActivity.class);
                    shopIntent.putExtra("storeId", ads.getRetail_ID());
                    context.startActivity(shopIntent);

                }
            }
        });

    }

    private void getStoreDetails(BannerAds ads, ViewHolder holder) {
        storeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);

                    assert retail != null;
                    if (retail.getRetail_id().equals(ads.getRetail_ID())){
                        try{
                            Picasso.get().load(retail.getImageUrl()).into(holder.storeImage);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAdDetails(BannerAds ads, ViewHolder holder) {
        adsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    BannerAds banner = ds.getValue(BannerAds.class);

                    assert banner != null;
                    if (banner.getBannerID().equals(ads.getBannerID()))
                        try{
                            Picasso.get().load(banner.getBannerIMG()).into(holder.adsSpace);
                        }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return adsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView adsSpace;
        CircleImageView storeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            adsSpace = itemView.findViewById(R.id.adsBannerSpace);
            storeImage = itemView.findViewById(R.id.adsStoreLogo);
        }
    }
}
