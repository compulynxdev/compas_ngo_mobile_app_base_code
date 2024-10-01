package com.compastbc.ui.beneficiary.list_beneficiary.detail;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;

/**
 * Created by Hemant Sharma on 10-10-19.
 * Divergent software labs pvt. ltd
 */
public interface BeneficiaryDetailMvpView extends MvpView {
    void openNextActivity(BeneficiaryListResponse.ContentBean data);

    void openNextActivity(String identityNo);

    void updateEditUi(BeneficiaryListResponse.ContentBean data);

    void updateUi(BeneficiaryListResponse.ContentBean data);

    void updateEditUiFromDB(Beneficiary data);

    void updateUiFromDB(Beneficiary data);
}
