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
import com.app.publicsafetycomission.model.DistrictModel;

import java.util.ArrayList;
import java.util.List;

public class DistrictAdapter2 extends RecyclerView.Adapter<DistrictAdapter2.ViewHolder>
            implements Filterable {
    private Context mContext;
    private List<DistrictModel> districtModels;
    Dialog dialog;
    public List<DistrictModel> mFilteredList;

    public DistrictAdapter2(Context mContext, List<DistrictModel> districtModels, Dialog dialog) {
        this.mContext = mContext;
        this.districtModels = districtModels;
        this.dialog = dialog;
        this.mFilteredList = districtModels;
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
        final DistrictModel districtMod = mFilteredList.get(position);

        holder.district_name.setText(districtMod.getDistrict_name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((ComplaintArea) mContext).recyclerTouchMethod(districtMod.getDistrict_id(),
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
