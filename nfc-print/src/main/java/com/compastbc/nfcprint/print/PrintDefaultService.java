package com.compastbc.nfcprint.print;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.compastbc.nfcprint.R;
import com.compastbc.core.CoreApplication;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.data.network.model.ReportModel;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.core.data.network.model.SalesBeneficiary;
import com.compastbc.core.data.network.model.SalesCategoryBean;
import com.compastbc.core.data.network.model.SalesCommodityBean;
import com.compastbc.core.data.network.model.SalesProgramBean;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.core.data.network.model.SyncReportModel;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.data.network.model.XReportBean;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.CalenderUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrintDefaultService implements PrinterCallback {

    private final int TEXT_SIZE_SMALL = 0;
    //private final int TEXT_SIZE_SMALL_BOLD = 1;
    //private final int TEXT_SIZE_MIDDLE = 2;
    //private final int TEXT_SIZE_LARGE = 3;
    private final int TEXT_ALIGN_LEFT = 0;
    //private final int TEXT_ALIGN_CENTER = 1;
    //private final int TEXT_ALIGN_RIGHT = 2;
    private final Context context;
    private final BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private final byte[] printFormat = new byte[]{0x1B, 0x21, 0x03};

    PrintDefaultService(Context context, BluetoothSocket bluetoothSocket) {
        this.context = context;
        this.bluetoothSocket = bluetoothSocket;
    }

    @Override
    public void printSummaryReport(SummaryReportBean summaryReportBean, OnPrinterInteraction interaction) {

        try {
            outputStream = bluetoothSocket.getOutputStream();

            outputStream.write(printFormat);
            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.SummaryReports)));
            printCustom(context.getString(R.string.TotalCardHolders).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCardHolder)), 0, 0);
            printCustom(context.getString(R.string.TotalTopups).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopup)), 0, 0);
            printCustom(context.getString(R.string.TotalCommodity).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCommodities)), 0, 0);
            printCustom(context.getString(R.string.TotalTopupLogs).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopupLog)), 0, 0);
            printCustom(context.getString(R.string.TotalTransaction).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTransactions)), 0, 0);
            printCustom(context.getString(R.string.BlockCard).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlBlockCards)), 0, 0);
            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    @Override
    public void printXReport(List<XReportBean> xReportBeanList, OnPrinterInteraction interaction) {
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);
            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.Xreports)));
            for (int i = 0; i < xReportBeanList.size(); i++) {
                XReportBean xReportBean = xReportBeanList.get(i);
                printCustom(context.getString(R.string.purvalue).concat(" ").concat(xReportBean.currencyType), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine(0, 1);
                printCustom(context.getString(R.string.txnCount_col).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(xReportBean.transactionCount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.voidCount).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(xReportBean.voidCount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.salesAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.transactionAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.VoidAmount).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.voidAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.NetSales).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.transactionAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

                if (i != (xReportBeanList.size() - 1))
                    printNewLine();
            }
            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    @Override
    public void printVoidTransactionReport(List<TransactionHistory> transactions, OnPrinterInteraction interaction) {
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);

            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.voidReport)));

            for (int i = 0; i < transactions.size(); i++) {
                printCustom(context.getString(R.string.transId).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(transactions.get(i).getReceiptNo()))), 0, 0);
                printCustom(context.getString(R.string.IdentificationNumber).concat(" : ").concat(transactions.get(i).getIdentityNo()), 0, 0);
                printCustom(context.getString(R.string.BenfName).concat(" : ").concat(transactions.get(i).getBenfName()), 0, 0);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", transactions.get(i).getCurrency(), Double.parseDouble(transactions.get(i).getAmount()))), 0, 0);
                printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.formatByLocale(transactions.get(i).getDate(), CalenderUtils.DATE_FORMAT, Locale.getDefault())), 0, 0);
                printDottedLine(0, 0);
            }
            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    @Override
    public void printSyncReport(List<SyncReportModel> syncReportModels, String count, String total_txns, String total_amount, OnPrinterInteraction interaction) {
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);

            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.SyncReport)));
            printCustom(context.getString(R.string.TotalDevicesSynced).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(count))), 0, 0);
            printCustom(context.getString(R.string.TotalNoOfTxns).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(total_txns))), 0, 0);
            //printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.valueOf(total_amount)), 0, 0);
            printDottedLine(0, 0);

            for (int i = 0; i < syncReportModels.size(); i++) {
                printCustom(context.getString(R.string.deviceId).concat(syncReportModels.get(i).getDeviceId()), 0, 0);
                printCustom(context.getString(R.string.NoOfTxns).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(syncReportModels.get(i).getTotalTxns()))), 0, 0);
                printCustom(context.getString(R.string.syncDate).concat(" ").concat(CalenderUtils.formatByLocale(syncReportModels.get(i).getSyncDate(), CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.getDefault())), 0, 0);
                List<String> amounts = syncReportModels.get(i).getCurrencyAmounts();
                for (int j = 0; j < amounts.size(); j++) {
                    printCustom(context.getString(R.string.totalAmountIn).concat(" ").concat(String.format(Locale.getDefault(), "%s", amounts.get(j))), 0, 0);
                }

                printDottedLine(0, 0);
            }

            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    //print vendor receipt
    @Override
    public void printVendorTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);
            printNewLine();
            printCustom(context.getString(R.string.Compas), 3, 1);
            printNewLine();
            printCustom(context.getString(R.string.vendorReceipt), 1, 1);
            printTransactionReceipt(receipt, true);
            outputStream.flush();
            interaction.onSuccess(PrintServices.VENDOR_RECEIPT_PRINTED);
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    //Print beneficiary receipt
    @Override
    public void printBeneficiaryTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);
            printNewLine();
            printCustom(context.getString(R.string.Compas), 3, 1);
            printNewLine();
            printCustom(context.getString(R.string.benfReceipt), 1, 1);
            printTransactionReceipt(receipt, false);
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    private void printTransactionReceipt(TransactionReceipt receipt, boolean isMac) {
        //printHeader("Receipt Type : Transaction");
        printCustom(context.getString(R.string.username).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getUser()), 0, 0);
        if (isMac)
            printCustom(context.getString(R.string.MacID).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getDeviceId()), 0, 0);
        printCustom(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.Transactions)), 0, 0);
        printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), 0, 0);
        printCustom(context.getString(R.string.Time).concat(CalenderUtils.getDateTime(CalenderUtils.TIME_FORMAT, Locale.getDefault())), 0, 0);

        printNewLine();
        printCustom(context.getString(R.string.receiptNo).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(receipt.getReceiptNo()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.IdentificationNo).concat(" : ").concat(receipt.getRation()), 0, 0);
        if (!receipt.getCardSerialNumber().isEmpty())
            printCustom(context.getString(R.string.card_serial_no).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(receipt.getCardSerialNumber()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printDottedLine(0, 1);
        for (PurchasedProducts products : receipt.getProductsList()) {
            printCustom(context.getString(R.string.name).concat(products.getServiceName()), 0, 0);
            printCustom(context.getString(R.string.qty).concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(products.getQuantity())).concat("(").concat(products.getUom()).concat(")")), 0, 0);
            printCustom(context.getString(R.string.Value).concat(" : ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(products.getTotalPrice()))), 0, 0);
            printDottedLine(0, 1);
        }
        printCustom(context.getString(R.string.openingBal).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getOpeningBal()))), 0, 0);
        printCustom(context.getString(R.string.txnValue).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getTxnValue()))), 0, 0);
        printCustom(context.getString(R.string.currentBal).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getCurrentBalance()))), 0, 0);
        printDottedLine(0, 1);
        printFooter();
    }

    @Override
    public void printDailyCommodityReport(List<CommodityReportBean> commodityList, OnPrinterInteraction interaction) {

        try {
            outputStream = bluetoothSocket.getOutputStream();

            outputStream.write(printFormat);

            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.commodity_report)));
            for (int k = 0; k < commodityList.size(); k++) {
                CommodityReportBean tmpBean = commodityList.get(k);
                List<ReportModel> list = tmpBean.getModelList();
                printCustom(tmpBean.getTitle(), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTtlAmt()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

                printDottedLine(0, 1);
                for (int i = 0; i < list.size(); i++) {
                    printCustom(context.getString(R.string.name).concat(" ").concat(list.get(i).getName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.Amount).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", list.get(i).getCurrency(), Double.parseDouble(list.get(i).getValue()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                }
                if (k != commodityList.size() - 1) {
                    printNewLine();
                }
            }

            printFooter();

            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    @Override
    public void printSalesTransactionHistoryReport(List<TransactionHistory> list, OnPrinterInteraction interaction) {

        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);
            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.SalesTransactionHistory)));
            HashMap<String, TransactionHistory> transId = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                TransactionHistory tmpBean = list.get(i);
                if (tmpBean.getTransactionType().equalsIgnoreCase("0")) {
                    transId.put(tmpBean.getReceiptNo(), tmpBean);
                }
            }
            for (Map.Entry<String, TransactionHistory> set : transId.entrySet()) {
                TransactionHistory mainBean = set.getValue();
                printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.formatByLocale(mainBean.getDate(), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), 0, 0);
                printCustom(context.getString(R.string.receiptNo).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getReceiptNo()))), 0, 0);
                printCustom(context.getString(R.string.IdentificationNo).concat(" : ").concat(mainBean.getIdentityNo()), 0, 0);
                if (!mainBean.getCardSerialNumber().isEmpty())
                    printCustom(context.getString(R.string.card_serial_no).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getCardSerialNumber()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                for (int i = 0; i < list.size(); i++) {
                    TransactionHistory bean = list.get(i);
                    if (bean.getReceiptNo().equalsIgnoreCase(mainBean.getReceiptNo())) {
                        printCustom(context.getString(R.string.CommodityName).concat(" : ").concat(bean.getCommodityName()), 0, 0);
                        printCustom(context.getString(R.string.Quantity).concat(" : ").concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(bean.getQuantity()))).concat(" (").concat(bean.getUom()).concat(")"), 0, 0);
                        printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", bean.getCurrency(), Double.parseDouble(bean.getAmount()))), 0, 0);
                    }
                }

                printDottedLine(0, 1);
            }
            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    //print submit transaction report
    @Override
    public void printSubmitTransactionReport(List<SubmitTransactionBean> subList, String amount, OnPrinterInteraction interaction) {

        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);
            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.submitreports)));

            for (int k = 0; k < subList.size(); k++) {
                SubmitTransactionBean tmpBean = subList.get(k);
                List<TransactionHistory> list = tmpBean.getList();
                HashMap<String, TransactionHistory> transId = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    TransactionHistory txnHist = list.get(i);
                    if (txnHist.getTransactionType().equalsIgnoreCase("0"))
                        transId.put(txnHist.getReceiptNo(), txnHist);
                }

                if (k != 0) {
                    printNewLine();
                }
                printCustom(tmpBean.getTitle(), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTtlAmt()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine(0, 1);
                for (Map.Entry<String, TransactionHistory> mainTxnHistSet : transId.entrySet()) {
                    TransactionHistory mainBean = mainTxnHistSet.getValue();
                    printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.formatByLocale(mainBean.getDate(), CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.receiptNo).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getReceiptNo()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.IdentificationNo).concat(" : ").concat(mainBean.getIdentityNo()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    if (!mainBean.getCardSerialNumber().isEmpty())
                        printCustom(context.getString(R.string.card_serial_no).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(mainBean.getCardSerialNumber()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

                    for (int i = 0; i < list.size(); i++) {
                        TransactionHistory bean = list.get(i);
                        if (bean.getReceiptNo().equalsIgnoreCase(mainBean.getReceiptNo())) {
                            printCustom(context.getString(R.string.CommodityName).concat(" : ").concat(bean.getCommodityName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                            printCustom(context.getString(R.string.Quantity).concat(" : ").concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(bean.getQuantity()))).concat(" (").concat(bean.getUom()).concat(")"), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", bean.getCurrency(), Double.parseDouble(bean.getAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                        }
                    }

                    printDottedLine(0, 1);
                }
            }

            printFooter();
            outputStream.flush();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    /*Print Sales Basket Report start here*/
    @Override
    public void printSalesBasketReport(SalesBasketReportModel salesBasketReport, OnPrinterInteraction interaction) {

        try {
            outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(printFormat);

            printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.sales)));
            printSalesBasketReportProgrammeL1(salesBasketReport);
            printFooter();
            interaction.onSuccess(PrintServices.PRINT_SUCCESSFULLY);
            outputStream.flush();

        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        }
    }

    private void printSalesBasketReportProgrammeL1(SalesBasketReportModel salesBasketReport) {
        //print single programme
        if (salesBasketReport.programBean != null && salesBasketReport.salesCategoryBeans != null) {
            printCustom(context.getString(R.string.ProgrammeName).concat(" : ").concat(salesBasketReport.programBean.getProgramName()), 0, 0);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.programBean.getCurrency(), Double.parseDouble(salesBasketReport.programBean.getTotalAmount()))), 0, 0);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.programBean.getBeneficiaryCount()))), 0, 0);
            printDottedLine(0, 1);
            printSalesBasketReportProductCategoryL2(salesBasketReport);
        } else if (salesBasketReport.salesProgramBeans != null) { //print all program
            for (SalesProgramBean tmpBean : salesBasketReport.salesProgramBeans) {
                printCustom(context.getString(R.string.ProgrammeName).concat(" : ").concat(tmpBean.getProgramName()), 0, 0);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), 0, 0);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), 0, 0);
                printDottedLine(0, 1);
            }
        }
    }

    private void printSalesBasketReportProductCategoryL2(SalesBasketReportModel salesBasketReport) {
        //print single product category
        if (salesBasketReport.categoryBean != null && salesBasketReport.salesCategoryBeans != null) {
            printCustom(context.getString(R.string.CategoryName).concat(" : ").concat(salesBasketReport.categoryBean.getCategoryName()), 0, 0);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.categoryBean.getCurrency(), Double.parseDouble(salesBasketReport.categoryBean.getTotalAmount()))), 0, 0);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.categoryBean.getBeneficiaryCount()))), 0, 0);
            printDottedLine(0, 1);
            printSalesBasketReportProductL3(salesBasketReport);
        } else if (salesBasketReport.salesCategoryBeans != null) {//print all product category
            for (SalesCategoryBean tmpBean : salesBasketReport.salesCategoryBeans) {
                printCustom(context.getString(R.string.CategoryName).concat(" : ").concat(tmpBean.getCategoryName()), 0, 0);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), 0, 0);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), 0, 0);

                printDottedLine(0, 1);
            }
        }
    }

    private void printSalesBasketReportProductL3(SalesBasketReportModel salesBasketReport) {
        //print single product
        if (salesBasketReport.commodityBean != null && salesBasketReport.salesCommodityBeans != null) {
            printCustom(context.getString(R.string.ProductName).concat(" : ").concat(salesBasketReport.commodityBean.getCommodityName()), 0, 0);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.commodityBean.getCurrency(), Double.parseDouble(salesBasketReport.commodityBean.getTotalAmount()))), 0, 0);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.commodityBean.getBeneficiaryCount()))), 0, 0);
            printDottedLine(0, 1);
            if (salesBasketReport.commodityBean.getCommodityType().equalsIgnoreCase("Commodity"))
                printSalesBasketReportProductUomL4(salesBasketReport);

            else printSalesBasketReportProductBnfL5(salesBasketReport);
        } else if (salesBasketReport.salesCommodityBeans != null) { //print all product
            for (SalesCommodityBean tmpBean : salesBasketReport.salesCommodityBeans) {
                printCustom(context.getString(R.string.ProductName).concat(" : ").concat(tmpBean.getCommodityName()), 0, 0);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), 0, 0);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), 0, 0);
                printDottedLine(0, 1);
            }
        }
    }

    private void printSalesBasketReportProductUomL4(SalesBasketReportModel salesBasketReport) {
        //print single product uom
        if (salesBasketReport.uom != null && salesBasketReport.uomList != null) {
            printCustom(context.getString(R.string.Uom).concat(" : ").concat(salesBasketReport.uom.getUom()), 0, 0);
            printCustom(context.getString(R.string.maxPrice).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.uom.getCurrency(), Double.parseDouble(salesBasketReport.uom.getMaxPrice()))), 0, 0);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.uom.getCount()))), 0, 0);
            printDottedLine(0, 1);
            printSalesBasketReportProductBnfL5(salesBasketReport);
        } else if (salesBasketReport.uomList != null) { //print all product uom
            for (Uom tmpBean : salesBasketReport.uomList) {
                printCustom(context.getString(R.string.Uom).concat(" : ").concat(tmpBean.getUom()), 0, 0);
                printCustom(context.getString(R.string.maxPrice).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getMaxPrice()))), 0, 0);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getCount()))), 0, 0);
                printDottedLine(0, 1);
            }
        }
    }

    private void printSalesBasketReportProductBnfL5(SalesBasketReportModel salesBasketReport) {
        //print all product bnf
        if (salesBasketReport.salesBeneficiaries != null) {
            for (SalesBeneficiary tmpBean : salesBasketReport.salesBeneficiaries) {
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(tmpBean.getName() + "(" + tmpBean.getIdentityNo() + ")"), 0, 0);
                printCustom(context.getString(R.string.Quantity).concat(" : ").concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(tmpBean.getQuantity()))), 0, 0);
                printCustom(context.getString(R.string.Value).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getValue()))), 0, 0);
                printDottedLine(0, 1);
            }
        }
    }

    //print custom
    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void printDottedLine(int size, int align) {
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write("--------------------------------".getBytes());
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print photo
    @SuppressWarnings("unused")
    private void printPhoto(Context context, int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), img);
            if (bmp != null) {
                byte[] command = Util.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            } else {
                AppLogger.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            AppLogger.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    @SuppressWarnings("unused")
    private void printUnicode() {
        try {
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Util.UNICODE_TEXT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    private void resetPrint() {
        try {
            outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print text
    @SuppressWarnings("unused")
    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printHeader(String title) {
        printCustom(context.getString(R.string.Compas), 3, 1);
        printNewLine();
        printCustom(context.getString(R.string.username).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getUser()), 0, 0);
        printCustom(context.getString(R.string.location).concat(" ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getLocationName()), 0, 0);
        printCustom(context.getString(R.string.MacID).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getDeviceId()), 0, 0);
        printCustom(title, 0, 0);
        printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), 0, 0);
        printCustom(context.getString(R.string.Time).concat(CalenderUtils.getDateTime(CalenderUtils.TIME_FORMAT, Locale.getDefault())), 0, 0);
        printNewLine();
    }

    private void printFooter() {
        printNewLine();
        printCustom(context.getString(R.string.PoweredByCompulynx), 0, 1);
        printNewLine();
        printNewLine();
    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
