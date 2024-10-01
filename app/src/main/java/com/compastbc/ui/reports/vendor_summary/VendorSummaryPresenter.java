package com.compastbc.ui.reports.vendor_summary;

import android.database.Cursor;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Topups;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.TxnCount;
import com.compastbc.core.data.db.model.TxnCountDao;
import com.compastbc.core.utils.CalenderUtils;

import java.util.List;

public class VendorSummaryPresenter extends BasePresenter<VendorSummaryMvpView>
implements VendorSummaryMvpPresenter{
    VendorSummaryPresenter(DataManager dataManager) {
        super(dataManager);
    }

    private void deleteData(){
        Topups topups = getDataManager().getDaoSession().getTopupsDao().queryBuilder()
                .whereOr(TopupsDao.Properties.StartDate.lt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                        TopupsDao.Properties.StartDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)))
                .whereOr(TopupsDao.Properties.EndDate.gt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT)),
                        TopupsDao.Properties.EndDate.eq(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT))).limit(1).unique();

        if (topups != null) {
            List<TxnCount> list = getDataManager().getDaoSession().getTxnCountDao().queryBuilder().where(
                    TxnCountDao.Properties.StartDate.notEq(topups.getStartDate()),
                            TxnCountDao.Properties.EndDate.notEq(topups.getEndDate())).list();
            getDataManager().getDaoSession().getTxnCountDao().deleteInTx(list);
        }
    }

    @Override
    public void getList(){
        getMvpView().showLoading();
        deleteData();
        List<TxnCount> list = getDataManager().getDaoSession().getTxnCountDao().queryBuilder().list();

        long count = 0;
        if (!list.isEmpty()) {
            Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + TxnCountDao.Properties.Count.columnName + ") from " +
                            TxnCountDao.TABLENAME
                    , new String[]{});

            if (cursor.moveToFirst()) {
                count = cursor.getLong(0);
            }
        }
        getMvpView().hideLoading();
        getMvpView().showData(list,count);
    }
}
