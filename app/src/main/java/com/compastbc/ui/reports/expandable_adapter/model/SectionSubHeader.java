package com.compastbc.ui.reports.expandable_adapter.model;

/**
 * Created by chris on 2017-06-13.
 */

public class SectionSubHeader {

    private String loadingString;
    private String errorString;

    public SectionSubHeader(String loadingString, String errorString) {
        this.loadingString = loadingString;
        this.errorString = errorString;
    }

    public String getLoadingString() {
        return loadingString;
    }

    public void setLoadingString(String loadingString) {
        this.loadingString = loadingString;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }
}
