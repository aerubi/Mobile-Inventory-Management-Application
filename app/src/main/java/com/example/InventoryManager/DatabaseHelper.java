package com.example.InventoryManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles all database operations including:
 * - User login storage
 * - Inventory CRUD operations
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "InventoryDB";
    private static final int DATABASE_VERSION = 2; // ⬅️ bumped version

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    // Inventory Table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String ITEM_ID = "item_id";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_QUANTITY = "quantity";
    private static final String USER_ID_FK = "user_id"; // ⬅️ NEW

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("PRAGMA foreign_keys=ON;");

        String createUsers = "CREATE TABLE " + TABLE_USERS + "("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME + " TEXT UNIQUE NOT NULL, "
                + PASSWORD + " TEXT NOT NULL)";

        // Updated Table to add relationship
        String createInventory = "CREATE TABLE " + TABLE_INVENTORY + "("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_NAME + " TEXT NOT NULL, "
                + ITEM_QUANTITY + " INTEGER NOT NULL CHECK(" + ITEM_QUANTITY + " >= 0), "
                + USER_ID_FK + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE)";

        db.execSQL(createUsers);
        db.execSQL(createInventory);

        // Optional performance improvement
        db.execSQL("CREATE INDEX idx_item_name ON " + TABLE_INVENTORY + "(" + ITEM_NAME + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ================= USER METHODS =================

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + USER_ID + " FROM " + TABLE_USERS + " WHERE " + USERNAME + "=?",
                new String[]{username}
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }

        cursor.close();
        return -1;
    }

    public String getUserPassword(String username) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT password FROM " + TABLE_USERS + " WHERE username=?",
                new String[]{username}
        );

        if (cursor.moveToFirst()) {
            String pass = cursor.getString(0);
            cursor.close();
            return pass;
        }

        cursor.close();
        return null;
    }

    // ================= INVENTORY METHODS =================

    // Updated to add user_id
    public boolean addItem(String name, int quantity, int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, name);
        values.put(ITEM_QUANTITY, quantity);
        values.put(USER_ID_FK, userId);

        long result = db.insert(TABLE_INVENTORY, null, values);
        return result != -1;
    }

    // Updated to filter by user
    public Cursor getAllItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_INVENTORY + " WHERE " + USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)}
        );
    }

    //  Improved query
    public Cursor getFilteredItems(int userId, String search, String sortType) {

        String orderBy = ITEM_NAME + " COLLATE NOCASE ASC";

        if (sortType.equals("QUANTITY")) {
            orderBy = ITEM_QUANTITY + " ASC";
        }

        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_INVENTORY +
                        " WHERE " + USER_ID_FK + "=? AND " + ITEM_NAME + " LIKE ?" +
                        " ORDER BY " + orderBy,
                new String[]{
                        String.valueOf(userId),
                        "%" + search + "%"
                }
        );
    }

    public boolean updateItem(int id, String name, int quantity) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, name);
        values.put(ITEM_QUANTITY, quantity);

        int result = db.update(TABLE_INVENTORY, values,
                ITEM_ID + "=?",
                new String[]{String.valueOf(id)});

        return result > 0;
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_INVENTORY,
                ITEM_ID + "=?",
                new String[]{String.valueOf(id)});
    }
}