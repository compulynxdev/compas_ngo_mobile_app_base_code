package com.compastbc.ui.beneficiary.list_beneficiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.ClickListener;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.network.Webservices;
import com.compastbc.core.data.network.model.BeneficiaryFilterBean;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.pagination.RecyclerViewScrollListener;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.list_beneficiary.detail.BeneficiaryDetailActivity;
import com.compastbc.ui.beneficiary.list_beneficiary.dialog.BeneficiaryFilterDialog;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryListActivity extends BaseActivity implements BeneficiaryListMvpView {

    private BeneficiaryListMvpPresenter<BeneficiaryListMvpView> beneficiaryListMvpPresenter;

    private TextView tvNoData;
    private BeneficiaryListAdapter beneficiaryListAdapter;
    private List<BeneficiaryListResponse.ContentBean> bnfList;
    private List<Beneficiary> bnfListDB;
    private boolean isOnline;

    private boolean isFilter = false;
    private BeneficiaryFilterBean filterBean;

    private int page = 0;
    private RecyclerViewScrollListener scrollListener;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, BeneficiaryListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_list);
        beneficiaryListMvpPresenter = new BeneficiaryListPresenter<>(getDataManager(), this);
        beneficiaryListMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tv_title = findViewById(R.id.tvTitle);
        tv_title.setText(R.string.BeneficiaryList);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        tvNoData = findViewById(R.id.tv_no_data);
        //view init
        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView rviewBnfList = findViewById(R.id.recycler_view);
