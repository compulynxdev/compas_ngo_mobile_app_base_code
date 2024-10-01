package com.compastbc.ui.login;

import com.compastbc.Compas;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppUtils;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class LoginPresenter<V extends LoginMvpView> extends BasePresenter<V>
        implements LoginMvpPresenter<V> {

    LoginPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onNextClick(String userName, String password) {
        getMvpView().createLog("Login", "Username Entered");
        verifyInput(userName, password);
    }

    @Override
    public void checkBioStatus() {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
           /* Map<String, Integer> partMap = new HashMap<>();
            partMap.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));*/
/*
            getDataManager().getAgentBio("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), partMap)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            getMvpView().hideLoading();
                            try {
                                if (response.code() == 200) {
                                    assert response.body() != null;
                                    JSONObject object;
                                    object = new JSONObject(response.body().string());
                                    if (object.has("bioStatus"))
                                        if (object.getBoolean("bioStatus"))
                                            getMvpView().openNextActivity(getDataManager().getUserName(), 2);

                                        else
                                            getMvpView().openNextActivity(getDataManager().getUserName(), 1);

                                } else if (response.code() == 401) {
                                    getMvpView().openActivityOnTokenExpire();
                                } else {
                                    assert response.errorBody() != null;
                                    JSONObject object;
                                    object = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object.getString("message"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                getMvpView().showMessage(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            handleApiFailure(call, t);
                        }
                    });
*/
            if (getDataManager().getUserDetail().isBioStatus())
                getMvpView().openNextActivity(getDataManager().getUserName(), 2);

            else
                getMvpView().openNextActivity(getDataManager().getUserName(), 1);
        } else {
            Users users = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.AgentId.eq(getDataManager().getUserDetail().getAgentId())).unique();
            getMvpView().hideLoading();
            if (users != null && users.getBio())
                getMvpView().openNextActivity(getDataManager().getUserName(), 2);

            else getMvpView().openNextActivity(getDataManager().getUserName(), 1);
        }
    }

    private void verifyInput(String userName, String password) {
        long size = AppUtils.getAvailableInternalMemorySize();
        if (size != 0) {
            if (!(size >= 500)) {
                getMvpView().showMessage(R.string.NotHaveSpace);

            } else if (Compas.LONGITUDE != 0.0 && Compas.LATITUDE != 0.0) {
                Details dt = new Details();
                dt.setLatitude(Compas.LATITUDE);
                dt.setLongitude(Compas.LONGITUDE);
                if (userName == null || userName.isEmpty()) {
                    getMvpView().showMessage(R.string.empty_username);

                } else if (password == null || password.isEmpty()) {
                    getMvpView().showMessage(R.string.empty_password);
                } else {
                    if (getDataManager().isFirstTime()) {
                        getDataManager().setUser(userName);
                        getDataManager().setPassword(password);
                        displayActivity(userName);
                    } else {
                        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                            if (getMvpView().isNetworkConnected()) {
                                getMvpView().showLoading();
                                getMvpView().getAccessToken(userName, password, () -> getMvpView().downloadMasterData(() -> displayActivity(userName)));
                            }
                        } else {
                            if (getDataManager().getUserDetail().getUser().equalsIgnoreCase(userName) && getDataManager().getUserDetail().getPassword().equalsIgnoreCase(password))
                                displayActivity(userName);
                            else {
                                invalidUser();
                            }
                        }

                    }
                }
            } else {
                getMvpView().showMessage(R.string.NotGettingLocation);

            }
        } else {
            getMvpView().showMessage(R.string.NotHaveSpace);
        }
    }


    private void invalidUser() {
        getMvpView().hideLoading();
        getMvpView().showMessage(R.string.invalid_credential);
    }

    private void displayActivity(String userName) {
        if (getDataManager().isFirstTime()) getMvpView().openNextActivity(userName, 0);

        else {
            //Details dt = new Details();
            if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
                checkBioStatus();
            } else {
                getMvpView().hideLoading();
                getDataManager().setLoggedIn(true);
                getMvpView().openNextActivity(userName, 3);
            }
        }
    }
}
