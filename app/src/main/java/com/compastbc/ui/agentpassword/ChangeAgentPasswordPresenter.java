package com.compastbc.ui.agentpassword;

import android.content.Context;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;

import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeAgentPasswordPresenter extends BasePresenter<ChangeAgentPasswordMvpView> implements ChangeAgentPasswordMvpPresenter {
    private final Context context;

    ChangeAgentPasswordPresenter(DataManager dataManager, Context context1) {
        super(dataManager);
        context = context1;
    }

    @Override
    public void doChangePwd(String current, String newPassword, String confirm) {
        if (current.isEmpty()) {
            getMvpView().showMessage(R.string.alert, R.string.PleaseEnterCurrent);
        } else if (newPassword.isEmpty()) {
            getMvpView().showMessage(R.string.alert, R.string.PleaseEnterNew);
        } else if (confirm.isEmpty()) {
            getMvpView().showMessage(R.string.alert, R.string.PleaseEnterConfirm);
        } else if (newPassword.equals(confirm)) {
            if (current.equals(getDataManager().getUserDetail().getPassword())) {
                savePassword(newPassword);
            } else getMvpView().showMessage(R.string.alert, R.string.CurrentPasswordInvalid);
        } else getMvpView().showMessage(R.string.alert, R.string.PasswordMisMatch);
    }

    private void savePassword(String newPassword) {
        if (getMvpView().isNetworkConnected()) {
            getMvpView().showLoading();
            getMvpView().getAccessToken(() -> {
                JSONObject object = new JSONObject();
                try {
                    object.put("username", getDataManager().getUserDetail().getUser());
                    object.put("password", newPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());
                getDataManager().changeAgentPassword("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            assert response.body() != null;
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                getMvpView().hideLoading();
                                if (jsonObject.has("result")) {
                                    Users user = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.Username.eq(getDataManager().getUserDetail().getUser())).unique();
                                    if (user != null)
                                        user.setPassword(newPassword);
                                    Details details = getDataManager().getUserDetail();
                                    details.setPassword(newPassword);
                                    getDataManager().setUserDetail(details);
                                    getDataManager().setPassword(newPassword);
                                    getDataManager().getConfigurationDetail().setPassword(newPassword);
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), jsonObject.getString("result")).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().openNextActivity();
                                    }).show();
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(R.string.alert, R.string.unableToChangePassword);
                                }
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().showMessage("", e.toString());
                            }

                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                getMvpView().hideLoading();
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().sweetAlert("", object.getString("message"));

                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().sweetAlert("", e.toString());
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() != null && t.getMessage().isEmpty() ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });
            });
        }
    }
}
