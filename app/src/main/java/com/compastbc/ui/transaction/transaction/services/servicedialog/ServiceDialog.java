package com.compastbc.ui.transaction.transaction.services.servicedialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.compastbc.R;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.PurchasedProductsDao;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.ui.base.BaseDialog;
import org.greenrobot.greendao.query.QueryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ServiceDialog extends BaseDialog implements ServiceDialogMvpView, View.OnClickListener {
    private static Services servicess;
    private static OnFragmentInteractionListener Listener;
    private static List<ServicePrices> servicePrices = new ArrayList<>();
    private ServiceDialogMvpPresenter<ServiceDialogMvpView> mvpPresenter;
    private TextView maxprice;
    private EditText inputQty, input_price;
    private Spinner spinner;
    private Double maxAmount=0.0;
    private String selectedUom;
    private final List<String> uoms = new ArrayList<>();

    public static ServiceDialog newInstance(Services services, List<ServicePrices> service, OnFragmentInteractionListener mListener) {
        ServiceDialog fragment = new ServiceDialog();
        Bundle args = new Bundle();
        Listener = mListener;
        servicePrices = service;
        servicess = services;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mvpPresenter = new ServiceDialogPresenter<>(getDataManager());
        mvpPresenter.onAttach(this);
        setUp(view);


    }

    @Override
    protected void setUp(View view) {
        uoms.clear();
        uoms.add(getString(R.string.SelectUom));
        ImageView imageView = view.findViewById(R.id.icon);
        TextView tvTitle = view.findViewById(R.id.title);

        spinner = view.findViewById(R.id.spinner);
        inputQty = view.findViewById(R.id.input);
        input_price = view.findViewById(R.id.input_price);
        //input_price.setVisibility(View.VISIBLE);
        maxprice = view.findViewById(R.id.maxP);
        maxprice.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        inputQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        Button btn_purchase = view.findViewById(R.id.purchase);
        Button btn_cancel = view.findViewById(R.id.cancel);
        btn_cancel.setOnClickListener(this);
        btn_purchase.setOnClickListener(this);

        for (int i = 0; i < servicePrices.size(); i++) {
            uoms.add(servicePrices.get(i).getUom());
        }
        //set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getBaseActivity(), android.R.layout.simple_spinner_item, uoms);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUom = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
                if (selectedUom.equalsIgnoreCase(getString(R.string.SelectUom)))
                    maxprice.setVisibility(View.GONE);
                else {
                    String max = mvpPresenter.getMaxPrice(servicePrices, selectedUom);
                    if (max != null && !max.equalsIgnoreCase("")) {
                        maxAmount = Double.parseDouble(max);
                        maxprice.setVisibility(View.VISIBLE);
                        maxprice.setText(getString(R.string.MaxPrice).concat(String.format(Locale.getDefault(), " %s %.2f", getDataManager().getCurrency(), maxAmount)));
                    }
                    input_price.setHint(getString(R.string.enterPriceFor).concat(" ").concat(selectedUom));
                    if(selectedUom.equalsIgnoreCase("kgs") || selectedUom.equalsIgnoreCase("litres")){
                        inputQty.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    }else inputQty.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Bitmap decodedByte;
        byte[] decodedString = Base64.decode(servicess.getServiceImage(), Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);
        tvTitle.setText(servicess.getServiceName());
        inputQty.setHint(R.string.Quantity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.purchase:
                if (Listener != null) {
                    if (!inputQty.getText().toString().isEmpty() && selectedUom != null/* && !input_price.getText().toString().trim().isEmpty()*/) {
                        /*if (Double.parseDouble(input_price.getText().toString()) > maxAmount) {
                            dismissDialog("Service Dialog");
                            // msg chng
                            showMessage(R.string.maxPriceCannotExceeds);
                        } else if (Double.parseDouble(input_price.getText().toString()) <= 0) {
                            dismissDialog("Service Dialog");
                            showMessage(R.string.maxPriceShouldGreaterThanZero);
                        } else {*/
                            String inp = inputQty.getText().toString();
                            //double totalPrice = Double.parseDouble(input_price.getText().toString()) * Double.parseDouble(inputQty.getText().toString());
                            double totalPrice = maxAmount * Double.parseDouble(inputQty.getText().toString());
                            Topups topups = getDataManager().getTopupDetails();
                            double remainingAmount = Double.parseDouble(topups.getVouchervalue()) - totalPrice;
                            if (remainingAmount >= 0) {
                                topups.setVouchervalue(String.format(Locale.ENGLISH, "%.2f", remainingAmount));
                                QueryBuilder<PurchasedProducts> db = getDataManager().getDaoSession().getPurchasedProductsDao().queryBuilder();
                                db.where(PurchasedProductsDao.Properties.ServiceId.eq(servicess.getServiceId()), PurchasedProductsDao.Properties.Uom.eq(selectedUom));
                                PurchasedProducts products = db.build().unique();
                                if (products == null) {
                                    PurchasedProducts purchasedProducts = new PurchasedProducts();
                                    purchasedProducts.setCardNumber(getDataManager().getTopupDetails().getCardnumber());
                                    purchasedProducts.setServiceId(servicess.getServiceId());
                                    purchasedProducts.setProgrammeId(getDataManager().getTopupDetails().getProgrammeid());
                                    purchasedProducts.setUom(selectedUom);
                                    purchasedProducts.setTotalPrice(String.valueOf(totalPrice));
                                    purchasedProducts.setVoucherId(getDataManager().getTopupDetails().getVoucherid());
                                    purchasedProducts.setQuantity(inp);
                                    purchasedProducts.setMaxPrice(String.valueOf(maxAmount));
                                    purchasedProducts.setServiceImage(servicess.getServiceImage());
                                    purchasedProducts.setServiceName(servicess.getServiceName());
                                    getDataManager().setTopupDetails(topups);
                                    getDataManager().getDaoSession().getPurchasedProductsDao().insert(purchasedProducts);
                                } else {
                                    String quantity = products.getQuantity();
                                    String productPrice = products.getTotalPrice();
                                    products.setTotalPrice(String.valueOf(totalPrice + Double.parseDouble(productPrice)));
                                    products.setQuantity(String.valueOf(Integer.parseInt(inp) + Integer.parseInt(quantity)));
                                    products.setMaxPrice(String.valueOf(maxAmount));
                                    getDataManager().setTopupDetails(topups);
                                    getDataManager().getDaoSession().getPurchasedProductsDao().update(products);
                                }
                                dismissDialog("Service Dialog");
                                Listener.onPurchase();
                            } else {
                                dismissDialog("Service Dialog");
                                showMessage(R.string.AmountExceeds);
                            }
                       // }
                    } else if (selectedUom == null) {
                        showMessage(R.string.PleaseSelectUom);
                    } else if (inputQty.getText().toString().trim().isEmpty()) {
                        showMessage(R.string.PleaseEnterQuantity);
                    } else showMessage(R.string.PleaseEnterPrice);

                }
                break;

            case R.id.cancel:
                dismissDialog("Service Dialog");
                break;
        }
    }


    public interface OnFragmentInteractionListener {
        void onPurchase();
    }
}
