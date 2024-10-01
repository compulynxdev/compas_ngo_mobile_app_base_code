package com.compastbc.core.utils.pagination;

import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.core.R;

/**
 * Created by hemant
 * Date: 12/05/18
 */

public final class FooterLoader extends RecyclerView.ViewHolder {

    public final ProgressBar mProgressBar;

    public FooterLoader(@NonNull View itemView) {
        super(itemView);
        mProgressBar = itemView.findViewById(R.id.progressbar);
    }
}
