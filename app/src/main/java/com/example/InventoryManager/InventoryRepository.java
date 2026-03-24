package com.example.InventoryManager;

import android.content.Context;
import android.database.Cursor;

import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;

public class InventoryRepository {

    private DatabaseHelper dbHelper;

    public InventoryRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ================= USER =================

    public boolean loginUser(String username, String password) {

        String storedPassword = dbHelper.getUserPassword(username);

        if (storedPassword == null) return false;

        return PasswordUtils.verifyPassword(password, storedPassword);
    }

    public boolean createUser(String username, String password) {

        String securePassword = PasswordUtils.generateSecurePassword(password);

        return dbHelper.insertUser(username, securePassword);
    }

    // ================= INVENTORY =================

    public boolean addItem(String name, int quantity) {
        return dbHelper.addItem(name, quantity);
    }

    public ArrayList<InventoryItem> getAllItems() {

        ArrayList<InventoryItem> items = new ArrayList<>();
        Cursor cursor = dbHelper.getAllItems();

        while (cursor.moveToNext()) {
            items.add(new InventoryItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            ));
        }

        return items;
    }

    public boolean updateItem(int id, String name, int quantity) {
        return dbHelper.updateItem(id, name, quantity);
    }

    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }

    // Sort by name (A-Z)
    public ArrayList<InventoryItem> getItemsSortedByName() {

        ArrayList<InventoryItem> items = getAllItems();

        Collections.sort(items, (a, b) ->
                a.getName().compareToIgnoreCase(b.getName())
        );

        return items;
    }

    // Sort by quantity (low to high)
    public ArrayList<InventoryItem> getItemsSortedByQuantity() {

        ArrayList<InventoryItem> items = getAllItems();

        Collections.sort(items, Comparator.comparingInt(InventoryItem::getQuantity));

        return items;
    }

    // Filter low stock
    public ArrayList<InventoryItem> getLowStockItems(int threshold) {

        ArrayList<InventoryItem> items = getAllItems();
        ArrayList<InventoryItem> filtered = new ArrayList<>();

        for (InventoryItem item : items) {

            // Only include items below threshold
            if (item.getQuantity() < threshold) {
                filtered.add(item);
            }
        }

        return filtered;
    }
}