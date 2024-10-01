package com.compastbc.ui.reports.expandable_adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;

public class HeaderViewHolderTwo extends RecyclerView.ViewHolder {

    private ImageView expandButton;
    private TextView headerTextView;

    public HeaderViewHolderTwo(View view) {
        super(view);
        headerTextView = view.findViewById(R.id.header_text_view);
        expandButton = view.findViewById(R.id.expand_button);
    }

    public void setHeaderText(String text) {
        headerTextView.setText(text);
    }


    public void setExpandButtonImage(Drawable drawable) {
        expandButton.setImageDrawable(drawable);
    }

    public void setExpandButtonRotation(int rotation) {
        expandButton.setRotation(rotation);
    }
}