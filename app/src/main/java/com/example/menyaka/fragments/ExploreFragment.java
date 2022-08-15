package com.example.menyaka.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.menyaka.Adapters.ExploreAdapter;
import com.example.menyaka.Models.HotDeal;
import com.example.menyaka.Models.Moment;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Models.ShippingAgents;
import com.example.menyaka.Models.User;
import com.example.menyaka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ExploreFragment extends Fragment {

    RecyclerView exploreRecycler;
    ExploreAdapter exploreAdapter;

    DatabaseReference productRef, userRef, storeRef, postsRef, agentRef, hotDealsRef;
    FirebaseUser firebaseUser;

    ArrayList<String> exploreItems;

    ImageView filterBTN;
    EditText searchArea;

    ProgressDialog pd;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container,false);

        exploreRecycler = view.findViewById(R.id.exploreRecycler);
        filterBTN = view.findViewById(R.id.searchFilterIcon);
        searchArea = view.findViewById(R.id.search_bar);

        productRef = FirebaseDatabase.getInstance().getReference("Products");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storeRef = FirebaseDatabase.getInstance().getReference("Retails");
        postsRef = FirebaseDatabase.getInstance().getReference("Moments");
        agentRef = FirebaseDatabase.getInstance().getReference("ShippingAgents");
        hotDealsRef = FirebaseDatabase.getInstance().getReference("HotDeals");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.show();

        exploreItems = new ArrayList<>();

        declareRecycler();
        getExploreItems();

        searchArea.setHint("Search Menyaka");

        filterBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(getActivity(), filterBTN, Gravity.END);

                popupMenu.getMenu().add(Menu.NONE, 0,0,"All");
                popupMenu.getMenu().add(Menu.NONE, 1,0,"Posts");
                popupMenu.getMenu().add(Menu.NONE, 2,0,"Stores");
                popupMenu.getMenu().add(Menu.NONE, 3,0,"Products");
                popupMenu.getMenu().add(Menu.NONE, 4,0,"People");
                popupMenu.getMenu().add(Menu.NONE, 5,0,"Shipping Agents");
                popupMenu.getMenu().add(Menu.NONE, 6,0,"Hot Deals");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        switch (id){
                            case 0:

                                pd.setMessage("Loading...");
                                pd.show();

                                declareRecycler();
                                getExploreItems();
                                pd.dismiss();

                                searchArea.setHint("Search Menyaka");

                                break;

                            case 1:

                                pd.setMessage("Loading Posts...");
                                pd.show();

                                declareRecycler();
                                getExploreImagePosts();
                                pd.dismiss();

                                searchArea.setHint("Search Posts");

                                break;

                            case 2:

                                pd.setMessage("Loading Stores...");
                                pd.show();

                                declareRecycler();
                                getExploreStores();
                                pd.dismiss();

                                searchArea.setHint("Search Stores");

                                break;

                            case 3:

                                pd.setMessage("Loading Products...");
                                pd.show();

                                declareRecycler();
                                getExploreProducts();
                                pd.dismiss();

                                searchArea.setHint("Search Products");

                                break;

                            case 4:

                                pd.setMessage("Loading Users...");
                                pd.show();

                                declareRecycler();
                                getExploreUsers();
                                pd.dismiss();

                                searchArea.setHint("Search Users");

                                break;

                            case 5:

                                pd.setMessage("Loading Shipping Agents...");
                                pd.show();

                                declareRecycler();
                                getShippingAgents();
                                pd.dismiss();

                                searchArea.setHint("Search Shipping Agents");

                                break;

                            case 6:

                                pd.setMessage("Loading Hot Deals...");
                                pd.show();

                                declareRecycler();
                                getExploreHotDeals();
                                pd.dismiss();

                                searchArea.setHint("Search Hot Deals");

                                break;

                            default:
                                throw new IllegalStateException("Unexpected value: " + id);
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        return view;

    }

    private void declareRecycler() {

        exploreItems.clear();

        int j = 5;

        exploreRecycler.hasFixedSize();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3,
                GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                if (position % 12 == 0 || position % 12 == 7){
                    return position == 1 ? 2 : 1;
                }else{
                    return position == 1 ? 1 : 1;
                }

                //return position == 1 ? 2 : 1;

                /*for (int i = 0; i < exploreItems.size(); i++){
                    if (0 == j % i){
                        return position == 1 ? 2 : 1;
                    }else{

                        return position == 1 ? 1 : 1;
                    }
                }
                return -1;*/
            }
        });

        exploreRecycler.setLayoutManager(layoutManager);

        //exploreRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    }

    private void updateRecycler() {

        Collections.shuffle(exploreItems);

        exploreAdapter = new ExploreAdapter(getActivity(), exploreItems);
        exploreRecycler.setAdapter(exploreAdapter);
        exploreAdapter.notifyDataSetChanged();
    }

    private void getExploreItems() {

        getShippingAgents();
        getExploreProducts();
        getExploreStores();
        getExploreImagePosts();
        getExploreHotDeals();
        getExploreUsers();

        pd.dismiss();

    }

    private void getExploreHotDeals() {

        hotDealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){

                    hotDealsRef.child(Objects.requireNonNull(ds.getKey())).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                HotDeal hotDeal = ds.getValue(HotDeal.class);
                                assert hotDeal != null;
                                exploreItems.add(hotDeal.getHotDeal_id());
                            }

                            updateRecycler();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Cannot retrieve hot deals", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getShippingAgents() {
        agentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ShippingAgents agents = ds.getValue(ShippingAgents.class);
                    assert agents != null;
                    exploreItems.add(agents.getCompanyID());
                }

                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "unable to retrieve shipping agents ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getExploreImagePosts() {

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Moment moment = ds.getValue(Moment.class);

                    assert moment != null;
                    try{
                        if (!moment.getPrivacy().equals("Private")){
                            /*if (!moment.getImageUrl().equals("noImage")){
                                exploreItems.add(moment.getMomentId());
                            }*/

                            //if (moment.getPostType().equals("imagePost") || moment.getPostType().equals("videoPost"))
                            if (moment.getPostType().equals("imagePost"))
                                exploreItems.add(moment.getMomentId());


                        }
                    }catch (NullPointerException ignored){}

                }

                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Cannot retrieve posts", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getExploreStores() {

        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Retail retail = ds.getValue(Retail.class);
                    assert retail != null;
                    exploreItems.add(retail.getRetail_id());
                }

                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Cannot retrieve stores", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getExploreUsers() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    User user = snap.getValue(User.class);
                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid()))
                        exploreItems.add(user.getId());
                }

                updateRecycler();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Cannot retrieve users", Toast.LENGTH_SHORT).show();}
        });
    }

    private void getExploreProducts() {

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    exploreItems.add(product.getProductId());

                }

                updateRecycler();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Cannot retrieve Products", Toast.LENGTH_SHORT).show();}
        });

    }

}
