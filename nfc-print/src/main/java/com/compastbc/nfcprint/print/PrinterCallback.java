package com.compastbc.nfcprint.print;

import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.core.data.network.model.SyncReportModel;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.core.data.network.model.XReportBean;

import java.util.List;

public interface PrinterCallback {
    void printSummaryReport(SummaryReportBean summaryReportBean, OnPrinterInteraction interaction);

    void printXReport(List<XReportBean> xReportBean, OnPrinterInteraction interaction);

    void printVoidTransactionReport(List<TransactionHistory> transactions, OnPrinterInteraction interaction);

    void printSyncReport(List<SyncReportModel> syncReportModels, String count, String total_txns, String total_amount, OnPrinterInteraction interaction);

    void printVendorTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction);

    void printBeneficiaryTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction);

    void printDailyCommodityReport(List<CommodityReportBean> list, OnPrinterInteraction interaction);

    void printSalesTransactionHistoryReport(List<TransactionHistory> list, OnPrinterInteraction interaction);

    void printSubmitTransactionReport(List<SubmitTransactionBean> list, String amount, OnPrinterInteraction interaction);

    void printSalesBasketReport(SalesBasketReportModel salesBasketReport, OnPrinterInteraction interaction);

}
