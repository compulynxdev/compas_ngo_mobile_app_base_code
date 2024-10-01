package com.compastbc.ui.transaction.transaction.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.compastbc.R;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.transaction.transaction.cart.CartActivity;
import com.compastbc.ui.transaction.transaction.services.cashdialog.CashDialog;
import com.compastbc.ui.transaction.transaction.services.servicedialog.ServiceDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class ServiceActivity extends BaseActivity implements ServiceMvpView, ServiceAdapter.ItemClickListener {

    private ServiceMvpPresenter<ServiceMvpView> servicePresenterServiceMvpPresenter;
    private RecyclerView serviceRecyclerView;
    private TextView tvVoucherValue;
    private FloatingActionButton btn_cart;
    private Services services;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ServiceActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        servicePresenterServiceMvpPresenter = new ServicePresenter<>(getDataManager());
        servicePresenterServiceMvpPresenter.onAttach(this);
        setUp();

        btn_cart.setOnClickListener(v -> {
            createLog("Service Activity", "Click on cart");
            Intent i = CartActivity.getStartIntent(ServiceActivity.this);
            startActivity(i);
        });
    }

    @Override
    protected void setUp() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        serviceRecyclerView = findViewById(R.id.serviceRecyclerView);
        btn_cart = findViewById(R.id.floating_action_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        tvVoucherValue = findViewById(R.id.tvVoucherValue);
        tvVoucherValue.setText(getDataManager().getCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(getDataManager().getTopupDetails().getVouchervalue()))));
        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.Services);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        servicePresenterServiceMvpPresenter.getCommodities();
    }

    @Override
    public void showServices(List<Services> servicesList) {
        serviceRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ServiceAdapter adapter = new ServiceAdapter(servicesList);
        adapter.setClickListener(this);
        serviceRecyclerView.setAdapter(adapter);
    }

    @Override
    public void showDialog(List<ServicePrices> servicePrices) {
        ServiceDialog.newInstance(services, servicePrices, () -> tvVoucherValue.setText(getDataManager().getCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(getDataManager().getTopupDetails().getVouchervalue()))))).show(getSupportFragmentManager(), "Service Dialog");
    }

    @Override
    public void onClick(View v, Services service) {
        createLog("Service Activity", "Commodity Selected");
        services = service;
        if (services.getServiceType().equalsIgnoreCase("Commodity"))
            servicePresenterServiceMvpPresenter.getUoms(services.getServiceId());
        else {
            CashDialog.newInstance(service, () -> tvVoucherValue.setText(getDataManager().getCurrency().concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(getDataManager().getTopupDetails().getVouchervalue()))))).show(getSupportFragmentManager(), "Cash Dialog");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUp();
    }

    @Override
    public void onBackPressed() {
        createLog("Service Activity", "Back");
        List<PurchasedProducts> products = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder().list();
        if (products.size() > 0) {
            showMessage(R.string.EmptyCart);
        } else {
            super.onBackPressed();
        }

    }
}
