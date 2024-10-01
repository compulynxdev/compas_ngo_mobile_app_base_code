package com.compastbc.ui.transaction.transaction.vouchers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.Vouchers;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;
import com.compastbc.ui.transaction.transaction.services.ServiceActivity;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class VouchersActivity extends BaseActivity implements VouchersMvpView, VouchersAdapter.ClickListener {
    private VouchersMvpPresenter<VouchersMvpView> vouchersMvpPresenter;
    private RecyclerView recyclerView;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, VouchersActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vouchers);
        vouchersMvpPresenter = new VouchersPresenter<>(getDataManager());
        vouchersMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        recyclerView = findViewById(R.id.voucherRecyclerView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.Vouchers);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        if (getDataManager().getConfigurableParameterDetail().isOnline())
            vouchersMvpPresenter.getTopups();

        else
            vouchersMvpPresenter.getVouchersByProgramId(Integer.parseInt(getDataManager().getTopupDetails().getProgrammeid()));
    }

    @Override
    public void onClick(View v, int voucherId) {
        createLog("Vouchers Activity", "Voucher Selected");
        if (getDataManager().getConfigurableParameterDetail().isOnline())
            getDataManager().setCurrency(getDataManager().getTopupDetails().getProgramCurrency());
        startActivity(ServiceActivity.getStartIntent(VouchersActivity.this));
    }

    @Override
    public void showVouchers(List<Vouchers> list) {
        if (!list.isEmpty()) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            VouchersAdapter adapter = new VouchersAdapter(list,this);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);
        } else {
            showMessage(R.string.NoVouchers);
        }
    }


    @Override
    public void onBackPressed() {
        createLog("Vouchers Activity", "Back");
        Intent i = MainActivity.getStartIntent(VouchersActivity.this);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
