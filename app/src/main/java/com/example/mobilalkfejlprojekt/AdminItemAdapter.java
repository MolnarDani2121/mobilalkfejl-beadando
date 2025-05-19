package com.example.mobilalkfejlprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AdminItemAdapter extends RecyclerView.Adapter<AdminItemAdapter.ViewHolder> {
    private ArrayList<Item> items;
    private Context context;
    private OnItemDeleteListener deleteListener;
    private OnItemEditListener editListener;

    public interface OnItemDeleteListener {
        void onItemDelete(int position, String documentId);
    }

    public interface OnItemEditListener {
        void onItemEdit(int position, Item item);
    }

    public AdminItemAdapter(Context context, ArrayList<Item> items) {
        this.context = context;
        this.items = items;
        if (context instanceof OnItemDeleteListener) {
            this.deleteListener = (OnItemDeleteListener) context;
        }
        if (context instanceof OnItemEditListener) {
            this.editListener = (OnItemEditListener) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item currentItem = items.get(position);
        holder.bindTo(currentItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(ArrayList<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView descriptionTextView;
        private TextView priceTextView;
        private Button deleteButton;
        private Button editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.itemNameTextView);
            descriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            priceTextView = itemView.findViewById(R.id.itemPriceTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onItemDelete(position, items.get(position).getDocumentId());
                }
            });

            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && editListener != null) {
                    editListener.onItemEdit(position, items.get(position));
                }
            });
        }

        public void bindTo(Item item) {
            nameTextView.setText(item.getName() != null ? item.getName() : "null");
            descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "null");
            priceTextView.setText(item.getPrice() != null ? item.getPrice() : "null");
        }
    }
}