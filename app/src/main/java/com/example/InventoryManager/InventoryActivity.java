package com.example.InventoryManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * InventoryActivity
 *
 * Main dashboard screen of the app.
 * Displays all inventory items and allows:
 * - Adding new items
 * - Viewing current stock
 * - Sending low inventory SMS alerts
 *
 * Uses InventoryRepository to separate UI from database logic.
 */
public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewInventory;
    private FloatingActionButton fabAddItem;

    // Repository handles all database operations
    private InventoryRepository repository;

    // Adapter binds data to RecyclerView
    private InventoryAdapter adapter;

    // Threshold for triggering low stock alerts
    private static final int LOW_STOCK_THRESHOLD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize repository (data layer)
        repository = new InventoryRepository(this);

        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        fabAddItem = findViewById(R.id.fabAddItem);

        // Display items in a 2-column grid layout
        recyclerViewInventory.setLayoutManager(new GridLayoutManager(this, 2));

        // Open dialog to add new item
        fabAddItem.setOnClickListener(v -> openAddItemDialog());

        // Load inventory on screen start
        loadInventory();
    }

    /**
     * Retrieves all items from the database and binds them to the UI.
     * Also triggers a low inventory check after loading.
     */
    private void loadInventory() {

        // Fetch data from repository (no direct DB access here)
        ArrayList<InventoryItem> items = repository.getAllItems();

        // Bind data to RecyclerView
        adapter = new InventoryAdapter(this, items, repository);
        recyclerViewInventory.setAdapter(adapter);

        // Check if any items are below threshold
        checkLowInventory(items);
    }

    /**
     * Displays a dialog allowing the user to add a new inventory item.
     * Includes validation to prevent invalid input.
     */
    private void openAddItemDialog() {

        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);

        builder.setTitle("Add Item");

        // Reuse existing layout for input fields
        android.view.View view = getLayoutInflater()
                .inflate(R.layout.dialog_edit_item, null);

        android.widget.EditText etName = view.findViewById(R.id.etEditName);
        android.widget.EditText etQuantity = view.findViewById(R.id.etEditQuantity);

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {

            String name = etName.getText().toString().trim();
            String quantityText = etQuantity.getText().toString().trim();

            // Validate item name
            if (name.isEmpty()) {
                Toast.makeText(this, "Item name required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate quantity input
            if (quantityText.isEmpty()) {
                Toast.makeText(this, "Quantity required", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;

            try {
                // Convert input string to integer safely
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                // Prevent crash if input is not a number
                Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure quantity is not negative
            if (quantity < 0) {
                Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add item via repository
            repository.addItem(name, quantity);

            // Refresh UI
            loadInventory();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Checks all inventory items and sends SMS alerts
     * for items below the defined threshold.
     *
     * NOTE:
     * - Requires SEND_SMS permission
     * - Runs every time inventory is loaded or updated
     */
    public void checkLowInventory(ArrayList<InventoryItem> items) {

        for (InventoryItem item : items) {

            // Identify low stock items
            if (item.getQuantity() < LOW_STOCK_THRESHOLD) {

                // Ensure permission is granted before sending SMS
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {

                    SmsManager smsManager = SmsManager.getDefault();

                    // Send alert message
                    smsManager.sendTextMessage(
                            "5551234567",
                            null,
                            "Low inventory alert: " + item.getName(),
                            null,
                            null
                    );

                    // Notify user visually
                    Toast.makeText(this,
                            "Low Inventory Alert: " + item.getName(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}