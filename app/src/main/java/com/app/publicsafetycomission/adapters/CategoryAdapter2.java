package com.app.publicsafetycomission.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.publicsafetycomission.ComplaintArea;
import com.app.publicsafetycomission.R;
import com.app.publicsafetycomission.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter2 extends RecyclerView.Adapter<CategoryAdapter2.ViewHolder>
            implements Filterable {
    private Context mContext;
    private List<CategoryModel> categoryModels;
    Dialog dialog;
    public List<CategoryModel> mFilteredList;

    public CategoryAdapter2(Context mContext, List<CategoryModel> categoryModels, Dialog dialog) {
        this.mContext = mContext;
        this.categoryModels = categoryModels;
        this.dialog = dialog;
        this.mFilteredList = categoryModels;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext)
                .inflate(R.layout.dist_dialog_adpater2,parent,false);

        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CategoryModel categoryMod = mFilteredList.get(position);

        holder.district_name.setText(categoryMod.getComplaint_category_name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((ComplaintArea) mContext).recyclerTouchMethod2(categoryMod.getComplaint_category_id(),
                        categoryMod.getComplaint_category_name());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    mFilteredList = categoryModels;
                } else {

                    List<CategoryModel> filteredList = new ArrayList<>();

                    for (CategoryModel androidVersion : categoryModels) {

                        if (androidVersion.getComplaint_category_name().toLowerCase().
                                contains(charString)) {

                            filteredList.add(androidVersion);
                        }else if (androidVersion.getComplaint_category_name().
                                toLowerCase().contains(charString)){

                            filteredList.add(androidVersion);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<CategoryModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView district_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            district_name = itemView.findViewById(R.id.district_name);
        }
    }
}
