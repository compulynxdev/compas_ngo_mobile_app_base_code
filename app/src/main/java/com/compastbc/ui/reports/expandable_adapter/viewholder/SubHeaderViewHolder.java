package com.compastbc.ui.reports.expandable_adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;

public class SubHeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView subHeaderTextView;

    public SubHeaderViewHolder(View view) {
        super(view);
        subHeaderTextView = view.findViewById(R.id.sub_header_text_view);
    }

    public void setSubHeaderText(String text) {
        subHeaderTextView.setText(text);
    }

}
