//package com.xilli.stealthnet.Activities;
//
//import android.os.Bundle;
//import android.widget.CompoundButton;
//import android.widget.RadioButton;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.Observer;
//
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingFlowParams;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.SkuDetails;
//import com.android.billingclient.api.SkuDetailsParams;
//import com.xilli.stealthnet.R;
//import com.xilli.stealthnet.Config;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class UnlockAllActivity extends AppCompatActivity implements PurchasesUpdatedListener, BillingClientStateListener {
//
//    private BillingClient billingClient;
//    private Map<String, SkuDetails> skusWithSkuDetails = new HashMap<>();
//    private final List<String> allSubs = new ArrayList<>(Arrays.asList(
//            Config.all_month_id,
//            Config.all_threemonths_id,
//            Config.all_sixmonths_id,
//            Config.all_yearly_id));
//
//    private MutableLiveData<Integer> all_check = new MutableLiveData<>();
//    @BindView(R.id.one_month)
//    RadioButton oneMonth;
//    @BindView(R.id.three_month)
//    RadioButton threeMonth;
//    @BindView(R.id.six_month)
//    RadioButton sixMonth;
//    @BindView(R.id.one_year)
//    RadioButton oneYear;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_unlock_all_indratech);
//
//        billingClient = BillingClient
//                .newBuilder(this)
//                .setListener(this)
//                .enablePendingPurchases()
//                .build();
//
//        connectToBillingService();
//
//        ButterKnife.bind(this);
//        all_check.setValue(-1);
//        all_check.observe(this, new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer integer) {
//                switch (integer) {
//                    case 0:
//                        threeMonth.setChecked(false);
//                        sixMonth.setChecked(false);
//                        oneYear.setChecked(false);
//                        break;
//                    case 1:
//                        oneMonth.setChecked(false);
//                        sixMonth.setChecked(false);
//                        oneYear.setChecked(false);
//                        break;
//                    case 2:
//                        threeMonth.setChecked(false);
//                        oneMonth.setChecked(false);
//                        oneYear.setChecked(false);
//                        break;
//                    case 3:
//                        threeMonth.setChecked(false);
//                        sixMonth.setChecked(false);
//                        oneMonth.setChecked(false);
//                        break;
//
//                }
//            }
//        });
//
//        oneMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) all_check.postValue(0);
//            }
//        });
//        threeMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) all_check.postValue(1);
//            }
//        });
//        sixMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) all_check.postValue(2);
//            }
//        });
//        oneYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) all_check.postValue(3);
//            }
//        });
//    }
//
//    private void connectToBillingService() {
//        if (!billingClient.isReady()) {
//            billingClient.startConnection(this);
//        }
//    }
//
//    @Override
//    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
//        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//            querySkuDetailsAsync(
//                    BillingClient.SkuType.SUBS,
//                    new ArrayList<>(allSubs)
//            );
//        }
//    }
//
//    @Override
//    public void onBillingServiceDisconnected() {
//        connectToBillingService();
//    }
//
//    @Override
//    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
//
//    }
//
//    private void querySkuDetailsAsync(@BillingClient.SkuType String skuType, List<String> skuList) {
//        SkuDetailsParams params = SkuDetailsParams
//                .newBuilder()
//                .setSkusList(skuList)
//                .setType(skuType)
//                .build();
//
//        billingClient.querySkuDetailsAsync(
//                params, (billingResult, skuDetailsList) -> {
//                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
//                        for (SkuDetails details : skuDetailsList) {
//                            skusWithSkuDetails.put(details.getSku(), details);
//                        }
//                    }
//                }
//        );
//    }
//
//    private void purchase(SkuDetails skuDetails) {
//        BillingFlowParams params = BillingFlowParams
//                .newBuilder()
//                .setSkuDetails(skuDetails)
//                .build();
//
//        billingClient.launchBillingFlow(this, params);
//    }
//
//    @OnClick(R.id.all_pur)
//    void unlockAll() {
//        if (all_check.getValue() != null) {
//            SkuDetails skuDetails = null;
//
//            switch (all_check.getValue()) {
//                case 0:
//                    skuDetails = skusWithSkuDetails.get(Config.all_month_id);
//                    break;
//                case 1:
//                    skuDetails = skusWithSkuDetails.get(Config.all_threemonths_id);
//                    break;
//                case 2:
//                    skuDetails = skusWithSkuDetails.get(Config.all_sixmonths_id);
//                    break;
//                case 3:
//                    skuDetails = skusWithSkuDetails.get(Config.all_yearly_id);
//                    break;
//            }
//
//            if (skuDetails != null) purchase(skuDetails);
//        }
//    }
//}
