package com.compastbc.ui.transaction.transaction.cart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.compastbc.R;
import com.compastbc.core.data.db.model.PurchasedProducts;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter {

    private final List<PurchasedProducts> list;
    private final Context context;

    CartAdapter(Context context, List<PurchasedProducts> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ViewHolder viewHolder = (ViewHolder) holder;
        PurchasedProducts bean = list.get(position);
        viewHolder.title.setText(bean.getServiceName());
        viewHolder.quantity.setText(context.getString(R.string.qty).concat(" ").concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(bean.getQuantity())).concat(" (").concat(bean.getUom()).concat(")")));
        Bitmap decodedByte;
        byte[] decodedString = Base64.decode(bean.getServiceImage(), Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        viewHolder.image.setImageBitmap(decodedByte);
        viewHolder.checkBox.setTag(R.id.holder_id, bean.getId());

        viewHolder.price.setText(context.getString(R.string.price).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(bean.getTotalPrice()))));
        viewHolder.checkBox.setOnClickListener(v -> showThemed(v, viewHolder));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showThemed(final View v, final ViewHolder holder) {
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.removeSure))
                .content(context.getString(R.string.removeproduct))
                .positiveText(context.getString(R.string.yes))
                .negativeText(context.getString(R.string.no))
                .positiveColorRes(R.color.google)
                .negativeColorRes(R.color.google)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(android.R.color.black)
                .contentColorRes(android.R.color.black)
                .backgroundColorRes(R.color.white)
                .dividerColorRes(R.color.light_blue)
                .positiveColor(context.getResources().getColor(R.color.black))
                .negativeColor(context.getResources().getColor(R.color.black))
                .theme(Theme.DARK)
                .onAny((dialog, which) -> {
                    if (which.name().equals("NEGATIVE")) {
                        holder.checkBox.setChecked(false);
                    } else {
                        ((CartActivity) context).Update(v.getTag(R.id.holder_id).toString());
                    }
                })
                .show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final CheckBox checkBox;
        private final TextView title;
        private final TextView quantity;
        private final TextView price;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.main);
            checkBox = itemView.findViewById(R.id.cb_remove);
            title = itemView.findViewById(R.id.title);
            quantity = itemView.findViewById(R.id.qty);
            price = itemView.findViewById(R.id.price);
        }
    }
}
