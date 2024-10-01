package com.compastbc.ui.cardrestore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.db.model.NFCCardData;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.cardrestore.restore.CardDataRestoreActivity;

import java.util.List;

public class CardRestoreActivity extends BaseActivity implements CardRestoreMvpView {

    private static final String TAG = "CardRestoreActivity";
    private TextView tvNoData;
    private List<NFCCardData> nfcCardDataList;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CardRestoreActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_restore);
        CardRestorePresenter<CardRestoreMvpView> mvpPresenter = new CardRestorePresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        setUp();
        nfcCardDataList = mvpPresenter.getNFCCardList();
        tvNoData.setVisibility(nfcCardDataList.isEmpty() ? View.VISIBLE : View.GONE);
        setUpAdapter();
    }

    private void setUpAdapter() {
        CardRestoreAdapter cardRestoreAdapter = new CardRestoreAdapter(nfcCardDataList, pos -> {
            NFCCardData bean = nfcCardDataList.get(pos);
            Intent intent = CardDataRestoreActivity.getStartIntent(this);
            intent.putExtra("NFCDataPrimaryKey", bean.getId());
            startActivity(intent);
        });
        cardRestoreAdapter.setHasStableIds(true);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(cardRestoreAdapter);
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.title_card_restore);
        tvNoData = findViewById(R.id.tv_no_data);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        img_back.setOnClickListener(v -> {
            createLog(TAG, "Back");
            onBackPressed();
        });
    }
}
