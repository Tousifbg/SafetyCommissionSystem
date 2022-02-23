package com.example.publicsafetycomission.adapters;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.publicsafetycomission.ComplaintArea;
import com.example.publicsafetycomission.R;
import com.example.publicsafetycomission.Registeration;
import com.example.publicsafetycomission.model.DistrictModel;

import java.util.ArrayList;
import java.util.List;

public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.ViewHolder>
            implements Filterable {

    private Context mContext;
    private List<DistrictModel> districtModels;
    Dialog dialog;
    public List<DistrictModel> mFilteredList;

    public DistrictAdapter(Context mContext, List<DistrictModel> districtModels, Dialog dialog) {
        this.mContext = mContext;
        this.districtModels = districtModels;
        this.dialog = dialog;
        this.mFilteredList = districtModels;
    }


    @NonNull
    @Override
    public DistrictAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext)
                .inflate(R.layout.dist_dialog_adpater2,parent,false);

        return new DistrictAdapter.ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull DistrictAdapter.ViewHolder holder, int position) {
        final DistrictModel districtMod = mFilteredList.get(position);

        holder.district_name.setText(districtMod.getDistrict_name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((Registeration) mContext).recyclerTouchMethod(districtMod.getDistrict_id(),
                        districtMod.getDistrict_name());
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

                    mFilteredList = districtModels;
                } else {

                    List<DistrictModel> filteredList = new ArrayList<>();

                    for (DistrictModel androidVersion : districtModels) {

                        if (androidVersion.getDistrict_name().toLowerCase().contains(charString)) {

                            filteredList.add(androidVersion);
                        }else if (androidVersion.getDistrict_name().toLowerCase().contains(charString)){

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
                mFilteredList = (ArrayList<DistrictModel>) filterResults.values;
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
