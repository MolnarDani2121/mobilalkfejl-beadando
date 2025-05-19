package com.example.mobilalkfejlprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context Context;
    private ArrayList<Item> ItemList;

    public CartAdapter(Context context, ArrayList<Item> itemList) {
        this.Context = context;
        this.ItemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(Context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item currentItem = ItemList.get(position);
        holder.bindTo(currentItem);
    }

    @Override
    public int getItemCount() {
        return ItemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemNameTextView;
        private TextView itemPriceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
        }

        public void bindTo(Item current) {
            itemNameTextView.setText(current.getName());
            itemPriceTextView.setText(current.getPrice());
        }
    }
} 