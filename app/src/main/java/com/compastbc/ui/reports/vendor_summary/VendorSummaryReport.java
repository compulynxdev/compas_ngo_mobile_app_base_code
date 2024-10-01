package com.compastbc.ui.reports.vendor_summary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.TxnCount;
import com.compastbc.ui.base.BaseActivity;

import java.util.List;
import java.util.Locale;

public class VendorSummaryReport extends BaseActivity implements VendorSummaryMvpView{

    private VendorSummaryMvpPresenter presenter;
    private LinearLayout layout;
    private TextView tv_nodata;

    public static Intent getStartIntent(Context context) {
        return new Intent(context,VendorSummaryReport.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_summary_report);

        presenter = new VendorSummaryPresenter(getDataManager());
        presenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.vendor_summary);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());
        tv_nodata = findViewById(R.id.noData);
        layout = findViewById(R.id.ll_top);
        presenter.getList();
    }

    @Override
    public void showData(List<TxnCount> list, long ttlCount) {
        if (list!=null && !list.isEmpty()){
            tv_nodata.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            VendorSummaryAdapter adapter = new VendorSummaryAdapter(list);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setAdapter(adapter);
            TextView tv_count = findViewById(R.id.total);
            tv_count.setText(getString(R.string.TotalTransaction).concat(" ").concat(String.format(Locale.getDefault(), "%d", ttlCount)));
        }else {
            tv_nodata.setVisibility(View.VISIBLE);
            layout.setVisibility(View.GONE);
        }
    }
}
