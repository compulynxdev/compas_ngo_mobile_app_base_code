package com.compastbc.ui.transaction.transaction;

import com.compastbc.core.base.MvpPresenter;

import java.util.List;

public interface TransactionMvpPresenter<V extends TransactionMvpView> extends MvpPresenter<V> {

    void readCardDetails();

    //void checkTopupStatus(List<Topups> object);

    void findTopups(String cardNo);

    //void doWriteTopups(List<Topups> tps,List<String> voucherValue,List<String> voucherId,List<String> programId,List<String> voucherNo);

    void findBlockCard(String cardNo);


    void findPrograms(List<Integer> programId,List<List<Integer>> canDoTxn);

    void setProgramId(int programId);

    void getBeneficiaryFingerPrints(String identityNo);

}
