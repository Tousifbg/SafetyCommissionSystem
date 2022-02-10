package com.example.publicsafetycomission.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.publicsafetycomission.R;

import java.util.ArrayList;

public class DistrictAdapter2 extends ArrayAdapter<String> {
    ArrayList<String> dist_id;
    ArrayList<String> dist_name ;
    String[] temp3;
    Context context;

    public DistrictAdapter2(Context context, ArrayList<String> dist_id, ArrayList<String> dist_name,
                            String[] temp3) {
        super(context, R.layout.dist_dialog_adpater2,temp3);
        this.dist_id = dist_id;
        this.dist_name = dist_name;
        this.temp3=temp3;
        this.context = context;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=layoutInflater.inflate(R.layout.dist_dialog_adpater2,null,true);
        TextView district_name=(TextView) convertView.findViewById(R.id.district_name);
        TextView nameurdu=(TextView) convertView.findViewById(R.id.nameurdu);
        TextView id=(TextView) convertView.findViewById(R.id.id);

        district_name.setText(dist_name.get(position));

        return convertView;
    }
}