//setupSearch here
        beneficiaryListMvpPresenter.setupSearch(etSearch);

        isOnline = getDataManager().getConfigurableParameterDetail().isOnline();
        if (isOnline) {
            bnfList = new ArrayList<>();
            beneficiaryListAdapter = new BeneficiaryListAdapter(bnfList, new ClickListener() {
                @Override
                public void onEditClick(int pos) {
                    startActivity(true, pos);
                }

                @Override
                public void onDeleteClick(int pos) {

                }

                @Override
                public void onItemClick(int pos) {
                    startActivity(false, pos);
                }
            });
        } else {
            bnfListDB = new ArrayList<>();
            beneficiaryListAdapter = new BeneficiaryListAdapter(true, bnfListDB, new ClickListener() {
                @Override
                public void onEditClick(int pos) {
                    startActivity(true, pos);
                }

                @Override
                public void onDeleteClick(int pos) {

                }

                @Override
                public void onItemClick(int pos) {
                    startActivity(false, pos);
                }
            });
        }

        beneficiaryListAdapter.setHasStableIds(true);
        rviewBnfList.setAdapter(beneficiaryListAdapter);

        scrollListener = new RecyclerViewScrollListener() {
            @Override
            public void onLoadMore() {
                beneficiaryListAdapter.showLoading(true);
                beneficiaryListAdapter.notifyDataSetChanged();
                if (isOnline)
                    page += 1;
                else page += AppConstants.LIMIT;

                if (isFilter)
                    doFilterBeneficiary(filterBean);
                else
                    beneficiaryListMvpPresenter.doGetBeneficiaryList(false, page, etSearch.getText().toString());
            }
        };
        rviewBnfList.addOnScrollListener(scrollListener);

        beneficiaryListMvpPresenter.doGetBeneficiaryList(true, page, "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.item_filter) {
            createLog("Beneficiary List", "Filter");
            BeneficiaryFilterDialog.newInstance(filterBean, new BeneficiaryFilterDialog.BeneficiaryFilterCallback() {
                @Override
                public void reset() {
                    isFilter = false;
                    filterBean = null;
                    if (isOnline) bnfList.clear();
                    else bnfListDB.clear();
                    scrollListener.onDataCleared();
                    page = 0;
                    beneficiaryListMvpPresenter.doGetBeneficiaryList(true, page, "");
                }

                @Override
                public void filterData(BeneficiaryFilterBean tmpFilterBean) {
                    //http://192.168.43.58:8086/compas/ngo/rest/online/filter_beneficiary/1?page=0&size=20&sort=firstName&search=
                    // &idPassPortNo=&firstName=hem&dateOfBirth=2019-08-01&gender=M&bioStatus=true&isAdvanceSearch=true
                    isFilter = true;
                    if (isOnline) bnfList.clear();
                    else bnfListDB.clear();
                    scrollListener.onDataCleared();
                    page = 0;
                    filterBean = tmpFilterBean;
                    doFilterBeneficiary(tmpFilterBean);
                }
            }).show(getSupportFragmentManager(), "BnfFilterDialog");
        }

        return super.onOptionsItemSelected(item);
    }

    private void doFilterBeneficiary(BeneficiaryFilterBean tmpFilterBean) {
        if (isOnline) {
            String url = Webservices.WEB_BNF_LIST_N_FILTER.concat(getDataManager().getUserDetail().getLocationId()
                    .concat("?page=".concat(String.valueOf(page))
                            .concat("&size=".concat(String.valueOf(AppConstants.LIMIT))
                                    .concat("&sort=firstName")
                                    .concat("&search=").concat("").concat("&idPassPortNo=").concat(tmpFilterBean.getTmpId())
                                    .concat("&firstName=").concat(tmpFilterBean.getTmpName()).concat("&dateOfBirth=").concat(tmpFilterBean.getTmpDob())
                                    .concat("&gender=").concat(filterBean.getTmpGender())
                                    .concat("&bioStatus=").concat(filterBean.getTmpBioStatus()).concat("&isAdvanceSearch=true"))));

            beneficiaryListMvpPresenter.doGetBeneficiaryList(true, page, "", url);
        } else
            beneficiaryListMvpPresenter.doGetBeneficiaryList(true, page, tmpFilterBean);
    }


    private void startActivity(boolean isEdit, int pos) {
        createLog("Beneficiary List", "Click on details");
        Intent intent = BeneficiaryDetailActivity.getStartIntent(getActivity());
        intent.putExtra("EditMode", isEdit);
        if (isOnline) {
            BeneficiaryListResponse.ContentBean bean = bnfList.get(pos);
            intent.putExtra("BnfDetail", bean);
            intent.putExtra("pos", pos);
        } else {
            intent.putExtra("IdentityNo", bnfListDB.get(pos).getIdentityNo());
            intent.putExtra("pos", pos);
        }
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onDestroy() {
        beneficiaryListMvpPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void hideFooterLoader() {
        beneficiaryListAdapter.showLoading(false);
        beneficiaryListAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateUI(List<BeneficiaryListResponse.ContentBean> contentBeanList) {
        bnfList.addAll(contentBeanList);
        beneficiaryListAdapter.notifyDataSetChanged();
        tvNoData.setVisibility(bnfList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateUIFromDB(List<Beneficiary> beneficiaryList) {
        bnfListDB.addAll(beneficiaryList);
        beneficiaryListAdapter.notifyDataSetChanged();
        tvNoData.setVisibility(bnfListDB.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void doSearch(int page, String search) {
        createLog("Beneficiary List", "Search");
        if (isOnline) bnfList.clear();
        else bnfListDB.clear();
        scrollListener.onDataCleared();
        this.page = page;
        beneficiaryListMvpPresenter.doGetBeneficiaryList(false, page, search);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                int pos = data.getIntExtra("pos", 0);
                if (isOnline) {
                    BeneficiaryListResponse.ContentBean bean = data.getParcelableExtra("result");
                    bnfList.set(pos, bean);
                } else {
                    String identityNo = data.getStringExtra("IdentityNo");
                    Beneficiary bean = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder()
                            .where(BeneficiaryDao.Properties.IdentityNo.eq(identityNo)).unique();
                    bnfListDB.set(pos, bean);
                }
                beneficiaryListAdapter.notifyItemChanged(pos);
            }
        }
    }
}
