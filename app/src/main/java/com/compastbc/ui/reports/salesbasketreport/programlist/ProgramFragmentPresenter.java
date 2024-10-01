package com.compastbc.ui.reports.salesbasketreport.programlist;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgramFragmentPresenter<V extends ProgramFragmentMvpView> extends BasePresenter<V>
        implements ProgramFragmentMvpPresenter<V> {
    ProgramFragmentPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public SalesProgramBean getProgramBean(List<SalesProgramBean> programBeans, String id) {
        SalesProgramBean bean;
        getMvpView().showLoading();
        for (int i = 0; i < programBeans.size(); i++) {
            if (programBeans.get(i).getProgramId().equalsIgnoreCase(id)) {
                getMvpView().hideLoading();
                bean = programBeans.get(i);
                return bean;
            }
        }
        getMvpView().hideLoading();
        return null;
    }

    @Override
    public void getProgrammesData(int offset) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {

            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    object.put("macAddress", getDataManager().getDeviceId());
                    object.put("page", offset);
                    object.put("locationId",getDataManager().getUserDetail().getLocationId());
                    object.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());
                    getDataManager().getProgrammesForSales("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {

                                assert response.body() != null;
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    List<SalesProgramBean> beans = new ArrayList<>();
                                    JSONArray array = jsonObject.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        SalesProgramBean bean = new SalesProgramBean();
                                        bean.setBeneficiaryCount(array.getJSONObject(i).getString("totalBeneficiary"));
                                        bean.setProgramId(array.getJSONObject(i).getString("programmeId"));
                                        bean.setProgramName(array.getJSONObject(i).getString("programmeName"));
                                        bean.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        bean.setTotalAmount(array.getJSONObject(i).getString("totalAmount"));
                                        bean.setProductId(array.getJSONObject(i).getString("productId"));
                                        beans.add(bean);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().setData(beans);
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    getMvpView().hideLoading();
                                    assert response.errorBody() != null;
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object.getString("message"));
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            handleApiFailure(call, t);
                        }
                    });

                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(e.getMessage());
                }

            }
        } else {
            List<SalesProgramBean> beans = new ArrayList<>();
            Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + ProgramsDao.TABLENAME + " GROUP BY "
                    + ProgramsDao.Properties.ProgramId.columnName + " LIMIT " + AppConstants.LIMIT + " OFFSET " + offset, new String[]{});
            List<Programs> programsList = getProgramList(cursor1);
            for (int i = 0; i < programsList.size(); i++) {
                SalesProgramBean bean = new SalesProgramBean();
                double amount;
                long count;
                bean.setProgramId(programsList.get(i).getProgramId());
                bean.setProductId(programsList.get(i).getProductId());
                bean.setProgramName(String.format(Locale.getDefault(), "%s", programsList.get(i).getProgramName()));
                Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + TransactionsDao.Properties.TotalAmountChargedByRetail.columnName + ") from " +
                        TransactionsDao.TABLENAME + " where " + TransactionsDao.Properties.ProgramId.columnName + "=? and "
                        + TransactionsDao.Properties.Date.columnName + "=? and " + TransactionsDao.Properties.TransactionType.columnName + " =? ", new String[]{programsList.get(i).getProgramId(), CalenderUtils.getTimestamp(CalenderUtils.DATE_FORMAT), "0"});
                if (cursor.moveToFirst()) {
                    amount = cursor.getDouble(0);
                    bean.setTotalAmount(String.valueOf(amount));
                    bean.setCurrency(programsList.get(i).getProgramCurrency());
                }

                cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + TransactionsDao.TABLENAME
                        + " where " + TransactionsDao.Properties.ProgramId.columnName + " = ? and " + TransactionsDao.Properties.Date.columnName + " =?  and "
                        + TransactionsDao.Properties.TransactionType.columnName + " =? "
                        + " GROUP BY " + TransactionsDao.Properties.IdentityNo.columnName, new String[]{programsList.get(i).getProgramId(), CalenderUtils.getTimestamp(CalenderUtils.DATE_FORMAT), "0"
                });
                if (cursor.moveToFirst()) {
                    count = cursor.getCount();
                    bean.setBeneficiaryCount(String.valueOf(count));
                }
                beans.add(bean);
            }
            getMvpView().hideLoading();
            getMvpView().setData(beans);
        }
    }

    private List<Programs> getProgramList(Cursor cursor1) {
        List<Programs> programsList = new ArrayList<>();
        if (cursor1.moveToFirst()) {
            do {
                Programs programs = new Programs();
                programs.setProductId(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProductId.columnName)));
                programs.setProgramName(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramName.columnName)));
                programs.setProgramCurrency(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramCurrency.columnName)));
                programs.setProgramId(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramId.columnName)));
                programsList.add(programs);
            } while (cursor1.moveToNext());
        }
        if (!cursor1.isClosed()) {
            cursor1.close();
        }
        return programsList;
    }
}
