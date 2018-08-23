package com.mrwinston.mypos;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.leupau.icardpossdk.ConnectionListener;
import eu.leupau.icardpossdk.Currency;
import eu.leupau.icardpossdk.POSHandler;
import eu.leupau.icardpossdk.POSInfoListener;
import eu.leupau.icardpossdk.TransactionData;

import static android.app.Activity.RESULT_OK;

public class myPOS extends CordovaPlugin {
    private static final int REQUEST_CODE_MAKE_PAYMENT  = 1;

    private CallbackContext callbackContext = null;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("payment")) {
            final Activity activity = this.cordova.getActivity();

            POSHandler.setConnectionType(ConnectionType.BLUETOOTH);
            POSHandler.setLanguage(Language.DUTCH);
            POSHandler.setCurrency(Currency.EUR);
            POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

            final POSHandler mPOSHandler = POSHandler.getInstance();

            cordova.setActivityResultCallback(myPOS.this);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if( mPOSHandler.isConnected()){
                        mPOSHandler.openPaymentActivity(
                                activity,
                                REQUEST_CODE_MAKE_PAYMENT,
                                data.optString(0),
                                UUID.randomUUID().toString()
                        );
                    }
                    else {
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

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);

            result.setKeepCallback(true);

            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
            callbackContext.success();
        }
        else {
            callbackContext.error(0);
        }
    }
}
