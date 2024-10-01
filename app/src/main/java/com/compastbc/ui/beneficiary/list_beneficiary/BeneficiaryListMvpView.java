package com.compastbc.ui.beneficiary.list_beneficiary;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;

import java.util.List;

/**
 * Created by Hemant Sharma on 26-09-19.
 * Divergent software labs pvt. ltd
 */
public interface BeneficiaryListMvpView extends MvpView {
    void hideFooterLoader();

    void updateUI(List<BeneficiaryListResponse.ContentBean> contentBeanList);

    void updateUIFromDB(List<Beneficiary> beneficiaryList);

    void doSearch(int page, String search);
}
