package com.example.mobilalkfejlprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<Item> Items;
    private ArrayList<Item> ItemsAll;
    private Context Context;
    private int LastPosition = -1;

    public ItemAdapter(Context context, ArrayList<Item> items){
        Items = items;
        ItemsAll = items;
        Context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(Context).inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item current = Items.get(position);
        holder.bindTo(current);

        if(holder.getAdapterPosition() > LastPosition){
            Animation anim = AnimationUtils.loadAnimation(Context, R.anim.slide_in_row);
            holder.itemView.startAnimation(anim);
            LastPosition = holder.getAbsoluteAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return Items.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    private Filter itemFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Item> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0){
                results.count = ItemsAll.size();
                results.values = ItemsAll;
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Item item : ItemsAll){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Items = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView Name;
        private TextView Description;
        private TextView Price;

        public ViewHolder(View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.foodNameTextView);
            Description = itemView.findViewById(R.id.foodDescriptionTextView);
            Price = itemView.findViewById(R.id.foodPriceTextView);

            itemView.findViewById(R.id.addToCartButton).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Item item = Items.get(position);
                        ((MainLoggedInActivity) Context).updateAlertIcon(item);
                    }
                }
            });
        }

        public void bindTo(Item current) {
            Name.setText(current.getName());
            Description.setText(current.getDescription());
            Price.setText(current.getPrice());
        }
    }
}
