package com.mrwinston.mypos;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.leupau.icardpossdk.BluetoothDevicesDialog;
import eu.leupau.icardpossdk.ConnectionListener;
import eu.leupau.icardpossdk.Currency;
import eu.leupau.icardpossdk.POSHandler;
import eu.leupau.icardpossdk.TransactionData;

public class myPOS extends CordovaPlugin {
    private static final int REQUEST_CODE_MAKE_PAYMENT  = 1;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("payment")) {
            final Activity activity = this.cordova.getActivity();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    POSHandler.setCurrency(Currency.EUR);
                    POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

                    final POSHandler mPOSHandler = POSHandler.getInstance();

                    if( POSHandler.getInstance().isConnected()){
                        mPOSHandler.openPaymentActivity(
                                activity,
                                REQUEST_CODE_MAKE_PAYMENT,
                                data.optString(0),
                                UUID.randomUUID().toString()
                        );
                    }
                    else {
                        final BluetoothDevicesDialog dialog = new BluetoothDevicesDialog(activity);

                        dialog.show();

                        mPOSHandler.setConnectionListener(new ConnectionListener() {
                            @Override
                            public void onConnected(final BluetoothDevice device) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(100);

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            mPOSHandler.openPaymentActivity(
                                                    activity,
                                                    REQUEST_CODE_MAKE_PAYMENT,
                                                    data.optString(0),
                                                    UUID.randomUUID().toString()
                                            );
                                        }
                                    });
                                }
                                catch (Exception e) {

                                }
                            }
                        });
                    }
                }
            });

            callbackContext.success();

            return true;
        } else {
            return false;
        }
    }
}
