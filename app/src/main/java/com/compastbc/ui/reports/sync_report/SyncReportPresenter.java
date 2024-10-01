package com.compastbc.ui.reports.sync_report;

import android.database.Cursor;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.SyncLogs;
import com.compastbc.core.data.db.model.SyncLogsDao;
import com.compastbc.core.data.network.model.SyncReportModel;
import com.compastbc.core.utils.CalenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SyncReportPresenter<V extends SyncReportMvpView> extends BasePresenter<V>
        implements SyncReportMvpPresenter<V> {

    SyncReportPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getData() {
        getMvpView().showLoading();

        Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + SyncLogsDao.TABLENAME
                + " where " + SyncLogsDao.Properties.Upload_by.columnName + " IS NOT NULL"
                + " GROUP BY " + SyncLogsDao.Properties.Upload_by.columnName, new String[]{
        });

        List<SyncLogs> syncLogsList = getSyncLogs(cursor);

        Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + SyncLogsDao.TABLENAME
                + " where " + SyncLogsDao.Properties.Upload_by.columnName + " IS NOT NULL"
                + " GROUP BY " + SyncLogsDao.Properties.ProgramCurrency.columnName, new String[]{
        });

        List<SyncLogs> currencyLogs = getSyncLogs(cursor1);

        List<SyncReportModel> syncReportModels = new ArrayList<>();

        int count = 0, totaltxns = 0;
        double totalamount = 0.0;
        for (int i = 0; i < syncLogsList.size(); i++) {
            count++;
            SyncReportModel model = new SyncReportModel();
            int txns = 0;
            double amount = 0.0;
            model.setDeviceId(syncLogsList.get(i).getUpload_deviceId());
            model.setSyncDate(CalenderUtils.formatDate(syncLogsList.get(i).getUpload_date(), CalenderUtils.DB_TIMESTAMP_FORMAT));
            List<String> currencyAmount = new ArrayList<>();

            for (int k = 0; k < currencyLogs.size(); k++) {
                //if currency not available that means program data not available on that device
                if (!currencyLogs.get(k).getProgramCurrency().isEmpty()) {
                    List<SyncLogs> synclogs = getDataManager().getDaoSession().getSyncLogsDao().queryBuilder().where(SyncLogsDao.Properties.Upload_by.isNotNull(),
                            SyncLogsDao.Properties.ProgramCurrency.eq(currencyLogs.get(k).getProgramCurrency()),
                            SyncLogsDao.Properties.Upload_by.eq(syncLogsList.get(i).getUpload_by())).list();

                    double tmpCurrencyWiseAmt = 0.0;
                    for (int j = 0; j < synclogs.size(); j++) {
                        txns = txns + Integer.parseInt(synclogs.get(j).getTotal_transaction());
                        tmpCurrencyWiseAmt = tmpCurrencyWiseAmt + Double.parseDouble(synclogs.get(j).getTotal_amount());
                    }
                    currencyAmount.add(currencyLogs.get(k).getProgramCurrency().concat(" : ").concat(String.format(Locale.getDefault(), "%.2f", tmpCurrencyWiseAmt)));
                }
            }

            model.setTotalAmount(String.valueOf(amount));
            model.setTotalTxns(String.valueOf(txns));
            model.setCurrencyAmounts(currencyAmount);
            totaltxns = totaltxns + txns;
            totalamount = totalamount + amount;
            syncReportModels.add(model);
        }
        getMvpView().hideLoading();
        getMvpView().setData(syncReportModels, String.valueOf(count), String.valueOf(totaltxns), String.valueOf(totalamount));
    }

    private List<SyncLogs> getSyncLogs(Cursor cursor1) {
        List<SyncLogs> logsList = new ArrayList<>();
        if (cursor1.moveToFirst()) {
            do {
                SyncLogs logs = new SyncLogs();
                logs.setUpload_deviceId(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Upload_deviceId.columnName)));
                logs.setUpload_by(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Upload_by.columnName)));
                logs.setTotal_transaction(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Total_transaction.columnName)));
                logs.setTotal_amount(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Total_amount.columnName)));
                logs.setProgramCurrency(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.ProgramCurrency.columnName)));
                //logs.setUpload_date(CalenderUtils.getDateFormat(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Upload_date.columnName)),CalenderUtils.DATE_FORMAT));
                logs.setUpload_date(CalenderUtils.getDateFromTimestamp(Long.parseLong(cursor1.getString(cursor1.getColumnIndexOrThrow(SyncLogsDao.Properties.Upload_date.columnName)))));
                logsList.add(logs);
            } while (cursor1.moveToNext());
        }
        if (!cursor1.isClosed()) {
            cursor1.close();
        }
        return logsList;
    }
}
