package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.menyaka.Adapters.OrdersAdapter;
import com.example.menyaka.Adapters.WishListAdapter;
import com.example.menyaka.Models.Orders;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.User;
import com.example.menyaka.Utils.CheckPermissions;
import com.example.menyaka.Utils.UniversalImageLoader;
import com.example.menyaka.fragments.ExploreTabFragment;
import com.example.menyaka.fragments.HomeFragment;
import com.example.menyaka.fragments.ExploreFragment;
import com.example.menyaka.fragments.ChatListFragment;
import com.example.menyaka.fragments.StoresFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = MainActivity.this;
    private final  String[] permissionList ={
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    };

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    FirebaseUser currentUser;
    DatabaseReference reference;
    ImageView openCartBTN, notificationsBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        openCartBTN = findViewById(R.id.openCartBTN);
        notificationsBTN = findViewById(R.id.notificationsBTN);
        TextView brand = findViewById(R.id.brand_name);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/orbitron-bold.otf");
        brand.setTypeface(typeface);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        initImageLoader();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        try{
            updateDrawerHeader();
        }catch (NullPointerException e){}

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();
        if(intent != null){
            String publisher = intent.getString("publisherId");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileId", publisher);
            editor.apply();

        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                new HomeFragment()).commit();

        openCartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CartStoreActivity.class));
            }
        });

        findViewById(R.id.brand_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProductListActivity.class));
                //startActivity(new Intent(MainActivity.this, FullScreenVideoActivity.class));
            }
        });

        notificationsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Notifications will display here", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
            }
        });
    }

    private void updateDrawerHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        final TextView navUsername = headerView.findViewById(R.id.drawer_name);
        final ImageView navUserImage = headerView.findViewById(R.id.drawer_image);
        final ImageView drawer_verification = headerView.findViewById(R.id.drawer_verification);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                if (user.isVerified())
                    drawer_verification.setVisibility(View.VISIBLE);
                else
                    drawer_verification.setVisibility(View.GONE);

                try{
                    navUsername.setText(user.getUsername());

                    if (user.getImageUrl().equals("")){
                        navUserImage.setImageResource(R.drawable.profile_png_1114185);
                    } else {
                        //change this
                        Glide.with(getApplicationContext()).load(user.getImageUrl()).into(navUserImage);
                    }

                }catch (NullPointerException ignored){ }

                headerView.findViewById(R.id.drawer_name_area).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_explore:
                            selectedFragment = new ExploreTabFragment();
                            break;
                        case R.id.nav_memory:
                            selectedFragment = null;
                            startActivity(new Intent(MainActivity.this, NyakallaActivity.class));
                            //startActivity(new Intent(MainActivity.this, MenyakaActivity.class));
                            //startActivity(new Intent(MainActivity.this, OtherCameraActivity.class));
                            break;
                        case R.id.nav_stores:
                            selectedFragment = new StoresFragment();
                            break;
                        case R.id.nav_messages:
                            selectedFragment = new ChatListFragment();
                            break;
                    }

                    if (selectedFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                                selectedFragment).commit();
                    }

                    return true;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        switch (item.getItemId()){
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                drawer.closeDrawer(GravityCompat.START, true);
                return true;

            case R.id.wishlist:
                //wishListDialog.show();
                startActivity(new Intent(MainActivity.this, WishListActivity.class));
                drawer.closeDrawer(GravityCompat.START, true);
                return true;

            case R.id.receipts:
                startActivity(new Intent(MainActivity.this, MyOrdersActivity.class));
                drawer.closeDrawer(GravityCompat.START, true);
                return true;

            case R.id.total_spending:
                startActivity(new Intent(MainActivity.this, LoyaltyActivity.class));
                drawer.closeDrawer(GravityCompat.START, true);
                return true;

            case R.id.Settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                drawer.closeDrawer(GravityCompat.START, true);
                return true;

            case R.id.logout:
                Toast.makeText(context, "Log Out", Toast.LENGTH_SHORT).show();

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();

                drawer.closeDrawer(GravityCompat.START, true);
                return true;
        }
        drawer.closeDrawer(GravityCompat.START, true);
        return false;
    }

    private void initImageLoader(){

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==0&&!CheckPermissions.hasPermissions(context, permissionList)) {
            finish();
        }
    }
}
