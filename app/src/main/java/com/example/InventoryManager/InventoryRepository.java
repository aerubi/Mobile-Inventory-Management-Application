package com.example.InventoryManager;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class InventoryRepository {

    private DatabaseHelper dbHelper;
    private int currentUserId = -1; // Track logged-in user

    public InventoryRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ================= USER =================

    public boolean loginUser(String username, String password) {

        String storedPassword = dbHelper.getUserPassword(username);

        if (storedPassword == null) return false;

        boolean valid = PasswordUtils.verifyPassword(password, storedPassword);

        if (valid) {
            currentUserId = dbHelper.getUserId(username); // store user session
        }

        return valid;
    }

    public boolean createUser(String username, String password) {

        String securePassword = PasswordUtils.generateSecurePassword(password);

        return dbHelper.insertUser(username, securePassword);
    }

    // ================= INVENTORY =================

    public boolean addItem(String name, int quantity) {
        return dbHelper.addItem(name, quantity, currentUserId);
    }

    public ArrayList<InventoryItem> getAllItems() {

        ArrayList<InventoryItem> items = new ArrayList<>();
        Cursor cursor = dbHelper.getAllItems(currentUserId);

        while (cursor.moveToNext()) {
            items.add(new InventoryItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            ));
        }

        cursor.close();
        return items;
    }

    public boolean updateItem(int id, String name, int quantity) {
        return dbHelper.updateItem(id, name, quantity);
    }

    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }
}