package com.example.publicsafetycomission.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.publicsafetycomission.R;
import com.example.publicsafetycomission.model.RegisteredComplaintModel;

import java.util.List;

public class MyRegisteredComplaintsAdapter extends
        RecyclerView.Adapter<MyRegisteredComplaintsAdapter.ViewHolder> {

    private Context context;
    private List<RegisteredComplaintModel> registeredComplaintModels;
    private Dialog clickToCall;

    public MyRegisteredComplaintsAdapter(Context context, List<RegisteredComplaintModel> registeredComplaintModels) {
        this.context = context;
        this.registeredComplaintModels = registeredComplaintModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.registered_complaint_itemview,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RegisteredComplaintModel registeredComplaintModel = registeredComplaintModels.get(position);
        holder.council_txt.setText(registeredComplaintModel.getComplaint_council());
        holder.complainant_name.setText(registeredComplaintModel.getComplainant_name());
        holder.district_name.setText(registeredComplaintModel.getDistrict_name());
        holder.category_name.setText(registeredComplaintModel.getComplaint_category_name());
        holder.complaint_detail_txt.setText(registeredComplaintModel.getComplaint_detail());
        holder.txtDate.setText(registeredComplaintModel.getComplaint_entry_timestamp());
        holder.complaint_status.setText(registeredComplaintModel.getComplaint_status_title());

        String color = registeredComplaintModel.getComplaint_status_color();

        holder.complaint_status.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
    }

    @Override
    public int getItemCount() {
        return registeredComplaintModels.size();

    }

    public class ViewHolder  extends RecyclerView.ViewHolder{

        TextView council_txt,complainant_name,district_name,category_name,complaint_status,
                txtDate,complaint_detail_txt;

        int color;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            council_txt = itemView.findViewById(R.id.council_txt);
            complainant_name = itemView.findViewById(R.id.complainant_name);
            district_name = itemView.findViewById(R.id.district_name);
            category_name = itemView.findViewById(R.id.category_name);
            complaint_status = itemView.findViewById(R.id.complaint_status);
            txtDate = itemView.findViewById(R.id.txtDate);
            complaint_detail_txt = itemView.findViewById(R.id.complaint_detail_txt);
        }
    }
}
