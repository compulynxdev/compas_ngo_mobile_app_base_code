package com.compastbc.ui.login.pin;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.compastbc.R;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;
import com.compastbc.ui.settings.SettingsActivity;

import java.util.Locale;

public class PinActivity extends BaseActivity implements PinMvpView, View.OnClickListener {

    private EditText password;
    View.OnClickListener pinHandler = v -> {
        Button pressed = (Button) v;
        password.setText(password.getText().toString().concat(pressed.getText().toString()));
    };
    private String userName;
    private PinMvpPresenter<PinMvpView> pinMvpPresenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, PinActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        pinMvpPresenter = new PinPresenter<>(getDataManager());
        pinMvpPresenter.onAttach(this);
        userName = getDataManager().getUserName();
        setUp();
    }

    @Override
    protected void setUp() {
        Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, clear, login;
        b0 = findViewById(R.id.button0);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        b5 = findViewById(R.id.button5);
        b6 = findViewById(R.id.button6);
        b7 = findViewById(R.id.button7);
        b8 = findViewById(R.id.button8);
        b9 = findViewById(R.id.button9);
        clear = findViewById(R.id.buttonDeleteBack);
        login = findViewById(R.id.buttonOk);
        password = findViewById(R.id.input);
        password.setEnabled(false);
        onClickListener(b1, b2, b3, b4, b5, b6, b7, b8, b9, b0);
        clear.setOnLongClickListener(v -> {
            password.setText("");
            return true;
        });
        clear.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    public void onClickListener(Button... b) {
        for (Button views : b)
            views.setOnClickListener(pinHandler);
    }

    @Override
    public void openLoginActivity() {
        createLog("Pin", "Login success");
        createAttendanceLog();
        Intent intent = MainActivity.getStartIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void openConfigureActivity() {
        Intent intent = SettingsActivity.getStartIntent(this);
        getDataManager().setPassword(String.format(Locale.ENGLISH, "%d", Long.parseLong(password.getText().toString().trim())));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonDeleteBack:
                int length = password.getText().length();
                if (length > 0) {
                    password.getText().delete(length - 1, length);
                }
                break;

            case R.id.buttonOk:
                createLog("Password", "Pin Entered");
                pinMvpPresenter.verifyInput(userName, String.format(Locale.ENGLISH, "%d", Long.parseLong(password.getText().toString().trim())));
                break;
        }

    }
}
