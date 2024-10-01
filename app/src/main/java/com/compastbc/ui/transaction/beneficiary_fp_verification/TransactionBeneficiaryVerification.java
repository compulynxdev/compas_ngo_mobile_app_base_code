package com.compastbc.ui.transaction.beneficiary_fp_verification;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.ui.base.BaseFragment;

import java.util.List;

public class TransactionBeneficiaryVerification extends BaseFragment implements BeneficiaryVerifyMvpView {

    private List<String> fingerPrints;
    private BeneficiaryVerifyMvpPresenter<BeneficiaryVerifyMvpView> mvpPresenter;
    private BeneficiaryVerificationInteraction verificationInteraction;
    private ImageView image;
    private String name;
    private Button verify_button;

    public static TransactionBeneficiaryVerification newInstance(List<String> fingers, String Name, BeneficiaryVerificationInteraction beneficiaryVerificationInteraction) {
        TransactionBeneficiaryVerification fragment = new TransactionBeneficiaryVerification();
        Bundle args = new Bundle();
        fragment.setDataNListener(Name, fingers, beneficiaryVerificationInteraction);
        fragment.setArguments(args);
        return fragment;
    }

    private void setDataNListener(String name, List<String> fingers, BeneficiaryVerificationInteraction beneficiaryVerificationInteraction) {
        this.name = name;
        fingerPrints = fingers;
        verificationInteraction = beneficiaryVerificationInteraction;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mvpPresenter = new BeneficiaryVerifyPresenter<>(getDataManager(), getActivity(), fingerPrints);
        mvpPresenter.onAttach(this);
        setUp(view);

        verify_button.setOnClickListener(v -> {
            createLog("Transaction Verification", "Verify Beneficiary");
            mvpPresenter.onClick();
        });
    }

    @Override
    protected void setUp(View view) {
        TextView username = view.findViewById(R.id.username);
        username.setText(name);
        image = view.findViewById(R.id.image_fp);
        verify_button = view.findViewById(R.id.btn_verify_fingerPrints);
        mvpPresenter.onViewLoaded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_beneficiary_verification, container, false);
    }

    @Override
    public void updateImage(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }

    @Override
    public void getPrograms() {
        verificationInteraction.onSuccess();
    }

    public interface BeneficiaryVerificationInteraction {
        void onSuccess();
    }
}
