package com.compastbc.ui.cardactivation.inputdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.ui.base.BaseDialog;

public class IdNumberInputDialog extends BaseDialog implements IdNumberInputDialogMvpView {
    private EditText et_input;
    private Button buttonOk, buttonCancel;
    private InteractionListener interactionListener;

    public static IdNumberInputDialog newInstance(InteractionListener interactionListener) {
        IdNumberInputDialog fragment = new IdNumberInputDialog();
        fragment.setCallBack(interactionListener);
        return fragment;
    }

    @Override
    protected void setUp(View view) {
        et_input = view.findViewById(R.id.et_input);
        buttonOk = view.findViewById(R.id.buttonOk);
        buttonCancel = view.findViewById(R.id.cancel);
    }

    private void setCallBack(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_identification_number_input_dialog, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUp(view);
        IdNumberInputDialogMvpPresenter<IdNumberInputDialogMvpView> idNumberInputDialogMvpPresenter = new IdNumberInputDialogPresenter<>(getDataManager());
        idNumberInputDialogMvpPresenter.onAttach(this);
        buttonOk.setOnClickListener(v -> {
            dismissDialog("Card Manually Activate");
            if (interactionListener != null)
                interactionListener.verifyInput(et_input.getText().toString().trim());

        });

        buttonCancel.setOnClickListener(v -> {
            createLog("Input Dialog", "Cancel");
            dismissDialog("Card Manually Activate");
        });

    }

    public interface InteractionListener {
        void verifyInput(String input);
    }
}
