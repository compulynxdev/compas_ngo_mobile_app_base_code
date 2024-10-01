package com.compastbc.ui.reports.submit_report;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.core.data.network.model.TransactionHistory;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SubmitPresenter<V extends SubmitMvpView> extends BasePresenter<V>
        implements SubmitMvpPresenter<V> {

    SubmitPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getData() {
        getMvpView().showLoading();
        List<SubmitTransactionBean> headerList = new ArrayList<>();
        double amount = 0.0;

        List<Transactions> transactionsCurrencyList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder()
                .where(new WhereCondition.StringCondition(TransactionsDao.Properties.Submit.columnName + " = 0 GROUP BY " + TransactionsDao.Properties.ProgramId.columnName)).build().list();
        for (int k = 0; k < transactionsCurrencyList.size(); k++) {
            List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.ProgramId.eq(transactionsCurrencyList.get(k).getProgramId()), TransactionsDao.Properties.Submit.eq("0")).list();
            SubmitTransactionBean submitTransactionBean = new SubmitTransactionBean();
            List<TransactionHistory> transactionHistories = new ArrayList<>();
            double programAmount = 0.0;
            String currency = transactionsCurrencyList.get(k).getProgramCurrency();

            for (int i = 0; i < transactionsList.size(); i++) {
                List<Commodities> commodities = getDataManager().getDaoSession().getCommoditiesDao().queryBuilder().where(CommoditiesDao.Properties.TransactionNo.eq(transactionsList.get(i).getReceiptNo())).list();
                for (int j = 0; j < commodities.size(); j++) {
                    TransactionHistory history = new TransactionHistory();
                    history.setAmount(String.valueOf(commodities.get(j).getTotalAmountChargedByRetailer()));
                    history.setCurrency(currency);
                    history.setReceiptNo(commodities.get(j).getTransactionNo());
                    history.setBenfName(commodities.get(j).getBeneficiaryName());
                    history.setCommodityName(commodities.get(j).getProductName());
                    history.setIdentityNo(transactionsList.get(i).getIdentityNo());
                    history.setCardSerialNumber(transactionsList.get(i).getCardSerialNumber());
                    history.setQuantity(commodities.get(j).getQuantityDeducted());
                    history.setTransactionType(transactionsList.get(i).getTransactionType());
                    history.setUom(commodities.get(j).getUom());
                    history.setDate(commodities.get(j).getDate());
                    transactionHistories.add(history);
                }
                programAmount = programAmount + Double.parseDouble(transactionsList.get(i).getTotalAmountChargedByRetail());
                amount = amount + Double.parseDouble(transactionsList.get(i).getTotalAmountChargedByRetail());
            }
            submitTransactionBean.setTitle(transactionsCurrencyList.get(k).getProgramName());
            submitTransactionBean.setTtlAmt(String.valueOf(programAmount));
            submitTransactionBean.setCurrency(currency);
            submitTransactionBean.setList(transactionHistories);
            headerList.add(submitTransactionBean);
        }
        getMvpView().hideLoading();
        getMvpView().setData(headerList, String.valueOf(amount));

    }

    @Override
    public void updateTransactions() {
        getMvpView().showLoading();
        List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.Submit.eq("0")).list();
        List<Transactions> transactions = new ArrayList<>();
        for (Transactions transaction : transactionsList) {
            transaction.setSubmit("1");
            transactions.add(transaction);
        }
        getDataManager().getDaoSession().getTransactionsDao().insertOrReplaceInTx(transactions);
        getMvpView().hideLoading();
        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.transactionUpdated).setConfirmClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            getMvpView().openNextActivity();
        }).show();
    }
}
