package com.compastbc.ui.beneficiary.create_beneficiary;

import android.app.DatePickerDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.utils.CalenderUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.ui.beneficiary.BenfAuthEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBeneficiaryPresenter<V extends CreateBeneficiaryMvpView> extends BasePresenter<V>
        implements CreateBeneficiaryMvpPresenter<V> {

    private final Context context;
    private final DatePickerDialog.OnDateSetListener datePicker = (view, selectedYear, selectedMonth, selectedDay) -> {
        selectedMonth += 1;
        getMvpView().setDate(CalenderUtils.formatDate(selectedDay + "/" + selectedMonth + "/" + selectedYear, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT));
    };

    CreateBeneficiaryPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void verifyInputs(String firstName, String lastName, String gender, String address, String dob, String signature, String idno, String mobile) {
        if (!(firstName.isEmpty()) && !(lastName.isEmpty()) && (gender != null) && !(address.isEmpty()) && (dob != null) && !(idno.isEmpty()) /*&& idno.length()==getDataManager().getConfigurableParameterDetail().getIdLength()*/)
            saveData(firstName, lastName, gender, address, dob, signature, idno, mobile);
        else if (firstName.isEmpty())
            getMvpView().showMessage(R.string.please_enter_first_name);
        else if (lastName.isEmpty())
            getMvpView().showMessage(R.string.please_enter_last_name);
        else if (idno.isEmpty())
            getMvpView().showMessage(R.string.enter_identificationno);
        else if (dob == null || dob.isEmpty())
            getMvpView().showMessage(R.string.please_select_dob);
        else if (gender == null || gender.isEmpty())
            getMvpView().showMessage(R.string.please_select_gender);
       /* else if (idno.length()!=getDataManager().getConfigurableParameterDetail().getIdLength())
            getMvpView().showMessage(context.getString(R.string.IdentificationNumberLengthShouldBe).concat(" ").concat(String.valueOf(getDataManager().getConfigurableParameterDetail().getIdLength())));
       */
        else
            getMvpView().showMessage(R.string.please_provide_address);
    }

    private void saveData(String firstName, String lastName, String gender, String address, String dob, String signature, String idno, String mobile) {
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            getMvpView().showLoading();
            JSONObject object = new JSONObject();
            try {
                object.put("firstName", firstName.concat(" ").concat(lastName));
                object.put("gender", gender);
                object.put("physicalAdd", address);
                object.put("dateOfBirth", dob);
                object.put("benfSignImage", signature);
                object.put("memberNo", idno);
                object.put("idPassPortNo", idno);
                object.put("locationId", getDataManager().getUserDetail().getLocationId());
                object.put("cellPhone", mobile);
                object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (getMvpView().isNetworkConnected()) {
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (object).toString());

                getDataManager().doUploadBeneficiary("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                        getMvpView().hideLoading();
                        try {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.BeneficiaryAdded).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    getMvpView().openNextActivity();
                                }).show();

                            } else if (response.code() == 401)
                                getMvpView().openActivityOnTokenExpire();

                            else {
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
        } else {
            Beneficiary beneficiaryBean = new Beneficiary();
            beneficiaryBean.setBio(false);
            beneficiaryBean.setBioVerifyStatus(String.valueOf(BenfAuthEnum.PENDING));
            beneficiaryBean.setGender(gender);
            beneficiaryBean.setIdentityNo(idno);
            beneficiaryBean.setFirstName(firstName);
            beneficiaryBean.setAddress(address);
            beneficiaryBean.setLastName(lastName);
            beneficiaryBean.setActivation("0");
            beneficiaryBean.setMobile(mobile);
            beneficiaryBean.setDateOfBirth(dob);
            beneficiaryBean.setIsUploaded("0");
            beneficiaryBean.setAgentId(getDataManager().getUserDetail().getAgentId());
            beneficiaryBean.setDeviceId(CommonUtils.getDeviceId(context));
            Random random = new Random();
            beneficiaryBean.setCardPin(String.format(Locale.US, "%04d", random.nextInt(10000)));
            beneficiaryBean.setCardNumber("440888" + String.format(Locale.US, "%04d", random.nextInt(1000)) + idno);
            beneficiaryBean.setSectionName(getDataManager().getUserDetail().getLocationId());

            if (signature != null) beneficiaryBean.setSignature(signature);

            getDataManager().getDaoSession().getBeneficiaryDao().save(beneficiaryBean);

            getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.BeneficiaryAdded).setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismissWithAnimation();
                getMvpView().openNextActivity();
            }).show();
        }
    }

    @Override
    public void onSelectDate() {
        //date 26/06/2018
        Calendar newCalender = Calendar.getInstance();

        int day, month, year;

        day = newCalender.get(Calendar.DAY_OF_MONTH);
        month = newCalender.get(Calendar.MONTH);
        year = newCalender.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePicker, year, month, day);
        Date maxDate = CalenderUtils.getDateFormat(CalenderUtils.getCurrentDate(), CalenderUtils.TIMESTAMP_FORMAT);
        assert maxDate != null;
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());

        datePickerDialog.show();
    }
}
