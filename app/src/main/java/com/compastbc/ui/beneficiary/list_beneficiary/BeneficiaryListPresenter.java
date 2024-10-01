package com.compastbc.ui.beneficiary.list_beneficiary;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.network.Webservices;
import com.compastbc.core.data.network.model.BeneficiaryFilterBean;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.utils.AppConstants;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hemant Sharma on 26-09-19.
 * Divergent software labs pvt. ltd
 */
public class BeneficiaryListPresenter<V extends BeneficiaryListMvpView> extends BasePresenter<V>
        implements BeneficiaryListMvpPresenter<V> {

    private final Context context;
    private final Callback<BeneficiaryListResponse> callback = new Callback<BeneficiaryListResponse>() {
        @Override
        public void onResponse(@NonNull Call<BeneficiaryListResponse> call, @NonNull Response<BeneficiaryListResponse> response) {
            getMvpView().hideLoading();
            getMvpView().hideFooterLoader();

            try {
                if (response.code() == 200) {
                    assert response.body() != null;

                    getMvpView().updateUI(response.body().getContent());
                } else if (response.code() == 401)
                    getMvpView().openActivityOnTokenExpire();

                else {
                    assert response.errorBody() != null;
                    JSONObject object;
                    object = new JSONObject(response.errorBody().string());
                    getMvpView().showMessage(object.has("message") ? object.getString("message") : context.getString(R.string.some_error));
                }
            } catch (Exception e) {
                e.printStackTrace();
                getMvpView().showMessage(e.getMessage());
            }
        }

        @Override
        public void onFailure(@NonNull Call<BeneficiaryListResponse> call, @NonNull Throwable t) {
            getMvpView().hideFooterLoader();
            getMvpView().hideLoading();
            getMvpView().showMessage(t.getMessage() != null && t.getMessage().isEmpty() ? context.getString(R.string.ServerError) : t.getMessage());
        }
    };

    BeneficiaryListPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void setupSearch(EditText etSearch) {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty())
                    getMvpView().doSearch(0, "");
                else if (editable.toString().length() > 2) {
                    getMvpView().doSearch(0, editable.toString());
                }
            }
        });
    }

    @Override
    public void doGetBeneficiaryList(boolean isLoader, int page, String search) {
        //page increase by 1

        if (getDataManager().getConfigurableParameterDetail().getOnline()) {
            if (getMvpView().isNetworkConnected()) {
                if (isLoader) getMvpView().showLoading();

                getDataManager().getBeneficiaryList("bearer ".concat(getDataManager().getConfigurationDetail().getAccess_token()),
                        Webservices.WEB_BNF_LIST_N_FILTER.concat(getDataManager().getUserDetail().getLocationId()
                                .concat("?page=".concat(String.valueOf(page))
                                        .concat("&size=".concat(String.valueOf(AppConstants.LIMIT))
                                                .concat("&sort=firstName")
                                                .concat("&search=").concat(search)))))
                        .enqueue(callback);

            }
        } else {
            LazyList<Beneficiary> list;
            if (search.isEmpty()) {
                list = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().offset(page).limit(AppConstants.LIMIT).listLazy();
            } else {
                list = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().whereOr(BeneficiaryDao.Properties.FirstName.like("%" + search + "%")
                        , BeneficiaryDao.Properties.IdentityNo.like("%" + search + "%")).offset(page).limit(AppConstants.LIMIT).listLazy();
            }
            getMvpView().hideFooterLoader();
            getMvpView().updateUIFromDB(list);
        }

    }

    @Override
    public void doGetBeneficiaryList(boolean isLoader, int page, String search, String url) {
        //page increase by 1
        if (getMvpView().isNetworkConnected()) {

            if (isLoader) getMvpView().showLoading();

            getDataManager().getBeneficiaryList("bearer ".concat(getDataManager().getConfigurationDetail().getAccess_token()), url)
                    .enqueue(callback);
        }
    }

    @Override
    public void doGetBeneficiaryList(boolean isLoader, int page, BeneficiaryFilterBean tmpFilterBean) {
        /*List<Beneficiary> list = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().whereOr(BeneficiaryDao.Properties.FirstName.like("%"+tmpFilterBean.getTmpName()+"%")
                ,BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()), BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob())
                ,BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()), BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus().toUpperCase())).offset(page).limit(AppConstants.LIMIT).list();*/

        List<Beneficiary> list = new ArrayList<>();
        QueryBuilder<Beneficiary> query = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder();

        if (!tmpFilterBean.getTmpName().isEmpty() && !tmpFilterBean.getTmpId().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpBioStatus().isEmpty()) {
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    query.or(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                            query.and(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                                    BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus())),
                            BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();
        } else if (!tmpFilterBean.getTmpId().isEmpty() && !tmpFilterBean.getTmpName().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    query.and(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                            BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                            BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpId().isEmpty() && !tmpFilterBean.getTmpName().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    query.and(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                            BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpId().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    query.and(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob())
                            , BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpId().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpBioStatus().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    query.and(BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus()),
                            BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpName().isEmpty() && !tmpFilterBean.getTmpBioStatus().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                    query.and(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName())
                            , BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())
                            , BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus()))).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpId().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpBioStatus().isEmpty() && !tmpFilterBean.getTmpId().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpName().isEmpty() && !tmpFilterBean.getTmpId().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpId().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.eq(tmpFilterBean.getTmpId()),
                    BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpName().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                    query.and(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName())
                            , BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpBioStatus().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpGender().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                    query.and(BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus()),
                            BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender()))).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpName().isEmpty())
            list = query.where(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                    BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpBioStatus().isEmpty() && !tmpFilterBean.getTmpName().isEmpty())
            list = query.where(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                    BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpDob().isEmpty() && !tmpFilterBean.getTmpName().isEmpty())
            list = query.where(BeneficiaryDao.Properties.FirstName.eq(tmpFilterBean.getTmpName()),
                    BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob())).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                    BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpBioStatus().isEmpty() && !tmpFilterBean.getTmpDob().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob()),
                    BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus())).offset(page).limit(AppConstants.LIMIT).list();


        else if (!tmpFilterBean.getTmpGender().isEmpty() && !tmpFilterBean.getTmpBioStatus().isEmpty())
            list = query.where(BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus()),
                    BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpGender().isEmpty())
            list = query.where(BeneficiaryDao.Properties.Gender.eq(tmpFilterBean.getTmpGender())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpDob().isEmpty())
            list = query.where(BeneficiaryDao.Properties.DateOfBirth.eq(tmpFilterBean.getTmpDob())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpName().isEmpty())
            list = query.where(BeneficiaryDao.Properties.FirstName.like("%" + tmpFilterBean.getTmpName() + "%")).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpBioStatus().isEmpty())
            list = query.where(BeneficiaryDao.Properties.Bio.eq(tmpFilterBean.getTmpBioStatus())).offset(page).limit(AppConstants.LIMIT).list();

        else if (!tmpFilterBean.getTmpId().isEmpty())
            list = query.where(BeneficiaryDao.Properties.IdentityNo.like("%" + tmpFilterBean.getTmpId() + "%")).offset(page).limit(AppConstants.LIMIT).list();


        getMvpView().hideFooterLoader();
        getMvpView().updateUIFromDB(list);
    }
}
