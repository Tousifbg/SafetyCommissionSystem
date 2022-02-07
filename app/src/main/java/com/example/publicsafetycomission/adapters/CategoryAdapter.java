package com.example.publicsafetycomission.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.publicsafetycomission.R;

import java.util.ArrayList;

public class CategoryAdapter extends ArrayAdapter<String> {
    ArrayList<String> categ_id;
    ArrayList<String> categ_name ;
    String[] temp1;
    Context context;

    public CategoryAdapter(Context context, ArrayList<String> categ_id, ArrayList<String> categ_name,
                           String[] temp1) {
        super(context, R.layout.cat_dialog_adpater,temp1);
        this.categ_id = categ_id;
        this.categ_name = categ_name;
        this.temp1=temp1;
        this.context = context;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=layoutInflater.inflate(R.layout.cat_dialog_adpater,null,true);
        TextView cat_name=(TextView) convertView.findViewById(R.id.cat_name);
        TextView nameurdu=(TextView) convertView.findViewById(R.id.nameurdu);
        TextView id=(TextView) convertView.findViewById(R.id.id);

        cat_name.setText(categ_name.get(position));

        return convertView;
    }
}
