package com.example.mobilalkfejlprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainLoggedInActivity extends AppCompatActivity {

    private FirebaseUser User;
    private RecyclerView RecyclerView;
    private ArrayList<Item> ItemList;
    private ArrayList<Item> CartItems;
    private ItemAdapter Adapter;
    private int GridNumber = 1;
    private boolean ViewRow = true;
    private FrameLayout RedCircle;
    private TextView ContentTextView;
    private int BasketItems = 0;
    private Toolbar toolbar;
    private TextView BadgeTextView;
    private TextView EmptyStateTextView;

    private FirebaseFirestore FireStore;
    private CollectionReference Items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_logged_in);

        User = FirebaseAuth.getInstance().getCurrentUser();

        if (User == null){
            finish();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Étlap");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        RecyclerView = findViewById(R.id.recyclerView);
        EmptyStateTextView = findViewById(R.id.emptyStateTextView);
        RecyclerView.setLayoutManager(new GridLayoutManager(this, GridNumber));
        RecyclerView.setItemAnimator(new CustomItemAnimator());
        ItemList = new ArrayList<>();
        CartItems = new ArrayList<>();
        Adapter = new ItemAdapter(this, ItemList);
        RecyclerView.setAdapter(Adapter);

        FireStore = FirebaseFirestore.getInstance();
        Items = FireStore.collection("Items");

        initializeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeData();
    }

    private void initializeData() {
        Items.get().addOnSuccessListener(queryDocumentSnapshots -> {
            ItemList.clear();
            for (var document : queryDocumentSnapshots) {
                try {
                    String name = new String(document.getString("name").getBytes("UTF-8"), "UTF-8");
                    String description = new String(document.getString("description").getBytes("UTF-8"), "UTF-8");
                    String price = new String(document.getString("price").getBytes("UTF-8"), "UTF-8");
                    
                    if (name != null && description != null && price != null) {
                        ItemList.add(new Item(name, description, price));
                    }
                } catch (Exception e) {
                    Log.e("MainLoggedInActivity", "Error decoding UTF-8 data: " + e.getMessage());
                }
            }
            Adapter.notifyDataSetChanged();
            
            if (ItemList.isEmpty()) {
                EmptyStateTextView.setVisibility(View.VISIBLE);
                RecyclerView.setVisibility(View.GONE);
            } else {
                EmptyStateTextView.setVisibility(View.GONE);
                RecyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Log.e("MainLoggedInActivity", "Error loading data: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok betöltése közben", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        MenuItem basketItem = menu.findItem(R.id.action_basket);
        View actionView = MenuItemCompat.getActionView(basketItem);
        BadgeTextView = actionView.findViewById(R.id.badge);
        updateBasketBadge();

        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("itemCount", BasketItems);
            intent.putExtra("cartItems", CartItems);
            startActivityForResult(intent, 1);
        });
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_admin) {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_basket) {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("itemCount", BasketItems);
            intent.putExtra("cartItems", CartItems);
            startActivityForResult(intent, 1);
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void changeSpanCount(MenuItem item, int icViewModule, int i) {
        ViewRow = !ViewRow;
        item.setIcon(icViewModule);
        GridLayoutManager layoutManager = (GridLayoutManager) RecyclerView.getLayoutManager();
        layoutManager.setSpanCount(i);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<Item> updatedCartItems = (ArrayList<Item>) data.getSerializableExtra("cartItems");
            if (updatedCartItems != null) {
                CartItems.clear();
                CartItems.addAll(updatedCartItems);
                BasketItems = CartItems.size();
                updateBasketBadge();
            }
        }
    }

    public void updateBasketBadge() {
        if (BadgeTextView != null) {
            if (BasketItems > 0) {
                BadgeTextView.setVisibility(View.VISIBLE);
                BadgeTextView.setText(String.valueOf(BasketItems));
            } else {
                BadgeTextView.setVisibility(View.GONE);
            }
        }
    }

    public void updateAlertIcon(Item item) {
        BasketItems += 1;
        CartItems.add(item);
        updateBasketBadge();
    }
}