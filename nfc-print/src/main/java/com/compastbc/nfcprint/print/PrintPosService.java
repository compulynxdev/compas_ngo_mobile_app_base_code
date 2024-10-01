package com.compastbc.nfcprint.print;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;

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
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrintPosService implements PrinterCallback {

    private final Printer printer;
    private final int TEXT_SIZE_SMALL = 0;
    private final int TEXT_SIZE_SMALL_BOLD = 1;
    //private final int TEXT_SIZE_MIDDLE = 2;
    private final int TEXT_SIZE_LARGE = 3;
    private final int TEXT_ALIGN_LEFT = 0;
    private final int TEXT_ALIGN_TITLE = 0;
    private final Context context;
    private Paint paint;
    private PrintTask printTask;
    private PrintCanvas canvas;

    PrintPosService(Context context) {
        this.context = context;
        printer = Printer.getInstance();
        paint = new Paint();
        printTask = new PrintTask();
        canvas = new PrintCanvas();
    }

    private boolean validatePrinterInstance(OnPrinterInteraction interaction) {
        if (printer == null) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
            return false;
        } else {
            switch (printer.getStatus()) {
                case Printer.PRINTER_OK:
                    return true;

                case Printer.PRINTER_STATUS_BUSY:
                    interaction.onPrintStatusBusy();
                    return false;

                case Printer.PRINTER_STATUS_HIGHT_TEMP:
                    interaction.onPrintStatusHighTemp();
                    return false;

                case Printer.PRINTER_STATUS_PAPER_LACK:
                    interaction.onPrintStatusPaperLack();
                    return false;

                case Printer.PRINTER_STATUS_NO_BATTERY:
                    interaction.onPrintStatusNoBattery();
                    return false;

                default:
                    interaction.onFail(PrintServices.PRINTER_ERROR);
                    return false;
            }
        }
    }

    @Override
    public void printSummaryReport(SummaryReportBean summaryReportBean, OnPrinterInteraction interaction) {

        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.SummaryReports)));
                printCustom(context.getString(R.string.TotalCardHolders).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCardHolder)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.TotalTopups).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopup)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.TotalCommodity).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlCommodities)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.TotalTopupLogs).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTopupLog)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.TotalTransaction).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlTransactions)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.BlockCard).concat(String.format(Locale.getDefault(), "%d", summaryReportBean.ttlBlockCards)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            AppLogger.e("print",e.toString());
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    @Override
    public void printXReport(List<XReportBean> xReportBeanList, OnPrinterInteraction interaction) {
        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.Xreports)));
                for (int i = 0; i < xReportBeanList.size(); i++) {
                    XReportBean xReportBean = xReportBeanList.get(i);
                    printCustom(context.getString(R.string.purvalue).concat(" ").concat(xReportBean.currencyType), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printDottedLine();
                    printCustom(context.getString(R.string.txnCount_col).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(xReportBean.transactionCount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.voidCount).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(xReportBean.voidCount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.salesAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.transactionAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.VoidAmount).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.voidAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.NetSales).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", xReportBean.currencyType, Double.parseDouble(xReportBean.transactionAmount))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

                    if (i != (xReportBeanList.size() - 1))
                        printNewLine();
                }
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    @Override
    public void printVoidTransactionReport(List<TransactionHistory> transactions, OnPrinterInteraction interaction) {
        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.voidReport)));

                for (int i = 0; i < transactions.size(); i++) {
                    printCustom(context.getString(R.string.transId).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(transactions.get(i).getReceiptNo()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.IdentificationNumber).concat(" : ").concat(transactions.get(i).getIdentityNo()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.BenfName).concat(" : ").concat(transactions.get(i).getBenfName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", transactions.get(i).getCurrency(), Double.parseDouble(transactions.get(i).getAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.formatByLocale(transactions.get(i).getDate(), CalenderUtils.DATE_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printDottedLine();
                }
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    @Override
    public void printSyncReport(List<SyncReportModel> syncReportModels, String count, String total_txns, String total_amount, OnPrinterInteraction interaction) {
        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.SyncReport)));
                printCustom(context.getString(R.string.TotalDevicesSynced).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(count))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.TotalNoOfTxns).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(total_txns))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                //printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.valueOf(total_amount)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();

                for (int i = 0; i < syncReportModels.size(); i++) {
                    printCustom(context.getString(R.string.deviceId).concat(syncReportModels.get(i).getDeviceId()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.NoOfTxns).concat(String.format(Locale.getDefault(), "%d", Long.parseLong(syncReportModels.get(i).getTotalTxns()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.syncDate).concat(" ").concat(CalenderUtils.formatByLocale(syncReportModels.get(i).getSyncDate(), CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    List<String> amounts = syncReportModels.get(i).getCurrencyAmounts();
                    for (int j = 0; j < amounts.size(); j++) {
                        printCustom(context.getString(R.string.totalAmountIn).concat(" ").concat(String.format(Locale.getDefault(), "%s", amounts.get(j))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    }

                    printDottedLine();
                }

                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    //print vendor receipt
    @Override
    public void printVendorTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        try {
            if (validatePrinterInstance(interaction)) {
                printNewLine();
                printCustom(context.getString(R.string.Compas), TEXT_SIZE_LARGE, TEXT_ALIGN_TITLE);
                printNewLine();
                printCustom(context.getString(R.string.vendorReceipt), TEXT_SIZE_SMALL_BOLD, 100);
                printTransactionReceipt(receipt, true);
                printReport(interaction, PrintServices.VENDOR_RECEIPT_PRINTED);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    //Print beneficiary receipt
    @Override
    public void printBeneficiaryTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        try {
            if (validatePrinterInstance(interaction)) {
                printNewLine();
                printCustom(context.getString(R.string.Compas), TEXT_SIZE_LARGE, TEXT_ALIGN_TITLE);
                printNewLine();
                printCustom(context.getString(R.string.benfReceipt), TEXT_SIZE_SMALL_BOLD, 75);
                printTransactionReceipt(receipt, false);
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    private void printTransactionReceipt(TransactionReceipt receipt, boolean isMac) {
        //printHeader("Receipt Type : Transaction");
        printCustom(context.getString(R.string.username).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getUser()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        if (isMac)
            printCustom(context.getString(R.string.MacID).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getDeviceId()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.Transactions)), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.Time).concat(CalenderUtils.getDateTime(CalenderUtils.TIME_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printNewLine();

        printCustom(context.getString(R.string.receiptNo).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(receipt.getReceiptNo()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.IdentificationNo).concat(" : ").concat(receipt.getRation()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        if (!receipt.getCardSerialNumber().isEmpty())
            printCustom(context.getString(R.string.card_serial_no).concat(" ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(receipt.getCardSerialNumber()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printDottedLine();
        for (PurchasedProducts products : receipt.getProductsList()) {
            printCustom(context.getString(R.string.name).concat(products.getServiceName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.qty).concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(products.getQuantity())).concat("(").concat(products.getUom()).concat(")")), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.Value).concat(" : ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(products.getTotalPrice()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printDottedLine();
        }
        printCustom(context.getString(R.string.openingBal).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getOpeningBal()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.txnValue).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getTxnValue()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.currentBal).concat(" ").concat(receipt.getProgramCurrency()).concat(" ").concat(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(receipt.getCurrentBalance()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printDottedLine();
        printFooter();
    }

    @Override
    public void printDailyCommodityReport(List<CommodityReportBean> commodityList, OnPrinterInteraction interaction) {

        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.commodity_report)));
                for (int k = 0; k < commodityList.size(); k++) {
                    CommodityReportBean tmpBean = commodityList.get(k);
                    List<ReportModel> list = tmpBean.getModelList();

                    printCustom(tmpBean.getTitle(), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTtlAmt()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

                    printDottedLine();
                    for (int i = 0; i < list.size(); i++) {
                        printCustom(context.getString(R.string.name).concat(" ").concat(list.get(i).getName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                        printCustom(context.getString(R.string.Amount).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", list.get(i).getCurrency(), Double.parseDouble(list.get(i).getValue()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                    }
                    if (k != commodityList.size() - 1) {
                        printNewLine();
                    }
                }

                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    @Override
    public void printSalesTransactionHistoryReport(List<TransactionHistory> list, OnPrinterInteraction interaction) {

        try {
            if (validatePrinterInstance(interaction)) {
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

                    printDottedLine();
                }
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    //print submit transaction report
    @Override
    public void printSubmitTransactionReport(List<SubmitTransactionBean> subList, String amount, OnPrinterInteraction interaction) {

        try {
            if (validatePrinterInstance(interaction)) {
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
                    printDottedLine();
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
                        printDottedLine();
                    }
                }
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    /*Print Sales Basket Report start here*/
    @Override
    public void printSalesBasketReport(SalesBasketReportModel salesBasketReport, OnPrinterInteraction interaction) {

        try {
            if (validatePrinterInstance(interaction)) {
                printHeader(context.getString(R.string.receiptType).concat(" ").concat(context.getString(R.string.sales)));
                printSalesBasketReportProgrammeL1(salesBasketReport);
                printFooter();
                printReport(interaction, PrintServices.PRINT_SUCCESSFULLY);
            }
        } catch (Exception | OutOfMemoryError e) {
            interaction.onFail(PrintServices.PRINTER_ERROR);
        } finally {
            resetPrint();
        }
    }

    private void printSalesBasketReportProgrammeL1(SalesBasketReportModel salesBasketReport) {
        //print single programme
        if (salesBasketReport.programBean != null && salesBasketReport.salesCategoryBeans != null) {
            printCustom(context.getString(R.string.ProgrammeName).concat(" : ").concat(salesBasketReport.programBean.getProgramName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.programBean.getCurrency(), Double.parseDouble(salesBasketReport.programBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.programBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printDottedLine();
            printSalesBasketReportProductCategoryL2(salesBasketReport);
        } else if (salesBasketReport.salesProgramBeans != null) { //print all program
            for (SalesProgramBean tmpBean : salesBasketReport.salesProgramBeans) {
                printCustom(context.getString(R.string.ProgrammeName).concat(" : ").concat(tmpBean.getProgramName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();
            }
        }
    }

    private void printSalesBasketReportProductCategoryL2(SalesBasketReportModel salesBasketReport) {
        //print single product category
        if (salesBasketReport.categoryBean != null && salesBasketReport.salesCategoryBeans != null) {
            printCustom(context.getString(R.string.CategoryName).concat(" : ").concat(salesBasketReport.categoryBean.getCategoryName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.categoryBean.getCurrency(), Double.parseDouble(salesBasketReport.categoryBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.categoryBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printDottedLine();
            printSalesBasketReportProductL3(salesBasketReport);
        } else if (salesBasketReport.salesCategoryBeans != null) {//print all product category
            for (SalesCategoryBean tmpBean : salesBasketReport.salesCategoryBeans) {
                printCustom(context.getString(R.string.CategoryName).concat(" : ").concat(tmpBean.getCategoryName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();
            }
        }
    }

    private void printSalesBasketReportProductL3(SalesBasketReportModel salesBasketReport) {
        //print single product
        if (salesBasketReport.commodityBean != null && salesBasketReport.salesCommodityBeans != null) {
            printCustom(context.getString(R.string.ProductName).concat(" : ").concat(salesBasketReport.commodityBean.getCommodityName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.commodityBean.getCurrency(), Double.parseDouble(salesBasketReport.commodityBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.commodityBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);

            printDottedLine();
            if (salesBasketReport.commodityBean.getCommodityType().equalsIgnoreCase("Commodity"))
                printSalesBasketReportProductUomL4(salesBasketReport);

            else printSalesBasketReportProductBnfL5(salesBasketReport);
        } else if (salesBasketReport.salesCommodityBeans != null) { //print all product
            for (SalesCommodityBean tmpBean : salesBasketReport.salesCommodityBeans) {
                printCustom(context.getString(R.string.ProductName).concat(" : ").concat(tmpBean.getCommodityName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.tAmt).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getTotalAmount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getBeneficiaryCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();
            }
        }
    }

    private void printSalesBasketReportProductUomL4(SalesBasketReportModel salesBasketReport) {
        //print single product uom
        if (salesBasketReport.uom != null && salesBasketReport.uomList != null) {
            printCustom(context.getString(R.string.Uom).concat(" : ").concat(salesBasketReport.uom.getUom()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.maxPrice).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", salesBasketReport.uom.getCurrency(), Double.parseDouble(salesBasketReport.uom.getMaxPrice()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(salesBasketReport.uom.getCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
            printDottedLine();
            printSalesBasketReportProductBnfL5(salesBasketReport);
        } else if (salesBasketReport.uomList != null) { //print all product uom
            for (Uom tmpBean : salesBasketReport.uomList) {
                printCustom(context.getString(R.string.Uom).concat(" : ").concat(tmpBean.getUom()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.maxPrice).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getMaxPrice()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(String.format(Locale.getDefault(), "%d", Long.parseLong(tmpBean.getCount()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();
            }
        }
    }

    private void printSalesBasketReportProductBnfL5(SalesBasketReportModel salesBasketReport) {
        //print all product bnf
        if (salesBasketReport.salesBeneficiaries != null) {
            for (SalesBeneficiary tmpBean : salesBasketReport.salesBeneficiaries) {
                printCustom(context.getString(R.string.Beneficiaries).concat(" : ").concat(tmpBean.getName() + "(" + tmpBean.getIdentityNo() + ")"), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Quantity).concat(" : ").concat(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(tmpBean.getQuantity()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printCustom(context.getString(R.string.Value).concat(" : ").concat(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getValue()))), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
                printDottedLine();
            }
        }
    }

    //print custom
    // 0- normal size text
    // 1- only bold text
    // 2- bold with medium text
    // 3- bold with large text
    private void printCustom(String msg, int size, int align) {
        try {

            switch (size) {
                case TEXT_SIZE_SMALL:
                    paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.NORMAL));
                    paint.setTextSize(25F);
                    break;

                case TEXT_SIZE_SMALL_BOLD:
                case TEXT_SIZE_LARGE:
                    paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.BOLD));
                    paint.setTextSize(25F);
                    break;
            }

            if (align == TEXT_ALIGN_LEFT) {//left align
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                canvas.setX(align);
            }
            printTask.setGray(130);
            canvas.drawText(msg, paint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printDottedLine() {
        try {
            paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.NORMAL));
            paint.setTextSize(25F);

            printTask.setGray(130);
            canvas.drawText("----------------------------------------------", paint);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //print new line
    private void printNewLine() {
        try {
            paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.NORMAL));
            paint.setTextSize(25F);

            printTask.setGray(130);
            canvas.drawText("\n", paint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetPrint() {
        if (printer != null) {
            printer.reset();
        }
        paint = null;
        printTask = null;
        canvas = null;
        System.gc();
    }

    private void printHeader(String title) {
        printCustom(context.getString(R.string.Compas), TEXT_SIZE_LARGE, TEXT_ALIGN_TITLE);
        printNewLine();
        printCustom(context.getString(R.string.username).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getUser()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.location).concat(" ").concat(CoreApplication.getInstance().getDataManager().getUserDetail().getLocationName()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.MacID).concat(" : ").concat(CoreApplication.getInstance().getDataManager().getDeviceId()), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(title, TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.Date_Col).concat(CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printCustom(context.getString(R.string.Time).concat(CalenderUtils.getDateTime(CalenderUtils.TIME_FORMAT, Locale.getDefault())), TEXT_SIZE_SMALL, TEXT_ALIGN_LEFT);
        printNewLine();
    }

    private void printFooter() {
        printNewLine();
        printCustom(context.getString(R.string.PoweredByCompulynx), TEXT_SIZE_SMALL, 45);
    }

    private void printReport(OnPrinterInteraction interaction, String msg) {
        printTask.setPrintCanvas(canvas);
        printer.startPrint(printTask, (i, tmpPrintTask) -> {
            switch (i) {
                case Printer.PRINTER_OK:
                    interaction.onSuccess(msg);
                    break;

                case Printer.PRINTER_STATUS_BUSY:
                    interaction.onPrintStatusBusy();
                    break;

                case Printer.PRINTER_STATUS_HIGHT_TEMP:
                    interaction.onPrintStatusHighTemp();
                    break;

                case Printer.PRINTER_STATUS_PAPER_LACK:
                    interaction.onPrintStatusPaperLack();
                    break;

                case Printer.PRINTER_STATUS_NO_BATTERY:
                    interaction.onPrintStatusNoBattery();
                    break;

                default:
                    interaction.onFail(PrintServices.PRINTER_ERROR);
                    break;
            }
        });
    }
}
