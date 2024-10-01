package com.compastbc.ui.transaction.pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.compastbc.R;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.ui.base.BaseFragment;

import java.util.Locale;

public class TransactionPinFragment extends BaseFragment implements View.OnClickListener {

    private static OnTransactionPinListener onTransactionPinListener;
    private static String benfPassword;
    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonOk, buttonDelete;
    private EditText inputPassword;
    private final View.OnClickListener pinHandler = v -> {
        Button pressed = (Button) v;
        inputPassword.setText(inputPassword.getText().toString().concat(pressed.getText().toString()));
    };

    public TransactionPinFragment() {
        // Required empty public constructor
    }

    public static TransactionPinFragment newInstance(OnTransactionPinListener mListener, String password) {
        TransactionPinFragment fragment = new TransactionPinFragment();
        Bundle args = new Bundle();
        onTransactionPinListener = mListener;
        benfPassword = password;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    protected void setUp(View view) {
        findIds(view);
        setClickListeners(button0, button1, button2, button3, button4, button5, button6, button7, button8, button9);
        buttonDelete.setOnClickListener(this);
        buttonOk.setOnClickListener(this);
        buttonDelete.setOnLongClickListener(v -> {
            inputPassword.setText("");
            return true;
        });

    }

    private void setClickListeners(View... views) {
        for (View v : views)
            v.setOnClickListener(pinHandler);
    }

    private void findIds(View view) {
        button0 = view.findViewById(R.id.button0);
        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button5 = view.findViewById(R.id.button5);
        button6 = view.findViewById(R.id.button6);
        button7 = view.findViewById(R.id.button7);
        button8 = view.findViewById(R.id.button8);
        button9 = view.findViewById(R.id.button9);
        buttonOk = view.findViewById(R.id.buttonOk);
        inputPassword = view.findViewById(R.id.input);
        inputPassword.setEnabled(false);
        buttonDelete = view.findViewById(R.id.buttonDeleteBack);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_pin, container, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOk:
                String inputPwd = inputPassword.getText().toString();
                if (!inputPwd.isEmpty()) {
                    verifyInputs(AppUtils.replaceNonstandardDigits(String.format(Locale.ENGLISH, "%s", inputPwd)), benfPassword);
                } else
                    showMessage(R.string.PleaseEnterInput);
                break;

            case R.id.buttonDeleteBack:
                int length = inputPassword.getText().length();
                if (length > 0) {
                    inputPassword.getText().delete(length - 1, length);
                }
                break;

        }
    }

    private void verifyInputs(String enteredPassword, String benfPassword) {
        if (enteredPassword.equalsIgnoreCase(benfPassword)) {
            if (onTransactionPinListener != null)
                onTransactionPinListener.onSuccess();
        } else {
            if (onTransactionPinListener != null)
                onTransactionPinListener.onFailure();
        }
    }

    public interface OnTransactionPinListener {
        void onSuccess();

        void onFailure();
    }
}
