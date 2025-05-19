package com.example.mobilalkfejlprojekt;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity implements AdminItemAdapter.OnItemDeleteListener, AdminItemAdapter.OnItemEditListener {
    private static final String TAG = "AdminActivity";
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private AdminItemAdapter adapter;
    private ArrayList<Item> items;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.adminRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        
        items = new ArrayList<>();
        adapter = new AdminItemAdapter(this, items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        try {
            firestore = FirebaseFirestore.getInstance();

            fetchItems();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore: " + e.getMessage());
        }
    }

    private void fetchItems() {
        try {
            firestore.collection("Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        items.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Item item = document.toObject(Item.class);
                                if (item != null) {
                                    item.setDocumentId(document.getId());
                                    items.add(item);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Item: " + e.getMessage());
                            }
                        }
                        runOnUiThread(() -> {
                            adapter.updateItems(new ArrayList<>(items));

                            if (items.isEmpty()) {
                                emptyStateTextView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                emptyStateTextView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                recyclerView.smoothScrollToPosition(items.size() - 1);
                            }
                        });
                    } else {
                        Log.e(TAG, "Error getting documents: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        runOnUiThread(() -> {
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        });
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchItems: " + e.getMessage());
            runOnUiThread(() -> {
                emptyStateTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void onItemDelete(int position, String documentId) {
        new AlertDialog.Builder(this)
            .setTitle("Tétel törlése")
            .setMessage("Biztosan törölni szeretnéd ezt a tételt?")
            .setPositiveButton("Igen", (dialog, which) -> {
                firestore.collection("Items")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tétel sikeresen törölve", Toast.LENGTH_SHORT).show();
                        fetchItems();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting item: " + e.getMessage());
                        Toast.makeText(this, "Hiba a tétel törlése közben", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Nem", null)
            .show();
    }

    @Override
    public void onItemEdit(int position, Item item) {
        showEditItemDialog(item);
    }

    private void showEditItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextInputEditText priceInput = dialogView.findViewById(R.id.priceInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        nameInput.setText(item.getName());
        descriptionInput.setText(item.getDescription());

        String priceWithoutFlurbo = item.getPrice().replace(" Flurbo", "");
        priceInput.setText(priceWithoutFlurbo);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String priceInputText = priceInput.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || priceInputText.isEmpty()) {
                Toast.makeText(this, "Kérjük, töltse ki az összes mezőt", Toast.LENGTH_SHORT).show();
                return;
            }

            String price = priceInputText + " Flurbo";


            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);

            updateItemInFirestore(item);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextInputEditText priceInput = dialogView.findViewById(R.id.priceInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String priceInputText = priceInput.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || priceInputText.isEmpty()) {
                Toast.makeText(this, "Kérjük, töltse ki az összes mezőt", Toast.LENGTH_SHORT).show();
                return;
            }

            String price = priceInputText + " Flurbo";

            Item newItem = new Item(name, description, price);
            saveItemToFirestore(newItem);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateItemInFirestore(Item item) {
        try {
            item.setName(new String(item.getName().getBytes("UTF-8"), "UTF-8"));
            item.setDescription(new String(item.getDescription().getBytes("UTF-8"), "UTF-8"));
            item.setPrice(new String(item.getPrice().getBytes("UTF-8"), "UTF-8"));

            firestore.collection("Items")
                .document(item.getDocumentId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tétel sikeresen frissítve", Toast.LENGTH_SHORT).show();
                    fetchItems();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating item: " + e.getMessage());
                    Toast.makeText(this, "Hiba a tétel frissítése közben", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error encoding item data: " + e.getMessage());
            Toast.makeText(this, "Hiba a karakterkódolás kezelése közben", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveItemToFirestore(Item item) {
        try {
            item.setName(new String(item.getName().getBytes("UTF-8"), "UTF-8"));
            item.setDescription(new String(item.getDescription().getBytes("UTF-8"), "UTF-8"));
            item.setPrice(new String(item.getPrice().getBytes("UTF-8"), "UTF-8"));

            firestore.collection("Items")
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tétel sikeresen hozzáadva", Toast.LENGTH_SHORT).show();
                    fetchItems();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding item: " + e.getMessage());
                    Toast.makeText(this, "Hiba a tétel hozzáadása közben", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error encoding item data: " + e.getMessage());
            Toast.makeText(this, "Hiba a karakterkódolás kezelése közben", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        MenuItem addItem = menu.findItem(R.id.action_add);
        addItem.getIcon().setTint(getResources().getColor(android.R.color.black, getTheme()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add) {
            showAddItemDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 