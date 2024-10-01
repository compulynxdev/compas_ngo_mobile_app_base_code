package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.R;
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

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectProgramPresenter<V extends SelectProgramMvpView> extends BasePresenter<V>
        implements SelectProgramMvpPresenter<V> {

    private final Context context;

    SelectProgramPresenter(Context context, DataManager dataManager) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void getPrograms(int offset, String startDate, String endDate) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    object.put("macAddress", getDataManager().getDeviceId());
                    object.put("page", offset);
                    object.put("locationId",getDataManager().getUserDetail().getLocationId());
                    object.put("startDate", startDate.contains("/") ? CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : startDate);
                    object.put("endDate", endDate.contains("/") ? CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : endDate);
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
                                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();

                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    getMvpView().hideLoading();
                                    assert response.errorBody() != null;
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().sweetAlert(1, "", object.getString("message")).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();

                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            getMvpView().hideLoading();
                            getMvpView().sweetAlert(1, context.getString(R.string.error), t.getMessage() != null && t.getMessage().isEmpty() ? context.getString(R.string.ServerError) : t.getMessage()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                getMvpView().dismissDialogView();
                            }).show();

                        }
                    });

                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        getMvpView().dismissDialogView();
                    }).show();

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
                bean.setProgramName(programsList.get(i).getProgramName());
                Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + TransactionsDao.Properties.TotalAmountChargedByRetail.columnName + ") from " +
                                TransactionsDao.TABLENAME + " where " + TransactionsDao.Properties.ProgramId.columnName + "=? and "
                                + TransactionsDao.Properties.Date.columnName + " between ? and ? and " + TransactionsDao.Properties.TransactionType.columnName + " =? ",
                        new String[]{programsList.get(i).getProgramId(),
                                CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT), CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT), "0"});
                if (cursor.moveToFirst()) {
                    amount = cursor.getDouble(0);
                    bean.setTotalAmount(String.valueOf(amount));
                    bean.setCurrency(programsList.get(i).getProgramCurrency());

                }
                cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + TransactionsDao.TABLENAME
                        + " where " + TransactionsDao.Properties.ProgramId.columnName + " = ? and " + TransactionsDao.Properties.Date.columnName + " between ? and ? and " + TransactionsDao.Properties.TransactionType.columnName + " =? "
                        + " GROUP BY " + TransactionsDao.Properties.IdentityNo.columnName, new String[]{programsList.get(i).getProgramId(),
                        CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT), CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT), "0"});
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
                programs.setProgramId(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramId.columnName)));
                programs.setProgramCurrency(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramCurrency.columnName)));
                programsList.add(programs);
            } while (cursor1.moveToNext());
        }
        if (!cursor1.isClosed()) {
            cursor1.close();
        }
        return programsList;
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
}
