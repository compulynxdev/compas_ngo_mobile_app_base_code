package com.compastbc.ui.cardbalance.carddialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.CardBalanceBean;
import com.compastbc.ui.base.BaseDialog;

import java.util.List;

public class CardBalanceDialog extends BaseDialog {

    private static String name, card, id;
    private static List<CardBalanceBean> list;
    private static OnInteraction interaction;
    private TextView tvName, tvCard, tvIdentity;
    private Button buttonOk;
    private RecyclerView recyclerView;

    public static CardBalanceDialog newInstance(String identityNo, String cardNo, String beneficiaryName, List<CardBalanceBean> cardBalanceBeans, OnInteraction onInteraction) {
        Bundle args = new Bundle();
        CardBalanceDialog fragment = new CardBalanceDialog();
        fragment.setArguments(args);
        name = beneficiaryName;
        card = cardNo;
        id = identityNo;
        list = cardBalanceBeans;
        interaction = onInteraction;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_balance_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
        tvName.setText(getString(R.string.name).concat(name));
        tvCard.setText(getString(R.string.CardNumberColon).concat(card));
        tvIdentity.setText(getString(R.string.IdNoColon).concat(id));
        buttonOk.setOnClickListener(v -> {
            if (interaction != null) {
                dismissDialog("Card Balance");
                interaction.dismiss();
            }
        });
        CardDialogAdapter adapter = new CardDialogAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void setUp(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvCard = view.findViewById(R.id.tv_cardno);
        tvIdentity = view.findViewById(R.id.tv_idno);
        buttonOk = view.findViewById(R.id.buttonOk);
        recyclerView = view.findViewById(R.id.cardRecyclerView);
    }

    public interface OnInteraction {
        void dismiss();
    }
}
