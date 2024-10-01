package com.compastbc.ui.transaction.transaction.services.cashdialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.PurchasedProductsDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.ui.base.BaseDialog;

import org.greenrobot.greendao.query.QueryBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CashDialog extends BaseDialog implements View.OnClickListener {
    private static Services service;
    private static OnCashDialogInteraction Listener;
    private EditText input;
    private Button btn_purchase, btn_cancel;

    public static CashDialog newInstance(Services services, OnCashDialogInteraction mListener) {
        CashDialog fragment = new CashDialog();
        Bundle args = new Bundle();
        Listener = mListener;
        service = services;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
        btn_purchase.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    protected void setUp(View view) {
        ImageView imageView = view.findViewById(R.id.icon);
        TextView tvTitle = view.findViewById(R.id.title);
        input = view.findViewById(R.id.input);
        btn_purchase = view.findViewById(R.id.purchase);
        btn_cancel = view.findViewById(R.id.cancel);

        Bitmap decodedByte;
        byte[] decodedString = Base64.decode(service.getServiceImage(), Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);
        tvTitle.setText(service.getServiceName());
        input.setHint(R.string.Cash);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.purchase:
                if (input.getText().toString().isEmpty()) {
                    showMessage(R.string.PleaseEnterAmount);
                } else if(!input.getText().toString().isEmpty() && Long.parseLong(input.getText().toString()) <= 0){
                    showMessage(R.string.cash_amount_greater);
                }else {
                        Double amount = Double.parseDouble(input.getText().toString());
                        Topups topups = getDataManager().getTopupDetails();
                        Double remainingAmount = Double.parseDouble(topups.getVouchervalue()) - amount;
                        if (remainingAmount >= 0) {
                            topups.setVouchervalue(String.valueOf(remainingAmount));
                            QueryBuilder<PurchasedProducts> db = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder();
                            db.where(PurchasedProductsDao.Properties.ServiceId.eq(service.getServiceId()), PurchasedProductsDao.Properties.Uom.eq(getDataManager().getCurrency()));
                            PurchasedProducts products = db.build().unique();
                            if (products == null) {
                                PurchasedProducts purchasedProducts = new PurchasedProducts();
                                purchasedProducts.setCardNumber(getDataManager().getTopupDetails().getCardnumber());
                                purchasedProducts.setServiceId(service.getServiceId());
                                purchasedProducts.setProgrammeId(getDataManager().getTopupDetails().getProgrammeid());
                                purchasedProducts.setUom(getDataManager().getCurrency());
                                purchasedProducts.setTotalPrice(String.valueOf(amount));
                                purchasedProducts.setVoucherId(getDataManager().getTopupDetails().getVoucherid());
                                purchasedProducts.setQuantity("1");
                                purchasedProducts.setMaxPrice(String.valueOf(amount));
                                purchasedProducts.setServiceImage(service.getServiceImage());
                                purchasedProducts.setServiceName(service.getServiceName());
                                getDataManager().setTopupDetails(topups);
                                getDataManager().getDaoSession().getPurchasedProductsDao().insert(purchasedProducts);
                            } else {
                                String productPrice = products.getTotalPrice();
                                products.setTotalPrice(String.valueOf(amount + Double.parseDouble(productPrice)));
                                int i = Integer.parseInt(products.getQuantity()) + 1;
                                products.setQuantity(String.valueOf(i));
                                products.setMaxPrice(String.valueOf(amount));
                                getDataManager().setTopupDetails(topups);
                                getDataManager().getDaoSession().getPurchasedProductsDao().update(products);
                            }
                            dismissDialog("Cash Dialog");
                            Listener.onPurchase();
                        } else {
                            dismissDialog("Cash Dialog");
                            showMessage(R.string.AmountExceeds);
                        }
                }
                break;

            case R.id.cancel:
                dismissDialog("Cash Dialog");
                break;

        }

    }

    public interface OnCashDialogInteraction {
        void onPurchase();
    }
}
