package com.compastbc.ui.login.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.ClickListener;
import com.compastbc.core.data.db.model.Language;

import java.util.List;
import java.util.Locale;


/**
 * Created by hemant
 * Date: 17/4/18
 * Time: 4:03 PM
 */

public class LanguageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public int lastPos = -1;
    private final List<Language> list;
    private final ClickListener clickListener;

    LanguageAdapter(List<Language> list, ClickListener clickListener) {
        this.list = list;
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rvHolder, int position) {
        LanguageAdapter.ViewHolder holder = ((LanguageAdapter.ViewHolder) rvHolder);

        Language bean = list.get(position);

        holder.frameCheck.setVisibility(bean.isSelected ? View.VISIBLE : View.GONE);

        String lng = bean.getLangName().substring(0, 2).toLowerCase();
        Locale loc = new Locale(lng);
        String name = loc.getDisplayLanguage(loc);
        holder.tvName.setText(name);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvName;
        private final FrameLayout frameCheck;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            frameCheck = v.findViewById(R.id.frameCheck);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(@NonNull final View view) {
            int tempPos = getAdapterPosition();
            if (tempPos != -1 && clickListener != null) {
                if (lastPos != -1) {
                    list.get(lastPos).isSelected = false;
                    notifyItemChanged(lastPos);
                }

                clickListener.onItemClick(getAdapterPosition());
                list.get(tempPos).isSelected = true;
                notifyItemChanged(tempPos);

                lastPos = tempPos;
            }

        }
    }
}

