package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hemant Sharma on 23-01-20.
 * Divergent software labs pvt. ltd
 */
@Entity(nameInDb = "Language")
public final class Language {
    public String localisationTitle = "";
    public String langName;
    public Boolean isSelected;

    @Generated(hash = 436318104)
    public Language(String localisationTitle, String langName, Boolean isSelected) {
        this.localisationTitle = localisationTitle;
        this.langName = langName;
        this.isSelected = isSelected;
    }

    @Generated(hash = 1478671802)
    public Language() {
    }

    public String getLocalisationTitle() {
        return this.localisationTitle;
    }

    public void setLocalisationTitle(String localisationTitle) {
        this.localisationTitle = localisationTitle;
    }

    public String getLangName() {
        return this.langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public Boolean getIsSelected() {
        return this.isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }
}