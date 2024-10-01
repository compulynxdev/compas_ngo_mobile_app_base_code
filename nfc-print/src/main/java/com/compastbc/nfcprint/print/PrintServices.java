package com.compastbc.nfcprint.print;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;

import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.data.network.model.SalesBasketReportModel;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.core.data.network.model.SyncReportModel;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.core.data.network.model.XReportBean;
import com.compastbc.core.utils.AppConstants;

import java.util.List;
import java.util.Set;

public class PrintServices implements PrinterCallback {

    public final static int REQUEST_ENABLE_BT = 1001;
    public final static String VENDOR_RECEIPT_PRINTED = "VENDOR_RECEIPT_PRINTED";
    public final static String PRINT_SUCCESSFULLY = "Print Successfully";
    final static String PRINTER_ERROR = "Printer Error";
    private final String modelName;
    private PrintDefaultService printDefaultService;
    private PrintPosService printPosService;

    public PrintServices(Context context) {
        modelName = Build.MODEL;
        printPosService = new PrintPosService(context);
    }

    public PrintServices(Context context, BluetoothSocket bluetoothSocket) {
        modelName = Build.MODEL;
        printDefaultService = new PrintDefaultService(context, bluetoothSocket);
    }


    /**
     * @param blueToothCallback the listener
     */
    public static void setOnBluetoothDeviceListener(BlueToothCallback blueToothCallback) {
        if (blueToothCallback == null) return;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            blueToothCallback.onBlueToothNotSupported();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                BluetoothDevice ret = null;
                for (BluetoothDevice dev : pairedDevices) {
                    //AppLogger.d("TestP", dev.getName() + " ");
                    if (dev.getName().equals("BT-SPP")) {
                        ret = dev;
                    } else if (dev.getName().equals("POS_Printer")) {
                        ret = dev;
                    } else {
                        ret = dev;
                    }
                }
                blueToothCallback.onBlueToothConnected(ret);
            } else {
                blueToothCallback.onBlueToothDisable();
            }
        }
    }

    @Override
    public void printSummaryReport(SummaryReportBean summaryReportBean, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printSummaryReport(summaryReportBean, interaction);
        } else {
            printDefaultService.printSummaryReport(summaryReportBean, interaction);
        }
    }

    @Override
    public void printXReport(List<XReportBean> xReportBean, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printXReport(xReportBean, interaction);
        } else {
            printDefaultService.printXReport(xReportBean, interaction);
        }
    }

    @Override
    public void printVoidTransactionReport(List<TransactionHistory> transactions, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printVoidTransactionReport(transactions, interaction);
        } else {
            printDefaultService.printVoidTransactionReport(transactions, interaction);
        }
    }

    @Override
    public void printSyncReport(List<SyncReportModel> syncReportModels, String count, String total_txns, String total_amount, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printSyncReport(syncReportModels, count, total_txns, total_amount, interaction);
        } else {
            printDefaultService.printSyncReport(syncReportModels, count, total_txns, total_amount, interaction);
        }
    }

    @Override
    public void printVendorTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printVendorTransactionReceipt(receipt, interaction);
        } else {
            printDefaultService.printVendorTransactionReceipt(receipt, interaction);
        }
    }

    @Override
    public void printBeneficiaryTransactionReceipt(TransactionReceipt receipt, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printBeneficiaryTransactionReceipt(receipt, interaction);
        } else {
            printDefaultService.printBeneficiaryTransactionReceipt(receipt, interaction);
        }
    }

    @Override
    public void printDailyCommodityReport(List<CommodityReportBean> list, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printDailyCommodityReport(list, interaction);
        } else {
            printDefaultService.printDailyCommodityReport(list, interaction);
        }
    }

    @Override
    public void printSalesTransactionHistoryReport(List<TransactionHistory> list, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printSalesTransactionHistoryReport(list, interaction);
        } else {
            printDefaultService.printSalesTransactionHistoryReport(list, interaction);
        }
    }

    @Override
    public void printSubmitTransactionReport(List<SubmitTransactionBean> list, String amount, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printSubmitTransactionReport(list, amount, interaction);
        } else {
            printDefaultService.printSubmitTransactionReport(list, amount, interaction);
        }
    }

    @Override
    public void printSalesBasketReport(SalesBasketReportModel salesBasketReport, OnPrinterInteraction interaction) {
        if (modelName.equals(AppConstants.MODEL_NEWPOS)) {
            printPosService.printSalesBasketReport(salesBasketReport, interaction);
        } else {
            printDefaultService.printSalesBasketReport(salesBasketReport, interaction);
        }
    }
}
