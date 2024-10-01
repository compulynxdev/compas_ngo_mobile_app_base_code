package com.compastbc.core.base;


import androidx.annotation.NonNull;

import com.compastbc.core.R;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * onAttach() and onDetach(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
public class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private static final String TAG = "BasePresenter";

    private final DataManager mDataManager;

    private V mMvpView;

    public BasePresenter(DataManager dataManager) {
        this.mDataManager = dataManager;
    }

    @Override
    public void onAttach(V mvpView) {
        mMvpView = mvpView;
    }

    @Override
    public void onDetach() {
        mMvpView = null;
    }

    private boolean isViewAttached() {
        return mMvpView != null;
    }

    public V getMvpView() {
        return mMvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    public void handleApiFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        getMvpView().hideLoading();
        if (t.getMessage() != null)
            getMvpView().showMessage(t.getMessage());
        else
            getMvpView().showMessage(R.string.ServerError);
    }

    public void handleApiError(String data) {
        getMvpView().hideLoading();
        try {
            if (data.isEmpty()) {
                getMvpView().sweetAlert(R.string.alert, R.string.ServerError).show();
            } else {
                JSONObject object = new JSONObject(data);
                if (object.has("detail")) {
                    getMvpView().sweetAlert(R.string.alert, object.getString("detail")).show();
                } else if (object.has("message")) {
                    getMvpView().sweetAlert(R.string.alert, object.getString("message")).show();
                } else if (object.has("respMessage"))
                    getMvpView().sweetAlert(R.string.alert, object.getString("respMessage")).show();
                else {
                    getMvpView().sweetAlert(R.string.alert, R.string.ServerError).show();
                }
            }
        } catch (Exception e) {
            getMvpView().sweetAlert(R.string.alert, R.string.ServerError).show();
        }
    }

    public String getProgramCurrency(String programId) {
        if (programId == null || programId.isEmpty()) return "";
        Programs programs = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(programId)).unique();
        String currency = "";
        if (programs != null)
            currency = programs.getProgramCurrency();

        return currency;
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        MvpViewNotAttachedException() {
            super("Please call Presenter.onAttach(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }
}
