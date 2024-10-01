package com.compastbc.ui.agentpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.ui.base.BaseActivity;

public class ChangeAgentPassword extends BaseActivity implements ChangeAgentPasswordMvpView {

    private EditText etCurrent, etNew, etConfirm;
    private ChangeAgentPasswordMvpPresenter mvpPresenter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ChangeAgentPassword.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_agent_password);
        mvpPresenter = new ChangeAgentPasswordPresenter(getDataManager(), this);
        mvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        setSupportActionBar(toolbar);
        tvTitle.setText(R.string.ChangeAgentPassword);

        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        etCurrent = findViewById(R.id.et_current);
        etNew = findViewById(R.id.et_new);
        etConfirm = findViewById(R.id.et_confirm);
        findViewById(R.id.btn_updPassword).setOnClickListener(view -> mvpPresenter.doChangePwd(etCurrent.getText().toString(), etNew.getText().toString(), etConfirm.getText().toString()));
    }

    @Override
    public void openNextActivity() {
        onBackPressed();
    }
}
