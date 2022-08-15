package com.example.menyaka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.menyaka.Adapters.MyStoreAdapter;
import com.example.menyaka.Adapters.ProductItemAdapter;
import com.example.menyaka.Adapters.RateReviewAdapter;
import com.example.menyaka.Adapters.RetailAdapter;
import com.example.menyaka.Models.CartItems;
import com.example.menyaka.Models.MyStore;
import com.example.menyaka.Models.Product;
import com.example.menyaka.Models.RatingAndReview;
import com.example.menyaka.Models.Retail;
import com.example.menyaka.Utils.RatingsCalculator;
import com.example.menyaka.Utils.UniversalFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    TextView productName, productPrice, productDescription, productShop, addToCart, totalQuantity;
    ImageView productImage, backBTN, viewCart, addToFavourites, plus, minus;
    RecyclerView similarProRecycler, proReviewRecycler;
    Button productSize;

    String productID, product, shopID, purchaseMethod, productCategory, product_Size, product_Price, key, timestamp;
    double totalPrice;

    DatabaseReference reference, productRef, cartReference;
    FirebaseUser firebaseUser;

    //for displaying similar products
    private ProductItemAdapter productAdapter;
    private ArrayList<Product> productList;

    //for displaying reviews
    private RateReviewAdapter ratingsAdapter;
    private List<RatingAndReview> ratingsList;
    ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5;
    RatingBar ratingSection, productRating;
    TextView ratingNumber, totalRatings;

    //for displaying retails with similar products
    ArrayList<MyStore> similarStores;
    MyStoreAdapter retailAdapter;
    RecyclerView retailRecycler;
    RelativeLayout moreRetails;

    private int quantity = 0;
    private boolean isCartItem = false;

    UniversalFunctions universalFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productName = findViewById(R.id.proD_Name);
        productShop = findViewById(R.id.productShop);
        productPrice = findViewById(R.id.proD_Price);
        productRating = findViewById(R.id.proD_Rating);
        addToCart = findViewById(R.id.proD_AddToCart);
        addToFavourites = findViewById(R.id.proD_AddToWishList);
        productDescription = findViewById(R.id.proD_Description);
        productImage = findViewById(R.id.proD_Image);
        backBTN = findViewById(R.id.proD_BackBTN);
        viewCart = findViewById(R.id.proD_GoToCart);
        similarProRecycler = findViewById(R.id.similar_products_Recycler);
        proReviewRecycler = findViewById(R.id.product_comments_recycler);
        retailRecycler = findViewById(R.id.similar_shops_Recycler);
        moreRetails = findViewById(R.id.similar_shops);
        plus = findViewById(R.id.incrementQuantity);
        minus = findViewById(R.id.decrementQuantity);
        totalQuantity = findViewById(R.id.totalQuantity);
        productSize = findViewById(R.id.productSize);

        universalFunctions = new UniversalFunctions(this);

        quantity = 1;
        totalQuantity.setText("" + quantity);
        product_Size = productSize.getText().toString();

        ratingSection = findViewById(R.id.product_rating_section);
        ratingNumber = findViewById(R.id.product_average_rating);
        totalRatings = findViewById(R.id.product_number_of_ratings);

        progressBar1 = findViewById(R.id.product_progress_1);
        progressBar2 = findViewById(R.id.product_progress_2);
        progressBar3 = findViewById(R.id.product_progress_3);
        progressBar4 = findViewById(R.id.product_progress_4);
        progressBar5 = findViewById(R.id.product_progress_5);

        productID = getIntent().getExtras().getString("productID");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Products");

        timestamp = String.valueOf(System.currentTimeMillis());
        cartReference = FirebaseDatabase.getInstance().getReference("ShoppingCarts");

        getProductDetails();
        getSimilarProducts();
        checkWish(productID);
        getSimilarStores();
        //checkCartItem();

        retailRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        retailRecycler.setLayoutManager(layoutManager);
        similarStores = new ArrayList<>();
        retailAdapter = new MyStoreAdapter(similarStores, ProductDetailActivity.this);
        retailRecycler.setAdapter(retailAdapter);

        similarProRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        similarProRecycler.setLayoutManager(linearLayoutManager);
        productList = new ArrayList<>();

        proReviewRecycler.setHasFixedSize(true);
        proReviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingsList = new ArrayList<>();
        ratingsAdapter = new RateReviewAdapter(this, ratingsList);
        proReviewRecycler.setAdapter(ratingsAdapter);

        universalFunctions.readRatingAndReviews(productID, ratingsList, ratingsAdapter);
        RatingsCalculator.ratingHeader(productID, productRating, totalRatings);
        RatingsCalculator.totalRating(productID, ratingSection);
        RatingsCalculator.setRating(productID, ratingNumber);

        RatingsCalculator.ratingBars(productID,5, progressBar5);
        RatingsCalculator.ratingBars(productID,4, progressBar4);
        RatingsCalculator.ratingBars(productID,3, progressBar3);
        RatingsCalculator.ratingBars(productID,2, progressBar2);
        RatingsCalculator.ratingBars(productID,1, progressBar1);

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        productShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shopIntent = new Intent(ProductDetailActivity.this, ShopDetailsActivity.class);
                shopIntent.putExtra("storeId", shopID);
                startActivity(shopIntent);
            }
        });

        viewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                intent.putExtra("storeId", shopID);
                startActivity(intent);
            }
        });

        findViewById(R.id.ratings_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MoreReviewsActivity.class);
                intent.putExtra("storeId", productID);
                startActivity(intent);
            }
        });

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*String btnString = addToCart.getText().toString().trim();

                switch (btnString){
                    case "Add To Cart":
                        purchaseMethod = "Cart Buy";
                        addToShoppingBasket(purchaseMethod);
                        break;

                    case "Remove From Cart":
                        removeBasketItem(productID);
                        break;

                    default:
                        Toast.makeText(ProductDetailActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();

                }*/

                if (cartItem()) {
                    removeBasketItem(productID);
                } else if (!cartItem()) {
                    purchaseMethod = "Cart Buy";
                    addToShoppingBasket(purchaseMethod);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        findViewById(R.id.addToWishListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addToFavourites.getTag().equals("noWish")){
                    FirebaseDatabase.getInstance().getReference().child("WishList").child(productID)
                            .child(firebaseUser.getUid()).setValue(true);
                    Toast.makeText(ProductDetailActivity.this, "Added to wish list successfully", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseDatabase.getInstance().getReference().child("WishList").child(productID)
                            .child(firebaseUser.getUid()).removeValue();
                    Toast.makeText(ProductDetailActivity.this, "Removed from the wish list successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                totalQuantity.setText("" + quantity);
                totalPrice = Double.parseDouble(product_Price) * Double.parseDouble(String.valueOf(quantity));
                Toast.makeText(ProductDetailActivity.this, "Total price is " + totalPrice, Toast.LENGTH_SHORT).show();
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1){
                    quantity--;
                    totalQuantity.setText("" + quantity);
                    totalPrice = Double.parseDouble(product_Price) * Double.parseDouble(String.valueOf(quantity));
                    Toast.makeText(ProductDetailActivity.this, "Total price is " + totalPrice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        moreRetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MoreBusinessActivity.class);
                intent.putExtra("storeId", shopID);
                intent.putExtra("storeCategory", productCategory);
                intent.putExtra("style", "similarProductStores");
                startActivity(intent);
            }
        });

        findViewById(R.id.proD_buy_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
                builder.setTitle("PAYMENT")
                        .setMessage("You Are Now Starting Your Secure Payment Process")
                        .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                purchaseMethod = "Buy Now";
                                addToBuyNowBasket(purchaseMethod);
                                //addToShoppingBasket(purchaseMethod);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        findViewById(R.id.moreProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MoreProductActivity.class);
                intent.putExtra("storeId", shopID);
                intent.putExtra("style", "similarProducts");
                startActivity(intent);
            }
        });

        findViewById(R.id.similarProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MoreProductActivity.class);
                intent.putExtra("storeId", shopID);
                intent.putExtra("style", "similarProducts");
                startActivity(intent);
            }
        });

    }

    private boolean cartItem() {
        cartReference.child(key).child("CartProducts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        CartItems items = snapshot1.getValue(CartItems.class);

                        assert items != null;
                        if (items.getProductID().equals(productID))
                            isCartItem = snapshot.exists();
                    }
                }else{
                    Toast.makeText(ProductDetailActivity.this, "This store does not have cart products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return isCartItem;
    }

    private void getProductDetails() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product myProduct = ds.getValue(Product.class);

                    assert myProduct != null;

                    if (myProduct.getProductId().equals(productID)) {
                        product = myProduct.getProductName();
                        shopID = myProduct.getStoreId();
                        productCategory = myProduct.getProductCategory();
                        product_Price = myProduct.getPrice();
                        key = firebaseUser.getUid() + myProduct.getStoreId();

                        isReadyToBuy(myProduct.getProductId());

                        productName.setText(myProduct.getProductName());
                        productPrice.setText("M " + myProduct.getPrice());
                        productDescription.setText("This product is of high quality and is produced in lesotho by the loving people and citizens of the lovely country");

                        try {
                            Picasso.get().load(myProduct.getProductImg()).into(productImage);
                        } catch (NullPointerException ignored) {
                        }

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Retails");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Retail retail = ds.getValue(Retail.class);

                                    assert retail != null;
                                    if (retail.getRetail_id().equals(myProduct.getStoreId()))
                                        productShop.setText(retail.getRetailName());
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

            }
        });
    }

    private void removeBasketItem(String productID) {

        final DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts")
                .child(key).child("CartProducts");
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CartItems items = ds.getValue(CartItems.class);

                        assert items != null;
                        if (items.getProductID().equals(productID)){
                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ProductDetailActivity.this, "The cart item has been deleted successfully", Toast.LENGTH_SHORT).show();
                                    if (!snapshot.exists()){
                                        snapshot.getRef().removeValue();
                                        Toast.makeText(ProductDetailActivity.this, "The cart has been deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }else{
                    Toast.makeText(ProductDetailActivity.this, "There are no cart products in this store", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*final DatabaseReference reference = cartReference.child(key).child("CartProducts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){

                        if (Objects.equals(ds.getKey(), productID))
                            ds.getRef().removeValue();
                    }
                }else{
                    Toast.makeText(ProductDetailActivity.this, "Could not locate this cart item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    private void addToBuyNowBasket(String purchaseMethod) {

        String buyKey = cartReference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("storeID", shopID);
        hashMap.put("status", "pending...");
        hashMap.put("purchaseMethod", purchaseMethod);
        hashMap.put("timestamp", timestamp);
        hashMap.put("totalPrice", String.valueOf(totalPrice));

        assert buyKey != null;
        cartReference.child(buyKey).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if ("Buy Now".equals(purchaseMethod)) {
                                buyNow(buyKey);
                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Unable to buy at the moment", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(ProductDetailActivity.this, "Cannot Process Transaction", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Error adding transaction", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addToShoppingBasket(String purchaseMethod) {

        final DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(key);
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    if ("Cart Buy".equals(purchaseMethod)) {
                        addCart();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Illegal Selection", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("userID", firebaseUser.getUid());
                    hashMap.put("storeID", shopID);
                    hashMap.put("status", "pending...");
                    hashMap.put("purchaseMethod", purchaseMethod);
                    hashMap.put("timestamp", timestamp);

                    cartReference.child(key).setValue(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        if ("Cart Buy".equals(purchaseMethod)) {
                                            addCart();
                                        } else {
                                            Toast.makeText(ProductDetailActivity.this, "Illegal Selection", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(ProductDetailActivity.this, "Cannot Process Transaction", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductDetailActivity.this, "Error adding transaction", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addCart() {

        final DatabaseReference refCart = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(key);

        HashMap<String, Object> productsMap = new HashMap<>();
        productsMap.put("productID", productID);
        productsMap.put("orderQuantity", String.valueOf(quantity));
        productsMap.put("productSize", product_Size);
        productsMap.put("productPrice", product_Price);
        productsMap.put("storeID", shopID);
        productsMap.put("status", "pending");

        refCart.child("CartProducts").child(productID).setValue(productsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProductDetailActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Unable to add product to cart", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void buyNow(String buyKey) {
        final DatabaseReference refCart = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(buyKey);

        HashMap<String, Object> productsMap = new HashMap<>();
        productsMap.put("productID", productID);
        productsMap.put("orderQuantity", String.valueOf(quantity));
        productsMap.put("productSize", product_Size);
        productsMap.put("productPrice", product_Price);
        productsMap.put("storeID", shopID);
        productsMap.put("status", "pending");

        refCart.child("CartProducts").child(productID).setValue(productsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(ProductDetailActivity.this, DeliveryOptionsActivity.class);
                        intent.putExtra("cartID", buyKey);
                        intent.putExtra("buyMethod", purchaseMethod);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Unable to add product to cart", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getSimilarStores() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Retails");
        reference.limitToFirst(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                similarStores.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MyStore retail = dataSnapshot.getValue(MyStore.class);
                    similarStores.add(retail);
                }
                productAdapter = new ProductItemAdapter(ProductDetailActivity.this, productList);
                similarProRecycler.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkWish(String productID) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("WishList").child(productID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(user.getUid()).exists()){
                    addToFavourites.setTag("wished");
                    addToFavourites.setImageResource(R.drawable.ic_baseline_favorite_24);
                }else{
                    addToFavourites.setTag("noWish");
                    addToFavourites.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isReadyToBuy(String productID) {
        final DatabaseReference reference = cartReference.child(key);
        reference.child("CartProducts")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        CartItems items = ds.getValue(CartItems.class);

                        assert items != null;
                        if (items.getProductID().equals(productID)){
                            addToCart.setText("Remove From Cart");
                            addToCart.setTag("CartItem");
                        }
                    }
                }else{
                    addToCart.setText("Add To Cart");
                    addToCart.setTag("NotCartItem");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*DatabaseReference cartItemRef = FirebaseDatabase.getInstance().getReference("ShoppingCarts").child(key)
                .child("CartProducts").child(productID);
        cartItemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    /*for(DataSnapshot ds : snapshot.getChildren()){
                        CartItems items = ds.getValue(CartItems.class);

                        if(items.getStatus().equals("pending...")){
                            addToCart.setText("Remove From Cart");
                            getOrderQuantity();
                        }else{
                            addToCart.setText("Add To Cart");
                        }
                    }

                    addToCart.setText("Remove From Cart");

                }else{
                    addToCart.setText("Add To Cart");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    private void getSimilarProducts() {
        productRef = FirebaseDatabase.getInstance().getReference("Products");
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    //if (product.getProductCategory().equals(productCategory))
                        productList.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}