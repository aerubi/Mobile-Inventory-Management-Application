package com.example.InventoryManager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * InventoryAdapter
 *
 * Binds inventory data to RecyclerView cards.
 * Handles:
 * - Displaying item data
 * - Deleting items
 * - Editing items via dialog
 *
 * Uses InventoryRepository for database operations.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<InventoryItem> items;
    private InventoryRepository repository;

    public InventoryAdapter(Context context,
                            ArrayList<InventoryItem> items,
                            InventoryRepository repository) {

        this.context = context;
        this.items = items;
        this.repository = repository;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate layout for each inventory card
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_inventory, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        InventoryItem item = items.get(position);

        // Bind data to UI elements
        holder.tvItemName.setText(item.getName());
        holder.tvQuantity.setText("Quantity: " + item.getQuantity());

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> {

            // Remove item from database
            repository.deleteItem(item.getId());

            // Remove item from local list
            items.remove(position);

            // Notify RecyclerView of item removal
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        });

        // Open edit dialog when clicking item
        holder.itemView.setOnClickListener(v -> openEditDialog(position));
    }

    /**
     * Opens a dialog allowing the user to edit item details.
     * Includes validation to prevent invalid input.
     */
    private void openEditDialog(int position) {

        InventoryItem currentItem = items.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Item");

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_item, null);

        EditText etName = view.findViewById(R.id.etEditName);
        EditText etQuantity = view.findViewById(R.id.etEditQuantity);

        // Pre-fill fields with current values
        etName.setText(currentItem.getName());
        etQuantity.setText(String.valueOf(currentItem.getQuantity()));

        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {

            String newName = etName.getText().toString().trim();
            String quantityText = etQuantity.getText().toString().trim();

            // Validate inputs
            if (newName.isEmpty()) {
                Toast.makeText(context, "Item name required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantityText.isEmpty()) {
                Toast.makeText(context, "Quantity required", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQuantity;

            try {
                // Convert safely to integer
                newQuantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Quantity must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newQuantity < 0) {
                Toast.makeText(context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update database
            repository.updateItem(
                    currentItem.getId(),
                    newName,
                    newQuantity
            );

            // Update local list
            items.set(position, new InventoryItem(
                    currentItem.getId(),
                    newName,
                    newQuantity
            ));

            notifyItemChanged(position);

            // Trigger low inventory check if available
            if (context instanceof InventoryActivity) {
                ((InventoryActivity) context).checkLowInventory(items);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Holds references to UI elements for each item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemName, tvQuantity;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}