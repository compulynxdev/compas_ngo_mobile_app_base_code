package com.compastbc.ui.login.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class UserFpEnrollPresenter<V extends UserFpEnrollMvpView> extends BasePresenter<V>
        implements UserFpEnrollMvpPresenter<V> {

    private final Context context;

    UserFpEnrollPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }


    @Override
    public void doSaveAgentData(Bitmap bitmap, MemberInfo memberInfo) {
        if (bitmap != null && memberInfo != null) {
            getMvpView().showLoading();
            if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                if (getMvpView().isNetworkConnected()) {
                    try {
                        getMvpView().showLoading();
                        JSONArray cs = new JSONArray();
                        JSONObject object = new JSONObject();
                        object.put("agentId", getDataManager().getUserDetail().getAgentId());
                        object.put("createdBy", getDataManager().getUserDetail().getAgentId());
                        object.put("locationId", getDataManager().getUserDetail().getLocationId());
                        object.put("deviceId", getDataManager().getDeviceId());
                        JSONObject fingerprint = new JSONObject();
                        fingerprint.put("leftFinger1", memberInfo.getLeftFront());
                        fingerprint.put("leftFinger2", memberInfo.getLeftOne());
                        fingerprint.put("leftFinger3", memberInfo.getLeftTwo());
                        fingerprint.put("rightFinger1", memberInfo.getRightFront());
                        fingerprint.put("rightFinger2", memberInfo.getRightOne());
                        fingerprint.put("rightFinger3", memberInfo.getRightTwo());
                        fingerprint.put("leftIndex", memberInfo.getLeftIndex());
                        fingerprint.put("leftThumb", memberInfo.getLeftThumb());
                        fingerprint.put("rightIndex", memberInfo.getRightIndex());
                        fingerprint.put("rightThumb", memberInfo.getRightThumb());
                        object.put("FingerPrints", fingerprint);
                        cs.put(object);
                        //minimum fp check
                        if (fingerprint.length() >= 10)
                            doUploadAgentInfo(AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, cs.toString()));

                        else {
                            getMvpView().hideLoading();
                            getMvpView().showMessage(context.getString(R.string.Ten).concat(" ").concat(context.getString(R.string.MinimumFingerError)));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Users user = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.AgentId.eq(getDataManager().getUserDetail().getAgentId())).unique();
                if (user != null) {
                    if (AppConstants.fpcount >= 10) {
                        user.setFplf(memberInfo.getLeftFront());
                        user.setF1(memberInfo.getLeftOne());
                        user.setF2(memberInfo.getLeftTwo());
                        user.setFprf(memberInfo.getRightFront());
                        user.setF3(memberInfo.getRightOne());
                        user.setF4(memberInfo.getRightTwo());
                        user.setFpli(memberInfo.getLeftIndex());
                        user.setFplt(memberInfo.getLeftThumb());
                        user.setFpri(memberInfo.getRightIndex());
                        user.setFprt(memberInfo.getRightThumb());
                        user.setIsuploaded("0");
                        user.setBio(true);
                        getDataManager().getDaoSession().getUsersDao().insertOrReplace(user);
                        getMvpView().hideLoading();
                        getMvpView().createLog("Enroll agent", "Bio Captured");
                        getMvpView().sweetAlert(2, R.string.success, R.string.AgentBiometricSaved).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            getMvpView().openNextActivity(0);
                        }).show();
                    } else {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(context.getString(R.string.Ten).concat(" ").concat(context.getString(R.string.MinimumFingerError)));
                    }
                } else {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.noUsers);
                }
            }
        } else if (bitmap == null)
            getMvpView().showMessage(R.string.PleaseCaptureImage);

        else getMvpView().showMessage(R.string.PleaseCaptureFingerPrints);
    }

    @Override
    public void getAgentBiometric() {
        getMvpView().showLoading();
        List<String> fps = new ArrayList<>();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject object = new JSONObject();
            if (getMvpView().isNetworkConnected()) {
                try {
                    object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    object.put("type", "AGENT");
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());
                    getDataManager().getBeneficiaryFingerPrint("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                try {
                                    assert response.body() != null;
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONObject fingers = jsonObject.getJSONObject("agentFingerPrint");
                                    if (fingers.length() > 0) {
                                        if (!fingers.isNull("leftFinger3B64"))
                                            fps.add(fingers.getString("leftFinger3B64"));

                                        if (!fingers.isNull("rightFinger3B64"))
                                            fps.add(fingers.getString("rightFinger3B64"));

                                        if (!fingers.isNull("rightFinger2B64"))
                                            fps.add(fingers.getString("rightFinger2B64"));

                                        if (!fingers.isNull("leftFinger2B64"))
                                            fps.add(fingers.getString("leftFinger2B64"));

                                        if (!fingers.isNull("leftFinger1B64"))
                                            fps.add(fingers.getString("leftFinger1B64"));

                                        if (!fingers.isNull("leftIndexB64"))
                                            fps.add(fingers.getString("leftIndexB64"));

                                        if (!fingers.isNull("rightFinger1B64"))
                                            fps.add(fingers.getString("rightFinger1B64"));

                                        if (!fingers.isNull("rightThumbB64"))
                                            fps.add(fingers.getString("rightThumbB64"));

                                        if (!fingers.isNull("rightIndexB64"))
                                            fps.add(fingers.getString("rightIndexB64"));

                                        if (!fingers.isNull("leftThumbB64"))
                                            fps.add(fingers.getString("leftThumbB64"));
                                        getMvpView().hideLoading();
                                        getMvpView().showVerifyView(fps);

                                    } else {
                                        getMvpView().hideLoading();
                                        getMvpView().sweetAlert(R.string.alert, R.string.NoBiometricFound).setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().openNextActivity(1);
                                        }).show();
                                    }
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
                                    JSONObject object1 = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object1.getString("message"));
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
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
            Users user = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.AgentId.eq(getDataManager().getUserDetail().getAgentId()), (UsersDao.Properties.Bio.eq("TRUE"))).unique();
            if (user != null) {
                if (user.getF1() != null)
                    fps.add(user.getF1());

                if (user.getF2() != null)
                    fps.add(user.getF2());

                if (user.getF3() != null)
                    fps.add(user.getF3());

                if (user.getF4() != null)
                    fps.add(user.getF4());

                if (user.getFplf() != null)
                    fps.add(user.getFplf());

                if (user.getFpli() != null)
                    fps.add(user.getFpli());

                if (user.getFplt() != null)
                    fps.add(user.getFplt());

                if (user.getFprf() != null)
                    fps.add(user.getFprf());

                if (user.getFpri() != null)
                    fps.add(user.getFpri());

                if (user.getFprt() != null)
                    fps.add(user.getFprt());

                getMvpView().hideLoading();
                getMvpView().showVerifyView(fps);
            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.noUsers);
            }
        }
    }

    private void doUploadAgentInfo(RequestBody body) {

        getDataManager().doUploadAgentBio("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                getMvpView().hideLoading();
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;

                        getMvpView().sweetAlert(2, R.string.success, R.string.AgentBiometricSaved).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            getMvpView().openNextActivity(0);
                        }).show();

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
    }
}


