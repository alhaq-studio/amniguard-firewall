package com.alhaq.amnishield.netblock;

/*
    This file is part of AmniGuard Firewall.

    AmniGuard Firewall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AmniGuard Firewall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AmniGuard Firewall.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2026 by Marcel Bokhorst (M66B) & Alhaq Initiative
*/

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modernized In-App Billing Manager using Google Play Billing Library 7.x.
 * Supports One-Time Lifetime Pro Purchase with zero subscription overhead.
 */
public class IAB implements PurchasesUpdatedListener {
    private static final String TAG = "AmniGuard.IAB";

    public static final String SKU_PRO = "pro";
    public static final String SKU_PRO_LEGACY = "pro1";
    public static final String SKU_PRO_LIFETIME = "pro_lifetime";

    private final Context context;
    private final Delegate delegate;
    private BillingClient billingClient;
    private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();
    private boolean isConnected = false;

    public interface Delegate {
        void onReady(IAB iab);
        default void onPurchased(String productId) {}
        default void onError(String message) {}
    }

    public void updatePurchases() {
        queryPurchases();
    }

    public IAB(Delegate delegate, Context context) {
        this.context = context.getApplicationContext();
        this.delegate = delegate;
    }

    public void bind() {
        Log.i(TAG, "Initializing Google Play Billing Client 7.x");
        PendingPurchasesParams pendingParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build();

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingParams)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing client connected successfully");
                    isConnected = true;
                    queryPurchases();
                    queryProducts();
                } else {
                    Log.w(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                    if (delegate != null) {
                        delegate.onError(billingResult.getDebugMessage());
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected");
                isConnected = false;
            }
        });
    }

    public void unbind() {
        if (billingClient != null) {
            Log.i(TAG, "Ending BillingClient connection");
            billingClient.endConnection();
            billingClient = null;
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected && billingClient != null && billingClient.isReady();
    }

    /**
     * Query active one-time in-app purchases from Google Play.
     */
    public void queryPurchases() {
        if (!isConnected()) return;

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    processPurchases(purchases);
                } else {
                    Log.w(TAG, "Failed to query purchases: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    /**
     * Query one-time in-app products details.
     */
    private void queryProducts() {
        if (!isConnected()) return;

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PRO_LEGACY)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PRO_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (ProductDetails details : productDetailsList) {
                        productDetailsMap.put(details.getProductId(), details);
                        Log.i(TAG, "Product available: " + details.getProductId() + " (" + details.getTitle() + ")");
                    }
                    if (delegate != null) {
                        delegate.onReady(IAB.this);
                    }
                } else {
                    Log.w(TAG, "Query product details failed: " + billingResult.getDebugMessage());
                    if (delegate != null) {
                        delegate.onReady(IAB.this);
                    }
                }
            }
        });
    }

    @Nullable
    public ProductDetails getProductDetails(String productId) {
        if (productDetailsMap.containsKey(productId)) {
            return productDetailsMap.get(productId);
        }
        if (productDetailsMap.containsKey(SKU_PRO)) {
            return productDetailsMap.get(SKU_PRO);
        }
        if (productDetailsMap.containsKey(SKU_PRO_LEGACY)) {
            return productDetailsMap.get(SKU_PRO_LEGACY);
        }
        return null;
    }

    public String getFormattedPrice(String productId) {
        ProductDetails details = getProductDetails(productId);
        if (details != null && details.getOneTimePurchaseOfferDetails() != null) {
            return details.getOneTimePurchaseOfferDetails().getFormattedPrice();
        }
        return "$4.99";
    }

    /**
     * Launch Google Play purchase flow for a one-time product.
     */
    public boolean launchPurchaseFlow(Activity activity, String productId) {
        if (!isConnected()) {
            Log.e(TAG, "BillingClient not ready");
            return false;
        }

        ProductDetails details = getProductDetails(productId);
        if (details == null) {
            Log.e(TAG, "Product details not found for " + productId);
            return false;
        }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = Collections.singletonList(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
        );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
        return result.getResponseCode() == BillingClient.BillingResponseCode.OK;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled the purchase flow");
        } else {
            Log.w(TAG, "onPurchasesUpdated error: " + billingResult.getDebugMessage());
            if (delegate != null) {
                delegate.onError(billingResult.getDebugMessage());
            }
        }
    }

    private void processPurchases(List<Purchase> purchases) {
        SharedPreferences prefs = context.getSharedPreferences("IAB", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                for (String product : purchase.getProducts()) {
                    Log.i(TAG, "Valid purchase confirmed for product: " + product);
                    editor.putBoolean(product, true);
                    editor.putBoolean(SKU_PRO, true);
                    editor.putBoolean(ActivityPro.SKU_PRO1, true);

                    if (delegate != null) {
                        delegate.onPurchased(product);
                    }
                }

                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            Log.i(TAG, "Acknowledge purchase response: " + billingResult.getResponseCode());
                        }
                    });
                }
            }
        }
        editor.apply();
    }

    public static void setBought(String sku, Context context) {
        Log.i(TAG, "Bought " + sku);
        SharedPreferences prefs = context.getSharedPreferences("IAB", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(sku, true).putBoolean(SKU_PRO, true).apply();
    }

    public static boolean isPurchased(String sku, Context context) {
        try {
            if (Util.isDebuggable(context)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                if (prefs.getBoolean("debug_iab", false)) return false;
            }

            if (LicenseValidator.isLicensed(context)) {
                return true;
            }

            SharedPreferences prefs = context.getSharedPreferences("IAB", Context.MODE_PRIVATE);
            return (prefs.getBoolean(sku, false) ||
                    prefs.getBoolean(SKU_PRO, false) ||
                    prefs.getBoolean(SKU_PRO_LEGACY, false) ||
                    prefs.getBoolean(SKU_PRO_LIFETIME, false) ||
                    prefs.getBoolean(ActivityPro.SKU_PRO1, false) ||
                    prefs.getBoolean(ActivityPro.SKU_DONATION, false) ||
                    Util.hasValidFingerprint(context));
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public static boolean isPurchasedAny(Context context) {
        try {
            if (Util.isDebuggable(context)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                if (prefs.getBoolean("debug_iab", false)) return false;
            }

            if (LicenseValidator.isLicensed(context)) {
                return true;
            }

            SharedPreferences prefs = context.getSharedPreferences("IAB", Context.MODE_PRIVATE);
            for (String key : prefs.getAll().keySet()) {
                if (prefs.getBoolean(key, false)) return true;
            }
            return Util.hasValidFingerprint(context);
        } catch (SecurityException ignored) {
            return false;
        }
    }
}
