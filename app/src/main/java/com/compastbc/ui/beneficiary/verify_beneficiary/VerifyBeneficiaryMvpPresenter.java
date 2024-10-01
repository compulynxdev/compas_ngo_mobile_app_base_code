package com.compastbc.ui.beneficiary.verify_beneficiary;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.MemberInfo;

public interface VerifyBeneficiaryMvpPresenter<V extends VerifyBeneficiaryMvpView> extends MvpPresenter<V> {
    void searchBeneficiary(String searchCriteria, String inputText);

    void searchBeneficiaryByIdNo(String input);

    void searchBeneficiaryByCardNo(String input);

    void doVerifyBenfFp(MemberInfo memberInfo);
}
