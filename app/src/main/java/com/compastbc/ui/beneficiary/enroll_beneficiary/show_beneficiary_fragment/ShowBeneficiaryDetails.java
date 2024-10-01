package com.compastbc.ui.beneficiary.enroll_beneficiary.show_beneficiary_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.ui.base.BaseFragment;
import com.compastbc.ui.beneficiary.BeneficiaryActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class ShowBeneficiaryDetails extends BaseFragment implements View.OnClickListener {

    private static OnFragmentInteractionListener mListener;
    private static JSONObject object;
    private TextView tv_dob;
    private EditText et_name, et_idno, et_address, et_cardNo;
    private CheckBox cb_male, cb_female, cb_false;
    private Button btn_save, btn_cancel;

    public ShowBeneficiaryDetails() {
        // Required empty public constructor
    }

    public static ShowBeneficiaryDetails newInstance(JSONObject object, OnFragmentInteractionListener interactionListener) {
        ShowBeneficiaryDetails fragment = new ShowBeneficiaryDetails();
        Bundle args = new Bundle();
        ShowBeneficiaryDetails.object = object;
        mListener = interactionListener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        findIds(view);
        btn_save.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        displayData();
    }

    private void displayData() {
        try {
            et_name.setText(object.getString("firstName"));
            et_idno.setText(object.getString("identityNo"));
            tv_dob.setText(object.getString("dateOfBirth"));
            et_cardNo.setText(object.getString("cardNumber"));
            if (object.has("address") && !object.isNull("address") && !object.getString("address").equalsIgnoreCase("null")) {
                et_address.setText(object.getString("address"));
            }

            if (object.getString("gender").equalsIgnoreCase("M"))
                cb_male.setChecked(true);
            else cb_female.setChecked(true);

            cb_false.setChecked(true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void findIds(View view) {
        tv_dob = view.findViewById(R.id.tv_displaydate);
        et_name = view.findViewById(R.id.et_name);
        et_idno = view.findViewById(R.id.et_idno);
        et_cardNo = view.findViewById(R.id.et_cardNo);
        et_address = view.findViewById(R.id.et_address);
        cb_male = view.findViewById(R.id.cb_male);
        cb_female = view.findViewById(R.id.cb_female);
        //cb_true=view.findViewById(R.id.cb_t1);
        cb_false = view.findViewById(R.id.cb_f1);
        btn_save = view.findViewById(R.id.btn_save);
        btn_cancel = view.findViewById(R.id.btn_cancel);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_beneficiary_details, container, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (mListener != null)
                    mListener.onFragmentInteraction();
                break;

            case R.id.btn_cancel:
                Intent intent = BeneficiaryActivity.getStartIntent(getActivity());
                startActivity(intent);
                break;
        }
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
