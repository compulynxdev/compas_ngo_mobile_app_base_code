package com.compastbc.ui.login.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.base.ClickListener;
import com.compastbc.core.data.db.model.Language;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.ui.base.BaseDialog;

import java.util.List;

public class LanguageDialog extends BaseDialog implements View.OnClickListener {

    private static final String TAG = LanguageDialog.class.getSimpleName();

    private String selectedLang = "";
    private List<Language> langList;
    private LanguageCallback callback;

    public static LanguageDialog newInstance(LanguageCallback callback) {
        Bundle args = new Bundle();

        LanguageDialog fragment = new LanguageDialog();
        fragment.setOnClick(callback);
        fragment.setArguments(args);
        return fragment;
    }

    private List<Language> getList() {
        langList = getDataManager().getDaoSession().getLanguageDao().queryBuilder().list();

        if (langList.isEmpty()) {
            Language language = new Language();
            language.langName = AppConstants.LANG_ENGLISH;
            language.isSelected = false;
            language.localisationTitle = getString(R.string.english);
            langList.add(language);

            language = new Language();
            language.langName = AppConstants.LANG_ARABIC;
            language.isSelected = false;
            language.localisationTitle = getString(R.string.arabic);
            langList.add(language);

        }
       /*
        language = new Language();
        language.langName = AppConstants.LANG_ARABIC;
        language.isSelected = false;
        language.localisationTitle = getString(R.string.arabic);
        langList.add(language);*/

        return langList;
    }

    private void setOnClick(LanguageCallback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        view.findViewById(R.id.btnDone).setOnClickListener(this);
        initLangRecycler(view);
    }

    private void initLangRecycler(View view) {
        RecyclerView rvChangeLang = view.findViewById(R.id.rvChangeLang);
        LanguageAdapter adapter = new LanguageAdapter(getList(), new ClickListener() {
            @Override
            public void onItemClick(int pos) {
                selectedLang = langList.get(pos).langName.toLowerCase();
            }

            @Override
            public void onEditClick(int pos) {
                //not usable
            }

            @Override
            public void onDeleteClick(int pos) {

            }
        });

        selectedLang = getDataManager().getLanguage();
        for (int i = 0; i < langList.size(); i++) {
            if (selectedLang.equalsIgnoreCase(langList.get(i).getLangName())) {
                adapter.lastPos = i;
                langList.get(i).setIsSelected(true);
                break;
            }
        }
        /*switch (selectedLang) {
            case AppConstants.LANG_ENGLISH:
                adapter.lastPos = 0;
                langList.get(0).isSelected = true;
                break;

            case AppConstants.LANG_ARABIC:
                adapter.lastPos = 1;
                langList.get(1).isSelected = true;
                break;
        }*/
        rvChangeLang.setAdapter(adapter);
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    @Override
    public void onClick(View v) {
        hideKeyboard();
        switch (v.getId()) {
            case R.id.btnDone:
                if (verifyInput()) {
                    dismissDialog(TAG);
                    callback.onDoneClick(selectedLang);
                } else dismissDialog(TAG);
                break;

            case R.id.btnCancel:
                dismissDialog(TAG);
                break;

        }
    }

    private Boolean verifyInput() {
        return !selectedLang.equals(getDataManager().getLanguage());
    }

    @Override
    protected void setUp(View view) {

    }
}
