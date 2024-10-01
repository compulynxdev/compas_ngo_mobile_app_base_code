package com.compastbc.ui.login.userpin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.compastbc.R;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;

public class UserPinActivity extends BaseActivity implements UserPinMvpView {
    private EditText password;
    private UserPinMvpPresenter presenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, UserPinActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pin);
        presenter = new UserPinPresenter(getDataManager());
        presenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        password = findViewById(R.id.input);
        findViewById(R.id.btn_next).setOnClickListener(view -> {
            hideKeyboard();
            presenter.verifyInput(password.getText().toString());
        });
        TextView tvUser = findViewById(R.id.tv_username);
        tvUser.setText(getString(R.string.username).concat(" : ").concat(getDataManager().getUserDetail().getUser()));
    }

    @Override
    public void openMainActivity() {
        createLog("Login", "Login success");
        createAttendanceLog();
        Intent intent = MainActivity.getStartIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
