package com.compastbc.ui.synchronization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.synchronization.transfer.TransferService;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.synchronization.receive.ReceiveActivity;

import java.io.File;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SynchronisationActivity extends BaseActivity implements View.OnClickListener {

    private Details userDetails;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SynchronisationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);
        userDetails = getDataManager().getUserDetail();
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tv_title = findViewById(R.id.tvTitle);
        ImageView img_back = findViewById(R.id.img_back);
        setSupportActionBar(toolbar);
        tv_title.setText(R.string.SYNC);
        img_back.setVisibility(View.VISIBLE);

        img_back.setOnClickListener(this);
        findViewById(R.id.frame_send).setOnClickListener(this);
        findViewById(R.id.frame_receive).setOnClickListener(this);
        deleteOldFile();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                createLog("Synchronisation", "Back");
                onBackPressed();
                break;

            case R.id.frame_send:
                createLog("Synchronisation", "Send");
                startActivity(new Intent(this, ShareActivity.class));
                break;

            case R.id.frame_receive:
                //Vendor 1 or Master 2
                createLog("Synchronisation", "Receive");
                if (userDetails.getLevel().equals("1")) {
                    if (verifyDBStatus()) startActivity(new Intent(this, ReceiveActivity.class));
                } else if (userDetails.getLevel().equals("2")) {
                    startActivity(new Intent(this, ReceiveActivity.class));
                }
                break;
        }
    }

    private void deleteOldFile() {
        File pathVendorData = new File(Environment.getExternalStorageDirectory() +
                File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.FILE_NAME));
        if (pathVendorData.exists()) AppUtils.deleteFile(pathVendorData);

        File pathAckData = new File(Environment.getExternalStorageDirectory() +
                File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.ACK_FILE_NAME));
        if (pathAckData.exists())
            AppUtils.deleteFile(pathAckData);
    }

    private boolean verifyDBStatus() {
        if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
            if (userDetails.getLevel().equalsIgnoreCase("1")) {
                //old query
                /*List<Beneficiary> beneficiaries = Beneficiary.find(Beneficiary.class, "isuploaded=?", "0");
                List<Transactions> transactionsList = Transactions.listAll(Transactions.class);*/

                List<Beneficiary> beneficiaries = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder()
                        .where(BeneficiaryDao.Properties.IsUploaded.eq("0")).list();
                List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().list();
                if (beneficiaries.size() > 0 || transactionsList.size() > 0) {
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.error))
                            .setContentText(getString(R.string.sync_first))
                            .setConfirmText(getString(R.string.Ok))
                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                    return false;

                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TransferService.startStopService(this, false);
    }
}
